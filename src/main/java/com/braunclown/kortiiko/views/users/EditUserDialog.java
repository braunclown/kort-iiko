package com.braunclown.kortiiko.views.users;

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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class EditUserDialog extends Dialog {

    private TextField usernameField;
    private TextField realNameField;
    private Button editPasswordButton;
    private EmailField emailField;
    private TextField phoneField;
    private CheckboxGroup<Role> roles;

    private Button closeButton;
    private Button deleteUserButton;
    private Button saveUserButton;

    private User userToEdit;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private BeanValidationBinder<User> binder;

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
                roles,
                createEditPasswordButton());
    }

    private Button createEditPasswordButton() {
        editPasswordButton = new Button("Сменить пароль");
        editPasswordButton.addClickListener(event -> {
            try {
                PasswordChangeDialog dialog = new PasswordChangeDialog(userToEdit, userService, passwordEncoder);
                dialog.open();
                dialog.addConfirmListener(confirm ->
                        userService.get(userToEdit.getId()).ifPresent(user -> userToEdit = user));
            } catch (ObjectOptimisticLockingFailureException e) {
                Notification n = Notification.show(
                        "Невозможно обновить запись. Кто-то другой обновил запись, пока вы вносили изменения");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
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
        closeButton = new Button("Отмена", new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(event -> close());
        return closeButton;
    }

    private void bindFields() {
        binder.forField(usernameField).bind("username");
        binder.forField(realNameField).bind("realName");
        binder.forField(emailField).bind("email");
        binder.forField(phoneField).bind("phone");
        binder.forField(roles).bind(User::getRoles, User::setRoles);
    }

    private Button createDeleteUserButton() {
        deleteUserButton = new Button("Удалить", new Icon(VaadinIcon.TRASH));
        deleteUserButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteUserButton.addClickListener(event -> {
            userService.delete(userToEdit.getId());
            close();
        });
        return deleteUserButton;
    }


    private Button createSaveUserButton() {
        saveUserButton = new Button("Сохранить", new Icon(VaadinIcon.CHECK));
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
                Notification n = Notification.show(
                        "Невозможно обновить запись. Кто-то другой обновил запись, пока вы вносили изменения");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Невозможно обновить запись. Проверьте правильность введённых данных");
            }
        });

        return saveUserButton;
    }
}
