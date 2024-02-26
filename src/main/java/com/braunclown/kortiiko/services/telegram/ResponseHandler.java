package com.braunclown.kortiiko.services.telegram;

import com.braunclown.kortiiko.data.User;
import com.braunclown.kortiiko.services.UserService;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

public class ResponseHandler {
    private final SilentSender sender;
    private final UserService userService;

    public ResponseHandler(SilentSender sender, UserService userService) {
        this.sender = sender;
        this.userService = userService;
    }

    public void replyToStart(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Введите команду /auth, чтобы получить инструкцию по подключению уведомлений");
        sender.execute(message);
    }

    public void replyToAuth(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Войдите в приложение под учётной записью администратора " +
                "и введите число из следующего сообщения в поле на странице 'Telegram-чат'");
        sender.execute(message);
        message.setText(String.valueOf(chatId));
        sender.execute(message);
    }

    public void replyWithError(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Произошла ошибка");
        sender.execute(message);
    }


    public void sendAdmins(String text) {
        List<User> admins = userService.findAdmins().stream().filter(user -> user.getChatId() != null).toList();
        for (User admin: admins) {
            SendMessage message = new SendMessage();
            message.setChatId(admin.getChatId());
            message.setText(text);
            sender.execute(message);
        }
    }
}
