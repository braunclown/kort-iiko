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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AddUserDialog extends Dialog {
    private TextField usernameField;
    private TextField realNameField;
    private PasswordField passwordField;
    private EmailField emailField;
    private TextField phoneField;
    private CheckboxGroup<Role> roles;

    private User userToEdit;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final BeanValidationBinder<User> binder;

    public AddUserDialog(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.binder = new BeanValidationBinder<>(User.class);
        configureDialog();
        add(createEditingFields());
        bindFields();
        getFooter().add(createFooterLayout());
    }

    private void configureDialog() {
        setHeaderTitle("Добавление пользователя");
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
        passwordField = new PasswordField("Пароль");
        return new FormLayout(usernameField, realNameField,
                emailField, phoneField,
                roles, passwordField);
    }

    private HorizontalLayout createFooterLayout() {
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setWidth("100%");
        footerLayout.add(createCloseButton(), createSaveUserButton());

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
                .withValidator(v -> !userService.usernameIsTaken(v), "Логин должен быть уникальным")
                .bind("username");
        binder.forField(realNameField).bind("realName");
        binder.forField(emailField).bind("email");
        binder.forField(phoneField).bind("phone");
        binder.forField(roles).bind(User::getRoles, User::setRoles);
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
                this.userToEdit.setHashedPassword(passwordEncoder.encode(passwordField.getValue()));
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
