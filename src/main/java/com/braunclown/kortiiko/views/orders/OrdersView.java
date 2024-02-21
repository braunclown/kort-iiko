package com.braunclown.kortiiko.views.orders;

import com.braunclown.kortiiko.data.CookOrder;
import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.security.AuthenticatedUser;
import com.braunclown.kortiiko.services.CookOrderService;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.PeriodService;
import com.braunclown.kortiiko.services.telegram.KortiikoBot;
import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@PageTitle("Заказы")
@Route(value = "orders", layout = MainLayout.class)
@PermitAll
public class OrdersView extends VerticalLayout {

    private final CookOrderService cookOrderService;
    private final PeriodService periodService;
    private final DishService dishService;
    private final AuthenticatedUser authenticatedUser;
    private final KortiikoBot bot;
    private List<CookOrder> orders;
    private Period period;
    private Button refreshButton;
    private SimpleTimer timer;
    private H3 caption;
    private VerticalLayout orderLayout;

    public OrdersView(CookOrderService cookOrderService,
                      PeriodService periodService,
                      DishService dishService,
                      AuthenticatedUser authenticatedUser,
                      KortiikoBot bot) {
        this.cookOrderService = cookOrderService;
        this.periodService = periodService;
        this.dishService = dishService;
        this.authenticatedUser = authenticatedUser;
        this.bot = bot;
        period = getCurrentPeriod();

        add(createLayout());
        setSizeFull();
    }

    private Component createLayout() {
        orderLayout = new VerticalLayout();
        caption = new H3();
        HorizontalLayout menuLayout = new HorizontalLayout();
        menuLayout.add(caption, createRefreshButton());

        orderLayout.add(menuLayout);
        if (period != null) {
            caption.setText("Заказы на текущий период");
            orders = getCurrentOrders();
            orderLayout.add(createTimer());
            for (CookOrder cookOrder: orders) {
                orderLayout.add(new OrderComponent(cookOrder, cookOrderService, dishService, authenticatedUser, bot));
            }
        } else {
            caption.setText("Следующий период не найден. Заказов нет");
        }
        return orderLayout;
    }

    private Component createTimer() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassNames(LumoUtility.FontSize.LARGE);
        timer = new SimpleTimer(ChronoUnit.SECONDS.between(LocalDateTime.now(), period.getEndTime()));
        timer.setHours(true);
        timer.setMinutes(true);
        timer.setFractions(false);
        timer.start();
        timer.addTimerEndEvent(event -> {
            caption.setText("Период закончился. Обновите страницу для получения новых заказов");
        });
        layout.add(new Span("До конца периода осталось "), timer);
        return layout;
    }

    private Button createRefreshButton() {
        refreshButton = new Button("Обновить", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(event -> UI.getCurrent().getPage().reload());
        return refreshButton;
    }

    private List<CookOrder> getCurrentOrders() {
        return cookOrderService.getCurrentOrders(period);
    }

    private Period getCurrentPeriod() {
        return periodService.getCurrent().orElseGet(() -> periodService.getNext().orElse(null));
    }

}
