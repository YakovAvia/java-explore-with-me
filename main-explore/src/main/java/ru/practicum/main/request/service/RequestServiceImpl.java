package ru.practicum.main.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.event.dto.EventState;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.DataIntegrityViolationException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.request.dto.*;
import ru.practicum.main.request.mapper.RequestMapper;
import ru.practicum.main.request.model.ParticipationRequest;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        return RequestMapper.toParticipationRequestDto(requestRepository.findAllByRequesterId(userId));
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new DataIntegrityViolationException("Cannot add a repeat request.");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new DataIntegrityViolationException("Initiator cannot add a request to their own event.");
        }

        if (event.getState() != EventState.PUBLISHED) {
            throw new DataIntegrityViolationException("Cannot participate in an unpublished event.");
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() != 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new DataIntegrityViolationException("The participant limit has been reached.");
        }

        ParticipationRequest newRequest = new ParticipationRequest();
        newRequest.setRequester(requester);
        newRequest.setEvent(event);
        newRequest.setCreated(LocalDateTime.now());

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            newRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            newRequest.setStatus(RequestStatus.PENDING);
        }

        return RequestMapper.toParticipationRequestDto(requestRepository.save(newRequest));
    }


    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new DataIntegrityViolationException("User is not the requester of this request.");
        }

        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new DataIntegrityViolationException("User is not the initiator of the event.");
        }

        return RequestMapper.toParticipationRequestDto(requestRepository.findAllByEventId(eventId));
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest statusUpdateRequest) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new DataIntegrityViolationException("User is not the initiator of the event.");
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            // No approval needed
            return new EventRequestStatusUpdateResult(new ArrayList<>(), new ArrayList<>());
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(statusUpdateRequest.getRequestIds());
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult(new ArrayList<>(), new ArrayList<>());
        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new DataIntegrityViolationException("Request must have status PENDING");
            }

            if (statusUpdateRequest.getStatus() == RequestStatusUpdate.CONFIRMED) {
                if (confirmedCount >= event.getParticipantLimit()) {
                    throw new DataIntegrityViolationException("The participant limit has been reached.");
                }
                request.setStatus(RequestStatus.CONFIRMED);
                result.getConfirmedRequests().add(RequestMapper.toParticipationRequestDto(request));
                confirmedCount++;
            } else {
                request.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(RequestMapper.toParticipationRequestDto(request));
            }
        }

        if (confirmedCount >= event.getParticipantLimit()) {
            requestRepository.findAllByEventId(eventId).stream()
                    .filter(r -> r.getStatus() == RequestStatus.PENDING)
                    .forEach(r -> {
                        r.setStatus(RequestStatus.REJECTED);
                        result.getRejectedRequests().add(RequestMapper.toParticipationRequestDto(r));
                    });
        }

        requestRepository.saveAll(requests);
        return result;
    }

}