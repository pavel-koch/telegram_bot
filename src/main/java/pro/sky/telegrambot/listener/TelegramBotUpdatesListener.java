package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.message.TgSendMessage;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final Pattern pattern = Pattern.compile(
            "(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{2})\\s+([А-я\\d\\s.,!&:]+)");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
            "dd.MM.yyyy HH:mm");

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;

    private final TgSendMessage tgSendMessage;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService, TgSendMessage tgSendMessage) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
        this.tgSendMessage = tgSendMessage;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.stream()
                    .filter(update -> update.message() != null)
                    .forEach(update -> {
                        //перенести try сюда
                        //вынести весь метод в отдельный приватный метод
                        logger.info("Handles update: {}", update);

                        Message message = update.message();
                        Long chatId = message.chat().id();
                        String text = message.text();


                        if ("/start".equals(text)) {
                            tgSendMessage.sendMessage(chatId, "Привет! Я могу планировать твои задачи! " +
                                    "Отправь мне ее в формате: ДД.ММ.ГГГГ 00:00 Сдать домашку");
                        } else if (text != null) {
                            Matcher matcher = pattern.matcher(text);
                            if (matcher.find()) {
                                LocalDateTime dateTime = parse(matcher.group(1));
                                if (Objects.isNull(dateTime)) {
                                    tgSendMessage.sendMessage(chatId, "Неккоректный формат даты и/или времени!");
                                } else {
                                    String txt = matcher.group(2);
                                    NotificationTask notificationTask = new NotificationTask();
                                    notificationTask.setChatId(chatId);
                                    notificationTask.setMessage(txt);
                                    notificationTask.setNotificationDateTime(dateTime);
                                    notificationTaskService.save(notificationTask);
                                    tgSendMessage.sendMessage(chatId, "Задача успешно добавлена!");
                                }
                            } else {
                                tgSendMessage.sendMessage(chatId, "Неккоректный формат сособщения!");
                            }
                        }

                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    @Nullable
    private LocalDateTime parse(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, dateTimeFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }


}