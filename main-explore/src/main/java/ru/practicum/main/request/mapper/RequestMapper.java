package ru.practicum.main.request.mapper;

import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.model.ParticipationRequest;

import java.util.ArrayList;
import java.util.List;

public class RequestMapper {
    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getEvent().getId(),
                request.getRequester().getId(),
                request.getStatus(),
                request.getCreated()
        );
    }
    public static List<ParticipationRequestDto> toParticipationRequestDto(Iterable<ParticipationRequest> requests) {
        List<ParticipationRequestDto> dtos = new ArrayList<>();
        for (ParticipationRequest request : requests) {
            dtos.add(toParticipationRequestDto(request));
        }
        return dtos;
    }
}
