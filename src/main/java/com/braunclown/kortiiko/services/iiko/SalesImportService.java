package com.braunclown.kortiiko.services.iiko;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.data.Sale;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.SaleService;
import com.braunclown.kortiiko.services.telegram.KortiikoBot;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SalesImportService {
    private final DishService dishService;
    private final SaleService saleService;
    private final IikoProperties iikoProperties;
    private final KortiikoBot bot;

    public SalesImportService(DishService dishService,
                              SaleService saleService,
                              IikoProperties iikoProperties,
                              KortiikoBot bot) {
        this.dishService = dishService;
        this.saleService = saleService;
        this.iikoProperties = iikoProperties;
        this.bot = bot;
    }

    public List<Sale> importSales(Period period) {
        try {
            IikoConnectionService service = new IikoConnectionService(iikoProperties);
            String token = service.login();
            String json = service.requestSales(token, period.getStartTime(), period.getEndTime());
            service.logout(token);
            List<Sale> sales = parseJson(json, period);
            for (Sale sale: sales) {
                saleService.update(sale);
                // Обновляем остатки
                Optional<Dish> optionalDish = dishService.get(sale.getDish().getId());
                if (optionalDish.isPresent()) {
                    Dish dish = optionalDish.get();
                    dish.setAmount(dish.getAmount() < sale.getAmount() ? 0: dish.getAmount() - sale.getAmount());
                    dishService.update(dish);
                }
            }
            return sales;
        } catch (URISyntaxException | InterruptedException | IOException e) {
            bot.sendAdmins("Произошла ошибка при получении продаж из iiko. " +
                    "Проверьте правильность настройки системы или сообщите разработчику об ошибке");
            throw new RuntimeException(e);
        }
    }

    private List<Sale> parseJson(String json, Period period) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        List<Sale> sales = new ArrayList<>();
        JsonObject response = JsonParser.parseString(json).getAsJsonObject();
        JsonArray data = response.get("data").getAsJsonArray();
        for (JsonElement element: data) {
            try {
                Sale sale = new Sale();
                Dish dish = dishService.getByIikoId(element.getAsJsonObject().get("DishId").getAsString());
                if (dish == null) continue;
                sale.setDish(dish);
                sale.setAmount(element.getAsJsonObject().get("DishAmountInt").getAsDouble());
                sale.setPeriod(period);
                sale.setTime(LocalDateTime.parse(
                        removeMilliseconds(element.getAsJsonObject().get("CloseTime").getAsString()), formatter));
                sales.add(sale);
            } catch (Exception e) {
                bot.sendAdmins("Произошла ошибка при чтении отчёта о продажах. " +
                        "Проверьте правильность настройки системы или сообщите разработчику об ошибке");
                throw new RuntimeException(e);
            }
        }
        return sales;
    }

    private String removeMilliseconds(String datetime) {
        StringBuilder builder = new StringBuilder(datetime);
        builder.delete(builder.lastIndexOf(":") + 3, builder.length());
        return builder.toString();
    }
}
