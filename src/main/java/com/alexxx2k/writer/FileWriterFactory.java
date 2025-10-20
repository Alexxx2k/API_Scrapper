package com.alexxx2k.writer;

import java.io.File;

public class FileWriterFactory {
    public static ResponseFileWriter createFileWriter(boolean isCsvFormat) {
        File outputFile = isCsvFormat
                ? new File("output/output.csv")
                : new File("output/output.json");

        return isCsvFormat
                ? new CsvFileWriter(outputFile)
                : new JsonFileWriter(outputFile);
    }
}