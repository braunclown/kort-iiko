package com.braunclown.kortiiko.views.dishsettings;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.DishSetting;
import com.braunclown.kortiiko.data.StablePeriod;
import com.braunclown.kortiiko.services.DishSettingService;
import com.braunclown.kortiiko.services.StablePeriodService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;

public class DishSettingsDialog extends Dialog {

    private final DishSettingService dishSettingService;
    private final StablePeriodService stablePeriodService;
    private DishSetting dishSetting;
    private TextField dishField;
    private TextField periodField;
    private Button minAmountChildrenButton;
    private Button minAmountPeriodsButton;
    private Button minAmountDoBothButton;
    private TextField minAmountField;
    private Button maxAmountChildrenButton;
    private Button maxAmountPeriodsButton;
    private Button maxAmountDoBothButton;
    private TextField maxAmountField;
    private TextField multiplicityField;
    private TextField measureField;
    private String errorMessage;

    private Button closeButton;
    private Button saveButton;

    private BeanValidationBinder<DishSetting> binder;
    // TODO: автообновление при закрытии
    // TODO: точка = запятая в полях ввода
    // TODO: кнопка "подробнее" в сообщении об ошибке (> 3 блюд)

    public DishSettingsDialog(DishSettingService dishSettingService,
                              DishSetting dishSetting,
                              StablePeriodService stablePeriodService) {
        this.dishSettingService = dishSettingService;
        this.dishSetting = dishSetting;
        this.stablePeriodService = stablePeriodService;
        configureDialog();
        add(createEditingFields());
        bindFields();
        setMaxWidth("1000px");
        getFooter().add(createFooterLayout());
    }

    private void configureDialog() {
        setHeaderTitle(dishSetting.getDish().getName()
                + " | " + dishSetting.getStablePeriod().getStartTime()
                + "-" + dishSetting.getStablePeriod().getEndTime());
        setResizable(true);
        setDraggable(true);
        setCloseOnEsc(false);
        setCloseOnOutsideClick(false);
    }

    private Component createEditingFields() {
        dishField = new TextField("Блюдо");
        dishField.setReadOnly(true);
        periodField = new TextField("Период");
        periodField.setReadOnly(true);
        measureField = new TextField("Единица измерения");
        measureField.setReadOnly(true);
        multiplicityField = new TextField("Кратность");
        multiplicityField.setReadOnly(true);

        return new FormLayout(dishField, periodField, measureField, multiplicityField,
                createMinAmountLayout(), createMaxAmountLayout());
    }

