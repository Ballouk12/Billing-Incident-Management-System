package ma.service.interfaces;

import ma.dto.AuditLogDTO;
import ma.entity.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditLogService {
    void logAction(Incident incident, String action, String details);
    List<AuditLogDTO> getAuditLogsByIncidentId(Long incidentId);
    Page<AuditLogDTO> getAuditLogsByUserId(Long userId, Pageable pageable);
    Page<AuditLogDTO> getAllAuditLogs(Pageable pageable);
}