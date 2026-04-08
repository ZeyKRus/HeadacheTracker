package com.github.zeykrus.headachetracker.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Класс-обертка для {@link Scanner}
 * <p>Используется для чтения с клавиатуры текста, чисел и даты, когда нужны значения по-умолчанию при некорректном вводе данных</p>
 * @author Lev Vylegzhanin
 */
public final class ConsoleInput {
    private static final Logger log = LoggerFactory.getLogger(ConsoleInput.class);
    private static final Scanner scanner = new Scanner(System.in);

    private ConsoleInput() {}

    /**
     * Метод для чтения строк с клавиатуры
     * @param defaultValue стандартное значение
     * @return возвращает строку, введенную с клавиатуры, либо defaultValue, если строки не существует или она пуста
     * @since 1.0
     */
    public static String readLine(String defaultValue) {
        String line = scanner.nextLine().trim();
        log.trace("Чтение строки: {}, defaultValue = {}",line,defaultValue);
        return line.isEmpty() ? defaultValue : line;
    }

    /**
     * Метод для чтения чисел с клавиатуры
     * @param defaultValue стандартное значение
     * @return возращает полученное число, либо defaultValue, если строки не существует или не прошел {@link Integer#parseInt}
     * @since 1.0
     */
    public static int readInt(int defaultValue) {
        String line = scanner.nextLine().trim();
        log.trace("Чтение числа: {}, defaultValue = {}",line,defaultValue);
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            log.warn("Ошибка преобразования строки в число. Получена строка: {}",line);
            return defaultValue;
        }
    }

    /**
     * Метод для чтения чисел с клавиатуры в указанных границах
     * @param defaultValue стандартное значение
     * @param min нижняя граница (включительно)
     * @param max верхняя граница (включительно)
     * @return возвращает полученное число, либо defaultValue, если строки не существует, не прошел {@link Integer#parseInt},
     * полученное число не вошло в заданные границы
     * @since 1.0
     */
    public static int readInt(int min, int max, int defaultValue) {
        String line = scanner.nextLine().trim();
        log.trace("Чтение числа: {}, defaultValue = {}, min = {}, max = {}",line,defaultValue,min,max);
        try {
            int current = Integer.parseInt(line);
            if (current >= min && current <= max) return current;
            else {
                return defaultValue;
            }
        } catch (NumberFormatException e) {
            log.warn("Ошибка преобразования строки в число (MIN/MAX). Получена строка: {}",line);
            return defaultValue;
        }
    }

    /**
     * Метод для чтения даты и времени, введенные с клавиатуры по шаблону
     * @param defaultValue стандартное значение
     * @param pattern шаблон для чтения даты и времени
     * @return возвращает полученную дату и время в виде объекта {@link LocalDateTime}, либо defaultValue, если не прошел метод {@link DateTimeFormatter#ofPattern}
     * @since 1.0
     */
    public static LocalDateTime readDateTime(String pattern, LocalDateTime defaultValue) {
        String line = scanner.nextLine().trim();
        log.trace("Чтение даты: {}, defaultValue = {}, pattern = {}",line,defaultValue,pattern);
        try {
            return LocalDateTime.parse(line, DateTimeFormatter.ofPattern(pattern));
        } catch (DateTimeParseException e) {
            log.warn("Ошибка преобразования строки в дату. Получена строка: {}, pattern = {}",line, pattern);
            return defaultValue;
        }
    }
    
    /**
     * Получить доступ к статичному сканеру {@link Scanner}
     * @return возвращает {@link Scanner}, который используется классом
     */
    public static Scanner getScanner() {
        return scanner;
    }
}
