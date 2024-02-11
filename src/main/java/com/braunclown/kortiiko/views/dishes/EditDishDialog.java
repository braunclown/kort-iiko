package com.braunclown.kortiiko.views.dishes;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.Mode;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.iiko.DishImportService;
import com.braunclown.kortiiko.services.iiko.IikoProperties;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class EditDishDialog extends Dialog {
    private TextField nameField;
    private TextField amountField;
    private HorizontalLayout iikoIdLayout;
    private Button iikoIdButton;
    private TextField iikoIdField;
    private TextField multiplicityField;
    private TextField initialAmountField;
    private ComboBox<Mode> mode;
    private TextField measureField;
    private Checkbox isGroup;
    private ComboBox<Dish> parentGroup;

    private Button closeButton;
    private Dish dishToEdit;

    private final DishService dishService;
    private final IikoProperties iikoProperties;
    private final BeanValidationBinder<Dish> binder;

    public EditDishDialog(Dish dishToEdit, DishService dishService, IikoProperties iikoProperties) {
        this.dishToEdit = dishToEdit;
        this.dishService = dishService;
        this.iikoProperties = iikoProperties;
        this.binder = new BeanValidationBinder<>(Dish.class);
        configureDialog();
        add(createEditingFields());
        bindFields();
        binder.readBean(this.dishToEdit);
        getFooter().add(createFooterLayout());
    }

    private void configureDialog() {
        setHeaderTitle(createTitle());
        setModal(false);
        setDraggable(true);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
    }

    private Component createEditingFields() {
        nameField = new TextField("Название");

        amountField = new TextField("Остатки (текущие)");

        iikoIdLayout = createIikoIdLayout();

        multiplicityField = new TextField("Кратность (какое количество может приготовить повар)");

        initialAmountField = new TextField("Остатки по умолчанию (на начало смены)");

        mode = new ComboBox<>("Режим пополнения");
        mode.setItems(Mode.SALES, Mode.MAX);
        mode.setItemLabelGenerator(m -> (m == Mode.MAX) ? "До макс." : "Продажи");
        // TODO: Починить binder, реализовать логику сохранения изменений для блюд и групп

        measureField = new TextField("Единица измерения");

        isGroup = new Checkbox("Является группой");

        parentGroup = new ComboBox<>("Родительская группа");
        parentGroup.setItems(dishService.findGroups());
        parentGroup.setItemLabelGenerator(Dish::getName);

        return new FormLayout(nameField, amountField, iikoIdLayout, multiplicityField,
                initialAmountField, mode, measureField, isGroup, parentGroup);
    }

    private HorizontalLayout createIikoIdLayout() {
        iikoIdField = new TextField("Идентификатор iiko");
        iikoIdField.setEnabled(false);
        iikoIdField.setMinWidth("100px");

        iikoIdButton = new Button("Запросить по названию");
        iikoIdButton.addClassNames(LumoUtility.Margin.Top.AUTO);
        iikoIdButton.addClickListener(event -> {
            DishImportService dishImportService = new DishImportService(dishService, iikoProperties);
            iikoIdField.setValue(dishImportService.getIikoId(nameField.getValue()));
        });

        iikoIdLayout = new HorizontalLayout(iikoIdField, iikoIdButton);
        iikoIdLayout.setFlexGrow(1, iikoIdField);
        return iikoIdLayout;
    }

    private void bindFields() {
        binder.forField(nameField).bind("name");
        binder.forField(amountField)
                .withConverter(new StringToDoubleConverter("Введите целое или дробное число"))
                .bind("amount");
        binder.forField(iikoIdField).bind("iikoId");
        binder.forField(multiplicityField)
                .withConverter(new StringToDoubleConverter("Введите целое или дробное число"))
                .bind("multiplicity");
        binder.forField(initialAmountField)
                .withConverter(new StringToDoubleConverter("Введите целое или дробное число"))
                .bind("initialAmount");
        binder.forField(mode).bind("mode");
        binder.forField(measureField).bind("measure");
        binder.forField(isGroup).bind(Dish::getGroup, Dish::setGroup);
        binder.forField(parentGroup).bind("parentGroup");
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
