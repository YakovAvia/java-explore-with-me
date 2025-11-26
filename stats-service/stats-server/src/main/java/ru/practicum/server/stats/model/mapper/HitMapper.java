package ru.practicum.server.stats.model.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.server.model.EndpointHit;

@UtilityClass
public class HitMapper {

    public static EndpointHit toHit(HitDto hitDto) {
        return EndpointHit.builder()
                .app(hitDto.getApp())
                .uri(hitDto.getUri())
                .ip(hitDto.getIp())
                .timestamp(hitDto.getTimestamp())
                .build();
    }
}
