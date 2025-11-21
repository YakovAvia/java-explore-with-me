package ru.practicum.server.stats.service;

import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {
    void createHit(HitDto body);

    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
