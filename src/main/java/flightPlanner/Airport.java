package flightPlanner;

public class Airport {
    private String code;// es.FLR
    private String name;// es. aeroporto di Firenze-Peretola
    private String city;
    private String country;

    public Airport(String code, String name, String city, String country) {
        this.code = code;
        this.name = name;
        this.city = city;
        this.country = country;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return code + " - " + name + ", " + city + ", " + country;
    }
}
