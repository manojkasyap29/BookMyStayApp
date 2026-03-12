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

}