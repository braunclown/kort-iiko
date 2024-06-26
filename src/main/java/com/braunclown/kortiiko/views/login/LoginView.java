package com.braunclown.kortiiko.views.login;

import com.braunclown.kortiiko.security.AuthenticatedUser;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@PageTitle("Вход в учётную запись")
@Route(value = "login")
public class LoginView extends LoginOverlay implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Kört-iiko");
        i18n.getForm().setTitle("Введите логин и пароль");
        i18n.getForm().setUsername("Имя пользователя");
        i18n.getForm().setPassword("Пароль");
        i18n.getForm().setSubmit("Войти");
        i18n.getErrorMessage().setTitle("Неправильное имя пользователя или пароль");
        i18n.getErrorMessage().setMessage("Проверьте правильность ввода и попробуйте снова");
        i18n.getErrorMessage().setUsername("Необходимо ввести имя пользователя");
        i18n.getErrorMessage().setPassword("Необходимо ввести пароль");
        i18n.setAdditionalInformation(null);
        setI18n(i18n);

        setForgotPasswordButtonVisible(false);
        setOpened(true);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            setOpened(false);
            event.forwardTo("");
        }

        setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
