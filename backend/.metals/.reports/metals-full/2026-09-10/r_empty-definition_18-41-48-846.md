error id: file:///D:/Users/HP/Desktop/backend/src/main/java/ma/service/AuditLogServiceImpl.java:ma/service/interfaces/AuditLogService#
file:///D:/Users/HP/Desktop/backend/src/main/java/ma/service/AuditLogServiceImpl.java
empty definition using pc, found symbol in pc: ma/service/interfaces/AuditLogService#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 281
uri: file:///D:/Users/HP/Desktop/backend/src/main/java/ma/service/AuditLogServiceImpl.java
text:
```scala
package ma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.dto.AuditLogDTO;
import ma.entity.AuditLog;
import ma.entity.Incident;
import ma.entity.User;
import ma.repository.jpa.AuditLogRepository;
import ma.service.interfaces.@@AuditLogService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ModelMapper modelMapper;

    @Override
    public void logAction(Incident incident, String action, String details) {
        try {
            // Récupérer l'utilisateur authentifié
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();

                AuditLog auditLog = AuditLog.builder()
                        .incident(incident)
                        .user(user)
                        .action(action)
                        .details(details)
                        .ipAddress(getClientIpAddress())
                        .build();

                auditLogRepository.save(auditLog);

                log.debug("Audit log created: {} for incident {}", action, incident.getId());
            }
        } catch (Exception e) {
            // Ne pas faire échouer l'opération principale si le log d'audit échoue
            log.error("Failed to create audit log: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogsByIncidentId(Long incidentId) {
        log.debug("Fetching audit logs for incident: {}", incidentId);

        List<AuditLog> auditLogs = auditLogRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId);

        return auditLogs.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAuditLogsByUserId(Long userId, Pageable pageable) {
        log.debug("Fetching audit logs for user: {}", userId);

        Page<AuditLog> auditLogs = auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return auditLogs.map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDTO> getAllAuditLogs(Pageable pageable) {
        log.debug("Fetching all audit logs");

        Page<AuditLog> auditLogs = auditLogRepository.findAll(pageable);

        return auditLogs.map(this::mapToDTO);
    }

    private AuditLogDTO mapToDTO(AuditLog auditLog) {
        AuditLogDTO dto = modelMapper.map(auditLog, AuditLogDTO.class);
        if (auditLog.getIncident() != null) {
            dto.setIncidentId(auditLog.getIncident().getId());
        }
        dto.setUserId(auditLog.getUser().getId());
        dto.setUsername(auditLog.getUser().getUsername());
        return dto;
    }

    private String getClientIpAddress() {
        // À implémenter avec HttpServletRequest si nécessaire
        return "127.0.0.1";
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: ma/service/interfaces/AuditLogService#