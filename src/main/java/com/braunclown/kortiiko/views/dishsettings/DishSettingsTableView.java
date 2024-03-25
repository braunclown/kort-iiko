package com.braunclown.kortiiko.views.dishsettings;

import com.braunclown.kortiiko.data.*;
import com.braunclown.kortiiko.services.DayTypeService;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.DishSettingService;
import com.braunclown.kortiiko.services.StablePeriodService;
import com.braunclown.kortiiko.views.MainLayout;
import com.braunclown.kortiiko.views.dishes.DishesView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Optional;

@PageTitle("Настройки пополнения")
@Route(value = "dish-settings-table/:dishID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class DishSettingsTableView extends Div implements BeforeEnterObserver {
    private final String DISH_ID = "dishID";

    private final StablePeriodService stablePeriodService;
    private final DishSettingService dishSettingService;
    private final DishService dishService;
    private final DayTypeService dayTypeService;

    private Dish dish;

    private final VerticalLayout layout = new VerticalLayout();
    private MultiSelectComboBox<DayType> dayTypeBox;
    private Grid<StablePeriod> grid;

    public DishSettingsTableView(StablePeriodService stablePeriodService,
                                 DishSettingService dishSettingService,
                                 DishService dishService,
                                 DayTypeService dayTypeService) {
        this.stablePeriodService = stablePeriodService;
        this.dishSettingService = dishSettingService;
        this.dishService = dishService;
        this.dayTypeService = dayTypeService;
        setSizeFull();
        addClassNames("dish-settings-table-view");

        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> dishId = event.getRouteParameters().get(DISH_ID).map(Long::parseLong);
        if (dishId.isPresent()) {
            Optional<Dish> dishFromBackend = dishService.get(dishId.get());
            if (dishFromBackend.isPresent()) {
                dish = dishFromBackend.get();
                layout.add(createMenu());
                layout.add(createGrid());
            } else {
                Notification.show(
                        String.format("Запрошенное блюдо не найдено, ID = %s", dishId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                event.forwardTo(DishesView.class);
            }
        }
    }

    private Component createMenu() {
        Div menuLayout = new Div();
        menuLayout.addClassNames(LumoUtility.Display.FLEX,
                LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.FlexWrap.WRAP,
                LumoUtility.Gap.Column.LARGE,
                LumoUtility.Gap.Row.SMALL,
                LumoUtility.Width.FULL,
                LumoUtility.BoxSizing.BORDER);
        menuLayout.add(createDishData(),
                createDayTypeLayout(),
                createNavigateLayout());
        return menuLayout;
    }

    private Component createDishData() {
        Div dishData = new Div(
                new Div((dish.getGroup() ? "Группа: " : "Блюдо: ") + dish.getName()),
                new Div("Режим пополнения: " + (dish.getMode() == Mode.MAX ? "До макс." : "Продажи")),
                new Div("Кратность: " + dish.getMultiplicity() + " " + dish.getMeasure()),
                new Div("Остатки по умолчанию: " + dish.getInitialAmount() + " " + dish.getMeasure())
        );
        dishData.addClassNames(LumoUtility.Margin.Top.AUTO, LumoUtility.Padding.Horizontal.SMALL);
        return dishData;
    }

    private Component createDayTypeLayout() {
        List<DayType> dayTypes = dayTypeService.findAll();
        dayTypeBox = new MultiSelectComboBox<>("Типы смен", dayTypes);
        dayTypeBox.setItemLabelGenerator(DayType::getName);
        dayTypeBox.select(dayTypes);
        dayTypeBox.addSelectionListener(event -> refreshGrid());
        dayTypeBox.addClassNames(LumoUtility.Margin.Top.AUTO);
        Button selectAll = new Button("Выбрать все");
        selectAll.addClickListener(event -> dayTypeBox.select(dayTypes));
        selectAll.addClassNames(LumoUtility.Margin.Top.AUTO);
        Button selectNone = new Button("Очистить");
        selectNone.addClickListener(event -> dayTypeBox.deselectAll());
        selectNone.addClassNames(LumoUtility.Margin.Top.AUTO);
        HorizontalLayout dayTypeLayout = new HorizontalLayout(dayTypeBox, selectAll, selectNone);
        dayTypeLayout.addClassNames(LumoUtility.Margin.Top.AUTO);
        return dayTypeLayout;
    }

    private Component createNavigateLayout() {
        Button refreshButton = new Button("Обновить", new Icon(VaadinIcon.REFRESH));
        refreshButton.addClickListener(event -> refreshGrid());
        Button getBackButton = new Button("Вернуться к блюдам", new Icon(VaadinIcon.ARROW_LEFT));
        getBackButton.addClickListener(event -> UI.getCurrent().navigate(DishesView.class));
        HorizontalLayout navigateLayout = new HorizontalLayout(refreshButton, getBackButton);
        navigateLayout.addClassNames(LumoUtility.Margin.Top.AUTO);
        return navigateLayout;
    }

    private Component createGrid() {
        grid = new Grid<>(StablePeriod.class, false);
        Grid.Column<StablePeriod> dayTypeColumn = grid.addColumn(stablePeriod -> stablePeriod.getDayType().getName()).setAutoWidth(true)
                .setHeader("Тип смены").setSortable(true);
        Grid.Column<StablePeriod> startTimeColumn = grid.addColumn(StablePeriod::getStartTime).setAutoWidth(true)
                .setHeader("Время начала").setSortable(true);
        grid.addColumn(StablePeriod::getEndTime).setAutoWidth(true)
                .setHeader("Время конца").setSortable(true);
        grid.addComponentColumn(stablePeriod -> {
            Optional<DishSetting> currentDishSetting = dishSettingService.getByDishAndStablePeriod(dish, stablePeriod);
            Span span = new Span("Не задано");
            currentDishSetting.ifPresent(dishSetting -> span.setText(dishSetting.getMinAmount().toString()));
            return span;
        }).setHeader("Мин. остаток").setSortable(true);
        grid.addComponentColumn(stablePeriod -> {
            Optional<DishSetting> currentDishSetting = dishSettingService.getByDishAndStablePeriod(dish, stablePeriod);
            Span span = new Span("Не задано");
            currentDishSetting.ifPresent(dishSetting -> span.setText(dishSetting.getMaxAmount().toString()));
            return span;
        }).setHeader("Макс. остаток").setSortable(true);
        grid.addComponentColumn(stablePeriod -> {
            Optional<DishSetting> currentDishSetting = dishSettingService.getByDishAndStablePeriod(dish, stablePeriod);
            DishSetting dishSetting = new DishSetting();
            if (currentDishSetting.isPresent()) {
                dishSetting = currentDishSetting.get();
            } else {
                dishSetting.setDish(dish);
                dishSetting.setStablePeriod(stablePeriod);
                dishSetting.setMinAmount(dishSetting.getDish().getMultiplicity());
                dishSetting.setMaxAmount(dishSetting.getDish().getMultiplicity() * 2);
            }
            return getDialogButton(dishSetting);
        }).setHeader("Настроить");
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
        grid.sort(new GridSortOrderBuilder<StablePeriod>().thenAsc(dayTypeColumn).thenAsc(startTimeColumn).build());
        refreshGrid();
        return grid;
    }

    private Button getDialogButton(DishSetting dishSetting) {
        Button button = new Button("Редактировать", event -> {
            DishSettingsDialog dishSettingsDialog =
                    new DishSettingsDialog(dishSettingService, dishSetting, stablePeriodService);
            dishSettingsDialog.open();
            dishSettingsDialog.addOpenedChangeListener(e -> {
                if (!e.isOpened()) {
                    refreshGrid();
                }
            });
        });
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        button.setIcon(VaadinIcon.EDIT.create());
        return button;
    }

    private void refreshGrid() {
        grid.setItems(stablePeriodService.findByDayTypeIn(dayTypeBox.getValue()));
    }

}
