package com.braunclown.kortiiko.services.iiko;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import java.io.IOException;
import java.net.URISyntaxException;

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

    public void logout(String token) throws URISyntaxException, IOException, InterruptedException {
        HttpRequestsService
                .sendGetRequest(iikoAddress + "/api/logout?key=" + token);
    }
}
