package com.github.zeykrus.headachetracker.service.impl;

import com.github.zeykrus.headachetracker.dto.EpisodeRequestDTO;
import com.github.zeykrus.headachetracker.dto.EpisodeResponseDTO;
import com.github.zeykrus.headachetracker.entity.HeadacheEpisodeEntity;
import com.github.zeykrus.headachetracker.exception.EpisodeNotFoundException;
import com.github.zeykrus.headachetracker.repository.HeadacheEpisodeRepository;
import com.github.zeykrus.headachetracker.service.EpisodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EpisodeServiceImpl implements EpisodeService {
    private static final Logger log = LoggerFactory.getLogger(EpisodeServiceImpl.class);
    private final HeadacheEpisodeRepository repo;
    
    public EpisodeServiceImpl(HeadacheEpisodeRepository repo) {
        this.repo = repo;
    }
    
    @Transactional
    @Override
    public EpisodeResponseDTO create(EpisodeRequestDTO request) {
        log.info("Начало операции добавления записи в БД: {}", request);
        HeadacheEpisodeEntity episode = HeadacheEpisodeEntity.fromRequest(request);
        HeadacheEpisodeEntity fromRepo = repo.save(episode);
        log.info("Добавлена новая запись в БД: {}", fromRepo);
        return EpisodeResponseDTO.fromEntity(fromRepo);
    }
    
    @Transactional(readOnly = true)
    @Override
    public Page<EpisodeResponseDTO> getLastEpisodes(int limit) {
        log.info("Начало операции поиска записей в БД");
        
        Pageable pageable = PageRequest.of(0, limit); //Формат получения страниц: 0 - номер страницы, limit - объектов на страницу
        Page<HeadacheEpisodeEntity> page = repo.findAllByOrderByDateTimeDesc(pageable); //Найти страницу записей, а не все записи из БД
        Page<EpisodeResponseDTO> resultPage = page.map(EpisodeResponseDTO::fromEntity); //Получить значения на странице в виде списка
        
        log.info("Успешно проведен поиск {} записей в БД. Найдено {}", limit, page.getNumberOfElements());
        return resultPage;
    }
    
    @Transactional
    @Override
    public boolean delete(Long id) {
        log.info("Начало операции удаления записи");
        if (repo.existsById(id)) {
            repo.deleteById(id);
            log.info("Успешное удаление записи, ID: {}", id);
            return true;
        } else {
            log.debug("Ошибка удаления записи: попытка удалить несуществующую запись: {}", id);
        }
        return false;
    }
    
    @Transactional(readOnly = true)
    @Override
    public EpisodeResponseDTO read(Long id) {
        log.info("Начало операции поиска записи. ID: {}", id);
        HeadacheEpisodeEntity entity = repo.findById(id).orElseThrow(() -> {
            log.warn("Попытка поиска несуществующей записи. ID: {}", id);
            return new EpisodeNotFoundException("Запись с указанным ID не существует");
        });
        log.debug("Успешное выполнение операции поиска записи по ID: {}", id);
        return EpisodeResponseDTO.fromEntity(entity);
    }
    
    //TODO: если будет какая-то служебная информация в Entity, то она так же перезапишется. Нужно ли это?
    @Transactional
    @Override
    public EpisodeResponseDTO update(Long id, EpisodeRequestDTO request) {
        log.info("Начало операции обновления записи. ID: {}", id);
        if (repo.existsById(id)) {
            HeadacheEpisodeEntity entity = HeadacheEpisodeEntity.fromRequest(request);
            entity.setId(id);
            repo.save(entity);
            log.debug("Запись успешно обновлена. ID: {}", id);
            return EpisodeResponseDTO.fromEntity(entity);
        } else {
            log.warn("Попытка обновления несуществующей записи. ID: {}", id);
            throw new EpisodeNotFoundException("Запись с указанным ID не существует");
        }
    }
}
