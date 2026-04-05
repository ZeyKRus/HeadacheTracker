package com.github.zeykrus.headachetracker;

import com.github.zeykrus.headachetracker.repository.HeadacheEpisodeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner consoleRunner(HeadacheEpisodeRepository repo) {
        return args -> {

        };
    }
}
