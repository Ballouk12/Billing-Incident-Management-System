package ma.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolutionDTO {

    @NotNull(message = "L'ID de l'incident est obligatoire")
    private Long incidentId;

    private Long createdById;
    private String createdByUsername;

    @NotBlank(message = "La description de la solution est obligatoire")
    @Size(min = 10, max = 5000, message = "La description doit contenir entre 10 et 5000 caractères")
    private String description;

    private String solutionType;
    private Boolean validated;
    private String comments;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
