package com.braunclown.kortiiko.views.daytype;

import com.braunclown.kortiiko.components.ErrorNotification;
import com.braunclown.kortiiko.data.DayType;
import com.braunclown.kortiiko.services.DayTypeService;
import com.braunclown.kortiiko.services.StablePeriodService;
import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToLongConverter;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Objects;
import java.util.Optional;

@PageTitle("Типы смен")
@Route(value = "day-type/:dayTypeID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class DayTypeView extends Div implements BeforeEnterObserver {

    private final String DAYTYPE_ID = "dayTypeID";
    private final String DAYTYPE_EDIT_ROUTE_TEMPLATE = "day-type/%s/edit";

    private final Grid<DayType> grid = new Grid<>(DayType.class, false);

    private TextField id;
    private TextField name;

    private final Button cancel = new Button("Отмена", new Icon(VaadinIcon.CLOSE));
    private final Button delete = new Button("Удалить", new Icon(VaadinIcon.TRASH));
    private final Button save = new Button("Сохранить", new Icon(VaadinIcon.CHECK));

    private final BeanValidationBinder<DayType> binder;

    private DayType dayType;

    private final DayTypeService dayTypeService;
    private final StablePeriodService stablePeriodService;

    public DayTypeView(DayTypeService dayTypeService,
                       StablePeriodService stablePeriodService) {
        this.dayTypeService = dayTypeService;
        this.stablePeriodService = stablePeriodService;
        addClassNames("day-type-view");

        // Создание пользовательского интерфейса
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Настройка таблицы
        grid.addColumn(DayType::getName).setAutoWidth(true).setSortable(true).setHeader("Название");
        grid.addColumn(stablePeriodService::countByDayType)
                .setAutoWidth(true).setSortable(true).setHeader("Кол-во 'стабильных' периодов");
        grid.setItems(dayTypeService.findAll());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // Заполнение полей редактирования при выборе периода в таблице
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(DAYTYPE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(DayTypeView.class);
            }
        });

        // Конфигурация формы
        binder = new BeanValidationBinder<>(DayType.class);

        // Привязать поля, добавить валидацию
        binder.forField(id)
                .withConverter(new StringToLongConverter("Введите целое число"))
                .bind(DayType::getId, DayType::setId);
        binder.forField(name)
                .withValidator(Objects::nonNull, "Заполните название")
                .bind(DayType::getName, DayType::setName);
        binder.addValueChangeListener(event -> binder.validate());

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        delete.addClickListener(e -> {
            if (this.dayType != null) {
                dayTypeService.delete(dayType.getId());
                Notification.show("Запись удалена");
            }
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.dayType == null) {
                    this.dayType = new DayType();
                }
                binder.writeBean(this.dayType);
                dayTypeService.update(this.dayType);
                clearForm();
                refreshGrid();
                Notification.show("Запись обновлена");
                UI.getCurrent().navigate(DayTypeView.class);
            } catch (ObjectOptimisticLockingFailureException exception) {
                ErrorNotification.show("Невозможно обновить запись. " +
                        "Кто-то другой обновил запись, пока вы вносили изменения");
            } catch (ValidationException validationException) {
                ErrorNotification.show("Невозможно обновить запись. Проверьте правильность введённых данных");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> dayTypeId = event.getRouteParameters().get(DAYTYPE_ID).map(Long::parseLong);
        if (dayTypeId.isPresent()) {
            Optional<DayType> dayTypeFromBackend = dayTypeService.get(dayTypeId.get());
            if (dayTypeFromBackend.isPresent()) {
                populateForm(dayTypeFromBackend.get());
            } else {
                Notification.show(
                        String.format("Запрошенный тип смены не найден, ID = %s", dayTypeId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // обновить таблицу, когда выбранная строка более недоступна
                refreshGrid();
                event.forwardTo(DayTypeView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        id = new TextField("Идентификатор");
        id.setReadOnly(true);
        name = new TextField("Название");
        formLayout.add(id, name);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        VerticalLayout buttonLayout = new VerticalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancel.addClassNames(LumoUtility.Width.FULL);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        delete.addClassNames(LumoUtility.Width.FULL);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClassNames(LumoUtility.Width.FULL);
        buttonLayout.add(save, delete, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.setItems(dayTypeService.findAll());
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(DayType value) {
        this.dayType = value;
        binder.readBean(value);
    }
}
