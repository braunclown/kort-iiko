package com.braunclown.kortiiko.views.dishes;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.Mode;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.DishSettingService;
import com.braunclown.kortiiko.services.iiko.DishImportService;
import com.braunclown.kortiiko.services.iiko.IikoProperties;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class AddDishDialog extends Dialog {
    private TextField nameField;
    private TextField amountField;
    private HorizontalLayout iikoIdLayout;
    private Button iikoIdButton;
    private TextField iikoIdField;
    private TextField multiplicityField;
    private TextField initialAmountField;
    private ComboBox<Mode> modeComboBox;
    private TextField measureField;
    private Checkbox isGroup;
    private ComboBox<Dish> parentGroup;

    private Button closeButton;
    private Button saveDishButton;

    private Dish dish;
    private final DishService dishService;
    private final IikoProperties iikoProperties;
    private final DishSettingService dishSettingService;
    private final BeanValidationBinder<Dish> binder;

    public AddDishDialog(DishService dishService,
                         IikoProperties iikoProperties,
                         DishSettingService dishSettingService) {
        this.dishService = dishService;
        this.iikoProperties = iikoProperties;
        this.dishSettingService = dishSettingService;
        this.binder = new BeanValidationBinder<>(Dish.class);
        configureDialog();
        add(createEditingFields());
        bindFields();
        getFooter().add(createFooterLayout());
    }

    private void configureDialog() {
        setHeaderTitle("Добавить блюдо или группу");
        setResizable(true);
        setDraggable(true);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
    }

    private Component createEditingFields() {
        nameField = new TextField("Название");

        isGroup = new Checkbox("Является группой");

        amountField = new TextField("Остатки (текущие)");
        initialAmountField = new TextField("Остатки по умолчанию (на начало смены)");

        modeComboBox = new ComboBox<>("Режим пополнения");
        modeComboBox.setItems(Mode.SALES, Mode.MAX);
        modeComboBox.setItemLabelGenerator(m -> (m == Mode.MAX) ? "До макс." : "Продажи");
        modeComboBox.setMinWidth("100px");

        multiplicityField = new TextField("Кратность (какое количество может приготовить повар)");
        measureField = new TextField("Единица измерения");

        parentGroup = new ComboBox<>("Родительская группа (будут загружены её настройки)");
        parentGroup.setItems(dishService.findGroups());
        parentGroup.setItemLabelGenerator(Dish::getName);
        parentGroup.addValueChangeListener(event -> showParentGroupProperties(event.getValue()));

        return new FormLayout(nameField, isGroup, parentGroup, createIikoIdLayout(),
                amountField, initialAmountField, modeComboBox, multiplicityField, measureField);

    }

    private void bindFields() {
        binder.forField(nameField).bind("name");
        binder.forField(amountField)
                .withConverter(new StringToDoubleConverter("Введите целое или дробное число"))
                .withValidator(number -> number >= 0, "Число должно быть неотрицательным")
                .bind("amount");
        binder.forField(iikoIdField).bind("iikoId");
        binder.forField(multiplicityField)
                .withConverter(new StringToDoubleConverter("Введите целое или дробное число"))
                .withValidator(number -> number > 0, "Число должно быть положительным")
                .bind("multiplicity");
        binder.forField(initialAmountField)
                .withConverter(new StringToDoubleConverter("Введите целое или дробное число"))
                .withValidator(number -> number >= 0, "Число должно быть неотрицательным")
                .bind("initialAmount");
        binder.forField(modeComboBox).bind("mode");
        binder.forField(measureField).bind("measure");
        binder.forField(isGroup).bind(Dish::getGroup, Dish::setGroup);
        binder.forField(parentGroup).bind("parentGroup");
    }

    private HorizontalLayout createIikoIdLayout() {
        iikoIdField = new TextField("Идентификатор iiko");
        iikoIdField.setValue("Не найдено");
        iikoIdField.setReadOnly(true);
        iikoIdField.setMinWidth("100px");

        iikoIdButton = new Button("Запросить по названию");
        iikoIdButton.addClassName(LumoUtility.Margin.Top.AUTO);
        iikoIdButton.addClickListener(event -> {
            DishImportService dishImportService = new DishImportService(dishService, iikoProperties, dishSettingService);
            iikoIdField.setValue(dishImportService.getIikoId(nameField.getValue()));
        });

        iikoIdLayout = new HorizontalLayout(iikoIdField, iikoIdButton);
        iikoIdLayout.setFlexGrow(1, iikoIdField);
        return iikoIdLayout;
    }

    private void showParentGroupProperties(Dish parent) {
        initialAmountField.setValue(parent.getInitialAmount().toString().replace(".", ","));
        amountField.setValue(parent.getAmount().toString().replace(".", ","));
        multiplicityField.setValue(parent.getMultiplicity().toString().replace(".", ","));
        modeComboBox.setValue(parent.getMode());
        measureField.setValue(parent.getMeasure());
    }

    private HorizontalLayout createFooterLayout() {
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setWidth("100%");
        footerLayout.add(createCloseButton(), createSaveDishButton());

        return footerLayout;
    }

    private Button createCloseButton() {
        closeButton = new Button("Отмена", new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(event -> close());
        return closeButton;
    }

    private Button createSaveDishButton() {
        saveDishButton = new Button("Сохранить", new Icon(VaadinIcon.CHECK));
        saveDishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveDishButton.addClickListener(event -> {
            try {
                dish = new Dish();
                binder.writeBean(dish);
                dishService.update(dish);
                close();
            } catch (ValidationException validationException) {
                Notification.show("Невозможно обновить запись. Проверьте правильность введённых данных");
            }
        });

        return saveDishButton;
    }

}
