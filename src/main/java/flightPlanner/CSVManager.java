package flightPlanner;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class CSVManager {
    private final Reader reader;
    private final char separator;
    private final char quoteChar;

    public CSVManager(Reader reader) {
        this(reader, ',', '\"');
    }

    public CSVManager(Reader reader, char separator, char quoteChar) {
        this.reader = reader;
        this.separator = separator;
        this.quoteChar = quoteChar;
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

    public void writeAll(List<String[]> records, String filePath) throws IOException {
        // Usare true per abilitare la modalità append
        // Blocco try-with-resources per gestire la chiusura automatica delle risorse
        try (FileWriter fileWriter = new FileWriter(filePath, false);
             CSVWriter csvWriter = new CSVWriter(fileWriter, separator, quoteChar, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            csvWriter.writeAll(records);
        }
    }

    // Metodo per aggiungere un singolo record al file CSV
    public void appendRecord(String[] record, String filePath) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filePath, true);
             CSVWriter csvWriter = new CSVWriter(fileWriter, separator, quoteChar, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            csvWriter.writeNext(record);
        }
    }
}
