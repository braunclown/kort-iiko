package com.braunclown.kortiiko.services.iiko;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;

public class IikoConnectionService {

    private final String iikoAddress;
    private final String iikoUsername;
    private final String iikoPassword;
    private final String department;

    public IikoConnectionService(IikoProperties iikoProperties) {
        this.iikoAddress = iikoProperties.getServerAddress();
        this.iikoUsername = iikoProperties.getUsername();
        this.iikoPassword = iikoProperties.getPassword();
        this.department = iikoProperties.getDepartment();
    }

    /**
     *
     * @return Токен авторизации. Необходим для обращения к серверу iiko
     */
    public String login() throws URISyntaxException, IOException, InterruptedException {
        String pass = Hashing.sha1().hashString(iikoPassword, Charsets.UTF_8).toString();
        return HttpRequestsService
                .sendGetRequest(iikoAddress + "/api/auth?login=" + iikoUsername + "&pass=" + pass)
                .body();
    }

    public String requestGroups(String token) throws URISyntaxException, IOException, InterruptedException {
        return HttpRequestsService.sendGetRequest(iikoAddress
                + "/api/v2/entities/products/group/list?includeDeleted=false&key=" + token)
                .body();
    }

    public String requestDishes(String token) throws URISyntaxException, IOException, InterruptedException {
        return HttpRequestsService.sendGetRequest(iikoAddress
        + "/api/v2/entities/products/list?includeDeleted=false&types=DISH&key=" + token)
                .body();
    }

    public String requestSales(String token, LocalDateTime startTime, LocalDateTime endTime)
            throws URISyntaxException, IOException, InterruptedException {
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString("{\n" +
                "\t\"reportType\": \"SALES\",\n" +
                "\t\"buildSummary\": \"false\",\n" +
                "\t\"groupByRowFields\": [\n" +
                "\t\t\"DishId\",\n" +
                "\t\t\"DishName\",\n" +
                "\t\t\"CloseTime\"\n" +
                "\t],\n" +
                "\t\"aggregateFields\": [\n" +
                "\t\t\"DishAmountInt\"\n" +
                "\t\t]," +
                "\t\"filters\": {\n" +
                "\t\t\"CloseTime\": {\n" +
                "\t\t\t\"filterType\": \"DateRange\",\n" +
                "\t\t\t\"periodType\": \"CUSTOM\",\n" +
                "\t\t\t\"from\": \"" + startTime + ":00.000\",\n" +
                "\t\t\t\"to\": \"" + endTime + ":00.000\"\n" +
                "\t\t},\n" +
                "\t\t\"OpenDate.Typed\": {\n" +
                "\t\t\t\"filterType\": \"DateRange\",\n" +
                "\t\t\t\"periodType\": \"CUSTOM\",\n" +
                "\t\t\t\"from\": \"2024-01-01T00:00:00.000\",\n" +
                "\t\t\t\"to\": \"2048-02-20T00:00:00.000\"\n" +
                "\t\t},\n" +
                "\t\t\"Department\": {\n" +
                "\t\t\t\"filterType\": \"IncludeValues\",\n" +
                "\t\t\t\"values\": [" + department + "]\n" +
                "\t\t}" +
                "\t}\n" +
                "}");
        return HttpRequestsService.sendPostRequest(iikoAddress + "/api/v2/reports/olap?key=" + token, body)
                .body();
    }

    public void logout(String token) throws URISyntaxException, IOException, InterruptedException {
        HttpRequestsService
                .sendGetRequest(iikoAddress + "/api/logout?key=" + token);
    }
}
