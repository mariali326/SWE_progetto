package flightPlanner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class AirportManager {
    private CSVManager csvManager;
    private List<Airport> airports;
    private String csvFilePath = "csv/airports.csv";

    public AirportManager() throws IOException {
        // Carica il file CSV dal classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.airports = new ArrayList<>();
        loadAirports();
    }

    private void loadAirports() throws IOException {
        List<String[]> records = csvManager.readAll();
        // Salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            Airport airport = new Airport(
                    record[0],
                    record[1],
                    record[2],
                    record[3]
            );
            airports.add(airport);
        }
    }

    public void addAirport(Airport airport) throws IOException {
        airports.add(airport);
        String[] record = {airport.getCode(), airport.getName(), airport.getCity(), airport.getCountry()};
        csvManager.appendRecord(record);
    }

    public void removeAirport(String code) throws IOException {
        Airport toRemove = null;
        for (Airport airport : airports) {
            if (airport.getCode().equalsIgnoreCase(code)) {
                toRemove = airport;
                break;
            }
        }
        if (toRemove != null) {
            airports.remove(toRemove);
            saveAllAirports();
        }
    }

    public void updateAirport(Airport updatedAirport) throws IOException {
        for (int i = 0; i < airports.size(); i++) {
            Airport airport = airports.get(i);
            if (airport.getCode().equalsIgnoreCase(updatedAirport.getCode())) {
                airports.set(i, updatedAirport);
                break;
            }
        }
        saveAllAirports();
    }

    private void saveAllAirports() throws IOException {
        List<String[]> records = new ArrayList<>();
        // Header
        records.add(new String[]{"code", "name", "city", "country"});
        // Dati da aggiungere
        for (Airport airport : airports) {
            records.add(new String[]{
                    airport.getCode(),
                    airport.getName(),
                    airport.getCity(),
                    airport.getCountry()
            });
        }
        csvManager.writeAll(records);
    }

    public Airport getAirportByCode(String code) {
        for (Airport airport : airports) {
            if (airport.getCode().equalsIgnoreCase(code)) {
                return airport;
            }
        }
        return null;
    }

    public List<Airport> getAllAirports() {
        return airports;
    }
}
