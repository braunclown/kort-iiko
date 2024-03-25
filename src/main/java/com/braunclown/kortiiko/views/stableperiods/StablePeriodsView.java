package com.braunclown.kortiiko.views.stableperiods;

import com.braunclown.kortiiko.components.ErrorNotification;
import com.braunclown.kortiiko.data.DayType;
import com.braunclown.kortiiko.data.StablePeriod;
import com.braunclown.kortiiko.services.DayTypeService;
import com.braunclown.kortiiko.services.StablePeriodService;
import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
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

@PageTitle("'Стабильные' периоды")
@Route(value = "stable-periods/:stablePeriodID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class StablePeriodsView extends Div implements BeforeEnterObserver {

    private final String STABLEPERIOD_ID = "stablePeriodID";
    private final String STABLEPERIOD_EDIT_ROUTE_TEMPLATE = "stable-periods/%s/edit";
    private final String DISHSETTING_EDIT_ROUTE_TEMPLATE = "dish-settings/%s/edit";

    private final Grid<StablePeriod> periodGrid = new Grid<>(StablePeriod.class, false);

    private TextField id;
    private Select<DayType> dayType;
    private TimePicker startTime;
    private TimePicker endTime;

    private final Button cancel = new Button("Отмена", new Icon(VaadinIcon.CLOSE));
    private final Button delete = new Button("Удалить", new Icon(VaadinIcon.TRASH));
    private final Button save = new Button("Сохранить", new Icon(VaadinIcon.CHECK));

    private final BeanValidationBinder<StablePeriod> binder;

    private StablePeriod stablePeriod;

    private final StablePeriodService stablePeriodService;
    private final DayTypeService dayTypeService;

    public StablePeriodsView(StablePeriodService stablePeriodService,
                             DayTypeService dayTypeService) {
        this.stablePeriodService = stablePeriodService;
        this.dayTypeService = dayTypeService;
        addClassNames("stable-periods-view");

        // Создание пользовательского интерфейса
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Настройка таблицы
        Grid.Column<StablePeriod> dayTypeColumn = periodGrid.addColumn(sp -> sp.getDayType().getName())
                .setAutoWidth(true).setSortable(true).setHeader("Тип смены");
        Grid.Column<StablePeriod> startTimeColumn = periodGrid.addColumn(StablePeriod::getStartTime)
                .setAutoWidth(true).setSortable(true).setHeader("Время начала");
        periodGrid.addColumn(StablePeriod::getEndTime).setAutoWidth(true).setSortable(true).setHeader("Время конца");
        periodGrid.addComponentColumn(stablePeriod -> {
            Button button = new Button("К настройкам пополнения", new Icon(VaadinIcon.EDIT));
            button.addClickListener(event ->
                    UI.getCurrent().navigate(String.format(DISHSETTING_EDIT_ROUTE_TEMPLATE, stablePeriod.getId())));
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            return button;
        });
        periodGrid.setItems(stablePeriodService.findAll());
        periodGrid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
        periodGrid.sort(new GridSortOrderBuilder<StablePeriod>()
                .thenAsc(dayTypeColumn).thenAsc(startTimeColumn).build());
        periodGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // Заполнение полей редактирования при выборе периода в таблице
        periodGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(STABLEPERIOD_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(StablePeriodsView.class);
            }
        });

        // Конфигурация формы
        binder = new BeanValidationBinder<>(StablePeriod.class);

        // Привязать поля, добавить валидацию

        binder.forField(id)
                .withConverter(new StringToLongConverter("Введите целое число"))
                .bind(StablePeriod::getId, StablePeriod::setId);
        binder.forField(dayType)
                .withValidator(Objects::nonNull, "Заполните тип смены")
                .bind(StablePeriod::getDayType, StablePeriod::setDayType);
        binder.forField(startTime).withValidator(
                startTime -> endTime.getValue() != null && startTime != null && startTime.isBefore(endTime.getValue()),
                "Начало периода должно быть раньше его конца"
        ).withValidator(
                (startTime, something) -> {
                    if (dayType.getValue() != null) {
                        for (StablePeriod period : stablePeriodService.findByDayType(dayType.getValue())) {
                            if (startTime != null && endTime.getValue() != null && !Objects.equals(stablePeriod, period)) {
                                StablePeriod currentPeriod = new StablePeriod();
                                currentPeriod.setStartTime(startTime);
                                currentPeriod.setEndTime(endTime.getValue());
                                if (stablePeriodService.isOverlapping(period, currentPeriod)) {
                                    return ValidationResult.error("Данный период пересекается с другим");
                                }
                            }
                        }
                    }
                    return ValidationResult.ok();
                }).bind("startTime");
        binder.forField(endTime).withValidator(
                endTime -> startTime.getValue() != null && endTime != null && endTime.isAfter(startTime.getValue()),
                "Конец периода должен быть позже его начала"
        ).withValidator(
                (endTime, something) -> {
                    if (dayType.getValue() != null) {
                        for (StablePeriod period : stablePeriodService.findByDayType(dayType.getValue())) {
                            if (startTime.getValue() != null && endTime != null && !Objects.equals(stablePeriod, period)) {
                                StablePeriod currerntPeriod = new StablePeriod();
                                currerntPeriod.setStartTime(startTime.getValue());
                                currerntPeriod.setEndTime(endTime);
                                if (stablePeriodService.isOverlapping(period, currerntPeriod)) {
                                    return ValidationResult.error("Данный период пересекается с другим");
                                }
                            }
                        }
                    }
                    return ValidationResult.ok();
                }).bind("endTime");
        binder.addValueChangeListener(event -> binder.validate());

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        delete.addClickListener(e -> {
            if (this.stablePeriod != null) {
                stablePeriodService.delete(stablePeriod.getId());
                Notification.show("Запись удалена");
            }
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.stablePeriod == null) {
                    this.stablePeriod = new StablePeriod();
                }
                binder.writeBean(this.stablePeriod);
                stablePeriodService.update(this.stablePeriod);

                clearForm();
                refreshGrid();
                Notification.show("Запись обновлена");
                UI.getCurrent().navigate(StablePeriodsView.class);
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
        Optional<Long> stablePeriodId = event.getRouteParameters().get(STABLEPERIOD_ID).map(Long::parseLong);
        if (stablePeriodId.isPresent()) {
            Optional<StablePeriod> stablePeriodFromBackend = stablePeriodService.get(stablePeriodId.get());
            if (stablePeriodFromBackend.isPresent()) {
                populateForm(stablePeriodFromBackend.get());
            } else {
                Notification.show(
                        String.format("Запрошенный стабильный период не найден, ID = %s", stablePeriodId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // обновить таблицу, когда выбранная строка более недоступна
                refreshGrid();
                event.forwardTo(StablePeriodsView.class);
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
        dayType = new Select<>();
        dayType.setLabel("Тип смены");
        dayType.setItems(dayTypeService.findAll());
        dayType.setItemLabelGenerator(DayType::getName);
        startTime = new TimePicker("Время начала");
        endTime = new TimePicker("Время конца");
        formLayout.add(id, dayType, startTime, endTime);

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
        wrapper.add(periodGrid);
    }

    private void refreshGrid() {
        periodGrid.setItems(stablePeriodService.findAll());
        periodGrid.select(null);
        periodGrid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(StablePeriod value) {
        this.stablePeriod = value;
        binder.readBean(this.stablePeriod);

    }

}
