package com.braunclown.kortiiko.views.main;

import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
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
        H3 adminHeader = new H3("Руководство администратора");
        Html adminGuide = new Html("<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/pH4N_2wd7E0?si=GJVvr8rCmd42XX00\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen></iframe>");
        H3 cookHeader = new H3("Руководство повара");
        Html cookGuide = new Html("<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/B7ycgm-0Lmc?si=usNkQDLSZ0FrsybD\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen></iframe>");


        return new VerticalLayout(adminHeader, adminGuide, cookHeader, cookGuide);
    }

}
