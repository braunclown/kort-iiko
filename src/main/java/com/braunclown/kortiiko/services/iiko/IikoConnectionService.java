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

    public IikoConnectionService(IikoProperties iikoProperties) {
        this.iikoAddress = iikoProperties.getServerAddress();
        this.iikoUsername = iikoProperties.getUsername();
        this.iikoPassword = iikoProperties.getPassword();
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
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString("""
                {
                	"reportType": "SALES",
                	"buildSummary": "false",
                	"groupByRowFields": [
                		"DishId",
                		"DishName",
                		"CloseTime"
                	],
                	"aggregateFields": [
                		"DishAmountInt"
                		],
                	"filters": {
                		"CloseTime": {
                			"filterType": "DateRange",
                			"periodType": "CUSTOM",
                			"from": "%s:00.000",
                			"to": "%s:00.000"
                		},
                		"DeletedWithWriteoff": {
                            "filterType": "IncludeValues",
                            "values": ["NOT_DELETED"]
                        },
                		"OpenDate.Typed": {
                			"filterType": "DateRange",
                			"periodType": "CUSTOM",
                			"from": "2024-01-01T00:00:00.000",
                			"to": "2048-02-20T00:00:00.000"
                		}
                	}
                }""".formatted(startTime, endTime));
        return HttpRequestsService.sendPostRequest(iikoAddress + "/api/v2/reports/olap?key=" + token, body)
                .body();
    }

    public void logout(String token) throws URISyntaxException, IOException, InterruptedException {
        HttpRequestsService
                .sendGetRequest(iikoAddress + "/api/logout?key=" + token);
    }
}
