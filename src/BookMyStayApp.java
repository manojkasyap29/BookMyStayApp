import java.util.*;
import java.io.*;

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
        BookingHistory history = new BookingHistory();
        
        // --- UC 12: Data Persistence & System Recovery ---
        System.out.println("Checking for Persisted Data (UC12)...\n");
        PersistenceService persistenceService = new PersistenceService();
        persistenceService.loadSystemState(inventory, history);
        
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
        // BookingHistory history = new BookingHistory(); // Already created and potentially loaded

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
        System.out.println("----------------------------------------------");

        // --- UC 10: Booking Cancellation & Inventory Rollback ---
        System.out.println("Booking Cancellation Service (UC10)...\n");
        CancellationService cancellationService = new CancellationService();

        // Simulate cancellation for Guest_2
        System.out.println("Requesting Cancellation for Guest_2...");
        cancellationService.cancelBooking("Guest_2", inventory, history, allocationService);

        System.out.println("Verifying Rollback State:");
        displayInventoryStatus(inventory);
        cancellationService.displayRollbackHistory();
        System.out.println("----------------------------------------------");

        // --- UC 11: Concurrent Booking Simulation ---
        ConcurrentBookingSimulation simulation = new ConcurrentBookingSimulation();
        simulation.runSimulation(inventory, allocationService, history);
        
        // --- UC 12: Saving State on Shutdown ---
        System.out.println("Saving System State (UC12)...\n");
        persistenceService.saveSystemState(inventory, history);
        System.out.println("----------------------------------------------");
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

    static class Reservation implements Serializable {
        private static final long serialVersionUID = 1L;
        private String guestName; private String roomType; private String assignedRoomID;
        public Reservation(String name, String type) { this.guestName = name; this.roomType = type; }
        public String getGuestName() { return guestName; }
        public String getRoomType() { return roomType; }
        public String getAssignedRoomID() { return assignedRoomID; }
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
    static class BookingHistory implements Serializable {
        private static final long serialVersionUID = 1L;
        private List<Reservation> historyList = Collections.synchronizedList(new ArrayList<>()); // Thread-safe List

        public void recordBooking(Reservation res) { historyList.add(res); }
        public void removeBooking(Reservation res) { historyList.remove(res); }
        public void setHistoryList(List<Reservation> list) { this.historyList.addAll(list); }
        public List<Reservation> getHistory() { return new ArrayList<>(historyList); } // Return copy for safe iteration
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
    static class RoomInventory implements Serializable {
        private static final long serialVersionUID = 1L;
        private Map<String, Integer> counts = new HashMap<>(); // Shared Mutable State

        public RoomInventory() { counts.put("SingleRoom", 5); counts.put("DoubleRoom", 3); counts.put("SuiteRoom", 2); }
        
        public void setCounts(Map<String, Integer> loadedCounts) { this.counts = loadedCounts; }

        public synchronized Map<String, Integer> getRoomAvailability() { return new HashMap<>(counts); } // Return copy
        
        // Critical Section: Atomic check and update
        public synchronized boolean tryReduceInventory(String type) {
            int current = counts.getOrDefault(type, 0);
            if (current > 0) {
                counts.put(type, current - 1);
                return true;
            }
            return false;
        }

        public synchronized void increment(String type) { counts.put(type, counts.get(type) + 1); }
        
        // Deprecated: Unsafe method kept for backward compatibility if needed, but safe version preferred
        public synchronized void decrement(String type) { 
             if (counts.get(type) > 0) counts.put(type, counts.get(type) - 1); 
        }
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
        
        public synchronized void addRequest(Reservation res) { 
            queue.add(res); 
            System.out.println(Thread.currentThread().getName() + " Added to Queue: " + res.getGuestName()); 
        }
        
        public synchronized Reservation pollRequest() {
            return queue.poll();
        }
        
        public synchronized boolean isEmpty() {
            return queue.isEmpty();
        }
        
        public synchronized Queue<Reservation> getQueue() { return queue; }
    }

    static class RoomAllocationService {
        private Map<String, Set<String>> allocated = new java.util.concurrent.ConcurrentHashMap<>();
        
        public RoomAllocationService() {
            allocated.put("SingleRoom", Collections.synchronizedSet(new HashSet<>()));
            allocated.put("DoubleRoom", Collections.synchronizedSet(new HashSet<>()));
            allocated.put("SuiteRoom", Collections.synchronizedSet(new HashSet<>()));
        }
        
        public void releaseRoom(String type, String id) {
            if (allocated.containsKey(type)) {
                allocated.get(type).remove(id);
            }
        }

        // Thread-safe allocation method
        public boolean allocateRoom(Reservation r, RoomInventory inv, BookingHistory history) {
             String type = r.getRoomType();
             
             // 1. Thread-Safe Inventory Check
             if (inv.tryReduceInventory(type)) {
                 
                 // 2. Critical Section: ID Generation
                 String id;
                 Set<String> roomSet = allocated.get(type);
                 synchronized(roomSet) {
                     id = type.substring(0, 1).toUpperCase() + "R-" + (101 + roomSet.size());
                     // Handle potential collision (simplified)
                     while(roomSet.contains(id)) {
                         id = type.substring(0, 1).toUpperCase() + "R-" + (101 + roomSet.size() + new Random().nextInt(100));
                     }
                     roomSet.add(id);
                 }
                 
                 r.setAssignedRoomID(id);
                 history.recordBooking(r);
                 System.out.println(Thread.currentThread().getName() + " Confirmed: " + r.getGuestName() + " -> " + id);
                 return true;
             } else {
                 System.out.println(Thread.currentThread().getName() + " Failed: No Inventory for " + r.getGuestName() + " (" + type + ")");
                 return false;
             }
        }

        public Map<String, String> processAllocations(BookingRequestQueue bq, RoomInventory inv, BookingHistory history) {
            Map<String, String> mapping = new HashMap<>();
            
            while (!bq.isEmpty()) {
                Reservation r = bq.pollRequest();
                if (r != null) {
                    if (allocateRoom(r, inv, history)) {
                        mapping.put(r.getGuestName(), r.getAssignedRoomID());
                    }
                }
            }
            return mapping;
        }
    }

    static class ConcurrentBookingSimulation {
        public void runSimulation(RoomInventory inventory, RoomAllocationService allocationService, BookingHistory history) {
            System.out.println("\n--- Starting Concurrent Booking Simulation (UC11) ---");
            BookingRequestQueue sharedQueue = new BookingRequestQueue();

            // Runnable Task: Guests submitting requests
            Runnable guestTask = () -> {
                String threadName = Thread.currentThread().getName();
                for (int i = 1; i <= 3; i++) {
                   sharedQueue.addRequest(new Reservation(threadName + "_Guest_" + i, "SingleRoom"));
                   try { Thread.sleep(50); } catch (InterruptedException e) {}
                }
            };

            // Runnable Task: System processing requests
            Runnable processorTask = () -> {
                while (true) {
                    Reservation r = sharedQueue.pollRequest();
                    if (r != null) {
                        allocationService.allocateRoom(r, inventory, history);
                    } else {
                        // In a real system, would wait. Here we break if empty for demo simplicity, 
                        // but with concurrent producers we should wait a bit or use a better signal.
                        // For this simulation, we'll just check if guests are done. 
                           try { Thread.sleep(100); } catch (InterruptedException e) {}
                           if (sharedQueue.isEmpty()) break; 
                    }
                }
            };

            // Simulation: 3 Concurrent Guest Threads + 2 Processor Threads
            Thread g1 = new Thread(guestTask, "MobileApp");
            Thread g2 = new Thread(guestTask, "WebPortal");
            Thread g3 = new Thread(guestTask, "Kiosk");
            
            // Start Producers
            g1.start(); g2.start(); g3.start();

            // Start Consumers/Processors
            Thread p1 = new Thread(processorTask, "Processor-1");
            Thread p2 = new Thread(processorTask, "Processor-2");
            p1.start(); p2.start();

            try {
                g1.join(); g2.join(); g3.join();
                // Wait for processors to finish emptying the queue
                // (In robust code, use ExecutorService or CountDownLatch)
                Thread.sleep(2000); 
            } catch (InterruptedException e) { e.printStackTrace(); }

            System.out.println("--- Concurrent Simulation Completed ---");
        }
    }

    static class CancellationService {
        private Stack<String> releasedRoomIds = new Stack<>(); // LIFO Rollback structure

        public void cancelBooking(String guestName, RoomInventory inventory, BookingHistory history, RoomAllocationService allocationService) {
            Reservation target = null;
            for (Reservation r : history.getHistory()) {
                if (r.getGuestName().equalsIgnoreCase(guestName) && r.getAssignedRoomID() != null) {
                    target = r;
                    break;
                }
            }

            if (target != null) {
                String roomId = target.getAssignedRoomID();
                String type = target.getRoomType();

                // 1. Add to rollback structure (Stack - LIFO)
                releasedRoomIds.push(roomId);

                // 2. Release from allocation service
                allocationService.releaseRoom(type, roomId);

                // 3. Restore inventory
                inventory.increment(type);

                // 4. Update history
                history.removeBooking(target);

                System.out.println("Cancellation Successful for " + guestName);
                System.out.println("Rolled back Room ID: " + roomId);
            } else {
                System.out.println("Cancellation Failed: Booking not found for " + guestName);
            }
        }

        public void displayRollbackHistory() {
            System.out.println("Recently Released Rooms (LIFO): " + releasedRoomIds);
        }
    }

    static class PersistenceService {
        private static final String DATA_FILE = "system_state.ser";

        public void saveSystemState(RoomInventory inventory, BookingHistory history) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
                oos.writeObject(inventory);
                oos.writeObject(history);
                System.out.println("System state saved successfully to " + DATA_FILE);
            } catch (IOException e) {
                System.err.println("Error saving system state: " + e.getMessage());
            }
        }

        public void loadSystemState(RoomInventory inventory, BookingHistory history) {
            File file = new File(DATA_FILE);
            if (!file.exists()) {
                System.out.println("No persistence file found. Starting with fresh state.");
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
                RoomInventory loadedInventory = (RoomInventory) ois.readObject();
                BookingHistory loadedHistory = (BookingHistory) ois.readObject();

                // Restore
                inventory.setCounts(loadedInventory.getRoomAvailability()); // Use getRoomAvailability which returns map copy
                history.setHistoryList(loadedHistory.getHistory());

                System.out.println("System state restored from " + DATA_FILE);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading system state: " + e.getMessage());
            }
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