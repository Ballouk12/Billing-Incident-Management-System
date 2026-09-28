package ma.repository.jpa;


import ma.entity.Incident;
import ma.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    // Recherche par type d'incident
    Page<Incident> findByIncidentTypeAndDeletedFalse(String incidentType, Pageable pageable);

    // Recherche par statut
    Page<Incident> findByStatusAndDeletedFalse(String status, Pageable pageable);

    // Recherche par utilisateur assigné
    Page<Incident> findByAssignedToAndDeletedFalse(User assignedTo, Pageable pageable);

    // Recherche par période
    Page<Incident> findByCreatedAtBetweenAndDeletedFalse(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    // Recherche multi-critères
    @Query("SELECT i FROM Incident i WHERE " +
            "(:incidentType IS NULL OR i.incidentType = :incidentType) AND " +
            "(:status IS NULL OR i.status = :status) AND " +
            "(:assignedToId IS NULL OR i.assignedTo.id = :assignedToId) AND " +
            "(:startDate IS NULL OR i.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR i.createdAt <= :endDate) AND " +
            "i.deleted = false")
    Page<Incident> searchIncidents(
            @Param("incidentType") String incidentType,
            @Param("status") String status,
            @Param("assignedToId") Long assignedToId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Statistiques
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.status = :status AND i.deleted = false")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT i.incidentType, COUNT(i) FROM Incident i WHERE i.deleted = false GROUP BY i.incidentType")
    List<Object[]> countByIncidentType();

    // Incidents non assignés
    Page<Incident> findByAssignedToIsNullAndDeletedFalse(Pageable pageable);

    // Tous les incidents actifs
    Page<Incident> findByDeletedFalse(Pageable pageable);

    // Recherche textuelle
    @Query("SELECT i FROM Incident i WHERE " +
            "(LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.referenceFacture) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(i.clientId) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "i.deleted = false")
    Page<Incident> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}