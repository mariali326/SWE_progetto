package flightPlanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LuggageManager {
    private static final Log log = LogFactory.getLog(LuggageManager.class);
    private final CSVManager csvManager;
    private final List<Luggage> luggageList;
    private final String csvFilePath = "csv/luggage.csv";

    public LuggageManager() throws IOException {
        // Viene caricato il file CSV dal classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.luggageList = loadLuggage();
    }

    private List<Luggage> loadLuggage() throws IOException {
        List<String[]> records = csvManager.readAll();
        List<Luggage> luggageList = new ArrayList<>();
        // Si salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);

            Luggage luggage = new Luggage(
                    record[0],
                    Double.parseDouble(record[6]),
                    record[2],
                    Double.parseDouble(record[7]),
                    Double.parseDouble(record[3]),
                    Double.parseDouble(record[4]),
                    Double.parseDouble(record[5]),
                    record[1]
            );
            luggageList.add(luggage);
        }
        return luggageList;
    }

    public void addLuggage(Luggage luggage) throws IOException {
        for (Luggage existingLuggage : luggageList) {
            if (existingLuggage.getLuggageId().equalsIgnoreCase(luggage.getLuggageId())) {
                throw new IllegalArgumentException("The luggage " + luggage.getLuggageId() + " already exists.");
            }
        }
        luggageList.add(luggage);

        String[] record = {
                luggage.getLuggageId(),
                luggage.getTicketNumber(),
                luggage.getType(),
                String.valueOf(luggage.getLength()),
                String.valueOf(luggage.getWidth()),
                String.valueOf(luggage.getHeight()),
                String.valueOf(luggage.getWeight()),
                String.valueOf(luggage.getCost())

        };
        try {
            csvManager.appendRecord(record, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while writing a luggage to the CSV file", e);
            throw e;
        }
    }

    public void removeLuggage(String luggageId) throws IOException {
        Luggage toRemove = null;
        for (Luggage luggage : luggageList) {
            if (luggage.getLuggageId().equalsIgnoreCase(luggageId)) {
                toRemove = luggage;
                break;
            }
        }
        if (toRemove != null) {
            luggageList.remove(toRemove);
            System.out.println("Luggage " + luggageId + " removed.");
            saveAllLuggage();
        } else {
            System.out.println("Luggage " + luggageId + " not found.");
        }
    }

    private void saveAllLuggage() throws IOException {
        List<String[]> records = new ArrayList<>();
        // Header
        records.add(new String[]{"luggageId", "ticketNumber", "luggageType", "length", "width", "height", "weight", "cost"});
        // Dati
        for (Luggage luggage : luggageList) {
            records.add(new String[]{
                    luggage.getLuggageId(),
                    luggage.getTicketNumber(),
                    luggage.getType(),
                    String.valueOf(luggage.getLength()),
                    String.valueOf(luggage.getWidth()),
                    String.valueOf(luggage.getHeight()),
                    String.valueOf(luggage.getWeight()),
                    String.valueOf(luggage.getCost())
            });
        }
        try {
            csvManager.writeAll(records, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while saving luggage to the CSV file: " + e.getMessage());
            throw e;
        }
    }

    public List<Luggage> getAllLuggage() {
        return luggageList;
    }

    public Luggage getLuggageById(String luggageId) {
        for (Luggage luggage : luggageList) {
            if (luggage.getLuggageId().equalsIgnoreCase(luggageId)) {
                return luggage;
            }
        }
        return null;
    }
}
