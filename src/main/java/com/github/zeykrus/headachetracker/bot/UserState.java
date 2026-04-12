package com.github.zeykrus.headachetracker.bot;

public enum UserState {
    IDLE,                       // ничего не ждёт

    AWAITING_DATE_TIME,         // ждёт дату и время
    AWAITING_INTENSITY,         // ждёт интенсивность
    AWAITING_LOCATION,          // ждёт место боли
    AWAITING_SYMPTOMS,          // ждёт симптомы
    AWAITING_TRIGGERS,          // ждёт причины
    AWAITING_COMMENT,           // ждёт комментарий
    EXCEPTION_WITH_SAVE,        // ошибка сохранения данных

    AWAITING_DELETE_ID,         // ждёт ID для удаления
    AWAITING_UPDATE_ID,         // ждёт ID для изменения
    AWAITING_UPDATE_FIELD,      // изменение конкретного поля
    AWAITING_LAST_COUNT         // ждёт количество для /last
}
