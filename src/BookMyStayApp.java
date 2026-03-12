import java.util.*;

/**
 * BookMyStayApp - Complete Master Version
 * Explicitly covers and executes Use Cases 1 through 9.
 */
public class BookMyStayApp {

    public static void main(String[] args) {

        // ========================================================
        // UC 1: Application Entry
        // ========================================================
        System.out.println("--- Use Case 1: Application Entry ---");
        System.out.println("Welcome to the Hotel Booking Management System");
        System.out.println("System initialized successfully.\n");

        // ========================================================
        // UC 2: Room Initialization & Static Availability
        // ========================================================
        System.out.println("--- Use Case 2: Room Initialization ---");
        Room single = new SingleRoom();
        Room doubleRm = new DoubleRoom();
        Room suite = new SuiteRoom();
        single.displayRoomDetails();
        doubleRm.displayRoomDetails();
        suite.displayRoomDetails();
        System.out.println();

        // ========================================================
        // UC 3: Centralized Inventory
        // ========================================================
        System.out.println("--- Use Case 3: Centralized Inventory (HashMap) ---");
        RoomInventory inventory = new RoomInventory();
        System.out.println("Initial Inventory State:");
        inventory.getRoomAvailability().forEach((type, count) ->
                System.out.println(" - " + type + ": " + count + " available"));
        System.out.println();

        // ========================================================
        // UC 4: Room Search Service
        // ========================================================
        System.out.println("--- Use Case 4: Room Search Service (Read-Only) ---");
        RoomSearchService searchService = new RoomSearchService();
        searchService.searchAvailableRooms(inventory);
        System.out.println();

        // ========================================================
        // UC 5 & UC 9: Request Queueing AND Validation / Error Handling
        // ========================================================
        System.out.println("--- Use Case 5 & 9: Booking Requests with Validation ---");
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        // Helper method to simulate UC9 validation before UC5 queuing
        submitBookingRequest("Guest_1", "SingleRoom", bookingQueue, inventory); // Valid
        submitBookingRequest("Guest_2", "DoubleRoom", bookingQueue, inventory); // Valid
        submitBookingRequest("Guest_3", "SuiteRoom", bookingQueue, inventory);  // Valid

        // UC9 Specific Tests (These will fail gracefully)
        submitBookingRequest("Guest_Error_1", "Penthouse", bookingQueue, inventory); // Invalid Room Type
        submitBookingRequest("", "SingleRoom", bookingQueue, inventory); // Invalid Name
        System.out.println();

        // ========================================================
        // UC 6: Reservation Confirmation & Room Allocation
        // ========================================================
        System.out.println("--- Use Case 6: Room Allocation (Set for Unique IDs) ---");
        RoomAllocationService allocationService = new RoomAllocationService();
        BookingHistory history = new BookingHistory(); // Prepared for UC8

        // Process the valid queue, decrement inventory, generate IDs, and save to history
        Map<String, String> guestToRoomMapping = allocationService.processAllocations(bookingQueue, inventory, history);
        System.out.println("\nInventory after allocation:");
        inventory.getRoomAvailability().forEach((type, count) ->
                System.out.println(" - " + type + ": " + count + " available"));
        System.out.println();

        // ========================================================
        // UC 7: Add-On Service Selection
        // ========================================================
        System.out.println("--- Use Case 7: Add-On Service Selection (Map & List) ---");
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        // Give Guest 1 some add-ons using their newly assigned Room ID
        String guest1RoomID = guestToRoomMapping.get("Guest_1");
        if (guest1RoomID != null) {
            serviceManager.addServiceToReservation(guest1RoomID, new AddOnService("Breakfast", 500.0));
            serviceManager.addServiceToReservation(guest1RoomID, new AddOnService("Spa", 2000.0));
            serviceManager.displayServicesForReservation(guest1RoomID);
        }

        // Give Guest 2 an add-on
        String guest2RoomID = guestToRoomMapping.get("Guest_2");
        if (guest2RoomID != null) {
            serviceManager.addServiceToReservation(guest2RoomID, new AddOnService("Late Checkout", 800.0));
            serviceManager.displayServicesForReservation(guest2RoomID);
        }
        System.out.println();

        // ========================================================
        // UC 8: Booking History & Reporting
        // ========================================================
        System.out.println("--- Use Case 8: Booking History & Reporting ---");
        BookingReportService reportService = new BookingReportService();
        reportService.generateSummaryReport(history);
    }

    // ========================================================
    // UC 9 Helper: Validation Logic (Fail-Fast)
    // ========================================================
    private static void submitBookingRequest(String name, String type, BookingRequestQueue queue, RoomInventory inv) {
        try {
            // UC9: Validate input before processing
            if (name == null || name.trim().isEmpty()) {
                throw new InvalidBookingException("Guest name cannot be empty.");
            }
            if (!inv.getRoomAvailability().containsKey(type)) {
                throw new InvalidBookingException("Invalid Room Type requested: '" + type + "'");
            }

            // UC5: If valid, add to Queue
            Reservation res = new Reservation(name, type);
            queue.addRequest(res);
            System.out.println("SUCCESS: Request accepted for " + name);

        } catch (InvalidBookingException e) {
            System.err.println("VALIDATION FAILED: " + e.getMessage());
        }
    }

