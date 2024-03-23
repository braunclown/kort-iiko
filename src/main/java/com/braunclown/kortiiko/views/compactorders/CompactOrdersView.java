package com.braunclown.kortiiko.views.compactorders;

import com.braunclown.kortiiko.data.CookOrder;
import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.security.AuthenticatedUser;
import com.braunclown.kortiiko.services.CookOrderService;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.PeriodService;
import com.braunclown.kortiiko.services.telegram.KortiikoBot;
import com.braunclown.kortiiko.views.MainLayout;
import com.braunclown.kortiiko.views.orders.SimpleTimer;
import com.braunclown.kortiiko.views.orders.UnableToCookDialog;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@PageTitle("Таблица заказов")
@Route(value = "compact-orders", layout = MainLayout.class)
@PermitAll
public class CompactOrdersView extends Div {
    private final CookOrderService cookOrderService;
    private final PeriodService periodService;
    private final DishService dishService;
    private final AuthenticatedUser authenticatedUser;
    private final KortiikoBot bot;
    private final Period period;
    private H3 caption;
    private Grid<CookOrder> grid;

    public CompactOrdersView(CookOrderService cookOrderService,
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
        addClassNames("compact-orders-view");
    }

    private Component createLayout() {
        VerticalLayout orderLayout = new VerticalLayout();
        orderLayout.addClassNames(LumoUtility.Height.FULL, LumoUtility.Gap.XSMALL, LumoUtility.Padding.SMALL);
        caption = new H3();
        HorizontalLayout menuLayout = new HorizontalLayout();
        menuLayout.add(caption, createRefreshButton());

        orderLayout.add(menuLayout);
        if (period != null) {
            caption.setText("Заказы на текущий период");
            menuLayout.add(createTimer());
            grid = new Grid<>(CookOrder.class, false);
            grid.addColumn(order -> order.getDish().getName()).setAutoWidth(true)
                    .setSortable(true).setHeader("Блюдо").setResizable(true);
            grid.addColumn(order -> order.getDish().getMeasure()).setAutoWidth(true)
                    .setSortable(true).setHeader("Ед. измерения");
            grid.addColumn(CookOrder::getAmountOrdered).setAutoWidth(true)
                    .setSortable(true).setHeader("Требуемое кол-во");
            grid.addComponentColumn(this::createCookButton).setHeader("Подтвердить приготовление");
            grid.addComponentColumn(this::createUnableToCookButton).setHeader("Невозможно приготовить");
            grid.setItems(getCurrentOrders());
            grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_WRAP_CELL_CONTENT);
            orderLayout.add(grid);
        } else {
            caption.setText("Следующий период не найден. Заказов нет");
        }
        return orderLayout;
    }

    private Component createCookButton(CookOrder cookOrder) {
        Button cookButton = new Button("Готово", new Icon(VaadinIcon.CHECK));
        cookButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_TERTIARY_INLINE);
        cookButton.addClickListener(event -> {
            authenticatedUser.get().ifPresent(user -> {
                ConfirmCookingDialog dialog = new ConfirmCookingDialog(bot, cookOrderService, dishService, user, cookOrder);
                dialog.open();
                dialog.addOpenedChangeListener(e -> {
                    if (!e.isOpened()) {
                        grid.setItems(getCurrentOrders());
                    }
                });
            });
        });
        return cookButton;
    }

    private Component createUnableToCookButton(CookOrder cookOrder) {
        Button unableToCookButton = new Button("Не могу", new Icon(VaadinIcon.CLOSE));
        unableToCookButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        unableToCookButton.addClickListener(event ->
                authenticatedUser.get().ifPresent(user -> {
                    UnableToCookDialog dialog = new UnableToCookDialog(bot, cookOrderService, user, cookOrder);
                    dialog.open();
                    dialog.addConfirmListener(confirm -> UI.getCurrent().getPage().reload());
                }));
        return unableToCookButton;
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

    private List<CookOrder> getCurrentOrders() {
        return cookOrderService.getCurrentOrders(period);
    }

    private Period getCurrentPeriod() {
        return periodService.getCurrent().orElseGet(() -> periodService.getNext().orElse(null));
    }
}
