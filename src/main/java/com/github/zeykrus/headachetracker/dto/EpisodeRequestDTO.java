package com.github.zeykrus.headachetracker.dto;

import java.time.LocalDateTime;

public record EpisodeRequestDTO(LocalDateTime dateTime, int intensity, String location,
                                String symptoms, String triggers, String comment) {
}
