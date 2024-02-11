package com.braunclown.kortiiko.services.iiko;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.Mode;
import com.braunclown.kortiiko.services.DishService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class DishImportService {

    private final DishService dishService;
    private final IikoProperties iikoProperties;

    public DishImportService(DishService dishService, IikoProperties iikoProperties) {
        this.dishService = dishService;
        this.iikoProperties = iikoProperties;
    }

    public void importDishesAndGroups() {
        try {
            IikoConnectionService service = new IikoConnectionService(iikoProperties);
            String token = service.login();
            String groupsJson = service.requestGroups(token);
            String dishesJson = service.requestDishes(token);
            service.logout(token);
            // Удаляем старую номенклатуру
            dishService.deleteAll();
            // Добавляем новую
            parseGroups(groupsJson);
            parseDishes(dishesJson);
        } catch (URISyntaxException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getIikoId(String name) {
        try {
            IikoConnectionService service = new IikoConnectionService(iikoProperties);
            String token = service.login();
            String dishJson = service.requestDishes(token);
            String groupJson = service.requestGroups(token);
            service.logout(token);
            return getIikoIdByName(dishJson, groupJson, name);
        } catch (URISyntaxException | InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseGroups(String json) {
        List<GroupPrototype> groupList = new ArrayList<>();
        JsonArray groups = JsonParser.parseString(json).getAsJsonArray();
        // Парсим все группы из json
        for (JsonElement groupElement: groups) {
            JsonObject group = groupElement.getAsJsonObject();
            GroupPrototype groupPrototype = new GroupPrototype();
            groupPrototype.setName(group.get("name").getAsString());
            groupPrototype.setIikoId(group.get("id").getAsString());
            groupPrototype.setParentIikoId(group.get("parent").isJsonNull() ? null : group.get("parent").getAsString());
            groupList.add(groupPrototype);
        }

        // Фильтруем
        DishGroupFilterService filterService = new DishGroupFilterService();
        List<GroupPrototype> dishGroups = filterService.filterGroups(groupList);

        // Сохраняем в БД
        addGroupsToDatabase(dishGroups);

        // Устанавливаем иерархию
        for (GroupPrototype dishGroup: dishGroups) {
            if (dishGroup.getParentIikoId() != null) {
                Dish registeredGroup = dishService.getByIikoId(dishGroup.getIikoId());
                registeredGroup.setParentGroup(dishService.getByIikoId(dishGroup.getParentIikoId()));
                dishService.update(registeredGroup);
            }
        }
    }

    private void addGroupsToDatabase(List<GroupPrototype> dishGroups) {
        for (GroupPrototype dishGroup: dishGroups) {
            Dish dish = new Dish();
            dish.setName(dishGroup.getName());
            dish.setAmount(0d);
            dish.setIikoId(dishGroup.getIikoId());
            dish.setMultiplicity(1d);
            dish.setInitialAmount(0d);
            dish.setMode(Mode.MAX);
            dish.setMeasure("ед.");
            dish.setGroup(true);
            dishService.update(dish);
        }
    }

    private void parseDishes(String json) {
        JsonArray dishes = JsonParser.parseString(json).getAsJsonArray();
        for (JsonElement dishElement: dishes) {
            JsonObject dishObject = dishElement.getAsJsonObject();
            Dish dish = new Dish();
            dish.setName(dishObject.get("name").getAsString());
            dish.setAmount(0d);
            dish.setIikoId(dishObject.get("id").getAsString());
            dish.setMultiplicity(1d);
            dish.setInitialAmount(0d);
            dish.setMode(Mode.MAX);
            dish.setMeasure("ед.");
            dish.setGroup(false);
            if (!dishObject.get("parent").isJsonNull()) {
                dish.setParentGroup(dishService.getByIikoId(dishObject.get("parent").getAsString()));
            }
            dishService.update(dish);
        }
    }

    private String getIikoIdByName(String dishJson, String groupJson, String name) {
        for (JsonElement dishElement: JsonParser.parseString(dishJson).getAsJsonArray()) {
            JsonObject dishObject = dishElement.getAsJsonObject();
            if (dishObject.get("name").getAsString().equals(name)) {
                return dishObject.get("id").getAsString();
            }
        }
        for (JsonElement groupElement: JsonParser.parseString(groupJson).getAsJsonArray()) {
            JsonObject groupObject = groupElement.getAsJsonObject();
            if (groupObject.get("name").getAsString().equals(name)) {
                return groupObject.get("id").getAsString();
            }
        }
        return "Не найдено";
    }
}
