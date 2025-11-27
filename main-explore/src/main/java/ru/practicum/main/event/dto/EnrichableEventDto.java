package ru.practicum.main.event.dto;

import java.time.LocalDateTime;

public interface EnrichableEventDto {
    Long getId();

    LocalDateTime getEventDate();

    void setViews(Long views);

    void setConfirmedRequests(Long confirmedRequests);
}
