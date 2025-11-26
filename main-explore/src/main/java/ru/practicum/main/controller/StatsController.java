package ru.practicum.main.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.client.HitClient;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.HitDto;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final HitClient hitClient;
    private final StatsClient statsClient;

    @PostMapping("/hit")
    public ResponseEntity<Object> createHit(@RequestBody HitDto hitDto) {
        log.info("Сохраняем hit: {}", hitDto.getApp());
        return hitClient.createHit(hitDto);
    }

    @GetMapping("/stats")
    public ResponseEntity<Object> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                           @RequestParam(required = false) List<String> uris,
                                           @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Получаем stats по параметрам dateStart: {}, dateEnd: {}, uris: {}", start, end, uris);
        return statsClient.getStats(start, end, uris, unique);
    }

}
