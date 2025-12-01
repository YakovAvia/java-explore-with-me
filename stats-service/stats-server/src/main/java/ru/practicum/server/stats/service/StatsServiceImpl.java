package ru.practicum.server.stats.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.server.stats.model.mapper.HitMapper;
import ru.practicum.server.stats.repository.StatsRepository;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository repository;

    @Override
    @Transactional
    public void createHit(HitDto body) {
        repository.save(HitMapper.toHit(body));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        if (unique) {
            if (uris == null || uris.isEmpty()) {
                return repository.getStatsUniqueIpAllUris(start, end);
            }
            return repository.getStatsUniqueIp(start, end, uris);
        } else {
            if (uris == null || uris.isEmpty()) {
                return repository.getStatsAllUris(start, end);
            }
            return repository.getStats(start, end, uris);
        }
    }
}
