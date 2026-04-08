package com.github.zeykrus.headachetracker.dto;

import com.github.zeykrus.headachetracker.entity.HeadacheEpisodeEntity;

import java.time.LocalDateTime;

public record EpisodeResponseDTO(Long id, LocalDateTime dateTime, int intensity, String location,
                                 String symptoms, String triggers, String comment) {
    
    private static final EpisodeResponseDTO blank = new EpisodeResponseDTO(-1L,LocalDateTime.of(1997,8,30,11,10),-1,"location","symptoms","triggers","comment");
    
    public static EpisodeResponseDTO fromEntity(HeadacheEpisodeEntity entity) {
        return new EpisodeResponseDTO(entity.getId(), entity.getDateTime(), entity.getIntensity(),
                entity.getLocation(), entity.getSymptoms(), entity.getTriggers(), entity.getComment());
    }
    
    public static EpisodeResponseDTO getBlank() {
        return blank;
    }
}
