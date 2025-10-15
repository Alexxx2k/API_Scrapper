package com.alexxx2k.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CsvFileWriter implements ResponseFileWriter {
    private final File file;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Set<String> allHeaders = new LinkedHashSet<>();
    private boolean headersWritten = false;

    public CsvFileWriter(File file) {
        this.file = file;
        file.getParentFile().mkdirs();
    }

    @Override
    public void writeResponse(String response) throws IOException {
        JsonNode node = mapper.readValue(response, JsonNode.class);

        if (node.isArray()) {
            processJsonArray(node);
        } else if (node.isObject()) {
            processJsonObject(node);
        } else {
            processPrimitiveValue(node);
        }
    }

    private void processJsonArray(JsonNode arrayNode) throws IOException {
        for (JsonNode element : arrayNode) {
            if (element.isObject()) {
                processJsonObject(element);
            } else if (element.isArray()) {
                processJsonArray(element);
            } else {
                processPrimitiveValue(element);
            }
        }
    }

    private void processJsonObject(JsonNode objectNode) throws IOException {
        Set<String> currentHeaders = new LinkedHashSet<>();
        Map<String, String> currentValues = new HashMap<>();

        collectFlatFields(objectNode, "", currentHeaders, currentValues);

        allHeaders.addAll(currentHeaders);

        if (!headersWritten) {
            writeHeaders();
            headersWritten = true;
        }

        // Записываем данные
        writeDataRow(currentValues);
    }

    private void processPrimitiveValue(JsonNode primitiveNode) throws IOException {
        Map<String, String> values = new HashMap<>();
        values.put("value", primitiveNode.asText());

        allHeaders.add("value");

        if (!headersWritten) {
            writeHeaders();
            headersWritten = true;
        }

        writeDataRow(values);
    }

    private void collectFlatFields(JsonNode node, String prefix, Set<String> headers, Map<String, String> values) {
        if (node.isObject()) {
            Iterator<String> fieldNames = node.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                JsonNode childNode = node.get(fieldName);
                String fullName = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;

                if (childNode.isObject()) {
                    collectFlatFields(childNode, fullName, headers, values);
                } else if (childNode.isArray()) {
                    String arrayValue = formatArrayValue(childNode);
                    headers.add(fullName);
                    values.put(fullName, arrayValue);
                } else {
                    headers.add(fullName);
                    values.put(fullName, childNode.asText());
                }
            }
        }
    }

    private String formatArrayValue(JsonNode arrayNode) {
        if (arrayNode.size() == 0) {
            return "";
        }

        List<String> values = new ArrayList<>();
        for (JsonNode element : arrayNode) {
            if (element.isObject()) {
                values.add(formatObjectBrief(element));
            } else {
                values.add(element.asText());
            }
        }

        return String.join("; ", values);
    }

    private String formatObjectBrief(JsonNode objectNode) {
        List<String> fields = new ArrayList<>();
        Iterator<String> fieldNames = objectNode.fieldNames();

        while (fieldNames.hasNext() && fields.size() < 3) {
            String fieldName = fieldNames.next();
            JsonNode valueNode = objectNode.get(fieldName);
            if (valueNode.isTextual() && valueNode.asText().length() < 50) {
                fields.add(fieldName + ":" + valueNode.asText());
            }
        }

        return "{" + String.join(",", fields) + "}";
    }

    private void writeHeaders() throws IOException {
        try (FileWriter writer = new FileWriter(file, false)) {
            List<String> headerList = new ArrayList<>(allHeaders);
            writer.append(String.join(",", escapeCsvValues(headerList))).append("\n");
        }
    }

    private void writeDataRow(Map<String, String> values) throws IOException {
        List<String> rowValues = new ArrayList<>();

        for (String header : allHeaders) {
            String value = values.getOrDefault(header, "");
            rowValues.add(value);
        }

        try (FileWriter writer = new FileWriter(file, true)) {
            writer.append(String.join(",", escapeCsvValues(rowValues))).append("\n");
        }
    }

    private List<String> escapeCsvValues(List<String> values) {
        List<String> escaped = new ArrayList<>();
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                escaped.add("");
            } else if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
                escaped.add("\"" + value.replace("\"", "\"\"") + "\"");
            } else {
                escaped.add(value);
            }
        }
        return escaped;
    }

    @Override
    public String getFileType() {
        return "csv";
    }
}