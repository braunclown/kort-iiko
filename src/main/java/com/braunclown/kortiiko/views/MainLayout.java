package com.braunclown.kortiiko.views;

import com.braunclown.kortiiko.data.User;
import com.braunclown.kortiiko.security.AuthenticatedUser;
import com.braunclown.kortiiko.views.dishes.DishesView;
import com.braunclown.kortiiko.views.main.MainView;
import com.braunclown.kortiiko.views.orders.OrdersView;
import com.braunclown.kortiiko.views.periods.PeriodsView;
import com.braunclown.kortiiko.views.stableperiods.StablePeriodsView;
import com.braunclown.kortiiko.views.users.UsersView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.io.ByteArrayInputStream;
import java.util.Optional;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private H2 viewTitle;

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1("Kort iiko");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        if (accessChecker.hasAccess(MainView.class)) {
            nav.addItem(new SideNavItem("Main", MainView.class, LineAwesomeIcon.HOME_SOLID.create()));

        }
        if (accessChecker.hasAccess(OrdersView.class)) {
            nav.addItem(new SideNavItem("Orders", OrdersView.class, LineAwesomeIcon.EDIT_SOLID.create()));

        }
        if (accessChecker.hasAccess(DishesView.class)) {
            nav.addItem(new SideNavItem("Dishes", DishesView.class, LineAwesomeIcon.PIZZA_SLICE_SOLID.create()));

        }
        if (accessChecker.hasAccess(PeriodsView.class)) {
            nav.addItem(new SideNavItem("Periods", PeriodsView.class, LineAwesomeIcon.CALENDAR_SOLID.create()));

        }
        if (accessChecker.hasAccess(StablePeriodsView.class)) {
            nav.addItem(
                    new SideNavItem("StablePeriods", StablePeriodsView.class, LineAwesomeIcon.CLOCK_SOLID.create()));

        }
        if (accessChecker.hasAccess(UsersView.class)) {
            nav.addItem(new SideNavItem("Users", UsersView.class, LineAwesomeIcon.USER_COG_SOLID.create()));

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
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            Anchor loginLink = new Anchor("login", "Sign in");
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
