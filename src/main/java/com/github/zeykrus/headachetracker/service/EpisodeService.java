package com.github.zeykrus.headachetracker.service;

import com.github.zeykrus.headachetracker.dto.EpisodeRequestDTO;
import com.github.zeykrus.headachetracker.dto.EpisodeResponseDTO;
import org.springframework.data.domain.Page;

public interface EpisodeService {
    //Classic CRUD
    EpisodeResponseDTO create(EpisodeRequestDTO request);
    EpisodeResponseDTO read(Long id);
    EpisodeResponseDTO update(Long id, EpisodeRequestDTO requestDTO);
    boolean delete(Long id);
    
    //Modified CRUD
    Page<EpisodeResponseDTO> getLastEpisodes(int limit);
}
