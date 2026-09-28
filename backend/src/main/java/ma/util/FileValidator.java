package ma.util;

import lombok.extern.slf4j.Slf4j;
import ma.exception.InvalidFileException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class FileValidator {

    // Extensions autorisées
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "csv", "xlsx", "xls", "txt"
    );

    // Types MIME autorisés
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "text/csv",
            "text/plain",
            "application/csv",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/octet-stream" // Fallback pour certains navigateurs
    );

    // Taille maximale: 50 MB
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    // Taille minimale: 10 bytes (fichier non vide)
    private static final long MIN_FILE_SIZE = 10;

    // Magic bytes pour détecter les types de fichiers
    private static final byte[] CSV_MAGIC = null; // CSV n'a pas de magic bytes
    private static final byte[] XLSX_MAGIC = {0x50, 0x4B, 0x03, 0x04}; // PK.. (ZIP)
    private static final byte[] XLS_MAGIC = {(byte)0xD0, (byte)0xCF, 0x11, (byte)0xE0}; // Compound File

    /**
     * Valide un fichier uploadé
     */
    public void validateFile(MultipartFile file) {
        log.debug("Validating file: {}", file.getOriginalFilename());

        // Vérifier si le fichier est null
        if (file == null) {
            throw new InvalidFileException("Aucun fichier fourni");
        }

        // Vérifier si le fichier est vide
        if (file.isEmpty()) {
            throw new InvalidFileException("Le fichier est vide");
        }

        // Vérifier le nom du fichier
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new InvalidFileException("Nom de fichier invalide");
        }

        // Vérifier la taille
        validateFileSize(file.getSize(), originalFilename);

        // Vérifier l'extension
        validateFileExtension(originalFilename);

        // Vérifier le type MIME
        validateMimeType(file.getContentType(), originalFilename);

        // Vérifier le contenu (magic bytes)
        try {
            validateFileContent(file, originalFilename);
        } catch (IOException e) {
            log.error("Error validating file content: {}", e.getMessage());
            throw new InvalidFileException("Erreur lors de la validation du contenu du fichier");
        }

        log.info("File validation successful: {}", originalFilename);
    }

    /**
     * Valide la taille du fichier
     */
    private void validateFileSize(long fileSize, String filename) {
        if (fileSize < MIN_FILE_SIZE) {
            throw new InvalidFileException(
                    String.format("Le fichier '%s' est trop petit (taille: %d bytes)", filename, fileSize)
            );
        }

        if (fileSize > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    String.format("Le fichier '%s' est trop volumineux (taille: %d bytes, max: %d bytes)",
                            filename, fileSize, MAX_FILE_SIZE)
            );
        }
    }

    /**
     * Valide l'extension du fichier
     */
    private void validateFileExtension(String filename) {
        String extension = getFileExtension(filename);

        if (extension == null || extension.isEmpty()) {
            throw new InvalidFileException("Le fichier n'a pas d'extension");
        }

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileException(
                    String.format("Extension de fichier non supportée: '%s'. Extensions acceptées: %s",
                            extension, ALLOWED_EXTENSIONS)
            );
        }
    }

    /**
     * Valide le type MIME
     */
    private void validateMimeType(String mimeType, String filename) {
        if (mimeType == null || mimeType.isEmpty()) {
            log.warn("MIME type is null or empty for file: {}", filename);
            return; // Ne pas bloquer si le MIME type n'est pas fourni
        }

        // Vérifier si le MIME type est dans la liste autorisée
        boolean isAllowed = ALLOWED_MIME_TYPES.stream()
                .anyMatch(allowed -> mimeType.toLowerCase().contains(allowed.toLowerCase()));

        if (!isAllowed) {
            log.warn("Potentially invalid MIME type '{}' for file: {}", mimeType, filename);
            // Ne pas bloquer, juste logger (certains navigateurs envoient des MIME types incorrects)
        }
    }

    /**
     * Valide le contenu du fichier en vérifiant les magic bytes
     */
    private void validateFileContent(MultipartFile file, String filename) throws IOException {
        String extension = getFileExtension(filename);

        if (extension == null) {
            return;
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = new byte[8];
            int bytesRead = inputStream.read(header);

            if (bytesRead < 4) {
                throw new InvalidFileException("Le fichier est trop petit pour être valide");
            }

            switch (extension.toLowerCase()) {
                case "xlsx":
                    validateMagicBytes(header, XLSX_MAGIC, filename, "XLSX");
                    break;
                case "xls":
                    validateMagicBytes(header, XLS_MAGIC, filename, "XLS");
                    break;
                case "csv":
                case "txt":
                    // CSV/TXT n'ont pas de magic bytes, vérifier si c'est du texte
                    validateTextFile(header, filename);
                    break;
            }
        }
    }

    /**
     * Valide les magic bytes
     */
    private void validateMagicBytes(byte[] fileHeader, byte[] expectedMagic, String filename, String type) {
        if (expectedMagic == null) {
            return;
        }

        for (int i = 0; i < expectedMagic.length; i++) {
            if (fileHeader[i] != expectedMagic[i]) {
                throw new InvalidFileException(
                        String.format("Le fichier '%s' prétend être un %s mais n'a pas la signature correcte",
                                filename, type)
                );
            }
        }
    }

    /**
     * Valide qu'un fichier est bien du texte
     */
    private void validateTextFile(byte[] header, String filename) {
        // Vérifier que les premiers bytes sont des caractères imprimables ou des caractères de contrôle valides
        for (byte b : header) {
            // ASCII imprimable (32-126) ou caractères de contrôle courants (9=TAB, 10=LF, 13=CR)
            if ((b < 32 && b != 9 && b != 10 && b != 13) || b == 127) {
                throw new InvalidFileException(
                        String.format("Le fichier '%s' ne semble pas être un fichier texte valide", filename)
                );
            }
        }
    }

    /**
     * Extrait l'extension d'un nom de fichier
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return null;
        }

        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Vérifie si un fichier est un CSV valide (basé sur le nom)
     */
    public boolean isCsvFile(String filename) {
        String extension = getFileExtension(filename);
        return extension != null && extension.equalsIgnoreCase("csv");
    }

    /**
     * Vérifie si un fichier est un Excel valide (basé sur le nom)
     */
    public boolean isExcelFile(String filename) {
        String extension = getFileExtension(filename);
        return extension != null &&
                (extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls"));
    }

    /**
     * Vérifie si le nom de fichier contient des caractères dangereux
     */
    public void validateFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new InvalidFileException("Nom de fichier vide");
        }

        // Caractères interdits dans les noms de fichiers
        String[] forbiddenChars = {"/", "\\", ":", "*", "?", "\"", "<", ">", "|", "\0"};

        for (String forbidden : forbiddenChars) {
            if (filename.contains(forbidden)) {
                throw new InvalidFileException(
                        String.format("Le nom de fichier contient un caractère interdit: '%s'", forbidden)
                );
            }
        }

        // Vérifier path traversal
        if (filename.contains("..")) {
            throw new InvalidFileException("Le nom de fichier contient une séquence interdite (..)");
        }

        // Longueur maximale
        if (filename.length() > 255) {
            throw new InvalidFileException("Le nom de fichier est trop long (max 255 caractères)");
        }
    }

    /**
     * Sanitize un nom de fichier
     */
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "unknown";
        }

        // Remplacer les caractères dangereux par des underscores
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Valide que le fichier n'est pas corrompu (vérifications basiques)
     */
    public boolean isFileCorrupted(MultipartFile file) {
        try {
            // Essayer de lire les premiers bytes
            try (InputStream is = file.getInputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead = is.read(buffer);

                if (bytesRead < 0) {
                    return true; // Fichier vide ou illisible
                }
            }
            return false;
        } catch (IOException e) {
            log.error("Error checking file corruption: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Format la taille d'un fichier en format lisible
     */
    public String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Obtient la taille maximale autorisée
     */
    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    /**
     * Obtient les extensions autorisées
     */
    public List<String> getAllowedExtensions() {
        return ALLOWED_EXTENSIONS;
    }
}