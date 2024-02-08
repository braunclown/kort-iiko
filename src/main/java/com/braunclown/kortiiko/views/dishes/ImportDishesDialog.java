package com.braunclown.kortiiko.views.dishes;

import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.iiko.DishImportService;
import com.braunclown.kortiiko.services.iiko.IikoProperties;
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
    private HorizontalLayout footerLayout;
    private final DishService dishService;
    private final IikoProperties iikoProperties;

    public ImportDishesDialog(DishService dishService, IikoProperties iikoProperties) {
        this.dishService = dishService;
        this.iikoProperties = iikoProperties;
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        setHeaderTitle("Импортировать номенклатуру из iiko?");
        add(createWarning());
        footerLayout = createFooterLayout();
        getFooter().add(footerLayout);
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
        footerLayout.remove(closeButton, importButton);
        DishImportService dishImportService = new DishImportService(dishService, iikoProperties);
        dishImportService.importDishesAndGroups();
        Dialog dialog = new Dialog("Импорт завершён");
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);
        Button closeButton = new Button("Закрыть");
        closeButton.addClickListener(event -> {
            dialog.close();
            this.close();
        });
        dialog.getFooter().add(closeButton);
        dialog.open();

    }
}
