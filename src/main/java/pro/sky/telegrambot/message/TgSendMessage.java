package pro.sky.telegrambot.message;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.listener.TelegramBotUpdatesListener;

@Component
public class TgSendMessage {
    private final TelegramBot telegramBot;
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    public TgSendMessage(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()) {
            logger.error("Error during sending message: {}", sendResponse.description());
        }
    }
}
