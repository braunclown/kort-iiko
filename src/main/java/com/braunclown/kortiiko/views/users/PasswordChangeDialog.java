package com.braunclown.kortiiko.views.users;

import com.braunclown.kortiiko.data.User;
import com.braunclown.kortiiko.services.UserService;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordChangeDialog extends ConfirmDialog {
    private PasswordField passwordField;


    public PasswordChangeDialog(User user, UserService userService, PasswordEncoder passwordEncoder) {
        setHeader("Сменить пароль");
        setCancelable(true);
        passwordField = new PasswordField("Пароль");
        passwordField.addClassName(LumoUtility.Width.FULL);
        add(passwordField);
        setCancelText("Отмена");
        setConfirmText("Сохранить");
        addConfirmListener(event -> {
            user.setHashedPassword(passwordEncoder.encode(passwordField.getValue()));
            userService.update(user);
        });
    }
}
