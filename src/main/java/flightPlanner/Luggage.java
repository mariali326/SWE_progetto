package flightPlanner;

public class Luggage {
    // Dimensioni gratis massime e peso gratis massimo del bagaglio a mano (fisso per tutte le classi)
    public static final double MAX_LENGTH_CABIN = 40;// In centimetri
    public static final double MAX_WIDTH_CABIN = 30;
    public static final double MAX_HEIGHT_CABIN = 20;
    public static final double MAX_WEIGHT_CABIN = 8;
    // Costo extra per bagagli oversize
    public static final double EXTRA_COST = 50;
    private final String luggageId;
    private final String type; // da stiva o a mano
    private final String ticketNumber;
    private final double weight; //In kg
    private final double cost;
    private final double length; // In cm
    private final double width;
    private final double height;

    public Luggage(String luggageId, double weight, String type, double cost, double length, double width, double height, String ticketNumber) {
        this.luggageId = luggageId;
        this.weight = weight;
        this.type = type;
        this.cost = cost;
        this.length = length;
        this.width = width;
        this.height = height;
        this.ticketNumber = ticketNumber;
    }

    // Dimensioni massime e peso massimo gratis per bagagli a stiva in base alle tre classi
    public static double getMaxLengthHold(String classType) {
        return switch (classType) {
            case "Economy" -> 50;
            case "Business" -> 60;
            case "First" -> 70;
            default -> 0;
        };
    }

    public static double getMaxWidthHold(String classType) {
        return switch (classType) {
            case "Economy" -> 40;
            case "Business" -> 50;
            case "First" -> 60;
            default -> 0;
        };
    }

    public static double getMaxHeightHold(String classType) {
        return switch (classType) {
            case "Economy" -> 20;
            case "Business" -> 25;
            case "First" -> 30;
            default -> 0;
        };
    }

    public static double getMaxWeightHold(String classType) {
        return switch (classType) {
            case "Economy" -> 23;
            case "Business" -> 32;
            case "First" -> 34;
            default -> 0;
        };
    }

    public String getLuggageId() {
        return luggageId;
    }

    public double getWeight() {
        return weight;
    }

    public String getType() {
        return type;
    }

    public double getCost() {
        return cost;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getLength() {
        return length;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    @Override
    public String toString() {
        return type + " luggage: " + length + "x" + width + "x" + height + ", " + weight + "kg, Cost: " + cost + " EUR";
    }
}
