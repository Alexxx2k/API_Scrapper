package com.alexxx2k.writer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;

public class JsonFileWriter implements ResponseFileWriter {
    private final File file;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonFileWriter(File file) {
        this.file = file;
        file.getParentFile().mkdirs();
    }

    @Override
    public void writeResponse(String response) throws IOException {
        ArrayNode array = file.exists() && file.length() > 0
                ? (ArrayNode) mapper.readTree(file) : mapper.createArrayNode();

        JsonNode jsonNode = mapper.readTree(response);
        array.add(jsonNode);

        mapper.writerWithDefaultPrettyPrinter().writeValue(file, array);
    }

    @Override
    public String getFileType() {
        return "json";
    }
}