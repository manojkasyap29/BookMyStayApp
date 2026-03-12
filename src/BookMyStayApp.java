import java.util.*;

/**
 * BookMyStayApp - Final Integrated Version
 * Covers Use Cases 1 to 8:
 * UC1: Entry | UC2: Models | UC3: Inventory | UC4: Search
 * UC5: Queuing | UC6: Allocation | UC7: Add-ons | UC8: History & Reporting
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
        displayInventoryStatus(inventory);
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
        bookingQueue.addRequest(new Reservation("Guest_3", "SuiteRoom"));
        System.out.println("----------------------------------------------");

        // --- UC 6 & 8: Allocation and Persistence ---
        System.out.println("Processing Allocations & Tracking History (UC6 & UC8)...\n");
        RoomAllocationService allocationService = new RoomAllocationService();
        BookingHistory history = new BookingHistory();

        // This processes the queue, updates inventory, and saves to history list
        Map<String, String> successfulAllocations = allocationService.processAllocations(bookingQueue, inventory, history);
        System.out.println("----------------------------------------------");

        // --- UC 7: Add-On Service Selection ---
        System.out.println("Add-On Service Selection (UC7)...\n");
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        // Loop through successful bookings to add services
        for (String guestName : successfulAllocations.keySet()) {
            String roomID = successfulAllocations.get(guestName);
            serviceManager.addServiceToReservation(roomID, new AddOnService("Breakfast", 500.0));
            if (roomID.contains("SR")) { // Add Spa for Suite Rooms
                serviceManager.addServiceToReservation(roomID, new AddOnService("Spa Treatment", 2500.0));
            }
            serviceManager.displayServicesForReservation(roomID);
        }
        System.out.println("----------------------------------------------");

        // --- UC 8: Administrative Reporting ---
        System.out.println("Administrative Reporting Service (UC8)\n");
        BookingReportService reportService = new BookingReportService();
        reportService.generateSummaryReport(history);
    }

    private static void displayInventoryStatus(RoomInventory inv) {
        inv.getRoomAvailability().forEach((type, count) ->
                System.out.println(type + " Count: " + count));
    }

    // ========================================================
    // DOMAIN MODELS (UC 2, 5, 7)
    // ========================================================
    abstract static class Room {
        protected int beds; protected double price;
        public Room(int beds, double price) { this.beds = beds; this.price = price; }
    }
    static class SingleRoom extends Room { public SingleRoom() { super(1, 1500.0); } }
    static class DoubleRoom extends Room { public DoubleRoom() { super(2, 2500.0); } }
    static class SuiteRoom extends Room { public SuiteRoom() { super(3, 5000.0); } }

    static class Reservation {
        private String guestName; private String roomType; private String assignedRoomID;
        public Reservation(String name, String type) { this.guestName = name; this.roomType = type; }
        public String getGuestName() { return guestName; }
        public String getRoomType() { return roomType; }
        public void setAssignedRoomID(String id) { this.assignedRoomID = id; }
        @Override public String toString() { return "Guest: " + guestName + " | Room: " + assignedRoomID + " (" + roomType + ")"; }
    }

    static class AddOnService {
        private String name; private double cost;
        public AddOnService(String name, double cost) { this.name = name; this.cost = cost; }
        @Override public String toString() { return name + " (Rs." + cost + ")"; }
        public double getCost() { return cost; }
    }

    // ========================================================
    // PERSISTENCE & REPORTING (UC 8)
    // ========================================================
    static class BookingHistory {
        private List<Reservation> historyList = new ArrayList<>(); // Sequential Audit Trail

        public void recordBooking(Reservation res) { historyList.add(res); }
        public List<Reservation> getHistory() { return Collections.unmodifiableList(historyList); }
    }

    static class BookingReportService {
        public void generateSummaryReport(BookingHistory history) {
            System.out.println("========== AUDIT REPORT ==========");
            List<Reservation> records = history.getHistory();
            if (records.isEmpty()) {
                System.out.println("No history records found.");
            } else {
                records.forEach(System.out::println);
                System.out.println("Total Operational Bookings: " + records.size());
            }
            System.out.println("==================================");
        }
    }

    // ========================================================
    // LOGIC SERVICES (UC 3, 4, 5, 6, 7)
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
                if (count > 0) System.out.println("Search Found: " + type + " (" + count + " units)");
            });
        }
    }

    static class BookingRequestQueue {
        private Queue<Reservation> queue = new LinkedList<>(); // FIFO
        public void addRequest(Reservation res) { queue.add(res); System.out.println("Added to Queue: " + res.getGuestName()); }
        public Queue<Reservation> getQueue() { return queue; }
    }

    static class RoomAllocationService {
        private Map<String, Set<String>> allocated = new HashMap<>();
        public RoomAllocationService() {
            allocated.put("SingleRoom", new HashSet<>());
            allocated.put("DoubleRoom", new HashSet<>());
            allocated.put("SuiteRoom", new HashSet<>());
        }

        public Map<String, String> processAllocations(BookingRequestQueue bq, RoomInventory inv, BookingHistory history) {
            Map<String, String> mapping = new HashMap<>();
            Queue<Reservation> q = bq.getQueue();
            while (!q.isEmpty()) {
                Reservation r = q.poll(); // Get first in line
                String type = r.getRoomType();
                if (inv.getRoomAvailability().get(type) > 0) {
                    // Generate unique ID using Set size to avoid collision
                    String id = type.substring(0, 1).toUpperCase() + "R-" + (101 + allocated.get(type).size());
                    allocated.get(type).add(id);
                    inv.decrement(type);
                    r.setAssignedRoomID(id);

                    history.recordBooking(r); // Persistence mindset (UC8)
                    mapping.put(r.getGuestName(), id);
                    System.out.println("Confirmed: " + r.getGuestName() + " -> " + id);
                }
            }
            return mapping;
        }
    }

    static class AddOnServiceManager {
        private Map<String, List<AddOnService>> serviceMap = new HashMap<>(); // One-to-Many
        public void addServiceToReservation(String roomID, AddOnService service) {
            serviceMap.computeIfAbsent(roomID, k -> new ArrayList<>()).add(service);
        }
        public void displayServicesForReservation(String roomID) {
            System.out.print("Add-ons for " + roomID + ": ");
            List<AddOnService> list = serviceMap.get(roomID);
            if (list != null) System.out.println(list);
            else System.out.println("None");
        }
    }
}