package com.braunclown.kortiiko.views.dishsettings;

import com.braunclown.kortiiko.data.DishSetting;
import com.braunclown.kortiiko.services.DishSettingService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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

public class DishSettingsDialog extends Dialog {

    private final DishSettingService dishSettingService;
    private final DishSetting dishSetting;
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

    private Button closeButton;
    private Button saveButton;

    private BeanValidationBinder<DishSetting> binder;


    public DishSettingsDialog(DishSettingService dishSettingService, DishSetting dishSetting) {
        this.dishSettingService = dishSettingService;
        this.dishSetting = dishSetting;
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


        return new FormLayout(dishField, periodField, createMinAmountLayout(), createMaxAmountLayout());
    }

    private Component createMinAmountLayout() {
        minAmountField = new TextField("Минимальные запасы");
        minAmountChildrenButton = new Button("Применить к детям");
        minAmountPeriodsButton = new Button("Применить ко всем периодам");
        minAmountDoBothButton = new Button("Применить к детям во всех периодах");
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
        maxAmountPeriodsButton = new Button("Применить ко всем периодам");
        maxAmountDoBothButton = new Button("Применить к детям во всех периодах");
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
                dishSettingService.update(dishSetting);
            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Невозможно обновить запись. Кто-то другой обновил запись, пока вы вносили изменения");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Невозможно обновить запись. Проверьте правильность введённых данных");
            }
            close();
        });

        return saveButton;
    }
}
