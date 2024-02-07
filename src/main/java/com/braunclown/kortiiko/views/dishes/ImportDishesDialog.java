package com.braunclown.kortiiko.views.dishes;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ImportDishesDialog extends Dialog {
    private Button closeButton;
    private Button importButton;

    public ImportDishesDialog() {
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setHeaderTitle("Импортировать номенклатуру из iiko?");
        add(createWarning());
        getFooter().add(createFooterLayout());
    }

    private VerticalLayout createWarning() {
        VerticalLayout warningLayout = new VerticalLayout();
        warningLayout.add(new Div(VaadinIcon.WARNING.create(), new Span("Внимание!")));
        warningLayout.add(new Div("Импорт из iiko приведёт к перезаписи имеющихся данных о номенклатуре и настроек пополнения."));
        warningLayout.add(new Div("Не закрывайте окно до окончания импорта."));
        return warningLayout;
    }

    private HorizontalLayout createFooterLayout() {
        HorizontalLayout footerLayout = new HorizontalLayout(createCloseButton(), createImportButton());
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setWidth("100%");
        return footerLayout;
    }

    private Button createCloseButton() {
        closeButton = new Button("Отмена");
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(event -> close());
        return closeButton;
    }

    private Button createImportButton() {
        importButton = new Button("Начать импорт");
        importButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        importButton.addClickListener(event -> importDishes());
        return importButton;
    }

    private void importDishes() {
        // TODO: Вызов сервиса импорта блюд
    }
}