    private Component createMinAmountLayout() {
        minAmountField = new TextField("Минимальные запасы");
        minAmountChildrenButton = new Button("Применить к детям");
        minAmountChildrenButton.addClickListener(event -> {
            try {
                errorMessage = "";
                Double minAmount = Double.parseDouble(minAmountField.getValue());
                updateChildrenMinAmount(dishSetting, minAmount);
                dishSettingService.getByDishAndStablePeriod(dishSetting.getDish(), dishSetting.getStablePeriod())
                        .ifPresent(d -> dishSetting = d);
                Notification.show("Дочерние элементы обновлены");
                openErrorDialog();
            } catch (Exception e) {
                Notification n = Notification.show(
                        "Проверьте правильность введённых данных");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        minAmountChildrenButton.setTooltipText("Применить настройку к дочерним группам и блюдам для данного периода");

        minAmountPeriodsButton = new Button("Применить ко всем периодам");
        minAmountPeriodsButton.addClickListener(event -> {
            try {
                errorMessage = "";
                Double minAmount = Double.parseDouble(minAmountField.getValue());
                updateAllPeriodsMinAmount(dishSetting.getDish(), minAmount);
                dishSettingService.getByDishAndStablePeriod(dishSetting.getDish(), dishSetting.getStablePeriod())
                        .ifPresent(d -> dishSetting = d);
                Notification.show("Настройки пополнения обновлены для всех периодов");
                openErrorDialog();
            } catch (Exception e) {
                Notification n = Notification.show(
                        "Проверьте правильность введённых данных");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        minAmountPeriodsButton.setTooltipText("Применить настройку данного блюда для всех 'стабильных' периодов");

        minAmountDoBothButton = new Button("Применить к детям во всех периодах");
        minAmountDoBothButton.addClickListener(event -> {
            try {
                errorMessage = "";
                Double minAmount = Double.parseDouble(minAmountField.getValue());
                updateChildrenAllPeriodsMinAmount(dishSetting.getDish(), minAmount);
                dishSettingService.getByDishAndStablePeriod(dishSetting.getDish(), dishSetting.getStablePeriod())
                        .ifPresent(d -> dishSetting = d);
                Notification.show("Настройки пополнения дочерних блюд обновлены для всех периодов");
                openErrorDialog();
            } catch (Exception e) {
                Notification n = Notification.show(
                        "Проверьте правильность введённых данных");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        minAmountDoBothButton
                .setTooltipText("Применить настройку к дочерним группам и блюдам для всех 'стабильных' периодов");

        VerticalLayout layout = new VerticalLayout(minAmountField, minAmountChildrenButton,
                minAmountPeriodsButton, minAmountDoBothButton);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        layout.addClassNames(LumoUtility.Margin.Vertical.SMALL, LumoUtility.Margin.Horizontal.XSMALL,
                LumoUtility.Border.ALL, LumoUtility.BorderColor.PRIMARY);
        return layout;
    }

    private Component createMaxAmountLayout() {
        maxAmountField = new TextField("Максимальные запасы");
        maxAmountChildrenButton = new Button("Применить к детям");
        maxAmountChildrenButton.addClickListener(event -> {
            try {
                errorMessage = "";
                Double maxAmount = Double.parseDouble(maxAmountField.getValue());
                updateChildrenMaxAmount(dishSetting, maxAmount);
                dishSettingService.getByDishAndStablePeriod(dishSetting.getDish(), dishSetting.getStablePeriod())
                        .ifPresent(d -> dishSetting = d);
                Notification.show("Дочерние элементы обновлены");
                openErrorDialog();
            } catch (Exception e) {
                Notification n = Notification.show(
                        "Проверьте правильность введённых данных");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        maxAmountChildrenButton.setTooltipText("Применить настройку к дочерним группам и блюдам для данного периода");

        maxAmountPeriodsButton = new Button("Применить ко всем периодам");
        maxAmountPeriodsButton.addClickListener(event -> {
            try {
                errorMessage = "";
                Double maxAmount = Double.parseDouble(maxAmountField.getValue());
                updateAllPeriodsMaxAmount(dishSetting.getDish(), maxAmount);
                dishSettingService.getByDishAndStablePeriod(dishSetting.getDish(), dishSetting.getStablePeriod())
                        .ifPresent(d -> dishSetting = d);
                Notification.show("Настройки пополнения обновлены для всех периодов");
                openErrorDialog();
            } catch (Exception e) {
                Notification n = Notification.show(
                        "Проверьте правильность введённых данных");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        maxAmountPeriodsButton.setTooltipText("Применить настройку данного блюда для всех 'стабильных' периодов");

        maxAmountDoBothButton = new Button("Применить к детям во всех периодах");
        maxAmountDoBothButton.addClickListener(event -> {
            try {
                errorMessage = "";
                Double maxAmount = Double.parseDouble(maxAmountField.getValue());
                updateChildrenAllPeriodsMaxAmount(dishSetting.getDish(), maxAmount);
                dishSettingService.getByDishAndStablePeriod(dishSetting.getDish(), dishSetting.getStablePeriod())
                        .ifPresent(d -> dishSetting = d);
                Notification.show("Настройки пополнения дочерних блюд обновлены для всех периодов");
                openErrorDialog();
            } catch (Exception e) {
                Notification n = Notification.show(
                        "Проверьте правильность введённых данных");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        maxAmountDoBothButton
                .setTooltipText("Применить настройку к дочерним группам и блюдам для всех 'стабильных' периодов");

        VerticalLayout layout = new VerticalLayout(maxAmountField, maxAmountChildrenButton,
                maxAmountPeriodsButton, maxAmountDoBothButton);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        layout.addClassNames(LumoUtility.Margin.Vertical.SMALL, LumoUtility.Margin.Horizontal.XSMALL,
                LumoUtility.Border.ALL, LumoUtility.BorderColor.PRIMARY);
        return layout;
    }

    private void bindFields() {
        this.binder = new BeanValidationBinder<>(DishSetting.class);
        dishField.setValue(dishSetting.getDish().getName());
        periodField.setValue(dishSetting.getStablePeriod().getStartTime()
                + "-" + dishSetting.getStablePeriod().getEndTime());
        measureField.setValue(dishSetting.getDish().getMeasure());
        multiplicityField.setValue(dishSetting.getDish().getMultiplicity().toString());
        binder.forField(minAmountField)
                .withConverter(new StringToDoubleConverter("Введите целое или дробное число"))
                .bind(DishSetting::getMinAmount, DishSetting::setMinAmount);
        binder.forField(maxAmountField)
                .withConverter(new StringToDoubleConverter("Введите целое или дробное число"))
                .bind(DishSetting::getMaxAmount, DishSetting::setMaxAmount);
        binder.readBean(this.dishSetting);
    }

    private Component createFooterLayout() {
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
        saveButton = new Button("Сохранить", new Icon(VaadinIcon.CHECK));
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            try {
                binder.writeBean(this.dishSetting);
                if (validateSetting(dishSetting)) {
                    dishSettingService.update(dishSetting);
                    close();
                } else {
                    errorMessage = "";
                    ConfirmDialog dialog = createErrorDialog(createSettingValidationErrorMessage(dishSetting));
                    dialog.open();
                }
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Невозможно обновить запись. Кто-то другой обновил запись, пока вы вносили изменения");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Невозможно обновить запись. Проверьте правильность введённых данных");
            }
        });
        saveButton.setTooltipText("Применить настройки данного блюда для выбранного периода");
        return saveButton;
    }

    private void updateChildrenMinAmount(DishSetting dishSetting, Double minAmount) {
        dishSetting.setMinAmount(minAmount);
        if (validateSetting(dishSetting)) {
            dishSettingService.update(dishSetting);
        } else {
            errorMessage += createSettingValidationErrorMessage(dishSetting);
        }
        for (Dish child: dishSetting.getDish().getChildDishes()) {
            Optional<DishSetting> optionalDishSetting =
                    dishSettingService.getByDishAndStablePeriod(child, dishSetting.getStablePeriod());
            DishSetting ds = new DishSetting();
            if (optionalDishSetting.isEmpty()) {
                ds.setDish(child);
                ds.setStablePeriod(dishSetting.getStablePeriod());
                ds.setMinAmount(minAmount);
                ds.setMaxAmount(minAmount + dishSetting.getDish().getMultiplicity());
            } else {
                ds = optionalDishSetting.get();
                ds.setMinAmount(minAmount);
            }
            updateChildrenMinAmount(ds, minAmount);
        }
    }

    private void updateChildrenMaxAmount(DishSetting dishSetting, Double maxAmount) {
        dishSetting.setMaxAmount(maxAmount);
        if (validateSetting(dishSetting)) {
            dishSettingService.update(dishSetting);
        } else {
            errorMessage += createSettingValidationErrorMessage(dishSetting);
        }
        for (Dish child: dishSetting.getDish().getChildDishes()) {
            Optional<DishSetting> optionalDishSetting =
                    dishSettingService.getByDishAndStablePeriod(child, dishSetting.getStablePeriod());
            DishSetting ds = new DishSetting();
            if (optionalDishSetting.isEmpty()) {
                ds.setDish(child);
                ds.setStablePeriod(dishSetting.getStablePeriod());
                ds.setMinAmount(1d);
                ds.setMaxAmount(maxAmount);
            } else {
                ds = optionalDishSetting.get();
                ds.setMaxAmount(maxAmount);
            }
            updateChildrenMaxAmount(ds, maxAmount);
        }
    }

    private void updateAllPeriodsMinAmount(Dish dish, Double minAmount) {
        List<StablePeriod> stablePeriods = stablePeriodService.findAll();
        for (StablePeriod stablePeriod: stablePeriods) {
            Optional<DishSetting> optionalDishSetting =
                    dishSettingService.getByDishAndStablePeriod(dish, stablePeriod);
            DishSetting ds = new DishSetting();
            if (optionalDishSetting.isEmpty()) {
                ds.setDish(dish);
                ds.setStablePeriod(stablePeriod);
                ds.setMinAmount(minAmount);
                ds.setMaxAmount(minAmount + dish.getMultiplicity());
            } else {
                ds = optionalDishSetting.get();
                ds.setMinAmount(minAmount);
            }
            if (validateSetting(ds)) {
                dishSettingService.update(ds);
            } else {
                errorMessage += createSettingValidationErrorMessage(ds);
            }
        }
    }

    private void updateAllPeriodsMaxAmount(Dish dish, Double maxAmount) {
        List<StablePeriod> stablePeriods = stablePeriodService.findAll();
        for (StablePeriod stablePeriod: stablePeriods) {
            Optional<DishSetting> optionalDishSetting =
                    dishSettingService.getByDishAndStablePeriod(dish, stablePeriod);
            DishSetting ds = new DishSetting();
            if (optionalDishSetting.isEmpty()) {
                ds.setDish(dish);
                ds.setStablePeriod(stablePeriod);
                ds.setMinAmount(1d);
                ds.setMaxAmount(maxAmount);
            } else {
                ds = optionalDishSetting.get();
                ds.setMaxAmount(maxAmount);
            }
            if (validateSetting(ds)) {
                dishSettingService.update(ds);
            } else {
                errorMessage += createSettingValidationErrorMessage(ds);
            }
        }
    }

    private void updateChildrenAllPeriodsMinAmount(Dish dish, Double minAmount) {
        updateAllPeriodsMinAmount(dish, minAmount);
        for (Dish child: dish.getChildDishes()) {
            updateChildrenAllPeriodsMinAmount(child, minAmount);
        }
    }

    private void updateChildrenAllPeriodsMaxAmount(Dish dish, Double maxAmount) {
        updateAllPeriodsMaxAmount(dish, maxAmount);
        for (Dish child: dish.getChildDishes()) {
            updateChildrenAllPeriodsMaxAmount(child, maxAmount);
        }
    }

    private String createSettingValidationErrorMessage(DishSetting dishSetting) {
        return (dishSetting.getDish().getGroup() ? "Группа ": "Блюдо ") + dishSetting.getDish().getName()
                + " | Период " + dishSetting.getStablePeriod().getStartTime()
                + "-" + dishSetting.getStablePeriod().getEndTime()
                + ": Кратность " + dishSetting.getDish().getMultiplicity() + " меньше разности максимума и минимума "
                + dishSetting.getMaxAmount() + "-" + dishSetting.getMinAmount() + "\n";
    }

    private boolean validateSetting(DishSetting dishSetting) {
        return dishSetting.getDish().getMultiplicity() <= dishSetting.getMaxAmount() - dishSetting.getMinAmount();
    }

    private ConfirmDialog createErrorDialog(String dialogBody) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Невозможно обновить следующие блюда/группы:");
        Paragraph p = new Paragraph(dialogBody);
        p.addClassName(LumoUtility.Whitespace.PRE_WRAP);
        dialog.setText(p);
        dialog.setConfirmText("Понятно");
        return dialog;
    }

    private void openErrorDialog() {
        if (!errorMessage.isEmpty()) {
            ConfirmDialog dialog = createErrorDialog(errorMessage);
            dialog.open();
        }
    }
}
