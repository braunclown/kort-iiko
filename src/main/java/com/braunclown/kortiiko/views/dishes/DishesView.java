package com.braunclown.kortiiko.views.dishes;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.Mode;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.DishSettingService;
import com.braunclown.kortiiko.services.iiko.DishImportService;
import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Set;

@PageTitle("Блюда")
@Route(value = "dishes", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
@CssImport(value = "./themes/kort-iiko/dish-grid.css", themeFor = "vaadin-grid")
public class DishesView extends Div {
    private final String DISHSETTING_EDIT_ROUTE_TEMPLATE = "dish-settings-table/%s/edit";

    private TreeGrid<Dish> treeGrid;
    private Button importDishesButton;
    private Button refreshTreeGridButton;
    private TextField nameFilterField;
    private Button nameFilterButton;
    private final DishService dishService;
    private final DishSettingService dishSettingService;
    private final DishImportService dishImportService;

    public DishesView(DishService dishService,
                      DishSettingService dishSettingService,
                      DishImportService dishImportService) {
        this.dishService = dishService;
        this.dishSettingService = dishSettingService;
        this.dishImportService = dishImportService;
        setSizeFull();
        addClassNames("dishes-view");

        VerticalLayout layout = new VerticalLayout(
                createMainMenu(), createTreeGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }


    private Component createMainMenu() {
        FlexLayout menuLayout = new FlexLayout(createFilter(), createRefreshTreeGridButton(), createImportDishesButton());
        menuLayout.addClassNames(LumoUtility.Padding.Vertical.XSMALL, LumoUtility.Padding.Horizontal.MEDIUM,
                LumoUtility.Width.FULL, LumoUtility.BoxSizing.BORDER);
        menuLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        menuLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return menuLayout;
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


    private Component createImportDishesButton() {
        importDishesButton = new Button("Импортировать блюда из iiko", new Icon(VaadinIcon.DOWNLOAD));
        importDishesButton.addClickListener(event -> {
            ImportDishesDialog dialog = new ImportDishesDialog(dishImportService);
            dialog.open();
            dialog.addOpenedChangeListener(e -> {
                if (!e.isOpened()) {
                    reloadTreeGrid();
                }
            });
        });
        return importDishesButton;
    }

    private Component createRefreshTreeGridButton() {
        refreshTreeGridButton = new Button("Обновить", new Icon(VaadinIcon.REFRESH));
        refreshTreeGridButton.addClickListener(event -> reloadTreeGrid());
        return refreshTreeGridButton;
    }

    private Component createTreeGrid() {
        treeGrid = new TreeGrid<>();
        List<Dish> roots = dishService.findRoots();
        treeGrid.setItems(roots, this::getChildren);
        treeGrid.addHierarchyColumn(Dish::getName).setHeader("Название")
                .setAutoWidth(true).setFlexGrow(1).setResizable(true);
        treeGrid.addColumn(Dish::getInitialAmount).setHeader("Остатки по умолчанию").setSortable(true);
        treeGrid.addColumn(Dish::getAmount).setHeader("Остатки").setSortable(true);
        treeGrid.addColumn(Dish::getMeasure).setHeader("Ед. измерения").setSortable(true);
        treeGrid.addColumn(Dish::getMultiplicity).setHeader("Кратность").setSortable(true);
        treeGrid.addComponentColumn(dish -> {
            Button button = new Button(dish.getMode() == Mode.MAX ? "До макс." : "Продажи", new Icon(VaadinIcon.EDIT));
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            button.addClickListener(event ->
                    UI.getCurrent().navigate(String.format(DISHSETTING_EDIT_ROUTE_TEMPLATE, dish.getId())));
            return button;
        }).setHeader("Режим пополнения").setSortable(true);
        treeGrid.addComponentColumn(dish -> {
            Button button = new Button("Редактировать", event -> {
                EditDishDialog editDishDialog =
                        new EditDishDialog(dish, dishService, dishSettingService, dishImportService);
                editDishDialog.open();
                editDishDialog.addOpenedChangeListener(e -> {
                    if (!e.isOpened()) {
                        reloadTreeGrid();
                    }
                });
            });
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            button.setIcon(new Icon(VaadinIcon.EDIT));
            return button;
        }).setHeader(createAddDishButton());

        treeGrid.setClassNameGenerator(dish -> dish.getGroup() ? "group-style" : null);
        treeGrid.addClassName("dish-tree-grid");
        treeGrid.addThemeVariants(GridVariant.LUMO_COMPACT);
        treeGrid.expandRecursively(roots, 99);
        treeGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        return treeGrid;
    }

    private Button createAddDishButton() {
        Button button = new Button("Добавить", new Icon(VaadinIcon.PLUS), event -> {
            AddDishDialog dialog = new AddDishDialog(dishService, dishImportService);
            dialog.open();
            dialog.addOpenedChangeListener(e -> {
                if (!e.isOpened()) {
                    reloadTreeGrid();
                }
            });
        });
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        return button;
    }

    private void filterTreeGrid() {
        TreeDataProvider<Dish> dataProvider = new TreeDataProvider<>(treeGrid.getTreeData());
        dataProvider.setFilter(dish -> dish.getName().toLowerCase().contains(nameFilterField.getValue().toLowerCase()) ||
                parentNameContainsString(dish, nameFilterField.getValue()));

        treeGrid.setDataProvider(dataProvider);
        treeGrid.expandRecursively(treeGrid.getTreeData().getRootItems(), 99);
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

    public Set<Dish> getChildren(Dish dish) {
        return dish.getChildDishes();
    }

}
