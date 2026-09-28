package ma.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
@Slf4j
public class CsvParser {

    private static final String BOM = "\uFEFF"; // Byte Order Mark UTF-8

    public List<Map<String, String>> parseCsv(String filePath) throws IOException {
        log.debug("Parsing CSV file: {}", filePath);

        List<Map<String, String>> records = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
                .withSkipLines(0)
                .build()) {

            List<String[]> allRows = reader.readAll();

            if (allRows.isEmpty()) {
                log.warn("CSV file is empty: {}", filePath);
                return records;
            }

            // Première ligne = headers
            String[] headers = allRows.get(0);

            // Nettoyer les headers
            headers = cleanHeaders(headers);

            log.debug("CSV Headers: {}", Arrays.toString(headers));

            // Parser les données (à partir de la ligne 2)
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);

                // Ignorer les lignes vides
                if (isEmptyRow(row)) {
                    log.debug("Skipping empty row at line {}", i + 1);
                    continue;
                }

                Map<String, String> record = new LinkedHashMap<>();

                // Mapper chaque colonne
                for (int j = 0; j < headers.length; j++) {
                    String value = (j < row.length) ? row[j] : "";
                    record.put(headers[j], value != null ? value.trim() : "");
                }

                records.add(record);
            }

            log.info("Successfully parsed {} records from CSV (total lines: {})",
                    records.size(), allRows.size() - 1);

        } catch (CsvException e) {
            log.error("Error parsing CSV file: {}", e.getMessage(), e);
            throw new IOException("Erreur de parsing CSV: " + e.getMessage(), e);
        }

        return records;
    }

    private String[] cleanHeaders(String[] headers) {
        String[] cleanedHeaders = new String[headers.length];

        for (int i = 0; i < headers.length; i++) {
            String header = headers[i];

            if (header == null || header.trim().isEmpty()) {
                cleanedHeaders[i] = "column_" + i;
                continue;
            }

            // Supprimer le BOM si présent
            if (header.startsWith(BOM)) {
                header = header.substring(BOM.length());
            }

            // Nettoyer le header
            header = header.trim()
                    .toLowerCase()
                    .replace(" ", "_")
                    .replace("-", "_")
                    .replace(".", "_")
                    .replaceAll("[^a-z0-9_]", "");

            cleanedHeaders[i] = header;
        }

        return cleanedHeaders;
    }

    private boolean isEmptyRow(String[] row) {
        if (row == null || row.length == 0) {
            return true;
        }

        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Parse un fichier CSV avec un délimiteur personnalisé
     */
    public List<Map<String, String>> parseCsvWithDelimiter(String filePath, char delimiter) throws IOException {
        log.debug("Parsing CSV file with delimiter '{}': {}", delimiter, filePath);

        List<Map<String, String>> records = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(filePath))
                .withCSVParser(new com.opencsv.CSVParserBuilder()
                        .withSeparator(delimiter)
                        .build())
                .build()) {

            List<String[]> allRows = reader.readAll();

            if (allRows.isEmpty()) {
                return records;
            }

            String[] headers = cleanHeaders(allRows.get(0));

            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);

                if (isEmptyRow(row)) {
                    continue;
                }

                Map<String, String> record = new LinkedHashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    String value = (j < row.length) ? row[j] : "";
                    record.put(headers[j], value != null ? value.trim() : "");
                }

                records.add(record);
            }

        } catch (CsvException e) {
            throw new IOException("Erreur de parsing CSV: " + e.getMessage(), e);
        }

        return records;
    }

    /**
     * Détecter automatiquement l'encodage du fichier
     */
    public String detectEncoding(String filePath) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));

            // Détecter UTF-8 BOM
            if (bytes.length >= 3 &&
                    bytes[0] == (byte)0xEF &&
                    bytes[1] == (byte)0xBB &&
                    bytes[2] == (byte)0xBF) {
                return "UTF-8";
            }

            // Détecter UTF-16 BE BOM
            if (bytes.length >= 2 &&
                    bytes[0] == (byte)0xFE &&
                    bytes[1] == (byte)0xFF) {
                return "UTF-16BE";
            }

            // Détecter UTF-16 LE BOM
            if (bytes.length >= 2 &&
                    bytes[0] == (byte)0xFF &&
                    bytes[1] == (byte)0xFE) {
                return "UTF-16LE";
            }

            // Par défaut, UTF-8
            return StandardCharsets.UTF_8.name();

        } catch (IOException e) {
            log.warn("Failed to detect encoding, using UTF-8: {}", e.getMessage());
            return StandardCharsets.UTF_8.name();
        }
    }
}
