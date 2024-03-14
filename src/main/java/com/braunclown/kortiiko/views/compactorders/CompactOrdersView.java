package com.braunclown.kortiiko.views.compactorders;

import com.braunclown.kortiiko.data.CookOrder;
import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.security.AuthenticatedUser;
import com.braunclown.kortiiko.services.CookOrderService;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.PeriodService;
import com.braunclown.kortiiko.views.MainLayout;
import com.braunclown.kortiiko.views.orders.SimpleTimer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@PageTitle("Таблица заказов")
@Route(value = "compact-orders", layout = MainLayout.class)
@PermitAll
public class CompactOrdersView extends Div {
    private final CookOrderService cookOrderService;
    private final PeriodService periodService;
    private final DishService dishService;
    private final AuthenticatedUser authenticatedUser;
    private final Period period;
    private List<CookOrder> orders;
    private H3 caption;
    private Grid<CookOrder> grid;

    public CompactOrdersView(CookOrderService cookOrderService,
                             PeriodService periodService,
                             DishService dishService,
                             AuthenticatedUser authenticatedUser) {
        this.cookOrderService = cookOrderService;
        this.periodService = periodService;
        this.dishService = dishService;
        this.authenticatedUser = authenticatedUser;
        period = getCurrentPeriod();
        add(createLayout());
        setSizeFull();
        addClassNames("compact-orders-view");
    }

    private Component createLayout() {
        VerticalLayout orderLayout = new VerticalLayout();
        orderLayout.addClassNames(LumoUtility.Height.FULL);
        caption = new H3();
        HorizontalLayout menuLayout = new HorizontalLayout();
        menuLayout.add(caption, createRefreshButton());

        orderLayout.add(menuLayout);
        if (period != null) {
            caption.setText("Заказы на текущий период");
            orderLayout.add(createTimer());
            grid = new Grid<>(CookOrder.class, false);
            grid.addColumn(order -> order.getDish().getName()).setAutoWidth(true).setSortable(true).setHeader("Блюдо");
            grid.addColumn(order -> order.getDish().getMeasure()).setAutoWidth(true).setSortable(true).setHeader("Ед. измерения");
            grid.addColumn(CookOrder::getAmountOrdered).setAutoWidth(true).setSortable(true).setHeader("Требуемое кол-во");
            orders = getCurrentOrders();
            grid.setItems(orders);
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
            Div gridDiv = new Div(grid);
            gridDiv.addClassNames();
            orderLayout.add(grid);
            menuLayout.add(createCookButton());
        } else {
            caption.setText("Следующий период не найден. Заказов нет");
        }
        return orderLayout;
    }

    private Component createTimer() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassNames(LumoUtility.FontSize.LARGE);
        SimpleTimer timer = new SimpleTimer(ChronoUnit.SECONDS.between(LocalDateTime.now(), period.getEndTime()));
        timer.setHours(true);
        timer.setMinutes(true);
        timer.setFractions(false);
        timer.start();
        timer.addTimerEndEvent(event -> caption.setText("Период закончился. Обновите страницу для получения новых заказов"));
        layout.add(new Span("До конца периода осталось "), timer);
        return layout;
    }

    private Button createRefreshButton() {
        Button refreshButton = new Button("Обновить", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(event -> UI.getCurrent().getPage().reload());
        return refreshButton;
    }

    private Button createCookButton() {
        Button cookButton = new Button("Приготовить всё", new Icon(VaadinIcon.CHECK));
        cookButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        cookButton.addClickListener(event -> {
            for (CookOrder order: orders) {
                try {
                    order.setAmountCooked(order.getAmountOrdered());
                    order.setVisible(false);
                    authenticatedUser.get().ifPresent(order::setCook);
                    cookOrderService.update(order);
                    Optional<Dish> d = dishService.get(order.getDish().getId());
                    if (d.isPresent()) {
                        Dish dish = d.get();
                        dish.setAmount(dish.getAmount() + order.getAmountOrdered());
                        dishService.update(dish);
                    }
                } catch (ObjectOptimisticLockingFailureException e) {
                    Notification n = Notification.show("Заказ на приготовление блюда " + order.getDish().getName()
                            + ":\nНевозможно обновить запись. Кто-то другой обновил запись, пока вы вносили изменения");
                    n.setPosition(Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                } catch (Exception e) {
                    Notification n = Notification.show(
                            "Произошла ошибка");
                    n.setPosition(Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            }
            grid.setItems(getCurrentOrders());
        });
        return cookButton;
    }

    private List<CookOrder> getCurrentOrders() {
        return cookOrderService.getCurrentOrders(period);
    }

    private Period getCurrentPeriod() {
        return periodService.getCurrent().orElseGet(() -> periodService.getNext().orElse(null));
    }
}
