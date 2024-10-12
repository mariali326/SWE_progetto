package flightPlanner;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            if (record.length >= 4) {
                Airport airport = new Airport(
                        record[0],
                        record[1],
                        record[2],
                        record[3]
                );
                airports.add(airport);
            } else {
                System.err.println("Invalid format: " + Arrays.toString(record));
            }
        }
    }

    public void addAirport(Airport airport) throws IOException {
        for (Airport existingAirport : airports) {
            if (existingAirport.getCode().equalsIgnoreCase(airport.getCode())) {
                throw new IllegalArgumentException("Airport " + airport.getCode() + " already exists.");
            }
        }
        airports.add(airport);
        String[] record = {airport.getCode(), airport.getName(), airport.getCity(), airport.getCountry()};
        try {
            csvManager.appendRecord(record, csvFilePath);
        } catch (IOException e) {
            System.out.println("Error details:");
            e.printStackTrace();
            throw new IOException("An error occurred while writing an airport on file CSV", e);
        }
    }

    public void removeAirport(String code) {
        Airport toRemove = null;
        for (Airport airport : airports) {
            if (airport.getCode().equalsIgnoreCase(code)) {
                toRemove = airport;
                break;
            } else {
                System.out.println("Airport " + code +" not found.");
            }
        }
        if (toRemove != null) {
            airports.remove(toRemove);
            saveAllAirports();
        }
    }

    public void updateAirport(Airport updatedAirport) {
        for (int i = 0; i < airports.size(); i++) {
            Airport airport = airports.get(i);
            if (airport.getCode().equalsIgnoreCase(updatedAirport.getCode())) {
                airports.set(i, updatedAirport);
                break;
            }
        }
        saveAllAirports();
    }

    private void saveAllAirports() {
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
        }catch (IOException e) {
            System.err.println("An error occurred while saving airports on file CSV: " + e.getMessage());
            System.out.println("Error details:");
            e.printStackTrace();
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
