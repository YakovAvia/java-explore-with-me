package ru.practicum.explorewithme.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.stats.dto.EndpointHitDto;
import ru.practicum.explorewithme.stats.dto.ViewStatsDto;
import ru.practicum.explorewithme.stats.server.mapper.EndpointHitMapper;
import ru.practicum.explorewithme.stats.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto endpointHitDto) {
        statsRepository.save(EndpointHitMapper.toEndpointHit(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        if (unique) {
            return statsRepository.getStatsUniqueIp(start, end, uris);
        } else {
            return statsRepository.getStats(start, end, uris);
        }
    }
}
