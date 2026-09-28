package ma.service.interfaces;


import ma.dto.IncidentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IncidentService {
    IncidentDTO createIncident(IncidentDTO incidentDTO);
    IncidentDTO updateIncident(Long id, IncidentDTO incidentDTO);
    IncidentDTO getIncidentById(Long id);
    Page<IncidentDTO> getAllIncidents(Pageable pageable);
    Page<IncidentDTO> searchIncidents(String incidentType, String status, Long assignedToId,
                                      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    void deleteIncident(Long id);
    IncidentDTO assignIncident(Long incidentId, Long userId);
    IncidentDTO resolveIncident(Long incidentId);
    Map<String, Long> getStatistics();
    List<IncidentDTO> getIncidentsByAssignedUser(Long userId);
}