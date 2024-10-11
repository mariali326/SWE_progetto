package flightPlanner;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.*;
import java.util.List;

//Da riguardare
public class CSVManager {
    private Reader reader;
    private Writer writer;
    //private String filePath;
    private char separator;
    private char quoteChar;

    public CSVManager(Reader reader) {
        this(reader, ',', '\"');
    }

    public CSVManager(Reader reader, char separator, char quoteChar) {
        this.reader = reader;
        this.separator = separator;
        this.quoteChar = quoteChar;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public List<String[]> readAll() throws IOException {
        List<String[]> records;
        try (CSVReader csvReader = new CSVReader(reader)) {
            records = csvReader.readAll();
        } catch (CsvException e) {
            throw new RuntimeException(e);
        }
        return records;
    }

    public void writeAll(List<String[]> records) throws IOException {
        try (CSVWriter csvWriter = new CSVWriter(writer, separator, quoteChar, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            csvWriter.writeAll(records);
        }
    }

    // Metodo per aggiungere un singolo record al file CSV
    public void appendRecord(String[] record) throws IOException {
        if (writer == null) {
            throw new IllegalStateException("Writer not set. Please set a writer before appending.");
        }
        try (CSVWriter csvWriter = new CSVWriter(writer, separator, quoteChar, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            csvWriter.writeNext(record);
        }
    }


}
