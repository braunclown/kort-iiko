package com.braunclown.kortiiko.views;

import com.braunclown.kortiiko.data.User;
import com.braunclown.kortiiko.security.AuthenticatedUser;
import com.braunclown.kortiiko.views.daytype.DayTypeView;
import com.braunclown.kortiiko.views.dishes.DishesView;
import com.braunclown.kortiiko.views.main.MainView;
import com.braunclown.kortiiko.views.orders.OrdersView;
import com.braunclown.kortiiko.views.periods.PeriodsView;
import com.braunclown.kortiiko.views.stableperiods.StablePeriodsView;
import com.braunclown.kortiiko.views.subscription.SubscriptionView;
import com.braunclown.kortiiko.views.users.UsersView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Optional;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private H2 viewTitle;

    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Открыть либо закрыть меню");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Image icon = new Image("images/logo32.png", "Логотип");
        icon.setWidth("32px");
        icon.setHeight("32px");

        H1 appName = new H1("Kört-iiko");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(icon, appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();
        if (accessChecker.hasAccess(MainView.class)) {
            nav.addItem(new SideNavItem("Главная", MainView.class, LineAwesomeIcon.HOME_SOLID.create()));
        }
        if (accessChecker.hasAccess(OrdersView.class)) {
            nav.addItem(new SideNavItem("Заказы", OrdersView.class, LineAwesomeIcon.EDIT_SOLID.create()));
        }
        if (accessChecker.hasAccess(DishesView.class)) {
            nav.addItem(new SideNavItem("Блюда", DishesView.class, LineAwesomeIcon.PIZZA_SLICE_SOLID.create()));
        }
        if (accessChecker.hasAccess(PeriodsView.class)) {
            nav.addItem(new SideNavItem("Сегодняшние периоды", PeriodsView.class, LineAwesomeIcon.BUSINESS_TIME_SOLID.create()));
        }
        if (accessChecker.hasAccess(StablePeriodsView.class)) {
            nav.addItem(
                    new SideNavItem("'Стабильные' периоды", StablePeriodsView.class, LineAwesomeIcon.CLOCK_SOLID.create()));
        }
        if (accessChecker.hasAccess(DayTypeView.class)) {
            nav.addItem(new SideNavItem("Типы смен", DayTypeView.class, LineAwesomeIcon.CALENDAR_SOLID.create()));
        }
        if (accessChecker.hasAccess(UsersView.class)) {
            nav.addItem(new SideNavItem("Пользователи", UsersView.class, LineAwesomeIcon.USER_COG_SOLID.create()));
        }
        if (accessChecker.hasAccess(SubscriptionView.class)) {
            nav.addItem(new SideNavItem("Telegram-чат", SubscriptionView.class, LineAwesomeIcon.TELEGRAM.create()));
        }
        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(user.getRealName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Выйти из учётной записи", e -> authenticatedUser.logout());

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Войти");
            layout.add(loginLink);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
