package com.alexxx2k.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CsvFileWriter implements ResponseFileWriter {
    private final File file;
    private final ObjectMapper mapper = new ObjectMapper();

    public CsvFileWriter(File file) {
        this.file = file;
        file.getParentFile().mkdirs();
    }

    @Override
    public void writeResponse(String response) throws IOException {
        JsonNode node = mapper.readValue(response, JsonNode.class);
        StringBuilder result = new StringBuilder();

        node.fields().forEachRemaining(entry -> {
            if (!result.isEmpty()) {
                result.append(", ");
            }
            if (entry.getValue().isArray()) {
                entry.getValue().forEach(field -> {
                    if (!result.isEmpty() && !result.toString().endsWith(", ")) {
                        result.append(", ");
                    }
                    result.append(field.asText());
                });
            } else {
                result.append(entry.getValue().asText());
            }
        });

        try (FileWriter writer = new FileWriter(file, true)) {
            if (!file.exists() || file.length() == 0) {
                StringBuilder header = new StringBuilder();
                node.fieldNames().forEachRemaining(field -> {
                    if (!header.isEmpty()) header.append(", ");
                    header.append(field);
                });
                writer.append(header).append("\n");
            }

            writer.append(result).append("\n");
            System.out.println("Written: " + result);
        }
    }

    @Override
    public String getFileType() {
        return "csv";
    }
}