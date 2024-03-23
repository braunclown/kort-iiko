package com.braunclown.kortiiko.views.users;

import com.braunclown.kortiiko.components.ErrorNotification;
import com.braunclown.kortiiko.data.Role;
import com.braunclown.kortiiko.data.User;
import com.braunclown.kortiiko.services.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToLongConverter;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class EditUserDialog extends Dialog {

    private TextField usernameField;
    private TextField realNameField;
    private EmailField emailField;
    private TextField phoneField;
    private TextField chatIdField;
    private CheckboxGroup<Role> roles;

    private User userToEdit;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final BeanValidationBinder<User> binder;

    public EditUserDialog(User user, UserService userService, PasswordEncoder passwordEncoder) {
        this.userToEdit = user;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.binder = new BeanValidationBinder<>(User.class);
        configureDialog();
        add(createEditingFields());
        bindFields();
        binder.readBean(this.userToEdit);
        getFooter().add(createFooterLayout());
    }

    private void configureDialog() {
        setHeaderTitle("Редактирование пользователя " + userToEdit.getRealName());
        setResizable(true);
        setDraggable(true);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
    }

    private Component createEditingFields() {
        usernameField = new TextField("Логин");
        realNameField = new TextField("Настоящее имя");
        emailField = new EmailField("Почта");
        phoneField = new TextField("Телефон");
        roles = new CheckboxGroup<>("Роли");
        roles.setItems(Role.values());
        roles.setItemLabelGenerator(role -> role == Role.ADMIN ? "Администратор": "Пользователь");
        return new FormLayout(usernameField, realNameField,
                emailField, phoneField,
                createChatIdLayout(),
                roles,
                createEditPasswordButton());
    }

    private Component createChatIdLayout() {
        chatIdField = new TextField("id чата с ботом");
        chatIdField.setReadOnly(true);
        Button deleteChatIdButton = new Button("Удалить");
        deleteChatIdButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteChatIdButton.addClassNames(LumoUtility.Margin.Top.AUTO);
        deleteChatIdButton.addClickListener(event -> {
            chatIdField.setValue("");
            Notification.show("Чтобы применить изменения, нажмите на кнопку 'Сохранить'");
        });
        return new HorizontalLayout(chatIdField, deleteChatIdButton);
    }

    private Button createEditPasswordButton() {
        Button editPasswordButton = new Button("Сменить пароль");
        editPasswordButton.addClickListener(event -> {
            try {
                PasswordChangeDialog dialog = new PasswordChangeDialog(userToEdit, userService, passwordEncoder);
                dialog.open();
                dialog.addConfirmListener(confirm ->
                        userService.get(userToEdit.getId()).ifPresent(user -> userToEdit = user));
            } catch (ObjectOptimisticLockingFailureException e) {
                ErrorNotification.show("Невозможно обновить запись. " +
                        "Кто-то другой обновил запись, пока вы вносили изменения");
            }
        });
        return editPasswordButton;
    }


    private HorizontalLayout createFooterLayout() {
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setWidth("100%");
        footerLayout.add(createCloseButton(), createDeleteUserButton(), createSaveUserButton());

        return footerLayout;
    }

    private Button createCloseButton() {
        Button closeButton = new Button("Отмена", new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(event -> close());
        return closeButton;
    }

    private void bindFields() {
        binder.forField(usernameField)
                .withValidator(v -> !userService.usernameIsTaken(v, userToEdit.getId()),
                        "Логин должен быть уникальным")
                .bind("username");
        binder.forField(realNameField).bind("realName");
        binder.forField(emailField).bind("email");
        binder.forField(phoneField).bind("phone");
        binder.forField(roles).bind(User::getRoles, User::setRoles);
        binder.forField(chatIdField)
                .withConverter(new StringToLongConverter("Должно быть числом"))
                .withNullRepresentation(-1L)
                .bind(User::getChatId, User::setChatId);
    }

    private Button createDeleteUserButton() {
        Button deleteUserButton = new Button("Удалить", new Icon(VaadinIcon.TRASH));
        deleteUserButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteUserButton.addClickListener(event -> {
            userService.delete(userToEdit.getId());
            close();
        });
        return deleteUserButton;
    }


    private Button createSaveUserButton() {
        Button saveUserButton = new Button("Сохранить", new Icon(VaadinIcon.CHECK));
        saveUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveUserButton.addClickListener(event -> {
            try {
                if (this.userToEdit == null) {
                    this.userToEdit = new User();
                }
                binder.writeBean(this.userToEdit);
                userService.update(this.userToEdit);
                close();
            } catch (ObjectOptimisticLockingFailureException exception) {
                ErrorNotification.show("Невозможно обновить запись. " +
                        "Кто-то другой обновил запись, пока вы вносили изменения");
            } catch (ValidationException validationException) {
                ErrorNotification.show("Невозможно обновить запись. Проверьте правильность введённых данных");
            }
        });

        return saveUserButton;
    }
}
