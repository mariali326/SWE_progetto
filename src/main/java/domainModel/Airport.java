package domainModel;

public class Airport {
    private final String code;// es.FLR
    private final String city;
    private final String country;
    private String name;// es. aeroporto di Firenze-Peretola

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

    public void setName(String name) {
        this.name = name;
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
