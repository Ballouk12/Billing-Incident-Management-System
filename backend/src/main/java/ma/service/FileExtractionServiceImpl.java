package ma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.dto.IncidentDTO;
import ma.exception.InvalidFileException;
import ma.service.interfaces.FileExtractionService;
import ma.service.interfaces.IncidentService;
import ma.util.CsvParser;
import ma.util.FileValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileExtractionServiceImpl implements FileExtractionService {

    private final IncidentService incidentService;
    private final CsvParser csvParser;
    private final FileValidator fileValidator;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.rollback.watch-dir}")
    private String rollbackWatchDir;

    @Override
    public List<IncidentDTO> extractIncidentsFromFile(MultipartFile file, Long userId) {
        log.info("Starting extraction from uploaded file: {}", file.getOriginalFilename());

        // Validation du fichier
        fileValidator.validateFile(file);

        try {
            // Créer le répertoire d'upload s'il n'existe pas
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath);
            }

            // Générer un nom de fichier unique avec timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String originalFilename = file.getOriginalFilename();
            String filename = timestamp + "_" + sanitizeFilename(originalFilename);
            Path filePath = uploadPath.resolve(filename);

            // Copier le fichier uploadé vers le répertoire
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved to: {}", filePath);

            // Extraire les incidents du fichier
            List<IncidentDTO> incidents = parseFileAndCreateIncidents(
                    filePath.toString(),
                    originalFilename,
                    userId
            );

            log.info("Successfully extracted {} incidents from file: {}", incidents.size(), originalFilename);
            return incidents;

        } catch (IOException e) {
            log.error("Error processing uploaded file: {}", e.getMessage(), e);
            throw new InvalidFileException("Erreur lors du traitement du fichier: " + e.getMessage());
        }
    }

    @Override
    public List<IncidentDTO> extractIncidentsFromPath(String filePath, Long userId) {
        log.info("Starting extraction from file path: {}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            log.error("File not found: {}", filePath);
            throw new InvalidFileException("Fichier introuvable: " + filePath);
        }

        if (!file.canRead()) {
            log.error("Cannot read file: {}", filePath);
            throw new InvalidFileException("Impossible de lire le fichier: " + filePath);
        }

        return parseFileAndCreateIncidents(filePath, file.getName(), userId);
    }

    @Override
    public void processRollbackDirectory() {
        log.info("Starting automatic processing of rollback directory: {}", rollbackWatchDir);

        Path watchPath = Paths.get(rollbackWatchDir);

        // Créer le répertoire s'il n'existe pas
        if (!Files.exists(watchPath)) {
            try {
                Files.createDirectories(watchPath);
                log.info("Created rollback watch directory: {}", watchPath);
                return; // Pas de fichiers à traiter
            } catch (IOException e) {
                log.error("Failed to create watch directory: {}", e.getMessage());
                return;
            }
        }

        // Créer le répertoire "processed" pour les fichiers traités
        Path processedDir = watchPath.resolve("processed");
        try {
            if (!Files.exists(processedDir)) {
                Files.createDirectories(processedDir);
            }
        } catch (IOException e) {
            log.error("Failed to create processed directory: {}", e.getMessage());
        }

        // Traiter tous les fichiers CSV et Excel
        try (Stream<Path> files = Files.list(watchPath)) {
            files.filter(path -> !Files.isDirectory(path))
                    .filter(path -> isProcessableFile(path.getFileName().toString()))
                    .forEach(path -> {
                        try {
                            log.info("Processing file from watch directory: {}", path.getFileName());

                            // Extraire les incidents (userId = null pour traitement auto)
                            List<IncidentDTO> incidents = extractIncidentsFromPath(path.toString(), null);

                            log.info("Extracted {} incidents from {}", incidents.size(), path.getFileName());

                            // Déplacer le fichier vers le répertoire "processed"
                            Path targetPath = processedDir.resolve(path.getFileName());
                            Files.move(path, targetPath, StandardCopyOption.REPLACE_EXISTING);

                            log.info("File moved to processed: {}", path.getFileName());

                        } catch (Exception e) {
                            log.error("Error processing file {}: {}", path.getFileName(), e.getMessage(), e);

                            // Déplacer vers un répertoire "error" en cas d'échec
                            try {
                                Path errorDir = watchPath.resolve("error");
                                if (!Files.exists(errorDir)) {
                                    Files.createDirectories(errorDir);
                                }
                                Path errorPath = errorDir.resolve(path.getFileName());
                                Files.move(path, errorPath, StandardCopyOption.REPLACE_EXISTING);
                                log.info("File moved to error directory: {}", path.getFileName());
                            } catch (IOException ex) {
                                log.error("Failed to move error file: {}", ex.getMessage());
                            }
                        }
                    });

            log.info("Completed automatic processing of rollback directory");

        } catch (IOException e) {
            log.error("Error reading rollback directory: {}", e.getMessage(), e);
        }
    }

    private List<IncidentDTO> parseFileAndCreateIncidents(String filePath, String fileName, Long userId) {
        List<IncidentDTO> createdIncidents = new ArrayList<>();

        log.info("Parsing file: {}", filePath);

        try {
            // Parser le fichier CSV
            List<Map<String, String>> records = csvParser.parseCsv(filePath);

            log.info("Parsed {} records from file", records.size());

            if (records.isEmpty()) {
                log.warn("No records found in file: {}", fileName);
                return createdIncidents;
            }

            // Traiter chaque ligne du fichier
            int lineNumber = 2; // Ligne 1 = headers, on commence à 2
            int successCount = 0;
            int errorCount = 0;

            for (Map<String, String> record : records) {
                try {
                    // Mapper la ligne vers un IncidentDTO
                    IncidentDTO incident = mapRecordToIncident(record, fileName, lineNumber, userId);

                    // Valider l'incident
                    if (isValidIncident(incident)) {
                        // Créer l'incident via le service
                        IncidentDTO created = incidentService.createIncident(incident);
                        createdIncidents.add(created);
                        successCount++;

                        log.debug("Created incident from line {}: ID={}", lineNumber, created.getId());
                    } else {
                        log.warn("Invalid incident at line {}, skipping", lineNumber);
                        errorCount++;
                    }

                } catch (Exception e) {
                    log.warn("Error processing line {}: {}", lineNumber, e.getMessage());
                    errorCount++;
                }

                lineNumber++;
            }

            log.info("Extraction completed - Success: {}, Errors: {}, Total: {}",
                    successCount, errorCount, records.size());

        } catch (Exception e) {
            log.error("Error parsing file {}: {}", fileName, e.getMessage(), e);
            throw new InvalidFileException("Erreur lors du parsing du fichier: " + e.getMessage());
        }

        return createdIncidents;
    }

    private IncidentDTO mapRecordToIncident(Map<String, String> record, String fileName,
                                            int lineNumber, Long userId) {

        // Construire le DTO à partir des colonnes du CSV
        IncidentDTO incident = IncidentDTO.builder()
                .incidentType(determineIncidentType(record))
                .description(buildDescription(record))
                .referenceFacture(getFieldValue(record, "reference_facture", "ref_facture", "facture"))
                .clientId(getFieldValue(record, "client_id", "id_client", "client"))
                .sourceFile(fileName)
                .lineNumber(lineNumber)
                .status("NOUVEAU")
                .priority(determinePriority(record))
                .createdById(userId != null ? userId : 1L) // User système par défaut
                .build();

        // Extraire le montant d'erreur
        String montantStr = getFieldValue(record, "montant_erreur", "montant", "amount");
        if (montantStr != null && !montantStr.isEmpty()) {
            try {
                double montant = Double.parseDouble(montantStr.replace(",", ".").trim());
                incident.setMontantErreur(montant);
            } catch (NumberFormatException e) {
                log.warn("Invalid montant value at line {}: {}", lineNumber, montantStr);
                incident.setMontantErreur(0.0);
            }
        } else {
            incident.setMontantErreur(0.0);
        }

        return incident;
    }

    private String determineIncidentType(Map<String, String> record) {
        // Récupérer les champs pertinents pour déterminer le type
        String errorCode = getFieldValue(record, "error_code", "code_erreur", "code").toLowerCase();
        String errorType = getFieldValue(record, "error_type", "type_erreur", "type").toLowerCase();
        String errorMessage = getFieldValue(record, "error_message", "message", "erreur").toLowerCase();

        // Logique de détection du type d'incident
        if (containsAny(errorCode + errorType + errorMessage, "dupl", "doublon", "duplicate")) {
            return "DOUBLON";
        } else if (containsAny(errorCode + errorType + errorMessage, "calc", "calcul", "computation")) {
            return "ERREUR_CALCUL";
        } else if (containsAny(errorCode + errorType + errorMessage, "incomp", "incomplete", "manqu")) {
            return "LIGNE_INCOMPLETE";
        } else if (containsAny(errorCode + errorType + errorMessage, "ref", "reference", "invalid")) {
            return "REFERENCE_INVALIDE";
        } else if (containsAny(errorCode + errorType + errorMessage, "montant", "amount", "prix")) {
            return "MONTANT_INCORRECT";
        } else if (containsAny(errorCode + errorType + errorMessage, "tva", "tax", "vat")) {
            return "ERREUR_TVA";
        } else {
            return "AUTRE";
        }
    }

    private String determinePriority(Map<String, String> record) {
        // Logique de détermination de la priorité basée sur le montant
        String montantStr = getFieldValue(record, "montant_erreur", "montant", "amount");

        double montant = 0.0;
        if (montantStr != null && !montantStr.isEmpty()) {
            try {
                montant = Double.parseDouble(montantStr.replace(",", ".").trim());
            } catch (NumberFormatException e) {
                log.debug("Cannot parse montant for priority: {}", montantStr);
            }
        }

        // Échelle de priorité basée sur le montant
        if (montant > 10000) {
            return "CRITIQUE";
        } else if (montant > 5000) {
            return "HAUTE";
        } else if (montant > 1000) {
            return "MOYENNE";
        } else {
            return "FAIBLE";
        }
    }

    private String buildDescription(Map<String, String> record) {
        StringBuilder description = new StringBuilder();

        description.append("Incident détecté dans le fichier rollback.\n\n");

        String errorCode = getFieldValue(record, "error_code", "code_erreur", "code");
        if (errorCode != null && !errorCode.isEmpty()) {
            description.append("Code erreur: ").append(errorCode).append("\n");
        }

        String errorMessage = getFieldValue(record, "error_message", "message", "erreur");
        if (errorMessage != null && !errorMessage.isEmpty()) {
            description.append("Message: ").append(errorMessage).append("\n");
        }

        String details = getFieldValue(record, "details", "description", "info");
        if (details != null && !details.isEmpty()) {
            description.append("\nDétails: ").append(details);
        }

        // Si aucune information n'est trouvée, description par défaut
        if (description.toString().equals("Incident détecté dans le fichier rollback.\n\n")) {
            description.append("Aucun détail supplémentaire disponible.");
        }

        return description.toString();
    }

    private boolean isValidIncident(IncidentDTO incident) {
        // Validation basique de l'incident
        return incident != null &&
                incident.getDescription() != null && !incident.getDescription().trim().isEmpty() &&
                incident.getIncidentType() != null && !incident.getIncidentType().isEmpty() &&
                incident.getLineNumber() != null && incident.getLineNumber() > 0;
    }

    private String getFieldValue(Map<String, String> record, String... possibleKeys) {
        // Essayer plusieurs clés possibles pour le même champ
        for (String key : possibleKeys) {
            if (record.containsKey(key)) {
                String value = record.get(key);
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
        }
        return "";
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean isProcessableFile(String filename) {
        if (filename == null) {
            return false;
        }
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".csv") ||
                lowerFilename.endsWith(".xlsx") ||
                lowerFilename.endsWith(".xls");
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unknown.csv";
        }
        // Remplacer les caractères non autorisés
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}