package ru.practicum.server.stats.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.server.stats.service.StatsService;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createHit(@RequestBody HitDto hit) {
        log.info("Сохраняем запрос на сервере к эндпоинту!");
        service.createHit(hit);
        return ResponseEntity.ok("Информация сохранена!");
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ViewStatsDto> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Получаем с сервера данные по параметрам для запроса /stats");
        return service.getStats(start, end, uris, unique);
    }

}
