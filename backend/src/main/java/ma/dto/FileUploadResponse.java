package ma.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponse {

    private String filename;
    private Integer incidentsExtracted;
    private Integer incidentsCreated;
    private String status;
    private String message;
}