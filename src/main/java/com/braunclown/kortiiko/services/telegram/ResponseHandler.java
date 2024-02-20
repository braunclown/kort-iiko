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
        message.setText("Введите команду '/auth (логин) (пароль)', чтобы начать получать сообщения для администраторов");
        sender.execute(message);
    }

    public void replyToAuth(String login, String password, long chatId) {
        login = login.substring(1, login.length() - 1);
        password = password.substring(1, password.length() - 1);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        if (userService.validateAdmin(login, password)) {
            User user = userService.findByUsername(login);
            user.setChatId(chatId);
            userService.update(user);
            message.setText("Вам будут приходить сообщения для администраторов");
        } else {
            message.setText("Администратора с такими логином и паролем не существует");
        }
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
