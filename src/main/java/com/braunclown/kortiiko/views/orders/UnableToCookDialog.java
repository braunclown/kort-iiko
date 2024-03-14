package com.braunclown.kortiiko.views.orders;

import com.braunclown.kortiiko.data.CookOrder;
import com.braunclown.kortiiko.data.User;
import com.braunclown.kortiiko.services.CookOrderService;
import com.braunclown.kortiiko.services.telegram.KortiikoBot;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

public class UnableToCookDialog extends ConfirmDialog {

    private final KortiikoBot bot;
    private final CookOrderService cookOrderService;
    private final User currentUser;
    private final CookOrder order;
    private CheckboxGroup<String> reasons;
    private TextArea reasonField;

    public UnableToCookDialog(KortiikoBot bot, CookOrderService cookOrderService, User user, CookOrder order) {
        this.bot = bot;
        this.cookOrderService = cookOrderService;
        this.currentUser = user;
        this.order = order;

        setCancelable(true);
        setHeader("Невозможно приготовить блюдо " + order.getDish().getName());
        add(createLayout());
        createFooterLayout();
    }

    private Component createLayout() {
        reasons = new CheckboxGroup<>("Причины: ");
        reasons.setItems("Отсутствуют ингредиенты",
                "Не хватает времени",
                "Имеющихся запасов блюд достаточно");
        reasons.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        reasonField = new TextArea("Свой вариант / комментарий:");
        reasonField.addClassNames(LumoUtility.Width.FULL);
        return new VerticalLayout(reasons, reasonField);
    }

    private void createFooterLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        Button closeButton = new Button("Отмена", new Icon(VaadinIcon.CLOSE));
        closeButton.addClickListener(event -> close());
        setCancelButton(closeButton);

        Button sendReasonsButton = new Button("Отправить", new Icon(VaadinIcon.ENVELOPE));
        sendReasonsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendReasonsButton.addClickListener(event -> {
            try {
                order.setVisible(false);
                cookOrderService.update(order);
                bot.sendAdmins(createMessage());
                close();
                Notification n = Notification.show("Запись обновлена");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
        setConfirmButton(sendReasonsButton);

    }

    private String createMessage() {
        StringBuilder message = new StringBuilder("Повар " + currentUser.getRealName()
                + " (" + currentUser.getPhone() + ", " + currentUser.getEmail()
                + ") не может приготовить блюдо " + order.getDish().getName() + ".\n");
        if (!reasons.getSelectedItems().isEmpty()) {
            message.append("Указаны причины: \n");
            for (String reason: reasons.getSelectedItems()) {
                message.append(reason).append("\n");
            }
        }
        if (!reasonField.isEmpty()) {
            message.append("Сообщение от повара: ").append(reasonField.getValue());
        }
        return message.toString();
    }
}
