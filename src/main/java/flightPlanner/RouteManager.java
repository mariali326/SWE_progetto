package flightPlanner;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;


public class RouteManager {
    private CSVManager csvManager;
    private List<Route> routes;

    public RouteManager(String csvFilePath) throws IOException {
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
        routes.add(route);
        String[] record = {
                route.getRouteId(),
                route.getDepartureAirportCode(),
                route.getArrivalAirportCode(),
                String.valueOf(route.getDistance())
        };
        csvManager.appendRecord(record);
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
        csvManager.writeAll(records);
    }
}
