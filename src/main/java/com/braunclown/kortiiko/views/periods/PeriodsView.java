package com.braunclown.kortiiko.views.periods;

import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.PeriodService;
import com.braunclown.kortiiko.services.iiko.SalesReceiver;
import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

    private final PeriodService periodService;
    private final SalesReceiver salesReceiver;
    private final DishService dishService;

    public PeriodsView(PeriodService periodService, SalesReceiver salesReceiver, DishService dishService) {
        this.periodService = periodService;
        this.salesReceiver = salesReceiver;
        this.dishService = dishService;
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
        Button updateDishAmount = new Button("Обновить остатки блюд");
        updateDishAmount.addClickListener(event -> dishService.updateAmounts());

        Button createPeriods = new Button("Запустить программу");
        createPeriods.setTooltipText(
                "Разбивает сегодняшний день на промежутки в соответствии со 'стабильными' периодами. " +
                        "Заполняет этими промежутками таблицу. " +
                        "Обновляет текущие остатки блюд.");
        createPeriods.addClickListener(event -> {
            try {
                if (periodService.findTodayPeriods().isEmpty()) {
                    periodService.createTodayPeriods();
                    List<Period> todayPeriods = periodService.findTodayPeriods();
                    grid.setItems(todayPeriods);
                    salesReceiver.planRequests(todayPeriods);
                    Notification.show("Программа запущена. Количество периодов сегодня: " + todayPeriods.size());
                } else {
                    Notification n = Notification.show("Программа уже была запущена сегодня");
                    n.setPosition(Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception e) {
                Notification n = Notification.show(
                        "Не удалось запустить программу");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        layout.add(updateDishAmount, createPeriods);
        return layout;
    }

    private Component createGrid() {
        grid = new Grid<>(Period.class, false);
        grid.addColumn("startTime")
                .setHeader("Время начала").setAutoWidth(true);
        grid.addColumn("endTime")
                .setHeader("Время конца").setAutoWidth(true);

        grid.setItems(periodService.findTodayPeriods());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}
