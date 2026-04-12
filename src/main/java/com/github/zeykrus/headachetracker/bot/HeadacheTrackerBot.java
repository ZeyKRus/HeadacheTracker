package com.github.zeykrus.headachetracker.bot;

import com.github.zeykrus.headachetracker.dto.EpisodeRequestDTO;
import com.github.zeykrus.headachetracker.exception.NotValidIntensityException;
import com.github.zeykrus.headachetracker.service.EpisodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HeadacheTrackerBot implements SpringLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(HeadacheTrackerBot.class);
    private final Map<Long, User> users;
    private final TelegramClient telegramClient;
    private final String token;
    private final EpisodeService service;
    
    @Value("${app.local.date.time.pattern}")
    private String pattern;
    
    @Value("${app.default.value.intensity}")
    private int intensityNotSpecified = 1; //int значение для defaultValue intensity
    
    @Value("${app.default.value.intensity.min}")
    private int intensityMin = 1;
    
    @Value("${app.default.value.intensity.max}")
    private int intensityMax = 10;
    
    public HeadacheTrackerBot(@Value("${telegram.bot.token}") String token, EpisodeService service) {
        this.service = service;
        this.users = new ConcurrentHashMap<>();
        this.token = token;
        this.telegramClient = new OkHttpTelegramClient(token);
    }
    
    @Override
    public String getBotToken() {
        return token;
    }
    
    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this::consume;
    }
    
    private void consume(List<Update> updates) {
        for (Update update : updates) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                Long chatId = update.getMessage().getChatId();
                String answer;
                SendMessage message;
                
                User user;
                if (users.containsKey(chatId)) {
                    user = users.get(chatId);
                } else {
                    user = new User();
                    users.put(chatId, user);
                }
                
                switch (user.getState()) {
                    case IDLE:
                        switch (text) {
                            case "/start":
                                answer = "Привет. Я бот для трекера головной боли.";
                                message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
                                try {
                                    telegramClient.execute(message);
                                } catch (TelegramApiException e) {
                                    log.error("Ошибка отправки сообщения на команду /start. chatId: {}; message: {}", chatId, answer);
                                }
                                break;
                            case "/help":
                                answer = "Команды: /start, /help";
                                message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
                                try {
                                    telegramClient.execute(message);
                                } catch (TelegramApiException e) {
                                    log.error("Ошибка отправки сообщения на команду /help. chatId: {}; message: {}", chatId, answer);
                                }
                                break;
                            case "/add":
                                createNewRecord(update);
                                break;
                        }
                        break;
                    
                    case AWAITING_DATE_TIME:
                    case AWAITING_INTENSITY:
                    case AWAITING_LOCATION:
                    case AWAITING_SYMPTOMS:
                    case AWAITING_TRIGGERS:
                    case AWAITING_COMMENT:
                        createNewRecord(update);
                        break;
                }
            }
        }
    }
    
    private void createNewRecord(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        User user = users.get(chatId);
        SendMessage message = null;
        String answer;
        EpisodeRequestDTO requestDTO;
        
        if (text.equals("/stop")) {
            answer = "Прекращение создания новой записи";
            message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
            user.reset();
            try {
                telegramClient.execute(message);
                return;
            } catch (TelegramApiException e) {
                log.error("Ошибка отправки сообщения при прекращении создания записи. userState: {}; chatId: {}", user.getState(), chatId);
                return;
            }
        }
        
        switch (user.getState()) {
            case IDLE:
                answer = "Начало добавления новой записи\n" +
                        "/stop - для прекращения ввода записи\n" +
                        "Введите дату и время в формате " + pattern + "\n";
                message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
                break;
            case AWAITING_DATE_TIME:
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(text, DateTimeFormatter.ofPattern(pattern));
                    user.setDateTime(dateTime);
                    answer = "Укажите уровень интенсивности (от 1 до 10)\n";
                } catch (DateTimeParseException e) {
                    LocalDateTime dateTime = LocalDateTime.now();
                    user.setDateTime(dateTime);
                    answer = """
                            Ошибка записи даты и времени. Применяется текущая дата и время
                            Укажите уровень интенсивности (от 1 до 10)
                            """;
                }
                message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
                break;
            case AWAITING_INTENSITY:
                try {
                    int intensity = Integer.parseInt(text);
                    if (intensity < intensityMin || intensity > intensityMax)
                        throw new NotValidIntensityException("Интенсивность вне границ");
                    user.setIntensity(intensity);
                    answer = "Укажите место боли\n";
                } catch (NumberFormatException | NotValidIntensityException e) {
                    Integer intensity = intensityNotSpecified;
                    user.setIntensity(intensity);
                    answer = "Ошибка записи интенсивности. Применяется значение по-умолчанию: " + intensityNotSpecified +
                            "\nУкажите место боли\n";
                }
                message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
                break;
            case AWAITING_LOCATION:
                user.setLocation(text);
                answer = "Укажите симптомы\n";
                message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
                break;
            case AWAITING_SYMPTOMS:
                user.setSymptoms(text);
                answer = "Укажите возможные причины боли\n";
                message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
                break;
            case AWAITING_TRIGGERS:
                user.setTriggers(text);
                answer = "Укажите комментарий\n";
                message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
                break;
            case AWAITING_COMMENT:
                user.setComment(text);
                
                requestDTO = new EpisodeRequestDTO(user.getDateTime(), user.getIntensity(), user.getLocation(),
                        user.getSymptoms(), user.getTriggers(), user.getComment(), chatId);
                if (service.create(requestDTO).isEmpty()) {
                    answer = "Ошибка сохранения записи. Попробуйте позднее\n" +
                            "Любое сообщение приведет к попытке повторной отправки";
                    user.setState(UserState.EXCEPTION_WITH_SAVE);
                } else {
                    answer = "Запись успешно сохранена\n";
                    user.reset();
                    user.setState(UserState.AWAITING_COMMENT);
                }
                
                message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
                break;
            case EXCEPTION_WITH_SAVE:
                requestDTO = new EpisodeRequestDTO(user.getDateTime(), user.getIntensity(), user.getLocation(),
                        user.getSymptoms(), user.getTriggers(), user.getComment(), chatId);
                if (service.create(requestDTO).isEmpty()) {
                    answer = "Ошибка сохранения записи. Попробуйте позднее\n" +
                            "Любое сообщение приведет к попытке повторной отправки";
                } else {
                    answer = "Запись успешно сохранена\n";
                    user.reset();
                    user.setState(UserState.AWAITING_COMMENT);
                }
                
                message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
                break;
        }
        try {
            telegramClient.execute(message);
            user.nextAddState();
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения. userState: {}; chatId: {}", user.getState(), chatId);
        }
    }
}
