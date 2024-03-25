package com.braunclown.kortiiko.views.compactorders;

import com.braunclown.kortiiko.components.ErrorNotification;
import com.braunclown.kortiiko.data.CookOrder;
import com.braunclown.kortiiko.data.User;
import com.braunclown.kortiiko.services.CookOrderService;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.telegram.KortiikoBot;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

public class ConfirmCookingDialog extends Dialog {
    private final KortiikoBot bot;
    private final CookOrderService cookOrderService;
    private final DishService dishService;
    private final User currentUser;
    private final CookOrder order;

    private TextField amountField;
    public ConfirmCookingDialog(KortiikoBot bot,
                                CookOrderService cookOrderService,
                                DishService dishService,
                                User user,
                                CookOrder order) {
        this.bot = bot;
        this.cookOrderService = cookOrderService;
        this.dishService = dishService;
        this.currentUser = user;
        this.order = order;

        setHeaderTitle("Подтвердить приготовление блюда " + order.getDish().getName());
        add(createLayout());
        createFooterLayout();
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
    }

    private Component createLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassNames(LumoUtility.Width.FULL, LumoUtility.Padding.XSMALL);
        amountField = new TextField("Количество (" + order.getDish().getMeasure() + ")");
        amountField.addClassNames(LumoUtility.Width.FULL, LumoUtility.FontSize.XLARGE);
        amountField.setValue(String.valueOf(order.getAmountOrdered()));
        layout.add(amountField, createKeyboard());
        return layout;
    }

    private Component createKeyboard() {
        Div keyboard = new Div();
        keyboard.addClassNames(LumoUtility.Display.GRID, LumoUtility.Grid.Column.COLUMNS_3, LumoUtility.Width.FULL,
                LumoUtility.Gap.SMALL);
        Div extraButtons = new Div(createDotButton(), createBackspaceButton(), createClearButton());
        extraButtons.addClassNames(LumoUtility.Grid.Column.COLUMN_SPAN_2,
                LumoUtility.Width.FULL, LumoUtility.Gap.SMALL, LumoUtility.Display.FLEX);
        keyboard.add(
                createNumberButton(7), createNumberButton(8), createNumberButton(9),
                createNumberButton(4), createNumberButton(5), createNumberButton(6),
                createNumberButton(1), createNumberButton(2), createNumberButton(3),
                createNumberButton(0), extraButtons
        );
        return keyboard;
    }

    private Component createNumberButton(int number) {
        Button key = new Button(String.valueOf(number));
        key.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Height.XLARGE);
        key.addClickListener(event -> amountField.setValue(amountField.getValue() + number));
        return key;
    }

    private Component createDotButton() {
        Button dotButton = new Button(".");
        dotButton.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Height.XLARGE);
        dotButton.addClickListener(event -> {
            if (!amountField.getValue().contains(".")) {
                amountField.setValue(amountField.getValue() + ".");
            }
        });
        return dotButton;
    }

    private Component createBackspaceButton() {
        Button backspace = new Button("←");
        backspace.addThemeVariants(ButtonVariant.LUMO_ERROR);
        backspace.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Height.XLARGE);
        backspace.addClickListener(event -> {
            if (!amountField.getValue().isEmpty()) {
                amountField.setValue(amountField.getValue().substring(0, amountField.getValue().length() - 1));
            }
        });
        return backspace;
    }

    private Component createClearButton() {
        Button clear = new Button("C");
        clear.addThemeVariants(ButtonVariant.LUMO_ERROR);
        clear.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.Height.XLARGE, LumoUtility.Flex.GROW);
        clear.addClickListener(event -> amountField.setValue(""));
        return clear;
    }

    private void createFooterLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.addClassNames(LumoUtility.Width.FULL);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        Button closeButton = new Button("Отмена", new Icon(VaadinIcon.CLOSE));
        closeButton.addClickListener(event -> close());
        layout.add(closeButton, createConfirmButton());
        getFooter().add(layout);
    }

    private Component createConfirmButton() {
        Button confirmButton = new Button("Подтвердить", new Icon(VaadinIcon.CHECK));
        confirmButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        confirmButton.addClickListener(event -> {
            try {
                order.setVisible(false);
                order.setAmountCooked(Double.parseDouble(amountField.getValue()));
                order.setCook(currentUser);
                cookOrderService.update(order);

                if (!order.getAmountCooked().equals(order.getAmountOrdered())) {
                    bot.sendAdmins(createMessage());
                }

                dishService.get(order.getDish().getId()).ifPresent(dish -> {
                    dish.setAmount(dish.getAmount() + order.getAmountCooked());
                    dishService.update(dish);
                });

                close();
                Notification n = Notification.show("Запись обновлена");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (NumberFormatException e) {
                ErrorNotification.show("Введённое количество не является числом");
            } catch (ObjectOptimisticLockingFailureException e) {
                ErrorNotification.show("Невозможно обновить запись. Кто-то другой обновил запись, пока вы вносили изменения");
            } catch (Exception e) {
                ErrorNotification.show("Произошла ошибка");
            }
        });
        return confirmButton;
    }

    private String createMessage() {
        return "Блюдо " + order.getDish().getName() + ": повар " + currentUser.getRealName()
                + " (" + currentUser.getPhone() + ", " + currentUser.getEmail()
                + ") приготовил " + order.getAmountCooked() + " " + order.getDish().getMeasure()
                + " вместо " + order.getAmountOrdered();
    }
}
