import java.util.*;

public class BookMyStayApp {

    public static void main(String[] args) {
        // --- UC 1: Application Entry ---
        System.out.println("Welcome to the Hotel Booking Management System");
        System.out.println("System initialized successfully.");
        System.out.println("----------------------------------------------");

        // --- UC 2: Room Initialization ---
        System.out.println("Hotel Room Initialization (UC2)...\n");
        Room single = new SingleRoom();
        Room doubleRm = new DoubleRoom();
        Room suite = new SuiteRoom();
        System.out.println("----------------------------------------------");

        // --- UC 3: Centralized Inventory ---
        System.out.println("Centralizing Inventory in HashMap (UC3)...\n");
        RoomInventory inventory = new RoomInventory();
        displayStatus(single, inventory);
        displayStatus(doubleRm, inventory);
        displayStatus(suite, inventory);
        System.out.println("----------------------------------------------");

        // --- UC 4: Room Search (Read-Only) ---
        System.out.println("Room Search Results for Guest (UC4):\n");
        RoomSearchService searchService = new RoomSearchService();
        searchService.searchAvailableRooms(inventory, single, doubleRm, suite);
        System.out.println("----------------------------------------------");

        // --- UC 5: Booking Request Queue (FIFO) ---
        System.out.println("Guest Submitting Booking Requests (UC5)...\n");
        BookingRequestQueue bookingQueue = new BookingRequestQueue();
        bookingQueue.addRequest(new Reservation("Guest_1", "SingleRoom"));
        bookingQueue.addRequest(new Reservation("Guest_2", "DoubleRoom"));
        bookingQueue.addRequest(new Reservation("Guest_3", "SingleRoom"));
        System.out.println("----------------------------------------------");

        // --- UC 6: Reservation Confirmation & Allocation ---
        System.out.println("Processing Allocations & Preventing Double Booking (UC6)...\n");
        RoomAllocationService allocationService = new RoomAllocationService();
        allocationService.processAllocations(bookingQueue, inventory);

        System.out.println("----------------------------------------------");
        System.out.println("Final System State (All Use Cases Complete):");
        displayStatus(single, inventory);
        displayStatus(doubleRm, inventory);
        displayStatus(suite, inventory);
    }

    /** Helper for display logic */
    private static void displayStatus(Room room, RoomInventory inventory) {
        String type = room.getClass().getSimpleName();
        room.displayRoomDetails();
        System.out.println("Availability: " + inventory.getRoomAvailability().get(type) + "\n");
    }

    // ========================================================
    // UC 2: Domain Model (Inheritance & Polymorphism)
    // ========================================================
    abstract static class Room {
        protected int beds;
        protected double price;
        public Room(int beds, double price) { this.beds = beds; this.price = price; }
        public void displayRoomDetails() {
            System.out.println("Beds: " + beds + " | Price per night: Rs." + price);
        }
    }

    static class SingleRoom extends Room { public SingleRoom() { super(1, 1500.0); System.out.println("Single Room Object Created."); } }
    static class DoubleRoom extends Room { public DoubleRoom() { super(2, 2500.0); System.out.println("Double Room Object Created."); } }
    static class SuiteRoom extends Room { public SuiteRoom() { super(3, 5000.0); System.out.println("Suite Room Object Created."); } }

    // ========================================================
    // UC 5: Reservation intent (Data Object)
    // ========================================================
    static class Reservation {
        private String guestName;
        private String roomType;
        public Reservation(String name, String type) { this.guestName = name; this.roomType = type; }
        public String getGuestName() { return guestName; }
        public String getRoomType() { return roomType; }
        @Override public String toString() { return guestName + " requested " + roomType; }
    }

    // ========================================================
    // SERVICES & DATA STRUCTURES (UC 3, 4, 5, 6)
    // ========================================================

    /** UC 3: HashMap Inventory */
    static class RoomInventory {
        private Map<String, Integer> availability = new HashMap<>();
        public RoomInventory() {
            availability.put("SingleRoom", 5);
            availability.put("DoubleRoom", 3);
            availability.put("SuiteRoom", 2);
        }
        public Map<String, Integer> getRoomAvailability() { return availability; }
        public void decrement(String type) { availability.put(type, availability.get(type) - 1); }
    }

    /** UC 4: Read-Only Search */
    static class RoomSearchService {
        public void searchAvailableRooms(RoomInventory inv, Room s, Room d, Room st) {
            for (String type : inv.getRoomAvailability().keySet()) {
                int count = inv.getRoomAvailability().get(type);
                if (count > 0) {
                    System.out.println("MATCH FOUND: " + type + " (" + count + " left)");
                }
            }
        }
    }

    /** UC 5: FIFO Queue */
    static class BookingRequestQueue {
        private Queue<Reservation> queue = new LinkedList<>();
        public void addRequest(Reservation res) {
            queue.add(res);
            System.out.println("FIFO Entry: " + res);
        }
        public Queue<Reservation> getQueue() { return queue; }
    }

    /** UC 6: Set-based Allocation */
    static class RoomAllocationService {
        private Map<String, Set<String>> allocatedRooms = new HashMap<>();

        public RoomAllocationService() {
            allocatedRooms.put("SingleRoom", new HashSet<>());
            allocatedRooms.put("DoubleRoom", new HashSet<>());
            allocatedRooms.put("SuiteRoom", new HashSet<>());
        }

        public void processAllocations(BookingRequestQueue bQueue, RoomInventory inventory) {
            Queue<Reservation> requests = bQueue.getQueue();
            while (!requests.isEmpty()) {
                Reservation res = requests.poll(); // FIFO Dequeue
                String type = res.getRoomType();

                if (inventory.getRoomAvailability().get(type) > 0) {
                    // Unique Room ID generation
                    String roomID = type.substring(0, 1) + "R-" + (101 + allocatedRooms.get(type).size());

                    // UC6: Uniqueness check via Set
                    allocatedRooms.get(type).add(roomID);
                    inventory.decrement(type);

                    System.out.println("SUCCESS: " + res.getGuestName() + " assigned to " + roomID);
                } else {
                    System.out.println("REJECTED: No availability for " + res.getGuestName());
                }
            }
        }
    }
}