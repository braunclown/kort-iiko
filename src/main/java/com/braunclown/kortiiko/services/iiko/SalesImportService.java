package com.braunclown.kortiiko.services.iiko;

import com.braunclown.kortiiko.data.Dish;
import com.braunclown.kortiiko.data.Period;
import com.braunclown.kortiiko.data.Sale;
import com.braunclown.kortiiko.services.DishService;
import com.braunclown.kortiiko.services.SaleService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesImportService {
    private final DishService dishService;
    private final SaleService saleService;

    public SalesImportService(DishService dishService, SaleService saleService) {
        this.dishService = dishService;
        this.saleService = saleService;
    }

    private List<Sale> parseJson(String json, Period period) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        List<Sale> sales = new ArrayList<>();
        JsonObject response = JsonParser.parseString(json).getAsJsonObject();
        JsonArray data = response.get("data").getAsJsonArray();
        for (JsonElement element: data) {
            Sale sale = new Sale();
            Dish dish = dishService.getByIikoId(element.getAsJsonObject().get("DishId").getAsString());
            // TODO: если dish нет в БД? -> игнор
            sale.setDish(dish);
            sale.setAmount(element.getAsJsonObject().get("DishAmountInt").getAsDouble());
            sale.setPeriod(period);
            sale.setTime(LocalDateTime.parse(element.getAsJsonObject().get("CloseTime").getAsString(), formatter));
            sales.add(sale);
        }
        return sales;
    }
}
