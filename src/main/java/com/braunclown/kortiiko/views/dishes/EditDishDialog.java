package com.braunclown.kortiiko.views.dishes;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.Mode;
import com.braunclown.kortiiko.services.DishService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.converter.StringToDoubleConverter;

public class EditDishDialog extends Dialog {
    private Button closeButton;
    private Dish dishToEdit;

    private final DishService dishService;
    private final BeanValidationBinder<Dish> binder;

    public EditDishDialog(Dish dishToEdit, DishService dishService) {
        this.dishToEdit = dishToEdit;
        this.dishService = dishService;
        this.binder = new BeanValidationBinder<>(Dish.class);
        setHeaderTitle(createTitle());
        setModal(false);
        setDraggable(true);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
        add(createEditingFields());
        getFooter().add(createFooterLayout());
    }

    private Component createEditingFields() {
        FormLayout layout = new FormLayout();
        binder.readBean(this.dishToEdit);

        TextField nameField = new TextField("Название");
        binder.forField(nameField).bind(Dish::getName, Dish::setName);

        TextField amountField = new TextField("Остатки (текущие)");
        binder.forField(amountField)
                .withConverter(new StringToDoubleConverter("Введите целое или дробное число"))
                .bind("amount");

        TextField iikoIdField = new TextField("Идентификатор iiko");
        binder.forField(iikoIdField).bind("iikoId");

        TextField multiplicityField = new TextField("Кратность (какое количество может приготовить повар)");
        binder.forField(multiplicityField)
                .withConverter(new StringToDoubleConverter("Введите целое или дробное число"))
                .bind("multiplicity");

        TextField initialAmountField = new TextField("Остатки по умолчанию (на начало смены)");
        binder.forField(initialAmountField)
                .withConverter(new StringToDoubleConverter("Введите целое или дробное число"))
                .bind("initialAmount");

        ComboBox<Mode> mode = new ComboBox<>("Режим пополнения");
        // TODO: Починить binder, реализовать логику сохранения изменений для блюд и групп
        binder.forField(mode).bind("mode");

        TextField measureField = new TextField("Единица измерения");
        binder.forField(measureField).bind("measure");


        layout.add(nameField, amountField, iikoIdField, multiplicityField, initialAmountField, mode, measureField);
        return layout;
    }

    private String createTitle() {
        return (dishToEdit.getGroup() ? "Группа '" : "Блюдо '") + dishToEdit.getName() + "'";
    }

    private HorizontalLayout createFooterLayout() {
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setWidth("100%");
        footerLayout.add(createCloseButton());

        return footerLayout;
    }

    private Button createCloseButton() {
        closeButton = new Button("Отмена");
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(event -> close());
        return closeButton;
    }
}
