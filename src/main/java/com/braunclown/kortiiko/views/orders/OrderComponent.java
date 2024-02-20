package com.braunclown.kortiiko.views.orders;

import com.braunclown.kortiiko.data.CookOrder;
import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.security.AuthenticatedUser;
import com.braunclown.kortiiko.services.CookOrderService;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.telegram.KortiikoBot;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

public class OrderComponent extends Div {
    private NumberField cookedAmountField;
    private CookOrder order;
    private final CookOrderService cookOrderService;
    private final DishService dishService;
    private final AuthenticatedUser authenticatedUser;
    private final KortiikoBot bot;

    public OrderComponent(CookOrder order,
                          CookOrderService cookOrderService,
                          DishService dishService,
                          AuthenticatedUser authenticatedUser,
                          KortiikoBot bot) {
        this.order = order;
        this.cookOrderService = cookOrderService;
        this.dishService = dishService;
        this.authenticatedUser = authenticatedUser;
        this.bot = bot;
        add(createLayout());


        addClassNames(LumoUtility.Background.CONTRAST_10);
    }

    private Component createLayout() {
        H3 dishName = new H3(order.getDish().getName());
        Span orderedAmount = new Span("Требуется: " + order.getAmountOrdered()
                + " " + order.getDish().getMeasure());
        cookedAmountField = new NumberField();
        cookedAmountField.setValue(order.getAmountOrdered());
        cookedAmountField.setSuffixComponent(new Span(order.getDish().getMeasure()));
        Span cookedAmount = new Span(new Span("Приготовлено: "), cookedAmountField);

        HorizontalLayout mainLayout = new HorizontalLayout(
                new VerticalLayout(dishName, orderedAmount, cookedAmount),
                new VerticalLayout(createConfirmButton(), createUnableToCookButton())
        );
        mainLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return mainLayout;
    }

    private Button createConfirmButton() {
        Button confirm = new Button("Подтвердить приготовление", new Icon(VaadinIcon.CHECK));
        confirm.addClickListener(event -> {
            try {
                Double amountCooked = cookedAmountField.getValue();
                order.setAmountCooked(amountCooked);
                order.setVisible(false);
                authenticatedUser.get().ifPresent(user -> order.setCook(user));
                cookOrderService.update(order);
                setVisible(false);
                Optional<Dish> d = dishService.get(order.getDish().getId());
                if (d.isPresent()) {
                    Dish dish = d.get();
                    dish.setAmount(dish.getAmount() + amountCooked);
                    dishService.update(dish);
                }
            } catch (ObjectOptimisticLockingFailureException e) {
                Notification n = Notification.show(
                        "Невозможно обновить запись. Кто-то другой обновил запись, пока вы вносили изменения");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (Exception e) {
                Notification n = Notification.show(
                        "Произошла ошибка");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirm.addClassNames(LumoUtility.Width.FULL);
        return confirm;
    }

    private Button createUnableToCookButton() {
        Button unableToCook = new Button("Невозможно приготовить", new Icon(VaadinIcon.CLOSE));
        unableToCook.addClickListener(event -> {
            authenticatedUser.get().ifPresent(user -> {
                UnableToCookDialog dialog = new UnableToCookDialog(bot, cookOrderService, user, order);
                dialog.open();
                dialog.addConfirmListener(confirm -> setVisible(false));
            });
        });
        unableToCook.addThemeVariants(ButtonVariant.LUMO_ERROR);
        unableToCook.addClassNames(LumoUtility.Width.FULL);
        return unableToCook;
    }
}
