package com.github.zeykrus.headachetracker;

import com.github.zeykrus.headachetracker.entity.HeadacheEpisodeEntity;
import com.github.zeykrus.headachetracker.repository.HeadacheEpisodeRepository;
import com.github.zeykrus.headachetracker.util.ConsoleInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Application {
    public static final Logger log = LoggerFactory.getLogger(Application.class);
    public static final String STRING_NOT_SPECIFIED = "Не указано"; //String значение для defaultValue
    public static final int INTENSITY_NOT_SPECIFIED = 1; //int значение для defaultValue intensity
    public static final int INTENSITY_MIN = 1;
    public static final int INTENSITY_MAX = 10;
    public static final int DEFAULT_LIMIT = 1;
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public CommandLineRunner consoleRunner(HeadacheEpisodeRepository repo) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            while (!exit) {
                System.out.println("\n=== Дневник головной боли ===");
                System.out.println("1. Добавить запись");
                System.out.println("2. Показать последние N записей");
                System.out.println("3. Удалить запись");
                System.out.println("0. Выход");
                System.out.print("Выберите действие: ");
                
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        addEpisode(repo);
                        break;
                    case "2":
                        showLastEpisodes(repo);
                        break;
                    case "3":
                        deleteEpisode(repo);
                        break;
                    case "0":
                        exit = true;
                        break;
                    default:
                        System.out.println("Неверный выбор");
                }
            }
        };
    }
    
    public static void addEpisode(HeadacheEpisodeRepository repo) {
        log.info("Начало операции добавления записи в БД");
        String pattern = "yyyy-MM-dd HH:mm";
        System.out.print("Введите дату и время в формате: ");
        System.out.println(pattern);
        LocalDateTime dateTime = ConsoleInput.readDateTime(pattern, LocalDateTime.now());
        
        System.out.println("Введите интенсивность боли (от 1 до 10)");
        int intensity = ConsoleInput.readInt(INTENSITY_MIN, INTENSITY_MAX, INTENSITY_NOT_SPECIFIED);
        
        System.out.println("Укажите место, где чувствуете боль");
        String location = ConsoleInput.readLine(STRING_NOT_SPECIFIED);
        
        System.out.println("Укажите симптомы");
        String symptoms = ConsoleInput.readLine(STRING_NOT_SPECIFIED);
        
        System.out.println("Укажите возможные причины боли");
        String triggers = ConsoleInput.readLine(STRING_NOT_SPECIFIED);
        
        System.out.println("Добавьте комментарий");
        String comment = ConsoleInput.readLine(STRING_NOT_SPECIFIED);
        
        HeadacheEpisodeEntity episode = new HeadacheEpisodeEntity();
        episode.setDateTime(dateTime);
        episode.setIntensity(intensity);
        episode.setLocation(location);
        episode.setSymptoms(symptoms);
        episode.setTriggers(triggers);
        episode.setComment(comment);
        repo.save(episode);
        System.out.println("Запись успешно добавлена");
        log.info("Добавлена новая запись в БД: {}", episode);
    }
    
    public static void showLastEpisodes(HeadacheEpisodeRepository repo) {
        log.info("Начало операции поиска записей в БД");
        System.out.println("Укажите количество записей для просмотра");
        int limit = ConsoleInput.readInt(DEFAULT_LIMIT);
        
        Pageable pageable = PageRequest.of(0, limit); //Формат получения страниц: 0 - номер страницы, limit - объектов на страницу
        Page<HeadacheEpisodeEntity> page = repo.findAllByOrderByDateTimeDesc(pageable); //Найти страницу записей, а не все записи из БД
        List<HeadacheEpisodeEntity> list = page.getContent(); //Получить значения на странице в виде списка
        
        list.forEach(System.out::println);
        log.info("Успешно проведен поиск {} записей в БД. Найдено {}", limit, page.getNumberOfElements());
    }
    
    public static void deleteEpisode(HeadacheEpisodeRepository repo) {
        log.info("Начало операции удаления записи");
        System.out.println("Введите ID удаляемой записи");
        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine().trim();
        try {
            Long currentId = Long.parseLong(str);
            if (repo.findById(currentId).isPresent()) {
                repo.deleteById(currentId);
                System.out.println("Успешное удаление записи");
                log.info("Успешное удаление записи, ID: {}",currentId);
            } else {
                System.out.println("Ошибка удаления записи: запись с таким ID не существует");
                log.debug("Ошибка удаления записи: попытка удалить несуществующую запись: {}",currentId);
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка удаления записи: введен некорректный ID для удаления");
            log.debug("Ошибка удаления записи: ошибка парсинга ID, получена строка: {}",str);
        }
    }
}
