import java.util.HashMap;
import java.util.Map;

/**
 * ========================================================
 * MAIN CLASS - BookMyStayApp
 * ========================================================
 * Integrated Use Cases 1, 2, 3, and 4
 * Includes: Welcome, Initialization, Inventory (HashMap), and Search
 * @version 4.0
 */
public class BookMyStayApp {

    public static void main(String[] args) {
        // --- UC 1: Application Entry ---
        System.out.println("Welcome to the Hotel Booking Management System");
        System.out.println("System initialized successfully.");
        System.out.println("----------------------------------------------");

        // --- UC 2: Room Initialization & Static Availability ---
        System.out.println("Hotel Room Initialization (UC2)\n");

        // Static availability for UC2 demonstration
        int singleRoomAvailability = 5;
        int doubleRoomAvailability = 3;
        int suiteRoomAvailability = 2;

        // Creating room objects using Polymorphism
        Room single = new SingleRoom();
        Room doubleRm = new DoubleRoom();
        Room suite = new SuiteRoom();

        single.displayRoomDetails();
        System.out.println("Available: " + singleRoomAvailability + "\n");

        doubleRm.displayRoomDetails();
        System.out.println("Available: " + doubleRoomAvailability + "\n");

        suite.displayRoomDetails();
        System.out.println("Available: " + suiteRoomAvailability);
        System.out.println("----------------------------------------------");

        // --- UC 3: Centralized Room Inventory Management ---
        System.out.println("Hotel Room Inventory Status (UC3 - HashMap)\n");

        // Initialize the centralized inventory (Single Source of Truth)
        RoomInventory inventory = new RoomInventory();

        // Use the UC3 helper method to display status from the HashMap
        displayStatus(single, inventory);
        displayStatus(doubleRm, inventory);
        displayStatus(suite, inventory);
        System.out.println("----------------------------------------------");

        // --- UC 4: Room Search & Availability Check ---
        System.out.println("Room Search Results (UC4 - Read Only)\n");

        // Initialize Search Service
        RoomSearchService searchService = new RoomSearchService();

        // Perform search using the centralized inventory and room objects
        searchService.searchAvailableRooms(inventory, single, doubleRm, suite);
    }

    // ========================================================
    // HELPER METHODS
    // ========================================================

    /** UC3 Helper: Bridges Room and Inventory */
    private static void displayStatus(Room room, RoomInventory inventory) {
        room.displayRoomDetails();
        String type = room.getClass().getSimpleName();
        System.out.println("Inventory Count: " + inventory.getRoomAvailability().get(type));
        System.out.println();
    }

    // ========================================================
    // NESTED STATIC CLASSES
    // ========================================================

    /** UC2: Abstract Room Class */
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
            System.out.println("Price per night: Rs." + pricePerNight);
        }
    }

    /** Concrete Room Types */
    static class SingleRoom extends Room {
        public SingleRoom() { super(1, 250, 1500.0); System.out.println("Single Room:"); }
    }

    static class DoubleRoom extends Room {
        public DoubleRoom() { super(2, 400, 2500.0); System.out.println("Double Room:"); }
    }

    static class SuiteRoom extends Room {
        public SuiteRoom() { super(3, 750, 5000.0); System.out.println("Suite Room:"); }
    }

    /** UC3: Centralized Inventory Class */
    static class RoomInventory {
        private Map<String, Integer> roomAvailability;

        public RoomInventory() {
            roomAvailability = new HashMap<>();
            initializeInventory();
        }

        private void initializeInventory() {
            roomAvailability.put("SingleRoom", 5);
            roomAvailability.put("DoubleRoom", 3);
            roomAvailability.put("SuiteRoom", 2);
        }

        public Map<String, Integer> getRoomAvailability() {
            return roomAvailability;
        }
    }

    /** UC4: Room Search Service (Read-Only) */
    static class RoomSearchService {
        public void searchAvailableRooms(
                RoomInventory inventory,
                Room singleRoom,
                Room doubleRoom,
                Room suiteRoom) {

            Map<String, Integer> availability = inventory.getRoomAvailability();

            // Logic: Only display if availability is > 0
            if (availability.getOrDefault("SingleRoom", 0) > 0) {
                System.out.println("[Search Hit] Single Room:");
                singleRoom.displayRoomDetails();
                System.out.println("Current Availability: " + availability.get("SingleRoom") + "\n");
            }

            if (availability.getOrDefault("DoubleRoom", 0) > 0) {
                System.out.println("[Search Hit] Double Room:");
                doubleRoom.displayRoomDetails();
                System.out.println("Current Availability: " + availability.get("DoubleRoom") + "\n");
            }

            if (availability.getOrDefault("SuiteRoom", 0) > 0) {
                System.out.println("[Search Hit] Suite Room:");
                suiteRoom.displayRoomDetails();
                System.out.println("Current Availability: " + availability.get("SuiteRoom") + "\n");
            }
        }
    }
}