package ru.practicum.main.event.dto;

public interface EnrichableEventDto {
    Long getId();

    void setViews(Long views);

    void setConfirmedRequests(Long confirmedRequests);
}
