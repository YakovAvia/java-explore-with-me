package ru.practicum.main.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.request.dto.RequestStatus;
import ru.practicum.main.request.model.ParticipationRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequesterId(Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    Boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);
    
    Long countByEventIdAndStatus(Long eventId, RequestStatus status);
    
    @Query("SELECT pr.event.id AS eventId, COUNT(pr.id) AS confirmedRequests " +
           "FROM ParticipationRequest pr " +
           "WHERE pr.event.id IN :eventIds AND pr.status = 'CONFIRMED' " +
           "GROUP BY pr.event.id")
    List<ConfirmedRequests> countConfirmedRequestsForEvents(@Param("eventIds") List<Long> eventIds);

    interface ConfirmedRequests {
        Long getEventId();
        Long getConfirmedRequests();
    }
}
