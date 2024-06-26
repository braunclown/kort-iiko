package com.braunclown.kortiiko.views.periods;

import com.braunclown.kortiiko.components.ErrorNotification;
import com.braunclown.kortiiko.data.DayType;
import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.services.DayTypeService;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.PeriodService;
import com.braunclown.kortiiko.services.iiko.SalesReceiver;
import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@PageTitle("Сегодняшние периоды")
@Route(value = "periods", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class PeriodsView extends Div {

    private Grid<Period> grid;
    private Button createPeriods;
    private Button stopTasks;
    private Select<DayType> dayTypeSelect;

    private final PeriodService periodService;
    private final SalesReceiver salesReceiver;
    private final DishService dishService;
    private final DayTypeService dayTypeService;

    public PeriodsView(PeriodService periodService,
                       SalesReceiver salesReceiver,
                       DishService dishService,
                       DayTypeService dayTypeService) {
        this.periodService = periodService;
        this.salesReceiver = salesReceiver;
        this.dishService = dishService;
        this.dayTypeService = dayTypeService;
        setSizeFull();
        addClassNames("periods-view");

        VerticalLayout layout = new VerticalLayout(createMenu(), createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private Component createMenu() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassNames(LumoUtility.Padding.MEDIUM);

        dayTypeSelect = new Select<>();
        dayTypeSelect.setLabel("Выберите тип смены");
        dayTypeSelect.setItemLabelGenerator(DayType::getName);
        dayTypeSelect.setItems(dayTypeService.findAll());

        layout.add(createUpdateAmountButton(), dayTypeSelect, createPeriodsButton(), createStopTasksButton());
        changeButtonsVisibility();
        return layout;
    }

    private Button createUpdateAmountButton() {
        Button updateDishAmount = new Button("Обновить остатки блюд", VaadinIcon.MAGIC.create());
        updateDishAmount.setTooltipText("Текущие остатки каждого блюда станут равными соответствующим остаткам по умолчанию");
        updateDishAmount.addClickListener(event -> {
            ConfirmDialog dialog = new ConfirmDialog("Вы уверены?",
                    "Текущие остатки каждого блюда станут равными соответствующим остаткам по умолчанию. Действие невозможно будет отменить",
                    "Да", e -> {
                dishService.updateAmounts();
                Notification n = Notification.show("Остатки обновлены");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            },
                    "Отмена", e -> {});
            dialog.open();
        });
        updateDishAmount.addClassNames(LumoUtility.Margin.Top.AUTO);
        return updateDishAmount;
    }

    private Button createPeriodsButton() {
        createPeriods = new Button("Запустить программу", new Icon(VaadinIcon.CLOCK));
        createPeriods.setTooltipText(
                "Разбивает сегодняшний день на промежутки в соответствии со 'стабильными' периодами. " +
                        "Заполняет этими промежутками таблицу.");
        createPeriods.addClassNames(LumoUtility.Margin.Top.AUTO);
        createPeriods.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createPeriods.addClickListener(event -> {
            try {
                if (dayTypeSelect.getValue() == null) {
                    ErrorNotification.show("Выберите тип смены");
                } else {
                    if (periodService.findTodayPeriods().isEmpty()) {
                        periodService.createTodayPeriods(dayTypeSelect.getValue());
                        List<Period> todayPeriods = periodService.findTodayPeriods();
                        grid.setItems(todayPeriods);
                        salesReceiver.planRequests(todayPeriods);
                        Notification.show("Программа запущена. Количество периодов сегодня: " + todayPeriods.size());
                    } else {
                        ErrorNotification.show("Программа уже была запущена сегодня");
                    }
                }
            } catch (Exception e) {
                ErrorNotification.show("Не удалось запустить программу");
            }
            changeButtonsVisibility();
        });
        return createPeriods;
    }

    private Button createStopTasksButton() {
        stopTasks = new Button("Остановить программу", new Icon(VaadinIcon.CLOSE));
        stopTasks.setTooltipText("Удаляет сегодняшние периоды и останавливает получение продаж и расчёт заказов");
        stopTasks.addClassNames(LumoUtility.Margin.Top.AUTO);
        stopTasks.addThemeVariants(ButtonVariant.LUMO_ERROR);
        stopTasks.addClickListener(event -> {
            try {
                salesReceiver.cancelTasks();
                List<Period> todayPeriods = periodService.findTodayPeriods();
                for (Period period: todayPeriods) {
                    periodService.delete(period.getId());
                }
                Notification.show("Программа остановлена");
            } catch (Exception e) {
                ErrorNotification.show("Не удалось остановить программу");
            }
            grid.setItems(periodService.findTodayPeriods());
            changeButtonsVisibility();
        });
        return stopTasks;
    }

    private void changeButtonsVisibility() {
        boolean todayPeriodsExist = !periodService.findTodayPeriods().isEmpty();
        createPeriods.setVisible(!todayPeriodsExist);
        stopTasks.setVisible(todayPeriodsExist);
    }

    private Component createGrid() {
        grid = new Grid<>(Period.class, false);
        Grid.Column<Period> startTimeColumn = grid.addColumn("startTime")
                .setHeader("Время начала").setAutoWidth(true);
        grid.addColumn("endTime")
                .setHeader("Время конца").setAutoWidth(true);

        grid.setItems(periodService.findTodayPeriods());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        grid.sort(new GridSortOrderBuilder<Period>().thenAsc(startTimeColumn).build());
        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}
