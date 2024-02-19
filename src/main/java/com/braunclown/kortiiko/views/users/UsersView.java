package com.braunclown.kortiiko.views.users;

import com.braunclown.kortiiko.data.Role;
import com.braunclown.kortiiko.data.User;
import com.braunclown.kortiiko.services.UserService;
import com.braunclown.kortiiko.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Users")
@Route(value = "users", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Uses(Icon.class)
public class UsersView extends Div {

    private Grid<User> grid;

    private Filters filters;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UsersView(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        setSizeFull();
        addClassNames("users-view");

        filters = new Filters(() -> refreshGrid());
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Фильтры");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div implements Specification<User> {
        private final TextField username = new TextField("Логин");
        private final TextField realName = new TextField("Имя");
        private final TextField phone = new TextField("Телефон");
        private final TextField email = new TextField("Почта");

        public Filters(Runnable onSearch) {

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);

            realName.setPlaceholder("Настоящее имя");

            // Кнопки действий
            Button resetBtn = new Button("Сбросить фильтры", new Icon(VaadinIcon.CLOSE));
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                username.clear();
                realName.clear();
                phone.clear();
                email.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Искать / Обновить", new Icon(VaadinIcon.REFRESH));
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(username, realName, phone, email, actions);
        }

        @Override
        public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (!username.isEmpty()) {
                String lowerCaseFilter = username.getValue().toLowerCase();
                Predicate usernameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("username")),
                        lowerCaseFilter + "%");
                predicates.add(criteriaBuilder.or(usernameMatch));
            }
            if (!realName.isEmpty()) {
                String lowerCaseFilter = realName.getValue().toLowerCase();
                Predicate realNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("realName")),
                        lowerCaseFilter + "%");
                predicates.add(criteriaBuilder.or(realNameMatch));
            }
            if (!phone.isEmpty()) {
                String databaseColumn = "phone";
                String ignore = "- ()";

                String lowerCaseFilter = ignoreCharacters(ignore, phone.getValue().toLowerCase());
                Predicate phoneMatch = criteriaBuilder.like(
                        ignoreCharacters(ignore, criteriaBuilder, criteriaBuilder.lower(root.get(databaseColumn))),
                        "%" + lowerCaseFilter + "%");
                predicates.add(phoneMatch);

            }
            if (!email.isEmpty()) {
                String lowerCaseFilter = email.getValue().toLowerCase();
                Predicate realNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                        lowerCaseFilter + "%");
                predicates.add(criteriaBuilder.or(realNameMatch));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }

        private String ignoreCharacters(String characters, String in) {
            String result = in;
            for (int i = 0; i < characters.length(); i++) {
                result = result.replace("" + characters.charAt(i), "");
            }
            return result;
        }

        private Expression<String> ignoreCharacters(String characters, CriteriaBuilder criteriaBuilder,
                Expression<String> inExpression) {
            Expression<String> expression = inExpression;
            for (int i = 0; i < characters.length(); i++) {
                expression = criteriaBuilder.function("replace", String.class, expression,
                        criteriaBuilder.literal(characters.charAt(i)), criteriaBuilder.literal(""));
            }
            return expression;
        }

    }

    private Component createGrid() {
        grid = new Grid<>(User.class, false);
        grid.addColumn("username").setAutoWidth(true).setHeader("Логин");
        grid.addColumn("realName").setAutoWidth(true).setHeader("Имя");
        grid.addColumn("email").setAutoWidth(true).setHeader("Почта");
        grid.addColumn("phone").setAutoWidth(true).setHeader("Телефон");
        grid.addComponentColumn(user -> new Paragraph(user.getActive() ? "Да" : "Нет"))
                .setAutoWidth(true).setHeader("Активен");
        grid.addComponentColumn(user -> {
            String content = "";
            if (user.getRoles().contains(Role.ADMIN)) {
                content += "Админ; ";
            }
            if (user.getRoles().contains(Role.USER)) {
                content += "Пользователь; ";
            }
            return new Span(content);
        }).setAutoWidth(true).setHeader("Роли");
        grid.addComponentColumn(user -> {
            Button editButton = new Button("Редактировать", new Icon(VaadinIcon.EDIT));
            editButton.addClickListener(event -> {
                EditUserDialog dialog = new EditUserDialog(user, userService, passwordEncoder);
                dialog.open();
                dialog.addOpenedChangeListener(e -> {
                    if (!e.isOpened()) {
                        refreshGrid();
                    }
                });
            });
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            return editButton;
        }).setHeader(createAddUserButton());

        grid.setItems(query -> userService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        return grid;
    }

    private Component createAddUserButton() {
        Button button = new Button("Добавить", new Icon(VaadinIcon.PLUS), event -> {
            AddUserDialog dialog = new AddUserDialog(userService, passwordEncoder);
            dialog.open();
            dialog.addOpenedChangeListener(e -> {
                if (!e.isOpened()) {
                    refreshGrid();
                }
            });
        });
        button.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        return button;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}
