package com.github.zeykrus.headachetracker.repository;

import com.github.zeykrus.headachetracker.entity.HeadacheEpisodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HeadacheEpisodeRepository extends JpaRepository<HeadacheEpisodeEntity, Long> {
    List<HeadacheEpisodeEntity> findAllByOrderByDateTimeDesc();
}
