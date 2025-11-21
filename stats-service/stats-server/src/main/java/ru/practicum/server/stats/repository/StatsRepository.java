package ru.practicum.server.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<ru.practicum.stats.server.model.EndpointHit, Long> {

    @Query("SELECT new ru.practicum.server.stats.model.dto.ViewStatsDto(h.app, h.uri, COUNT(DISTINCT h.ip) as uniqueHits) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY uniqueHits DESC")
    List<ViewStatsDto> getStatsUniqueIp(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.server.stats.model.dto.ViewStatsDto(h.app, h.uri, COUNT(h.ip) as hist) " +
            "FROM EndpointHit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY hist DESC ")
    List<ViewStatsDto> getStatsUnique(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("uris") List<String> uris);
}
