package flightPlanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AirportManager {
    private static final Log log = LogFactory.getLog(AirportManager.class);
    private CSVManager csvManager;
    private List<Airport> airports;
    private String csvFilePath = "csv/airports.csv";

    public AirportManager() throws IOException {
        // Viene caricato il file CSV dal classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.airports = loadAirports();
    }

    private List<Airport> loadAirports() throws IOException {
        List<String[]> records = csvManager.readAll();
        List<Airport> airports = new ArrayList<>();
        // Si salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            if (record.length >= 4) {
                Airport airport = new Airport(
                        record[0],
                        record[1],
                        record[2],
                        record[3]
                );
                airports.add(airport);
            } else {
                System.err.println("Invalid row format: " + Arrays.toString(record));
            }
        }
        return airports;
    }

    public void addAirport(Airport airport) throws IOException {
        for (Airport existingAirport : airports) {
            if (existingAirport.getCode().equalsIgnoreCase(airport.getCode())) {
                throw new IllegalArgumentException("Airport " + airport.getCode() + " already exists.");
            }
        }
        airports.add(airport);
        System.out.println("Added Airport: " + airport);
        String[] record = {airport.getCode(), airport.getName(), airport.getCity(), airport.getCountry()};
        try {
            csvManager.appendRecord(record, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while writing an airport on file CSV", e);
            throw e;
        }
    }

    public void removeAirport(String code) throws IOException {
        Airport toRemove = null;
        for (Airport airport : airports) {
            if (airport.getCode().equalsIgnoreCase(code)) {
                toRemove = airport;
                break;
            } else {
                System.out.println("Airport " + code + " not found.");
            }
        }
        if (toRemove != null) {
            airports.remove(toRemove);
            System.out.println("Airport " + code + " removed.");
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
        try {
            csvManager.writeAll(records, csvFilePath);
        } catch (IOException e) {
            log.error("An error occurred while saving airports on file CSV: " + e.getMessage());
            throw e;
        }
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
