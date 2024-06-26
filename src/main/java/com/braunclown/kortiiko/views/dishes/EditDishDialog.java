package com.braunclown.kortiiko.views.dishes;

import com.braunclown.kortiiko.components.ErrorNotification;
import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.DishSetting;
import com.braunclown.kortiiko.data.Mode;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.DishSettingService;
import com.braunclown.kortiiko.services.iiko.DishImportService;
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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

public class EditDishDialog extends Dialog {
    private TextField nameField;
    private TextField amountField;
    private TextField iikoIdField;
    private TextField multiplicityField;
    private TextField initialAmountField;
    private ComboBox<Mode> modeComboBox;
    private TextField measureField;
    private Checkbox isGroup;
    private ComboBox<Dish> parentGroup;

    private Dish dishToEdit;

    private final DishService dishService;
    private final DishSettingService dishSettingService;
    private final DishImportService dishImportService;
    private final BeanValidationBinder<Dish> binder;

    public EditDishDialog(Dish dishToEdit,
                          DishService dishService,
                          DishSettingService dishSettingService,
                          DishImportService dishImportService) {
        this.dishToEdit = dishToEdit;
        this.dishService = dishService;
        this.dishSettingService = dishSettingService;
        this.dishImportService = dishImportService;
        this.binder = new BeanValidationBinder<>(Dish.class);
        configureDialog();
        add(createEditingFields());
        bindFields();
        binder.readBean(this.dishToEdit);
        getFooter().add(createFooterLayout());
    }

    private void configureDialog() {
        setHeaderTitle(createTitle());
        setResizable(true);
        setDraggable(true);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
    }

    private Component createEditingFields() {
        nameField = new TextField("Название");

        isGroup = new Checkbox("Является группой");

        parentGroup = new ComboBox<>("Родительская группа");
        parentGroup.setItems(dishService.findGroups());
        parentGroup.setItemLabelGenerator(Dish::getName);

        return new FormLayout(nameField, createAmountLayout(), createIikoIdLayout(), createMultiplicityLayout(),
                createInitialAmountLayout(), createModeLayout(), createMeasureLayout(), isGroup, parentGroup);
    }

    private HorizontalLayout createIikoIdLayout() {
        iikoIdField = new TextField("Идентификатор iiko");
        iikoIdField.setReadOnly(true);
        iikoIdField.setMinWidth("100px");

        Button iikoIdButton = new Button("Запросить по названию");
        iikoIdButton.addClassName(LumoUtility.Margin.Top.AUTO);
        iikoIdButton.addClickListener(event -> iikoIdField.setValue(dishImportService.getIikoId(nameField.getValue())));

        HorizontalLayout iikoIdLayout = new HorizontalLayout(iikoIdField, iikoIdButton);
        iikoIdLayout.setFlexGrow(1, iikoIdField);
        return iikoIdLayout;
    }

    private HorizontalLayout createAmountLayout() {
        amountField = new TextField("Остатки (текущие)");
        amountField.setMinWidth("100px");

        Button amountButton = new Button("Обновить детей");
        amountButton.setTooltipText("Обновить остатки данной группы и всех вложенных подгрупп и блюд");
        amountButton.addClassName(LumoUtility.Margin.Top.AUTO);
        amountButton.addClickListener(event -> {
            try {
                double amount = Double.parseDouble(amountField.getValue().replace(",", "."));
                if (amount >= 0) {
                    updateChildDishAmount(dishToEdit, amount);
                    dishService.get(dishToEdit.getId()).ifPresent(d -> dishToEdit = d);
                    Notification.show("Дочерние элементы обновлены");
                } else {
                    ErrorNotification.show("Число должно быть неотрицательным");
                }
            } catch (Exception e) {
                ErrorNotification.show("Проверьте правильность введённых данных");
            }
        });
        amountButton.setVisible(!dishToEdit.getChildDishes().isEmpty());

        HorizontalLayout amountLayout = new HorizontalLayout(amountField, amountButton);
        amountLayout.setFlexGrow(1, amountField);
        return amountLayout;
    }

