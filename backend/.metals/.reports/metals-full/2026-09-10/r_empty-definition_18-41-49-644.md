error id: file:///D:/Users/HP/Desktop/backend/src/main/java/ma/service/IncidentServiceImpl.java:ma/service/interfaces/AuditLogService#
file:///D:/Users/HP/Desktop/backend/src/main/java/ma/service/IncidentServiceImpl.java
empty definition using pc, found symbol in pc: ma/service/interfaces/AuditLogService#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 295
uri: file:///D:/Users/HP/Desktop/backend/src/main/java/ma/service/IncidentServiceImpl.java
text:
```scala
package ma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.dto.IncidentDTO;
import ma.entity.Incident;
import ma.entity.User;
import ma.repository.jpa.IncidentRepository;
import ma.repository.jpa.UserRepository;
import ma.service.interfaces.@@AuditLogService;
import ma.service.interfaces.IncidentService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ModelMapper modelMapper;

    @Override
    public IncidentDTO createIncident(IncidentDTO incidentDTO) {
        log.debug("Creating new incident: {}", incidentDTO);

        Incident incident = modelMapper.map(incidentDTO, Incident.class);

        // Récupérer l'utilisateur créateur
        if (incidentDTO.getCreatedById() != null) {
            User createdBy = userRepository.findById(incidentDTO.getCreatedById())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            incident.setCreatedBy(createdBy);
        }

        // Statut initial
        if (incident.getStatus() == null) {
            incident.setStatus("NOUVEAU");
        }

        Incident savedIncident = incidentRepository.save(incident);

        // Créer un log d'audit
        auditLogService.logAction(savedIncident, "CREATED",
                "Incident créé: " + savedIncident.getIncidentType());

        log.info("Incident created successfully with ID: {}", savedIncident.getId());
        return modelMapper.map(savedIncident, IncidentDTO.class);
    }

    @Override
    public IncidentDTO updateIncident(Long id, IncidentDTO incidentDTO) {
        log.debug("Updating incident with ID: {}", id);

        Incident existingIncident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        // Conserver les anciennes valeurs pour audit
        String oldStatus = existingIncident.getStatus();

        // Mise à jour des champs
        modelMapper.map(incidentDTO, existingIncident);
        existingIncident.setId(id); // Garder l'ID original

        Incident updatedIncident = incidentRepository.save(existingIncident);

        // Log d'audit
        String details = String.format("Statut changé de %s à %s", oldStatus, updatedIncident.getStatus());
        auditLogService.logAction(updatedIncident, "UPDATED", details);

        log.info("Incident updated successfully: {}", id);
        return modelMapper.map(updatedIncident, IncidentDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public IncidentDTO getIncidentById(Long id) {
        log.debug("Fetching incident with ID: {}", id);

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        return modelMapper.map(incident, IncidentDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentDTO> getAllIncidents(Pageable pageable) {
        log.debug("Fetching all incidents with pagination: {}", pageable);

        Page<Incident> incidents = incidentRepository.findByDeletedFalse(pageable);
        return incidents.map(incident -> modelMapper.map(incident, IncidentDTO.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IncidentDTO> searchIncidents(String incidentType, String status, Long assignedToId,
                                             LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Searching incidents with filters");

        Page<Incident> incidents = incidentRepository.searchIncidents(
                incidentType, status, assignedToId, startDate, endDate, pageable
        );

        return incidents.map(incident -> modelMapper.map(incident, IncidentDTO.class));
    }

    @Override
    public void deleteIncident(Long id) {
        log.debug("Deleting incident with ID: {}", id);

        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));

        // Suppression logique
        incident.setDeleted(true);
        incidentRepository.save(incident);

        auditLogService.logAction(incident, "DELETED", "Incident supprimé (logiquement)");

        log.info("Incident deleted (soft delete): {}", id);
    }

    @Override
    public IncidentDTO assignIncident(Long incidentId, Long userId) {
        log.debug("Assigning incident {} to user {}", incidentId, userId);

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        incident.setAssignedTo(user);
        incident.setStatus("EN_COURS");

        Incident updatedIncident = incidentRepository.save(incident);

        auditLogService.logAction(updatedIncident, "ASSIGNED",
                "Incident assigné à " + user.getUsername());

        log.info("Incident {} assigned to user {}", incidentId, userId);
        return modelMapper.map(updatedIncident, IncidentDTO.class);
    }

    @Override
    public IncidentDTO resolveIncident(Long incidentId) {
        log.debug("Resolving incident: {}", incidentId);

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));

        incident.setStatus("RESOLU");
        incident.setResolvedAt(LocalDateTime.now());

        Incident resolvedIncident = incidentRepository.save(incident);

        auditLogService.logAction(resolvedIncident, "RESOLVED", "Incident résolu");

        log.info("Incident resolved: {}", incidentId);
        return modelMapper.map(resolvedIncident, IncidentDTO.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getStatistics() {
        log.debug("Fetching incident statistics");

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", incidentRepository.count());
        stats.put("nouveau", incidentRepository.countByStatus("NOUVEAU"));
        stats.put("en_cours", incidentRepository.countByStatus("EN_COURS"));
        stats.put("resolu", incidentRepository.countByStatus("RESOLU"));
        stats.put("ferme", incidentRepository.countByStatus("FERME"));

        // 🔥 Statistiques par type
        List<Object[]> typeCounts = incidentRepository.countByIncidentType();

        for (Object[] row : typeCounts) {
            String incidentType = (String) row[0];
            Long count = (Long) row[1];

            // Exemple : "erreur_calcul" pour le frontend
            String key = incidentType.toLowerCase().replace(" ", "_");
            stats.put(key, count);
        }

        return stats;

    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidentDTO> getIncidentsByAssignedUser(Long userId) {
        log.debug("Fetching incidents for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Incident> incidents = incidentRepository.findByAssignedToAndDeletedFalse(
                user, Pageable.unpaged()
        );

        return incidents.stream()
                .map(incident -> modelMapper.map(incident, IncidentDTO.class))
                .collect(Collectors.toList());
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: ma/service/interfaces/AuditLogService#