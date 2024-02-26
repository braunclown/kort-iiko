package com.braunclown.kortiiko.views.main;

import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Главная")
@Route(value = "main", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class MainView extends VerticalLayout {

    public MainView() {
        add(createLayout());
    }

    private Component createLayout() {
        H3 header = new H3("Скоро здесь появятся полезные советы");

        return new VerticalLayout(header);
    }

}
