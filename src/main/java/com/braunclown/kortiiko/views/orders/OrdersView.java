package com.braunclown.kortiiko.views.orders;

import com.braunclown.kortiiko.data.CookOrder;
import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.security.AuthenticatedUser;
import com.braunclown.kortiiko.services.CookOrderService;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.PeriodService;
import com.braunclown.kortiiko.services.telegram.KortiikoBot;
import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@PageTitle("Orders")
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
        setSizeFull();
        period = getCurrentPeriod();
        createLayout();
    }

    private void createLayout() {
        add(createRefreshButton());
        if (period != null) {
            orders = getCurrentOrders();
            add(new H3("Заказы на текущий период"));
            for (CookOrder cookOrder: orders) {
                add(new OrderComponent(cookOrder, cookOrderService, dishService, authenticatedUser, bot));
            }

            // TODO: Добавить таймер до конца периода


        } else {
            add(new H3("Следующий период не найден. Заказов нет"));
        }
    }

    private Button createRefreshButton() {
        refreshButton = new Button("Обновить", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(event -> {
            removeAll();
            createLayout();
        });
        return refreshButton;
    }

    private List<CookOrder> getCurrentOrders() {
        return cookOrderService.getCurrentOrders(period);
    }

    private Period getCurrentPeriod() {
        return periodService.getCurrent().orElseGet(() -> periodService.getNext().orElse(null));
    }

}
