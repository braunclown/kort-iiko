package com.braunclown.kortiiko.services.telegram;

import com.braunclown.kortiiko.services.UserService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
public class KortiikoBot extends AbilityBot {
    private final ResponseHandler responseHandler;

    public KortiikoBot(Environment environment, UserService userService) {
        super(environment.getProperty("telegram.bot-token"), environment.getProperty("telegram.bot-name"));
        responseHandler = new ResponseHandler(silent, userService);
    }

    public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> responseHandler.replyToStart(ctx.chatId()))
                .build();
    }

    public Ability authenticate() {
        return Ability
                .builder()
                .name("auth")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    try {
                        responseHandler.replyToAuth(ctx.firstArg(), ctx.secondArg(), ctx.chatId());
                    } catch (Exception e) {
                        responseHandler.replyWithError(ctx.chatId());
                    }
                })
                .build();
    }

    public void sendAdmins(String text) {
        responseHandler.sendAdmins(text);
    }

    @Override
    public long creatorId() {
        return 1L;
    }
}
