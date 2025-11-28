package ru.practicum.main.event.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.DataIntegrityViolationException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.location.mapper.LocationMapper;
import ru.practicum.main.location.model.Location;
import ru.practicum.main.location.repository.LocationRepository;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;
import ru.practicum.stats.client.HitClient;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.HitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final LocalDateTime MIN_DATE = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final RequestRepository requestRepository;
    private final HitClient hitClient;
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getEventsByInitiator(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        List<Event> events = eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size));
        List<EventShortDto> dtos = EventMapper.toEventShortDto(events);
        enrichEvents(dtos);
        return dtos;
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new DataIntegrityViolationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + newEventDto.getEventDate());
        }

        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + newEventDto.getCategory() + " was not found"));

        Location location = locationRepository.findByLatAndLon(newEventDto.getLocation().getLat(), newEventDto.getLocation().getLon())
                .orElseGet(() -> locationRepository.save(LocationMapper.toLocation(newEventDto.getLocation())));

        Event event = EventMapper.toEvent(newEventDto, category, initiator, LocalDateTime.now());
        event.setLocation(location);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventByIdAndInitiator(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        EventFullDto dto = EventMapper.toEventFullDto(event);
        enrichEvents(List.of(dto));
        return dto;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByInitiator(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new DataIntegrityViolationException("User is not the initiator of the event.");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new DataIntegrityViolationException("Only pending or canceled events can be changed");
        }

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + updateRequest.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            if (updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new DataIntegrityViolationException("Event date must be at least 2 hours in the future.");
            }
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(LocationMapper.toLocation(updateRequest.getLocation()));
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (updateRequest.getStateAction() == UserStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }
        EventFullDto dto = EventMapper.toEventFullDto(eventRepository.save(event));
        enrichEvents(List.of(dto));
        return dto;
    }

    @Override
    public List<EventFullDto> searchEvents(List<Long> users, List<String> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        Specification<Event> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            if (users != null && !users.isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(users));
            }
            if (states != null && !states.isEmpty()) {
                predicates.add(root.get("state").as(String.class).in(states));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }
            if (rangeStart != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<Event> events = eventRepository.findAll(spec, PageRequest.of(from / size, size, Sort.by("eventDate"))).getContent();
        List<EventFullDto> dtos = EventMapper.toEventFullDto(events);
        enrichEvents(dtos);
        return dtos;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + updateRequest.getCategory() + " was not found"));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            if (updateRequest.getEventDate().isBefore(LocalDateTime.now())) {
                throw new DataIntegrityViolationException("Event date must be in the future.");
            }
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            Location location = locationRepository.findByLatAndLon(updateRequest.getLocation().getLat(), updateRequest.getLocation().getLon())
                    .orElseGet(() -> locationRepository.save(LocationMapper.toLocation(updateRequest.getLocation())));
            event.setLocation(location);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new DataIntegrityViolationException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new DataIntegrityViolationException("Cannot publish the event because its start date is less than 1 hour from now.");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (updateRequest.getStateAction() == AdminStateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new DataIntegrityViolationException("Cannot reject the event because it's already published.");
                }
                event.setState(EventState.CANCELED);
            }
        }

        EventFullDto dto = EventMapper.toEventFullDto(eventRepository.save(event));
        enrichEvents(List.of(dto));
        return dto;
    }

    @Override
    public List<EventShortDto> getPublishedEvents(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                                  String sort, Integer from, Integer size, String ip) {
        hitClient.createHit(new HitDto("ewm-main-service", "/events", ip, LocalDateTime.now()));
        Specification<Event> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("state"), EventState.PUBLISHED));
            if (text != null) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text.toLowerCase() + "%")
                ));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }
            if (paid != null) {
                predicates.add(criteriaBuilder.equal(root.get("paid"), paid));
            }
            if (rangeStart != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
            if (rangeStart == null && rangeEnd == null) {
                predicates.add(criteriaBuilder.greaterThan(root.get("eventDate"), LocalDateTime.now()));
            }
            if (onlyAvailable != null && onlyAvailable) {
                // This logic is tricky with a simple query. A subquery or join would be better.
                // For now, I will filter in memory, but this is not optimal.
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<Event> events = eventRepository.findAll(spec, PageRequest.of(from / size, size)).getContent();
        List<EventShortDto> dtos = EventMapper.toEventShortDto(events);
        enrichEvents(dtos);

        if (onlyAvailable != null && onlyAvailable) {
            dtos = dtos.stream()
                    .filter(dto -> {
                        Event event = events.stream().filter(e -> e.getId().equals(dto.getId())).findFirst().get();
                        return event.getParticipantLimit() == 0 || dto.getConfirmedRequests() < event.getParticipantLimit();
                    })
                    .collect(Collectors.toList());
        }

        if (sort != null && sort.equalsIgnoreCase("VIEWS")) {
            dtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        return dtos;
    }

    @Override
    public EventFullDto getPublishedEventById(Long eventId, String ip) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        hitClient.createHit(new HitDto("ewm-main-service", "/events/" + eventId, ip, LocalDateTime.now()));
        EventFullDto dto = EventMapper.toEventFullDto(event);
        enrichEvents(List.of(dto));
        return dto;
    }

    private <T extends EnrichableEventDto> void enrichEvents(List<T> dtos) {
        enrichWithViews(dtos);
        enrichWithConfirmedRequests(dtos);
    }

    private <T extends EnrichableEventDto> void enrichWithViews(List<T> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        List<String> uris = dtos.stream()
                .map(dto -> "/events/" + dto.getId())
                .collect(Collectors.toList());

        LocalDateTime start = dtos.stream()
                .map(T::getEventDate)
                .min(LocalDateTime::compareTo)
                .orElse(MIN_DATE);

        // Ensure 'start' is not after 'end' (LocalDateTime.now())
        if (start.isAfter(LocalDateTime.now())) {
            start = LocalDateTime.now();
        }

        ResponseEntity<List<ViewStatsDto>> response = statsClient.getStats(start, LocalDateTime.now(), uris, true);
        List<ViewStatsDto> viewStats = response.getBody();

        if (viewStats != null) {
            Map<Long, Long> viewsMap = viewStats.stream()
                    .collect(Collectors.toMap(
                            stat -> Long.parseLong(stat.getUri().substring("/events/".length())),
                            ViewStatsDto::getHits
                    ));
            dtos.forEach(dto -> dto.setViews(viewsMap.getOrDefault(dto.getId(), 0L)));
        }
    }

    private <T extends EnrichableEventDto> void enrichWithConfirmedRequests(List<T> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        List<Long> eventIds = dtos.stream().map(T::getId).collect(Collectors.toList());
        Map<Long, Long> confirmedRequestsMap = requestRepository.countConfirmedRequestsForEvents(eventIds).stream()
                .collect(Collectors.toMap(RequestRepository.ConfirmedRequests::getEventId, RequestRepository.ConfirmedRequests::getConfirmedRequests));

        dtos.forEach(dto -> dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(dto.getId(), 0L)));
    }
}
