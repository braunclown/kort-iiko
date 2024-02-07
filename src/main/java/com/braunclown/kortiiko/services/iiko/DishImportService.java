package com.braunclown.kortiiko.services.iiko;

import org.springframework.beans.factory.annotation.Value;

public class DishImportService {
    @Value("${iiko.server.address}")
    private String iikoAddress;

    @Value("${iiko.username}")
    private String iikoUsername;

    @Value("${iiko.password}")
    private String iikoPassword;

    // TODO: Импорт блюд и категорий
}
