package com.github.zeykrus.headachetracker.bot;

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

import java.util.List;

@Component
public class HeadacheTrackerBot implements SpringLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(HeadacheTrackerBot.class);
    private final TelegramClient telegramClient;
    private final String token;
    
    public HeadacheTrackerBot(@Value("${telegram.bot.token}") String token) {
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
                String answer = "default";
                switch(text) {
                    case "/start":
                        answer = "Введена команда /start";
                        break;
                    case "/help":
                        answer = "Команды: /start, /help";
                        break;
                }
                SendMessage message = SendMessage.builder().text(answer).chatId(chatId.toString()).build();
                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    log.error("Ошибка отправки сообщения. chatId: {}; message: {}",chatId,answer);
                }
                // логика обработки команд
            }
        }
    }
}