    private HorizontalLayout createInitialAmountLayout() {
        initialAmountField = new TextField("Остатки по умолчанию (на начало смены)");
        initialAmountField.setMinWidth("100px");

        Button initialAmountButton = new Button("Обновить детей");
        initialAmountButton.setTooltipText("Обновить остатки по умолчанию данной группы и всех вложенных подгрупп и блюд");
        initialAmountButton.addClassName(LumoUtility.Margin.Top.AUTO);
        initialAmountButton.addClickListener(event -> {
            try {
                double initialAmount = Double.parseDouble(initialAmountField.getValue()
                        .replace(",", "."));
                if (initialAmount >= 0) {
                    updateChildDishInitialAmount(dishToEdit, initialAmount);
                    dishService.get(dishToEdit.getId()).ifPresent(d -> dishToEdit = d);
                    Notification.show("Дочерние элементы обновлены");
                } else {
                    ErrorNotification.show("Число должно быть неотрицательным");
                }
            } catch (Exception e) {
                ErrorNotification.show("Проверьте правильность введённых данных");
            }
        });
        initialAmountButton.setVisible(!dishToEdit.getChildDishes().isEmpty());

        HorizontalLayout initialAmountLayout = new HorizontalLayout(initialAmountField, initialAmountButton);
        initialAmountLayout.setFlexGrow(1, initialAmountField);
        return initialAmountLayout;
    }

    private HorizontalLayout createModeLayout() {
        modeComboBox = new ComboBox<>("Режим пополнения");
        modeComboBox.setItems(Mode.SALES, Mode.MAX);
        modeComboBox.setItemLabelGenerator(m -> (m == Mode.MAX) ? "До макс." : "Продажи");
        modeComboBox.setMinWidth("100px");

        HorizontalLayout modeLayout = new HorizontalLayout(modeComboBox, createModeButton());
        modeLayout.setFlexGrow(1, modeComboBox);
        return modeLayout;
    }

    private Button createModeButton() {
        Button modeButton = new Button("Обновить детей");
        modeButton.setTooltipText("Обновить режим пополнения данной группы и всех вложенных подгрупп и блюд");
        modeButton.addClassName(LumoUtility.Margin.Top.AUTO);
        modeButton.addClickListener(event -> {
            try {
                updateChildDishMode(dishToEdit, modeComboBox.getValue());
                dishService.get(dishToEdit.getId()).ifPresent(d -> dishToEdit = d);
                Notification.show("Дочерние элементы обновлены");
            } catch (Exception e) {
                ErrorNotification.show("Проверьте правильность введённых данных");
            }
        });
        modeButton.setVisible(!dishToEdit.getChildDishes().isEmpty());
        return modeButton;
    }

    private HorizontalLayout createMultiplicityLayout() {
        multiplicityField = new TextField("Кратность (какое количество может приготовить повар)");
        multiplicityField.setMinWidth("100px");

        Button multiplicityButton = new Button("Обновить детей");
        multiplicityButton.setTooltipText("Обновить кратность данной группы и всех вложенных подгрупп и блюд");
        multiplicityButton.addClassName(LumoUtility.Margin.Top.AUTO);
        multiplicityButton.addClickListener(event -> {
            try {
                double multiplicity = Double.parseDouble(multiplicityField.getValue().replace(",", "."));
                if (multiplicity > 0) {
                    updateChildDishMultiplicity(dishToEdit, multiplicity);
                    dishService.get(dishToEdit.getId()).ifPresent(d -> dishToEdit = d);
                    Notification.show("Дочерние элементы обновлены");
                } else {
                    ErrorNotification.show("Число должно быть положительным");
                }
            } catch (Exception e) {
                ErrorNotification.show("Проверьте правильность введённых данных");
            }
        });
        multiplicityButton.setVisible(!dishToEdit.getChildDishes().isEmpty());

        HorizontalLayout multiplicityLayout = new HorizontalLayout(multiplicityField, multiplicityButton);
        multiplicityLayout.setFlexGrow(1, multiplicityField);
        return multiplicityLayout;
    }

