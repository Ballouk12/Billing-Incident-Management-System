package ma.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentDTO {

    private Long id;

    @NotBlank(message = "Le type d'incident est obligatoire")
    private String incidentType;

    @NotBlank(message = "Le statut est obligatoire")
    private String status;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 5000, message = "La description doit contenir entre 10 et 5000 caractères")
    private String description;

    private String referenceFacture;
    private String clientId;
    private Double montantErreur;
    private String sourceFile;

    @NotNull(message = "Le numéro de ligne est obligatoire")
    private Integer lineNumber;

    private String priority;

    private Long assignedToId;
    private String assignedToUsername;

    private Long createdById;
    private String createdByUsername;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime resolvedAt;

    private List<SolutionDTO> solutions;
}