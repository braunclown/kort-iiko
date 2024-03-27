package com.braunclown.kortiiko.views.amount;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

@PageTitle("Остатки блюд")
@Route(value = "amount", layout = MainLayout.class)
@PermitAll
public class AmountView extends Div {

    private final DishService dishService;

    private Grid<Dish> grid;

    public AmountView(DishService dishService) {
        this.dishService = dishService;

        add(createLayout());
        setSizeFull();
        addClassNames("amount-view");
    }

    private Component createLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.addClassNames(LumoUtility.Height.FULL, LumoUtility.Gap.XSMALL, LumoUtility.Padding.SMALL);

        Button refreshGrid = new Button("Обновить", VaadinIcon.REFRESH.create());
        refreshGrid.addClickListener(event -> refreshGrid());

        grid = new Grid<>(Dish.class, false);
        Grid.Column<Dish> nameColumn = grid.addColumn(Dish::getName).setAutoWidth(true).setSortable(true).setHeader("Блюдо").setResizable(true);
        grid.addColumn(Dish::getAmount).setAutoWidth(true).setSortable(true).setHeader("Текущие остатки");
        grid.addColumn(Dish::getInitialAmount).setAutoWidth(true).setSortable(true).setHeader("Остатки по умолчанию");
        grid.addComponentColumn(this::createEditButton).setAutoWidth(true).setHeader("Редактировать");
        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);
        refreshGrid();
        grid.sort(new GridSortOrderBuilder<Dish>().thenAsc(nameColumn).build());
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.addItemDoubleClickListener(event -> openEditAmountDialog(event.getItem()));
        layout.add(new HorizontalLayout(refreshGrid, createUpdateAmountButton()), grid);
        return layout;
    }

    private Button createUpdateAmountButton() {
        Button updateDishAmount = new Button("Применить настройки по умолчанию", VaadinIcon.MAGIC.create());
        updateDishAmount.setTooltipText("Текущие остатки каждого блюда станут равными соответствующим остаткам по умолчанию");
        updateDishAmount.addClickListener(event -> {
            ConfirmDialog dialog = new ConfirmDialog("Вы уверены?",
                    "Текущие остатки каждого блюда станут равными соответствующим остаткам по умолчанию. Действие невозможно будет отменить",
                    "Да", e -> {
                dishService.updateAmounts();
                Notification n = Notification.show("Остатки обновлены");
                n.setPosition(Notification.Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshGrid();
            },
                    "Отмена", e -> {});
            dialog.open();
        });
        updateDishAmount.addClassNames(LumoUtility.Margin.Top.AUTO);
        return updateDishAmount;
    }

    private Component createEditButton(Dish dish) {
        Button editButton = new Button("Редактировать", VaadinIcon.EDIT.create());
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        editButton.addClickListener(event -> openEditAmountDialog(dish));
        return editButton;
    }

    private void openEditAmountDialog(Dish dish) {
        EditAmountDialog dialog = new EditAmountDialog(dishService, dish);
        dialog.open();
        dialog.addOpenedChangeListener(e -> {
            if (!e.isOpened()) {
                refreshGrid();
            }
        });
    }

    private void refreshGrid() {
        grid.setItems(dishService.findDishes());
    }
}
