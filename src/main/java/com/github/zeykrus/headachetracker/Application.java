package com.github.zeykrus.headachetracker;

import com.github.zeykrus.headachetracker.dto.EpisodeRequestDTO;
import com.github.zeykrus.headachetracker.dto.EpisodeResponseDTO;
import com.github.zeykrus.headachetracker.exception.EpisodeNotFoundException;
import com.github.zeykrus.headachetracker.service.EpisodeService;
import com.github.zeykrus.headachetracker.util.ConsoleInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

@SpringBootApplication
public class Application {
    public static final Logger log = LoggerFactory.getLogger(Application.class);
    
    @Value("${app.default.value.string}")
    private String stringNotSpecified = "Не указано"; //String значение для defaultValue
    
    @Value("${app.default.value.intensity}")
    private int intensityNotSpecified = 1; //int значение для defaultValue intensity
    
    @Value("${app.default.value.intensity.min}")
    private int intensityMin = 1;
    
    @Value("${app.default.value.intensity.max}")
    private int intensityMax = 10;
    
    @Value("${app.default.value.limit}")
    private int defaultLimit = 1;
    
    @Value("${app.local.date.time.pattern}")
    private String pattern;
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public CommandLineRunner consoleRunner(EpisodeService service) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            while (!exit) {
                System.out.println("\n=== Дневник головной боли ===");
                System.out.println("1. Добавить запись");
                System.out.println("2. Показать последние N записей");
                System.out.println("3. Удалить запись");
                System.out.println("4. Изменить запись по ID");
                System.out.println("0. Выход");
                System.out.print("Выберите действие: ");
                
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1":
                        addEpisode(service);
                        break;
                    case "2":
                        showLastEpisodes(service);
                        break;
                    case "3":
                        deleteEpisode(service);
                        break;
                    case "4":
                        updateEpisode(service);
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
    
    public void addEpisode(EpisodeService service) {
        System.out.print("Введите дату и время в формате: ");
        System.out.println(pattern);
        LocalDateTime dateTime = ConsoleInput.readDateTime(pattern, LocalDateTime.now());
        
        System.out.println("Введите интенсивность боли (от "+intensityMin+" до "+intensityMax+")");
        int intensity = ConsoleInput.readInt(intensityMin, intensityMax, intensityNotSpecified);
        
        System.out.println("Укажите место, где чувствуете боль");
        String location = ConsoleInput.readLine(stringNotSpecified);
        
        System.out.println("Укажите симптомы");
        String symptoms = ConsoleInput.readLine(stringNotSpecified);
        
        System.out.println("Укажите возможные причины боли");
        String triggers = ConsoleInput.readLine(stringNotSpecified);
        
        System.out.println("Добавьте комментарий");
        String comment = ConsoleInput.readLine(stringNotSpecified);
        
        EpisodeRequestDTO request = new EpisodeRequestDTO(dateTime, intensity, location, symptoms, triggers, comment);
        EpisodeResponseDTO response = service.create(request);
        System.out.println("Запись успешно добавлена");
        System.out.println(response.toString());
    }
    
    public void showLastEpisodes(EpisodeService service) {
        System.out.println("Укажите количество записей для просмотра");
        int limit = ConsoleInput.readInt(defaultLimit);
        Page<EpisodeResponseDTO> page = service.getLastEpisodes(limit);
        page.forEach(System.out::println);
    }
    
    public void deleteEpisode(EpisodeService service) {
        System.out.println("Введите ID удаляемой записи");
        Scanner scanner = ConsoleInput.getScanner();
        String str = scanner.nextLine().trim();
        try {
            Long currentId = Long.parseLong(str);
            if(service.delete(currentId)) System.out.println("Запись успешно удалена");
        } catch (NumberFormatException e) {
            System.out.println("Ошибка удаления записи: введен некорректный ID для удаления");
            log.debug("Ошибка удаления записи: ошибка парсинга ID, получена строка: {}",str);
        }
    }
    
    public void updateEpisode(EpisodeService service) {
        System.out.println("Введите ID изменяемой записи");
        LocalDateTime dateTime;
        int intensity;
        String location;
        String symptoms;
        String triggers;
        String comment;
        
        Scanner scanner = ConsoleInput.getScanner();
        String str = scanner.nextLine().trim();
        long currentId = 0;
        EpisodeResponseDTO response;
        try {
            currentId = Long.parseLong(str);
            response = service.read(currentId);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка изменения записи: введен некорректный ID для изменения");
            log.warn("Ошибка изменения записи: ошибка парсинга ID, получена строка: {}", str);
            return;
        } catch (EpisodeNotFoundException e) {
            System.out.println("Ошибка изменения записи: введен некорректный ID для изменения");
            log.warn("Ошибка изменения записи: запись с указанным ID не существует: {}", currentId);
            return;
        }
        
        System.out.println("Указанные в записи дата и время: "+response.dateTime().format(DateTimeFormatter.ofPattern(pattern)));
        System.out.println("Введите новую дату и время в формате: yyyy-MM-dd HH:mm (Enter - не изменять)");
        dateTime = ConsoleInput.readDateTime(pattern, response.dateTime());
        
        System.out.println("Указанная интенсивность: "+response.intensity());
        System.out.println("Введите новую интенсивность (от "+intensityMin+" до "+intensityMax+") (Enter - не изменять)");
        intensity = ConsoleInput.readInt(intensityMin, intensityMax, response.intensity());
        
        System.out.println("Указанное место боли: "+response.location());
        System.out.println("Введите новое место боли (Enter - не изменять)");
        location = ConsoleInput.readLine(response.location());
        
        System.out.println("Указанные симптомы: "+response.symptoms());
        System.out.println("Введите новые симптомы (Enter - не изменять)");
        symptoms = ConsoleInput.readLine(response.symptoms());
        
        System.out.println("Указанные причины: "+response.triggers());
        System.out.println("Введите новые причины (Enter - не изменять)");
        triggers = ConsoleInput.readLine(response.triggers());
        
        System.out.println("Указанный комментарий: "+response.comment());
        System.out.println("Введите новый комментарий (Enter - не изменять)");
        comment = ConsoleInput.readLine(response.comment());
        
        EpisodeRequestDTO request = new EpisodeRequestDTO(dateTime, intensity, location, symptoms, triggers, comment);
        try {
            EpisodeResponseDTO updated = service.update(currentId, request);
            System.out.println("Запись успешно изменена");
            System.out.println(updated.toString());
        } catch (EpisodeNotFoundException e) {
            System.out.println("Ошибка изменения записи: "+e.getMessage());
        }
    }
}
