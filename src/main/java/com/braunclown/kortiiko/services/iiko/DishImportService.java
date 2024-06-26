package com.braunclown.kortiiko.services.iiko;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.DishSetting;
import com.braunclown.kortiiko.data.Mode;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.DishSettingService;
import com.braunclown.kortiiko.services.telegram.KortiikoBot;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DishImportService {

    private final DishService dishService;
    private final IikoProperties iikoProperties;
    private final DishSettingService dishSettingService;
    private final KortiikoBot bot;

    public DishImportService(DishService dishService,
                             IikoProperties iikoProperties,
                             DishSettingService dishSettingService,
                             KortiikoBot bot) {
        this.dishService = dishService;
        this.iikoProperties = iikoProperties;
        this.dishSettingService = dishSettingService;
        this.bot = bot;
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
            bot.sendAdmins("Произошла ошибка при импорте блюд. " +
                    "Проверьте правильность настройки системы или сообщите разработчику об ошибке");
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
            bot.sendAdmins("Произошла ошибка при получении id блюда из iiko. " +
                    "Проверьте правильность настройки системы или сообщите разработчику об ошибке");
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

        // Раньше фильтровали, теперь не фильтруем
        // DishGroupFilterService filterService = new DishGroupFilterService();
        // List<GroupPrototype> dishGroups = filterService.filterGroups(groupList);

        // Сохраняем в БД
        addGroupsToDatabase(groupList);

        // Устанавливаем иерархию
        for (GroupPrototype dishGroup: groupList) {
            if (dishGroup.getParentIikoId() != null) {
                dishService.getByIikoId(dishGroup.getIikoId()).ifPresent(registeredGroup -> {
                    dishService.getByIikoId(dishGroup.getParentIikoId()).ifPresent(parent -> {
                        registeredGroup.setParentGroup(parent);
                        dishService.update(registeredGroup);
                    });
                });
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
                Optional<Dish> parent = dishService.getByIikoId(dishObject.get("parent").getAsString());
                parent.ifPresent(dish::setParentGroup);
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

    public void updateDatabase() {
        List<Dish> dishesReserve = dishService.findAll();
        List<DishSetting> settingsReserve = dishSettingService.findAll();
        importDishesAndGroups();
        List<Dish> dishes = dishService.findAll();
        for (Dish dish: dishes) {
            Optional<Dish> reserveDish = dishesReserve.stream().filter(d -> d.getIikoId().equals(dish.getIikoId())).findFirst();
            if (reserveDish.isPresent()) {
                Dish d = reserveDish.get();
                dish.setAmount(d.getAmount());
                dish.setMode(d.getMode());
                dish.setInitialAmount(d.getInitialAmount());
                dish.setMeasure(d.getMeasure());
                dish.setMultiplicity(d.getMultiplicity());
                dishService.update(dish);
                List<DishSetting> dishSettings = settingsReserve.stream()
                        .filter(ds -> ds.getDish().getIikoId().equals(dish.getIikoId())).toList();
                for (DishSetting ds: dishSettings) {
                    DishSetting newSetting = new DishSetting();
                    newSetting.setDish(dish);
                    newSetting.setStablePeriod(ds.getStablePeriod());
                    newSetting.setMinAmount(ds.getMinAmount());
                    newSetting.setMaxAmount(ds.getMaxAmount());
                    dishSettingService.update(newSetting);
                }
            }
        }

    }
}
