import java.util.*;

/**
 * BookMyStayApp - Integrated Hotel Management System
 * Use Cases: 1 (Entry), 2 (Rooms), 3 (Inventory), 4 (Search),
 * 5 (Queue), 6 (Allocation), 7 (Add-ons)
 */
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
        System.out.println("Room Search Results (UC4):\n");
        RoomSearchService searchService = new RoomSearchService();
        searchService.searchAvailableRooms(inventory);
        System.out.println("----------------------------------------------");

        // --- UC 5: Booking Request Queue (FIFO) ---
        System.out.println("Guest Submitting Booking Requests (UC5)...\n");
        BookingRequestQueue bookingQueue = new BookingRequestQueue();
        bookingQueue.addRequest(new Reservation("Guest_1", "SingleRoom"));
        bookingQueue.addRequest(new Reservation("Guest_2", "DoubleRoom"));
        bookingQueue.addRequest(new Reservation("Guest_3", "SingleRoom"));
        System.out.println("----------------------------------------------");

        // --- UC 6: Reservation Confirmation & Allocation ---
        System.out.println("Processing Allocations & Unique Room IDs (UC6)...\n");
        RoomAllocationService allocationService = new RoomAllocationService();
        // This processes the queue and stores the successful allocations
        Map<String, String> guestToRoomMapping = allocationService.processAllocations(bookingQueue, inventory);
        System.out.println("----------------------------------------------");

        // --- UC 7: Add-On Service Selection ---
        System.out.println("Add-On Service Selection (UC7)...\n");
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        // Guest_1 and Guest_2 get their Room IDs from the allocation mapping
        String g1Room = guestToRoomMapping.get("Guest_1");
        String g2Room = guestToRoomMapping.get("Guest_2");

        if (g1Room != null) {
            serviceManager.addServiceToReservation(g1Room, new AddOnService("Breakfast", 500.0));
            serviceManager.addServiceToReservation(g1Room, new AddOnService("Spa", 2000.0));
            serviceManager.displayServicesForReservation(g1Room);
        }

        if (g2Room != null) {
            serviceManager.addServiceToReservation(g2Room, new AddOnService("Late Checkout", 800.0));
            serviceManager.displayServicesForReservation(g2Room);
        }

        System.out.println("----------------------------------------------");
        System.out.println("Final System State (UC1-UC7 Complete)");
    }

    private static void displayStatus(Room room, RoomInventory inventory) {
        String type = room.getClass().getSimpleName();
        System.out.println(type + " Availability: " + inventory.getRoomAvailability().get(type));
    }

    // ========================================================
    // DOMAIN MODELS (UC 2 & UC 5)
    // ========================================================
    abstract static class Room {
        protected int beds; protected double price;
        public Room(int beds, double price) { this.beds = beds; this.price = price; }
    }
    static class SingleRoom extends Room { public SingleRoom() { super(1, 1500.0); } }
    static class DoubleRoom extends Room { public DoubleRoom() { super(2, 2500.0); } }
    static class SuiteRoom extends Room { public SuiteRoom() { super(3, 5000.0); } }

    static class Reservation {
        private String guestName; private String roomType;
        public Reservation(String name, String type) { this.guestName = name; this.roomType = type; }
        public String getGuestName() { return guestName; }
        public String getRoomType() { return roomType; }
    }

    // ========================================================
    // UC 7: Add-On Service logic
    // ========================================================
    static class AddOnService {
        private String name; private double cost;
        public AddOnService(String name, double cost) { this.name = name; this.cost = cost; }
        @Override public String toString() { return name + " (Rs." + cost + ")"; }
        public double getCost() { return cost; }
    }

    static class AddOnServiceManager {
        private Map<String, List<AddOnService>> serviceMap = new HashMap<>();

        public void addServiceToReservation(String roomID, AddOnService service) {
            serviceMap.computeIfAbsent(roomID, k -> new ArrayList<>()).add(service);
            System.out.println("Service " + service + " linked to " + roomID);
        }

        public void displayServicesForReservation(String roomID) {
            System.out.println("\nAdd-ons for " + roomID + ":");
            List<AddOnService> list = serviceMap.get(roomID);
            double total = 0;
            for (AddOnService s : list) {
                System.out.println("- " + s);
                total += s.getCost();
            }
            System.out.println("Total Additional Cost: Rs." + total);
        }
    }

    // ========================================================
    // SYSTEM SERVICES (UC 3, 4, 5, 6)
    // ========================================================
    static class RoomInventory {
        private Map<String, Integer> counts = new HashMap<>();
        public RoomInventory() { counts.put("SingleRoom", 5); counts.put("DoubleRoom", 3); counts.put("SuiteRoom", 2); }
        public Map<String, Integer> getRoomAvailability() { return counts; }
        public void decrement(String type) { counts.put(type, counts.get(type) - 1); }
    }

    static class RoomSearchService {
        public void searchAvailableRooms(RoomInventory inv) {
            inv.getRoomAvailability().forEach((type, count) -> {
                if (count > 0) System.out.println("Available: " + type + " (" + count + " left)");
            });
        }
    }

    static class BookingRequestQueue {
        private Queue<Reservation> queue = new LinkedList<>();
        public void addRequest(Reservation res) { queue.add(res); System.out.println("Queued: " + res.getGuestName()); }
        public Queue<Reservation> getQueue() { return queue; }
    }

    static class RoomAllocationService {
        private Map<String, Set<String>> allocated = new HashMap<>();
        public RoomAllocationService() { allocated.put("SingleRoom", new HashSet<>()); allocated.put("DoubleRoom", new HashSet<>()); allocated.put("SuiteRoom", new HashSet<>()); }

        public Map<String, String> processAllocations(BookingRequestQueue bq, RoomInventory inv) {
            Map<String, String> guestToRoom = new HashMap<>();
            Queue<Reservation> q = bq.getQueue();
            while (!q.isEmpty()) {
                Reservation r = q.poll();
                String type = r.getRoomType();
                if (inv.getRoomAvailability().get(type) > 0) {
                    String id = type.substring(0, 1) + "R-" + (101 + allocated.get(type).size());
                    allocated.get(type).add(id);
                    inv.decrement(type);
                    guestToRoom.put(r.getGuestName(), id);
                    System.out.println("SUCCESS: " + r.getGuestName() + " -> " + id);
                }
            }
            return guestToRoom;
        }
    }
}