    // ========================================================
    // EXCEPTION CLASS (UC 9)
    // ========================================================
    static class InvalidBookingException extends Exception {
        public InvalidBookingException(String message) {
            super(message);
        }
    }

    // ========================================================
    // DOMAIN MODELS (UC 2, 5, 7)
    // ========================================================
    abstract static class Room {
        protected int beds; protected double price;
        public Room(int beds, double price) { this.beds = beds; this.price = price; }
        public void displayRoomDetails() {
            System.out.println(this.getClass().getSimpleName() + " initialized (Beds: " + beds + ", Price: " + price + ")");
        }
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
        public double getCost() { return cost; }
        @Override public String toString() { return name + " (Rs." + cost + ")"; }
    }

    // ========================================================
    // SERVICES (UC 3, 4, 5, 6, 7, 8)
    // ========================================================

    /** UC 3: Inventory */
    static class RoomInventory {
        private Map<String, Integer> counts = new HashMap<>();
        public RoomInventory() {
            counts.put("SingleRoom", 5);
            counts.put("DoubleRoom", 3);
            counts.put("SuiteRoom", 2);
        }
        public Map<String, Integer> getRoomAvailability() { return counts; }

        // UC 9: Guarding System State
        public void decrement(String type) throws InvalidBookingException {
            int current = counts.get(type);
            if (current <= 0) {
                throw new InvalidBookingException("Inventory exhausted for: " + type);
            }
            counts.put(type, current - 1);
        }
    }

    /** UC 4: Search */
    static class RoomSearchService {
        public void searchAvailableRooms(RoomInventory inv) {
            System.out.println("Searching for available rooms...");
            inv.getRoomAvailability().forEach((type, count) -> {
                if (count > 0) System.out.println(" - MATCH FOUND: " + type + " has " + count + " rooms left.");
            });
        }
    }

    /** UC 5: Queue */
    static class BookingRequestQueue {
        private Queue<Reservation> queue = new LinkedList<>();
        public void addRequest(Reservation res) { queue.add(res); }
        public Queue<Reservation> getQueue() { return queue; }
    }

    /** UC 6: Allocation */
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
                Reservation r = q.poll(); // FIFO
                try {
                    String type = r.getRoomType();
                    inv.decrement(type); // Triggers UC9 Exception if empty

                    // UC6: Uniqueness
                    String id = type.substring(0, 1) + "R-" + (101 + allocated.get(type).size());
                    allocated.get(type).add(id);
                    r.setAssignedRoomID(id);

                    history.recordBooking(r); // UC8: Save to History
                    mapping.put(r.getGuestName(), id);
                    System.out.println("ALLOCATED: " + r.getGuestName() + " assigned to " + id);

                } catch (InvalidBookingException e) {
                    System.err.println("ALLOCATION FAILED: " + e.getMessage());
                }
            }
            return mapping;
        }
    }

    /** UC 7: Add-On Service Manager */
    static class AddOnServiceManager {
        private Map<String, List<AddOnService>> serviceMap = new HashMap<>();
        public void addServiceToReservation(String roomID, AddOnService service) {
            serviceMap.computeIfAbsent(roomID, k -> new ArrayList<>()).add(service);
            System.out.println("Added " + service.name + " to " + roomID);
        }
        public void displayServicesForReservation(String roomID) {
            System.out.println("Services billed to " + roomID + ":");
            List<AddOnService> list = serviceMap.get(roomID);
            double total = 0;
            if (list != null) {
                for (AddOnService s : list) {
                    System.out.println("  - " + s);
                    total += s.getCost();
                }
            }
            System.out.println("  Total Add-on Cost: Rs." + total);
        }
    }

    /** UC 8: Booking History & Reports */
    static class BookingHistory {
        private List<Reservation> historyList = new ArrayList<>();
        public void recordBooking(Reservation res) { historyList.add(res); }
        public List<Reservation> getHistory() { return historyList; }
    }

    static class BookingReportService {
        public void generateSummaryReport(BookingHistory history) {
            System.out.println("========== OFFICIAL AUDIT REPORT ==========");
            List<Reservation> records = history.getHistory();
            if (records.isEmpty()) {
                System.out.println("No records found.");
            } else {
                for (int i = 0; i < records.size(); i++) {
                    System.out.println((i + 1) + ". " + records.get(i));
                }
                System.out.println("\nTotal Confirmed Bookings: " + records.size());
            }
            System.out.println("===========================================");
        }
    }
}