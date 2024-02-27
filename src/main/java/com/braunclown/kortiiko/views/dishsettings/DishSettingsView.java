package com.braunclown.kortiiko.views.dishsettings;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.DishSetting;
import com.braunclown.kortiiko.data.StablePeriod;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.DishSettingService;
import com.braunclown.kortiiko.services.StablePeriodService;
import com.braunclown.kortiiko.views.MainLayout;
import com.braunclown.kortiiko.views.stableperiods.StablePeriodsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@PageTitle("Настройки пополнения")
@Route(value = "dish-settings/:stablePeriodID?/:action?(edit)", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
@CssImport(value = "./themes/kort-iiko/dish-grid.css", themeFor = "vaadin-grid")
public class DishSettingsView extends Div implements BeforeEnterObserver {
    private final String STABLEPERIOD_ID = "stablePeriodID";
    private final String STABLEPERIOD_ROUTE_TEMPLATE = "dish-settings/%s/edit";

    private final StablePeriodService stablePeriodService;
    private final DishSettingService dishSettingService;
    private final DishService dishService;
    private StablePeriod currentPeriod;

    private TextField nameFilterField;
    private Button nameFilterButton;
    private Button getBackButton;
    private Button refreshTreeGridButton;

    private TreeGrid<Dish> treeGrid;
    private VerticalLayout layout = new VerticalLayout();
    private Div periodDiv;
    private int gridIndex;
    public DishSettingsView(StablePeriodService stablePeriodService,
                            DishSettingService dishSettingService,
                            DishService dishService) {
        this.stablePeriodService = stablePeriodService;
        this.dishSettingService = dishSettingService;
        this.dishService = dishService;
        setSizeFull();
        addClassNames("dish-settings-view");

        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);

    }

    private Component createMainMenu() {
        FlexLayout menuLayout = new FlexLayout(createFilter(), createRefreshTreeGridButton(), createGetBackButton());
        menuLayout.addClassNames(LumoUtility.Padding.Vertical.XSMALL, LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Width.FULL, LumoUtility.BoxSizing.BORDER);
        menuLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        menuLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return menuLayout;
    }

    private Component createRefreshTreeGridButton() {
        refreshTreeGridButton = new Button("Обновить", new Icon(VaadinIcon.REFRESH));
        refreshTreeGridButton.addClickListener(event -> reloadTreeGrid());
        return refreshTreeGridButton;
    }

    private Component createGetBackButton() {
        getBackButton = new Button("Вернуться к промежуткам", new Icon(VaadinIcon.ARROW_LEFT));
        getBackButton.addClickListener(event -> UI.getCurrent().navigate(StablePeriodsView.class));
        return getBackButton;
    }

    private void reloadTreeGrid() {
        List<Dish> roots = dishService.findRoots();
        treeGrid.setItems(roots, this::getChildren);
        treeGrid.expandRecursively(roots, 99);
        treeGrid.getDataProvider().refreshAll();
        if (!nameFilterField.getValue().isEmpty()) {
            filterTreeGrid();
        }
    }

    private HorizontalLayout createFilter() {
        nameFilterField = new TextField();
        nameFilterField.setMinWidth("250px");
        nameFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        nameFilterField.setPlaceholder("Название блюда или группы");

        nameFilterButton = new Button("Искать", new Icon(VaadinIcon.SEARCH));
        nameFilterButton.addClassName(LumoUtility.Margin.Top.AUTO);
        nameFilterButton.addClickListener(event -> filterTreeGrid());
        HorizontalLayout nameFilterLayout = new HorizontalLayout(nameFilterField, nameFilterButton);
        nameFilterLayout.addClassNames(LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Vertical.XSMALL);
        return nameFilterLayout;
    }

    private void filterTreeGrid() {
        TreeDataProvider<Dish> dataProvider = new TreeDataProvider<>(treeGrid.getTreeData());
        dataProvider.setFilter(dish -> dish.getName().toLowerCase().contains(nameFilterField.getValue().toLowerCase()) ||
                parentNameContainsString(dish, nameFilterField.getValue()));

        treeGrid.setDataProvider(dataProvider);
        treeGrid.expandRecursively(treeGrid.getTreeData().getRootItems(), 99);
    }

    private boolean parentNameContainsString(Dish dish, String name) {
        // Не верь подсказке IDE! Тут бывают null'ы
        while (dish.getParentGroup() != null) {
            if (dish.getParentGroup().getName().toLowerCase().contains(name.toLowerCase())) {
                return true;
            }
            dish = dish.getParentGroup();
        }
        return false;
    }

    private Component createTreeGrid(StablePeriod stablePeriod) {
        treeGrid = new TreeGrid<>();
        List<Dish> roots = dishService.findRoots();
        treeGrid.setItems(roots, this::getChildren);
        treeGrid.addHierarchyColumn(Dish::getName).setHeader("Название")
                .setAutoWidth(true).setFlexGrow(1).setResizable(true);
        treeGrid.addComponentColumn(dish -> {
            Optional<DishSetting> currentDishSetting = dishSettingService.getByDishAndStablePeriod(dish, stablePeriod);
            Span span = new Span("Не задано");
            currentDishSetting.ifPresent(dishSetting -> span.setText(dishSetting.getMinAmount().toString()));
            return span;
        }).setHeader("Минимальный остаток");
        treeGrid.addComponentColumn(dish -> {
            Optional<DishSetting> currentDishSetting = dishSettingService.getByDishAndStablePeriod(dish, stablePeriod);
            Span span = new Span("Не задано");
            currentDishSetting.ifPresent(dishSetting -> span.setText(dishSetting.getMaxAmount().toString()));
            return span;
        }).setHeader("Максимальный остаток");
        treeGrid.addColumn(Dish::getMultiplicity).setHeader("Кратность");

        treeGrid.addComponentColumn(dish -> {
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
            Button button = getDialogButton(dishSetting);
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            button.setIcon(new Icon(VaadinIcon.EDIT));
            return button;
        }).setHeader("Настроить");

        treeGrid.setClassNameGenerator(dish -> dish.getGroup() ? "group-style" : null);
        treeGrid.addClassName("dish-tree-grid");
        treeGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        treeGrid.expandRecursively(roots, 99);
        treeGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        return treeGrid;
    }

    private Button getDialogButton(DishSetting dishSetting) {
        return new Button("Редактировать", event -> {
            DishSettingsDialog dishSettingsDialog =
                    new DishSettingsDialog(dishSettingService, dishSetting, stablePeriodService);
            dishSettingsDialog.open();
            dishSettingsDialog.addOpenedChangeListener(e -> {
                if (!e.isOpened()) {
                    reloadTreeGrid();
                }
            });
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> stablePeriodId = event.getRouteParameters().get(STABLEPERIOD_ID).map(Long::parseLong);
        if (stablePeriodId.isPresent()) {
            Optional<StablePeriod> stablePeriodFromBackend = stablePeriodService.get(stablePeriodId.get());
            if (stablePeriodFromBackend.isPresent()) {
                periodDiv = new Div("для периода " + stablePeriodFromBackend.get().getDayType().getName() + " | " +
                        stablePeriodFromBackend.get().getStartTime() +
                        "-" + stablePeriodFromBackend.get().getEndTime());
                periodDiv.addClassNames(LumoUtility.Margin.Horizontal.SMALL);
                layout.add(periodDiv, createMainMenu(), createTreeGrid(stablePeriodFromBackend.get()));
            } else {
                Notification.show(
                        String.format("Запрошенный стабильный период не найден, ID = %s", stablePeriodId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                event.forwardTo(StablePeriodsView.class);
            }
        }
    }


    public Set<Dish> getChildren(Dish dish) {
        return dish.getChildDishes();
    }

}
