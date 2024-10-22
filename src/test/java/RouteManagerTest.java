import flightPlanner.Route;
import flightPlanner.RouteManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RouteManagerTest {

    public static RouteManager routeManager;

    @BeforeAll
    public static void setUp() throws IOException {
        routeManager = new RouteManager();
    }

    @Test
    @DisplayName("Text that checks loading routes from the CSV file and verifies that it's possible to find a route listed in the file")
    public void testLoadRoutesFromCSV() {
        List<Route> routes = routeManager.getAllRoutes();

        assertNotNull(routes);
        assertFalse(routes.isEmpty());

        Route route = routeManager.getRouteById("R001");
        assertEquals("R001", route.getRouteId());
    }

    @Test
    @DisplayName("Test that checks if adding a route works correctly and that the CSV file is updated after addition")
    public void testAddRoute() throws IOException {
        Route newRoute = new Route("R010", "FLR", "PEK", 14547, Route.parseDuration("11h45m"));

        routeManager.addRoute(newRoute);

        Route addedRoute = routeManager.getRouteById("R010");
        assertNotNull(addedRoute);
        assertEquals("FLR", addedRoute.getDepartureAirportCode());
        assertEquals("PEK", addedRoute.getArrivalAirportCode());
    }

    @Test
    @DisplayName("Test that checks it's impossible to add the same route more than once")
    public void testAddDuplicateRoute() throws IOException {
        Route newRoute = new Route("R011", "FLR", "LHR", 1498, Route.parseDuration("2h25m"));

        routeManager.addRoute(newRoute);
        assertThrows(IllegalArgumentException.class, () -> routeManager.addRoute(newRoute));
    }

    @Test
    @DisplayName("Test that checks removing a route from the CSV file works correctly")
    public void testRemoveRoute() throws IOException {
        String routeId = "R002";

        routeManager.removeRoute(routeId);

        Route removedRoute = routeManager.getRouteById(routeId);
        assertNull(removedRoute);
    }

    @Test
    @DisplayName("Test that checks that updating a route status works correctly")
    public void testUpdateRoute() throws IOException {
        Route updateRoute = new Route("R005", "FLR", "PVG", 14047, Route.parseDuration("16h30"));
        routeManager.updateRoute(updateRoute);

        Route updated = routeManager.getRouteById("R005");
        assertEquals("16h30m", updated.getFormattedDuration());
    }
}

