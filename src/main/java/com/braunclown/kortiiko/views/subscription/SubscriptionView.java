package com.braunclown.kortiiko.views.subscription;

import com.braunclown.kortiiko.data.User;
import com.braunclown.kortiiko.security.AuthenticatedUser;
import com.braunclown.kortiiko.services.UserService;
import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToLongConverter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@PageTitle("Telegram-чат")
@Route(value = "subscribe", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class SubscriptionView extends VerticalLayout {

    private final UserService userService;
    private final AuthenticatedUser authenticatedUser;
    private final BeanValidationBinder<User> binder;

    private TextField chatIdField;
    private Button chatIdButton;

    public SubscriptionView(UserService userService,
                            AuthenticatedUser authenticatedUser) {
        this.userService = userService;
        this.authenticatedUser = authenticatedUser;
        this.binder = new BeanValidationBinder<>(User.class);
        add(createLayout());
    }

    private Component createLayout() {
        H3 header = new H3("Введите число, полученное от бота, чтобы получать уведомления");
        if (authenticatedUser.get().isPresent()) {
            User user = authenticatedUser.get().get();
            chatIdField = new TextField("id вашего чата");
            binder.forField(chatIdField)
                    .withConverter(new StringToLongConverter("Должно быть целым числом"))
                    .withNullRepresentation(-1L)
                    .bind(User::getChatId, User::setChatId);
            binder.readBean(user);

            chatIdButton = new Button("Сохранить");
            chatIdButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            chatIdButton.addClickListener(event -> {
                try {
                    binder.writeBean(user);
                    userService.update(user);
                    Notification n = Notification.show("Данные сохранены");
                    n.setPosition(Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (ObjectOptimisticLockingFailureException exception) {
                    Notification n = Notification.show(
                            "Невозможно обновить запись. Кто-то другой обновил запись, пока вы вносили изменения");
                    n.setPosition(Notification.Position.MIDDLE);
                    n.addThemeVariants(NotificationVariant.LUMO_ERROR);
                } catch (ValidationException validationException) {
                    Notification.show("Невозможно обновить запись. Проверьте правильность введённых данных");
                }
            });
        }
        return new VerticalLayout(header, chatIdField, chatIdButton);
    }
}