    private HorizontalLayout createMeasureLayout() {
        measureField = new TextField("Единица измерения");
        measureField.setMinWidth("100px");

        Button measureButton = new Button("Обновить детей");
        measureButton.setTooltipText("Обновить единицу измерения данной группы и всех вложенных подгрупп и блюд");
        measureButton.addClassName(LumoUtility.Margin.Top.AUTO);
        measureButton.addClickListener(event -> {
            try {
                updateChildDishMeasure(dishToEdit, measureField.getValue());
                dishService.get(dishToEdit.getId()).ifPresent(d -> dishToEdit = d);
                Notification.show("Дочерние элементы обновлены");
            } catch (Exception e) {
                ErrorNotification.show("Проверьте правильность введённых данных");
            }
        });
        measureButton.setVisible(!dishToEdit.getChildDishes().isEmpty());

        HorizontalLayout measureLayout = new HorizontalLayout(measureField, measureButton);
        measureLayout.setFlexGrow(1, measureField);
        return measureLayout;
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
                .withValidator((number, context) -> {
                    for (DishSetting dishSetting: dishSettingService.findByDish(dishToEdit)) {
                        if (number > dishSetting.getMaxAmount() - dishSetting.getMinAmount()) {
                            return ValidationResult.error(
                                    "Кратность больше разности максимума и минимума для периода "
                                            + dishSetting.getStablePeriod().getDayType().getName()
                                            + " | " + dishSetting.getStablePeriod().getStartTime()
                                            + "-" + dishSetting.getStablePeriod().getEndTime());
                        }
                    }
                    return ValidationResult.ok();
                })
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

    private String createTitle() {
        return (dishToEdit.getGroup() ? "Группа '" : "Блюдо '") + dishToEdit.getName() + "'";
    }

    private HorizontalLayout createFooterLayout() {
        HorizontalLayout footerLayout = new HorizontalLayout();
        footerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerLayout.setWidth("100%");
        footerLayout.add(createCloseButton(), createDeleteDishButton(), createSaveDishButton());

        return footerLayout;
    }

    private Button createCloseButton() {
        Button closeButton = new Button("Отмена", new Icon(VaadinIcon.CLOSE));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        closeButton.addClickListener(event -> close());
        return closeButton;
    }

    private Button createDeleteDishButton() {
        Button deleteDishButton = new Button("Удалить", new Icon(VaadinIcon.TRASH));
        deleteDishButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteDishButton.addClickListener(event -> {
            dishService.cascadeDelete(dishToEdit);
            close();
        });
        return deleteDishButton;
    }

    private Button createSaveDishButton() {
        Button saveDishButton = new Button("Сохранить", new Icon(VaadinIcon.CHECK));
        saveDishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveDishButton.addClickListener(event -> {
            try {
                if (this.dishToEdit == null) {
                    this.dishToEdit = new Dish();
                }
                binder.writeBean(this.dishToEdit);
                dishService.update(this.dishToEdit);
                close();
            } catch (ObjectOptimisticLockingFailureException exception) {
                ErrorNotification.show("Невозможно обновить запись. " +
                        "Кто-то другой обновил запись, пока вы вносили изменения");
            } catch (ValidationException validationException) {
                ErrorNotification.show("Невозможно обновить запись. Проверьте правильность введённых данных");
            }
        });

        return saveDishButton;
    }


    private void updateChildDishAmount(Dish dish, Double amount) {
        dish.setAmount(amount);
        dishService.update(dish);
        for (Dish child: dish.getChildDishes()) {
            updateChildDishAmount(child, amount);
        }
    }

    private void updateChildDishInitialAmount(Dish dish, Double initialAmount) {
        dish.setInitialAmount(initialAmount);
        dishService.update(dish);
        for (Dish child: dish.getChildDishes()) {
            updateChildDishInitialAmount(child, initialAmount);
        }
    }

    private void updateChildDishMultiplicity(Dish dish, Double multiplicity) {
        boolean isMultiplicityValid = true;
        for (DishSetting dishSetting: dishSettingService.findByDish(dish)) {
            if (multiplicity > dishSetting.getMaxAmount() - dishSetting.getMinAmount()) {
                isMultiplicityValid = false;
                break;
            }
        }
        if (isMultiplicityValid) {
            dish.setMultiplicity(multiplicity);
            dishService.update(dish);
        } else {
            Notification n = Notification.show("Невозможно обновить "
                    + (dish.getGroup() ? "группу ": "блюдо ") + dish.getName()
                    + ": кратность меньше разности максимума и минимума");
            n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
        for (Dish child: dish.getChildDishes()) {
            updateChildDishMultiplicity(child, multiplicity);
        }
    }

    private void updateChildDishMeasure(Dish dish, String measure) {
        dish.setMeasure(measure);
        dishService.update(dish);
        for (Dish child: dish.getChildDishes()) {
            updateChildDishMeasure(child, measure);
        }
    }

    private void updateChildDishMode(Dish dish, Mode mode) {
        dish.setMode(mode);
        dishService.update(dish);
        for (Dish child: dish.getChildDishes()) {
            updateChildDishMode(child, mode);
        }
    }
}
