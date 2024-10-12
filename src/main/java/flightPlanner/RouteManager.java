package flightPlanner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class RouteManager {
    private CSVManager csvManager;
    private List<Route> routes;
    private String csvFilePath = "csv/routes.csv";

    public RouteManager() throws IOException {
        // Carica il file CSV dal classpath
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);
        if (inputStream == null) {
            throw new FileNotFoundException("File not found in resources: " + csvFilePath);
        }
        this.csvManager = new CSVManager(new InputStreamReader(inputStream));
        this.routes = new ArrayList<>();
        loadRoutes();
    }

    public void addRoute(Route route) throws IOException {
        for (Route existingRoute : routes) {
            if (existingRoute.getRouteId().equalsIgnoreCase(route.getRouteId())) {
                throw new IllegalArgumentException("Route " + route.getRouteId() + " already exists.");
            }
        }
        routes.add(route);
        String[] record = {
                route.getRouteId(),
                route.getDepartureAirportCode(),
                route.getArrivalAirportCode(),
                String.valueOf(route.getDistance())
        };
        try {
            csvManager.appendRecord(record, csvFilePath);
        } catch (IOException e) {
            System.out.println("Error details:");
            e.printStackTrace();
            throw new IOException("An error occurred while writing on a file CSV", e);
        }
    }

    public void removeRoute(String routeId) throws IOException {
        Route toRemove = null;
        for (Route route : routes) {
            if (route.getRouteId().equalsIgnoreCase(routeId)) {
                toRemove = route;
                break;
            }
        }
        if (toRemove != null) {
            routes.remove(toRemove);
            saveAllRoutes();
        } else {
            System.out.println("Route " + routeId +" not found.");
        }
    }

    public List<Route> getAllRoutes() {
        return routes;
    }

    public Route getRouteById(String routeId) {
        for (Route route : routes) {
            if (route.getRouteId().equalsIgnoreCase(routeId)) {
                return route;
            }
        }
        return null;
    }

    public List<Route> searchRoutes(String originCode, String destinationCode) {
        List<Route> result = new ArrayList<>();
        for (Route route : routes) {
            if (route.getDepartureAirportCode().equalsIgnoreCase(originCode) && route.getArrivalAirportCode().equalsIgnoreCase(destinationCode)) {
                result.add(route);
            }
        }
        return result;
    }

    private void loadRoutes() throws IOException {
        List<String[]> records = csvManager.readAll();
        // Salta l'header
        for (int i = 1; i < records.size(); i++) {
            String[] record = records.get(i);
            Route route = new Route(
                    record[0],
                    record[1],
                    record[2],
                    Double.parseDouble(record[3])
            );
            routes.add(route);
        }
    }

    public void updateRoute(Route updatedRoute) throws IOException {
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            if (route.getRouteId().equalsIgnoreCase(updatedRoute.getRouteId())) {
                routes.set(i, updatedRoute);
                break;
            }
        }
        saveAllRoutes();
    }

    private void saveAllRoutes() throws IOException {
        List<String[]> records = new ArrayList<>();
        // Header
        records.add(new String[]{"routeId", "departureAirportCode", "destinationAirportCode", "distance"});
        // Dati
        for (Route route : routes) {
            records.add(new String[]{
                    route.getRouteId(),
                    route.getDepartureAirportCode(),
                    route.getArrivalAirportCode(),
                    String.valueOf(route.getDistance())
            });
        }
        try {
            csvManager.writeAll(records, csvFilePath);
        }catch (IOException e) {
            System.err.println("An error occurred while saving on file CSV: " + e.getMessage());
            System.out.println("Error details:");
            e.printStackTrace();
        }
    }
}
