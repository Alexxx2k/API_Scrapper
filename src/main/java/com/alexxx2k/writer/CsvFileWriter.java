package com.alexxx2k.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class CsvFileWriter implements ResponseFileWriter {
    private final File file;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> headers = new LinkedHashSet<>();
    private boolean headersWritten = false;

    public CsvFileWriter(File file) {
        this.file = file;
        file.getParentFile().mkdirs();
    }

    @Override
    public void writeResponse(String response) throws Exception {
        JsonNode node = mapper.readTree(response);

        if (node.isArray()) {
            for (JsonNode element : node) {
                processElement(element);
            }
        } else {
            processElement(node);
        }
    }

    private void processElement(JsonNode node) throws Exception {
        Map<String, String> row = new HashMap<>();
        extractFields(node, "", row);

        headers.addAll(row.keySet());

        try (FileWriter writer = new FileWriter(file, headersWritten)) {
            if (!headersWritten) {
                writer.write(String.join(",", headers) + "\n");
                headersWritten = true;
            }
            List<String> values = new ArrayList<>();
            for (String header : headers) {
                values.add(row.getOrDefault(header, ""));
            }
            writer.write(String.join(",", values) + "\n");
        }
    }

    private void extractFields(JsonNode node, String prefix, Map<String, String> row) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String key = prefix + entry.getKey();
                extractFields(entry.getValue(), key + ".", row);
            });
        } else if (node.isArray()) {
            List<String> values = new ArrayList<>();
            node.forEach(item -> values.add(item.asText()));
            row.put(prefix, String.join(";", values));
        } else {
            row.put(prefix, node.asText());
        }
    }

    @Override
    public String getFileType() {
        return "csv";
    }
}