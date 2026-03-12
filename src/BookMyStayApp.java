import java.util.HashMap;
import java.util.Map;

public class BookMyStayApp {

    /**
     * Application entry point.
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        // UC 1:
        System.out.println("Welcome to the Hotel Booking Management System");
        System.out.println("System initialized successfully.");
        System.out.println("----------------------------------------------");

        // UC2: Room Initialization & Static Availability
        System.out.println("Hotel Room Initialization\n");

        // Static availability variables (Pre-data structure approach)
        int singleRoomAvailability = 5;
        int doubleRoomAvailability = 3;
        int suiteRoomAvailability = 2;

        // Creating room objects using Polymorphism
        Room single = new SingleRoom();
        Room doubleRm = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Displaying Details
        single.displayRoomDetails();
        System.out.println("Available: " + singleRoomAvailability + "\n");

        doubleRm.displayRoomDetails();
        System.out.println("Available: " + doubleRoomAvailability + "\n");

        suite.displayRoomDetails();
        System.out.println("Available: " + suiteRoomAvailability);

        // --- UC 3: Centralized Room Inventory Management ---
        System.out.println("Hotel Room Inventory Status (UC3 - HashMap)\n");

        // Initialize the centralized inventory
        RoomInventory inventory = new RoomInventory();

        // Use the helper method to display status from the HashMap
        displayStatus(single, inventory);
        displayStatus(doubleRm, inventory);
        displayStatus(suite, inventory);

    }
    /** Abstract Class - Room */
    abstract static class Room {
        protected int numberOfBeds;
        protected int squareFeet;
        protected double pricePerNight;

        public Room(int numberOfBeds, int squareFeet, double pricePerNight) {
            this.numberOfBeds = numberOfBeds;
            this.squareFeet = squareFeet;
            this.pricePerNight = pricePerNight;
        }

        public void displayRoomDetails() {
            System.out.println("Beds: " + numberOfBeds);
            System.out.println("Size: " + squareFeet + " sqft");
            System.out.println("Price per night: " + pricePerNight);
        }
    }

    /** Concrete Class - SingleRoom */
    static class SingleRoom extends Room {
        public SingleRoom() {
            super(1, 250, 1500.0);
            System.out.println("Single Room:");
        }
    }

    /** Concrete Class - DoubleRoom */
    static class DoubleRoom extends Room {
        public DoubleRoom() {
            super(2, 400, 2500.0);
            System.out.println("Double Room:");
        }
    }

    /** Concrete Class - SuiteRoom */
    static class SuiteRoom extends Room {
        public SuiteRoom() {
            super(3, 750, 5000.0);
            System.out.println("Suite Room:");
        }
    }
    /** UC3 Helper: Bridges Room and Inventory */
    private static void displayStatus(Room room, RoomInventory inventory) {
        room.displayRoomDetails();
        String type = room.getClass().getSimpleName();
        // Fetches from the centralized HashMap
        System.out.println("Inventory Count: " + inventory.getRoomAvailability().get(type));
        System.out.println();
    }

    /** UC3: Centralized Inventory Class */
    static class RoomInventory {
        private Map<String, Integer> roomAvailability;

        public RoomInventory() {
            roomAvailability = new HashMap<>();
            initializeInventory();
        }

        private void initializeInventory() {
            // Mapping room types to counts - This replaces scattered variables
            roomAvailability.put("SingleRoom", 5);
            roomAvailability.put("DoubleRoom", 3);
            roomAvailability.put("SuiteRoom", 2);
        }

        public Map<String, Integer> getRoomAvailability() {
            return roomAvailability;
        }
    }

}