package transaction;

import lockmgr.DeadlockException;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Collection;

import static transaction.Reservation.*;

/**
 * Workflow Controller for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the WC.  In the real
 * implementation, the WC should forward calls to either RM or TM,
 * instead of doing the things itself.
 */

public class WorkflowControllerImpl
        extends java.rmi.server.UnicastRemoteObject
        implements WorkflowController {

    private static final String TABLE_NAME_FLIGHT = "FLIGHTS";
    private static final String TABLE_NAME_HOTEL = "HOTELS";
    private static final String TABLE_NAME_CAR = "CARS";
    private static final String TABLE_NAME_CUSTOMER = "CUSTOMERS";
    private static final String TABLE_NAME_RESERVATION = "RESERVATIONS";
    private ResourceManager rmFlights = null;
    private ResourceManager rmHotels = null;
    private ResourceManager rmCars = null;
    private ResourceManager rmCustomers = null;
    private ResourceManager rmReservations = null;
    private TransactionManager tm = null;

    public WorkflowControllerImpl() throws RemoteException {
        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }
        // get reference of all RMs and TM
        try {
            rmFlights = (ResourceManager) Naming.lookup(rmiPort + ResourceManager.RMINameFlights);
            rmHotels = (ResourceManager) Naming.lookup(rmiPort + ResourceManager.RMINameRooms);
            rmCars = (ResourceManager) Naming.lookup(rmiPort + ResourceManager.RMINameCars);
            rmCustomers = (ResourceManager) Naming.lookup(rmiPort + ResourceManager.RMINameCustomers);
            rmReservations = (ResourceManager) Naming.lookup(rmiPort + ResourceManager.RMINameReservations);

            tm = (TransactionManager) Naming.lookup(rmiPort + TransactionManager.RMIName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // using while to ensure all components can talk to each other finally after failure
        while (!reconnect()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            WorkflowControllerImpl obj = new WorkflowControllerImpl();
            Naming.rebind(rmiPort + WorkflowController.RMIName, obj);
            System.out.println("WC bound");
        } catch (Exception e) {
            System.err.println("WC not bound:" + e);
            System.exit(1);
        }
    }

    // TRANSACTION INTERFACE
    // Logic is done by invoke interfaces of TransactionManager
    public int start()
            throws RemoteException {
        return tm.start();
    }

    public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        return tm.commit(xid);
    }

    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException {
        tm.abort(xid);
    }


    // ADMINISTRATIVE INTERFACE
    // Logic is done by invoke interfaces of corresponding RM***

    // TODO: I am not sure whether try-catch is correctly used below. When will a DeadlockException occur?

    public boolean addFlight(int xid, String flightNum, int numSeats, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (flightNum == null || numSeats < 0) {
            return false;
        }

        try {
            Flight flight = (Flight) rmFlights.query(xid, TABLE_NAME_FLIGHT, flightNum);

            if (flight == null) { // the flight does not exist, add a new flight
                price = price > 0 ? price : 0;
                flight = new Flight(flightNum, price, numSeats, numSeats);
                return rmFlights.insert(xid, TABLE_NAME_FLIGHT, flight);
            } else { // the flight already exists, update the flight
                // update Seats
                flight.setNumSeats(flight.getNumSeats() + numSeats);
                flight.setNumAvail(flight.getNumAvail() + numSeats);

                // update price
                if (price >= 0) {
                    flight.setPrice(price);
                }
                return rmFlights.update(xid, TABLE_NAME_FLIGHT, flightNum, flight);
            }
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "Deadlock in Resource Manager");
        }
    }

    public boolean deleteFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (flightNum == null) {
            return false;
        }

        try {
            Collection reservations = rmReservations.query(xid, TABLE_NAME_RESERVATION, INDEX_RESERVATION_KEY, flightNum);
            // If someone has a reservation on the flight, delete is not allowed!
            if (reservations.isEmpty()) {
                return rmFlights.delete(xid, TABLE_NAME_FLIGHT, flightNum);
            } else {
                return false;
            }
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "Deadlock in Resource Manager");
        } catch (InvalidIndexException e) {
            return false;
        }
    }

    public boolean addRooms(int xid, String location, int numRooms, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (location == null || numRooms < 0) {
            return false;
        }

        try {
            Hotel hotel = (Hotel) rmHotels.query(xid, TABLE_NAME_HOTEL, location);

            if (hotel == null) { // the hotel does not exist, add a new hotel
                price = price > 0 ? price : 0;
                hotel = new Hotel(location, price, numRooms, numRooms);
                return rmHotels.insert(xid, TABLE_NAME_HOTEL, hotel);
            } else { // the hotel already exists, update the hotel
                // update Rooms
                hotel.setNumRooms(hotel.getNumRooms() + numRooms);
                hotel.setNumAvail(hotel.getNumAvail() + numRooms);

                // update price
                if (price >= 0) {
                    hotel.setPrice(price);
                }
                return rmHotels.update(xid, TABLE_NAME_HOTEL, location, hotel);
            }
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "Deadlock in Resource Manager");
        }
    }

    public boolean deleteRooms(int xid, String location, int numRooms)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (location == null || numRooms < 0) {
            return false;
        }

        try {
            Hotel hotel = (Hotel) rmHotels.query(xid, TABLE_NAME_HOTEL, location);

            // hotel does not exist
            if (hotel == null) {
                return false;
            }
            // Rooms are not enough
            if (numRooms > hotel.getNumRooms() || numRooms > hotel.getNumAvail()) {
                return false;
            }

            // update the number of Rooms
            hotel.setNumRooms(hotel.getNumRooms() - numRooms);
            hotel.setNumAvail(hotel.getNumAvail() - numRooms);
            return rmHotels.update(xid, TABLE_NAME_HOTEL, location, hotel);

        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "Deadlock in Resource Manager");
        }
    }

    public boolean addCars(int xid, String location, int numCars, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (location == null || numCars < 0) {
            return false;
        }

        try {
            Car car = (Car) rmCars.query(xid, TABLE_NAME_CAR, location);

            if (car == null) { // the car does not exist, add a new car
                price = price > 0 ? price : 0;
                car = new Car(location, price, numCars, numCars);
                return rmCars.insert(xid, TABLE_NAME_CAR, car);
            } else { // the car already exists, update the car
                // update Cars
                car.setNumCars(car.getNumCars() + numCars);
                car.setNumAvail(car.getNumAvail() + numCars);

                // update price
                if (price >= 0) {
                    car.setPrice(price);
                }
                return rmCars.update(xid, TABLE_NAME_CAR, location, car);
            }
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "Deadlock in Resource Manager");
        }
    }

    public boolean deleteCars(int xid, String location, int numCars)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (location == null || numCars < 0) {
            return false;
        }

        try {
            Car car = (Car) rmCars.query(xid, TABLE_NAME_CAR, location);

            // car does not exist
            if (car == null) {
                return false;
            }
            // Cars are not enough
            if (numCars > car.getNumCars() || numCars > car.getNumAvail()) {
                return false;
            }

            // update the number of Cars
            car.setNumCars(car.getNumCars() - numCars);
            car.setNumAvail(car.getNumAvail() - numCars);
            return rmCars.update(xid, TABLE_NAME_CAR, location, car);

        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "Deadlock in Resource Manager");
        }
    }

    public boolean newCustomer(int xid, String customerName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (customerName == null) {
            return false;
        }

        try {
            Customer customer = (Customer) rmCustomers.query(xid, TABLE_NAME_CUSTOMER, customerName);

            // The customer already exists, return true directly
            if (customer != null) {
                return true;
            }

            // Add a new customer
            customer = new Customer(customerName);
            return rmCustomers.insert(xid, TABLE_NAME_CUSTOMER, customer);
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "Deadlock in Resource Manager");
        }
    }


    public boolean deleteCustomer(int xid, String customerName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (customerName == null) {
            return false;
        }

        try {
            Customer customer = (Customer) rmCustomers.query(xid, TABLE_NAME_CUSTOMER, customerName);

            // The customer does not exist, return false directly
            if (customer == null) {
                return false;
            }

            // Get all the reservations of the customer
            Collection reservations = rmReservations.query(xid, TABLE_NAME_RESERVATION, INDEX_CUSTOMER_NAME, customerName);

            if (reservations != null) {
                // Un-reserve all the reservations before delete the customer
                Reservation reservation;
                for (Object r :
                        reservations) {
                    reservation = (Reservation) r;
                    // un-reserve the reservation first
                    switch (reservation.getReservationType()) {
                        case RESERVATION_TYPE_FLIGHT:
                            unreserveFlight(xid, (String) reservation.getIndex(INDEX_RESERVATION_KEY));
                            break;
                        case RESERVATION_TYPE_HOTEL:
                            unreserveRoom(xid, (String) reservation.getIndex(INDEX_RESERVATION_KEY));
                            break;
                        case RESERVATION_TYPE_CAR:
                            unreserveCar(xid, (String) reservation.getIndex(INDEX_RESERVATION_KEY));
                            break;
                    }
                    // delete the reservation itself
                    rmReservations.delete(xid, TABLE_NAME_RESERVATION, reservation.getKey());
                }
            }

            // After all the reservations related to the customer have been un-reserved, delete the customer
            return rmCustomers.delete(xid, TABLE_NAME_CUSTOMER, customerName);

        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "Deadlock in Resource Manager");
        } catch (InvalidIndexException e) {
            return false;
        }
    }


    // QUERY INTERFACE
    public int queryFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (flightNum == null) {
            return -1;
        }

        try {
            Flight flight = (Flight) rmFlights.query(xid, TABLE_NAME_FLIGHT, flightNum);
            if (flight == null) {
                return -1;
            }
            return flight.getNumAvail();
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "DeadLock in Resource Manager");
        }
    }

    public int queryFlightPrice(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (flightNum == null) {
            return -1;
        }

        try {
            Flight flight = (Flight) rmFlights.query(xid, TABLE_NAME_FLIGHT, flightNum);
            if (flight == null) {
                return -1;
            }
            return flight.getPrice();
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "DeadLock in Resource Manager");
        }
    }

    public int queryRooms(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (location == null) {
            return -1;
        }

        try {
            Hotel hotel = (Hotel) rmHotels.query(xid, TABLE_NAME_HOTEL, location);
            if (hotel == null) {
                return -1;
            }
            return hotel.getNumAvail();
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "DeadLock in Resource Manager");
        }
    }

    public int queryRoomsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (location == null) {
            return -1;
        }

        try {
            Hotel hotel = (Hotel) rmHotels.query(xid, TABLE_NAME_HOTEL, location);
            if (hotel == null) {
                return -1;
            }
            return hotel.getPrice();
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "DeadLock in Resource Manager");
        }
    }

    public int queryCars(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (location == null) {
            return -1;
        }

        try {
            Car car = (Car) rmCars.query(xid, TABLE_NAME_CAR, location);
            if (car == null) {
                return -1;
            }
            return car.getNumAvail();
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "DeadLock in Resource Manager");
        }
    }

    public int queryCarsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (location == null) {
            return -1;
        }

        try {
            Car car = (Car) rmCars.query(xid, TABLE_NAME_CAR, location);

            if (car == null) {
                return -1;
            }
            return car.getPrice();
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "DeadLock in Resource Manager");
        }
    }

    public int queryCustomerBill(int xid, String customerName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (customerName == null) {
            return -1;
        }

        try {
            Customer customer = (Customer) rmCustomers.query(xid, TABLE_NAME_CUSTOMER, customerName);

            // customer does not exist, return -1 directly
            if (customer == null) {
                return -1;
            }

            // get all reservations of the customer
            Collection reservations = rmReservations.query(xid, TABLE_NAME_RESERVATION, INDEX_CUSTOMER_NAME, customerName);
            int totalBill = 0;
            Reservation reservation;
            // count the total bill
            for (Object o :
                    reservations) {
                reservation = (Reservation) o;
                switch (reservation.getReservationType()) {
                    case RESERVATION_TYPE_FLIGHT:
                        totalBill += queryFlightPrice(xid, (String) reservation.getIndex(INDEX_RESERVATION_KEY));
                        break;
                    case RESERVATION_TYPE_HOTEL:
                        totalBill += queryRoomsPrice(xid, (String) reservation.getIndex(INDEX_RESERVATION_KEY));
                        break;
                    case RESERVATION_TYPE_CAR:
                        totalBill += queryCarsPrice(xid, (String) reservation.getIndex(INDEX_RESERVATION_KEY));
                        break;
                }
            }
            return totalBill;

        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "DeadLock in Resource Manager");
        } catch (InvalidIndexException e) {
            return -1;
        }

    }


    // RESERVATION INTERFACE
    public boolean reserveFlight(int xid, String customerName, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (customerName == null || flightNum == null) {
            return false;
        }

        try {
            Customer customer = (Customer) rmCustomers.query(xid, TABLE_NAME_CUSTOMER, customerName);
            // The customer does not exist
            if (customer == null) {
                return false;
            }

            Flight flight = (Flight) rmFlights.query(xid, TABLE_NAME_FLIGHT, flightNum);

            // The flight does not exist or there is no seat
            if (flight == null || flight.getNumAvail() <= 0) {
                return false;
            }

            // decrease the numAvail first
            flight.setNumAvail(flight.getNumAvail() - 1);
            rmFlights.update(xid, TABLE_NAME_FLIGHT, flight.getFlightNum(), flight);

            // add a new Reservation
            Reservation reservation = new Reservation(customerName, RESERVATION_TYPE_FLIGHT, flightNum);
            rmReservations.insert(xid, TABLE_NAME_RESERVATION, reservation);

            return true;
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "DeadLock in Resource Manager");
        }
    }

    public boolean reserveCar(int xid, String customerName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (customerName == null || location == null) {
            return false;
        }

        try {
            Customer customer = (Customer) rmCustomers.query(xid, TABLE_NAME_CUSTOMER, customerName);
            // The customer does not exist
            if (customer == null) {
                return false;
            }

            Car car = (Car) rmCars.query(xid, TABLE_NAME_CAR, location);

            // The car does not exist or there is no car
            if (car == null || car.getNumAvail() <= 0) {
                return false;
            }

            // decrease the numAvail first
            car.setNumAvail(car.getNumAvail() - 1);
            rmCars.update(xid, TABLE_NAME_CAR, car.getLocation(), car);

            // add a new Reservation
            Reservation reservation = new Reservation(customerName, RESERVATION_TYPE_CAR, location);
            rmReservations.insert(xid, TABLE_NAME_RESERVATION, reservation);

            return true;
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "DeadLock in Resource Manager");
        }
    }

    public boolean reserveRoom(int xid, String customerName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        if (customerName == null || location == null) {
            return false;
        }

        try {
            Customer customer = (Customer) rmCustomers.query(xid, TABLE_NAME_CUSTOMER, customerName);
            // The customer does not exist
            if (customer == null) {
                return false;
            }

            Hotel hotel = (Hotel) rmHotels.query(xid, TABLE_NAME_HOTEL, location);

            // The hotel does not exist or there is no room
            if (hotel == null || hotel.getNumAvail() <= 0) {
                return false;
            }

            // decrease the numAvail first
            hotel.setNumAvail(hotel.getNumAvail() - 1);
            rmHotels.update(xid, TABLE_NAME_HOTEL, hotel.getLocation(), hotel);

            // add a new Reservation
            Reservation reservation = new Reservation(customerName, RESERVATION_TYPE_HOTEL, location);
            rmReservations.insert(xid, TABLE_NAME_RESERVATION, reservation);

            return true;
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "DeadLock in Resource Manager");
        }
    }

    // UN-RESERVE INTERFACE

    // The logic of un-reserve is simple: just update the numAvail = numAvail + 1
    private void unreserveFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        try {
            Flight flight = (Flight) rmFlights.query(xid, TABLE_NAME_FLIGHT, flightNum);
            flight.setNumAvail(flight.getNumAvail() + 1);
            rmFlights.update(xid, TABLE_NAME_FLIGHT, flight.getFlightNum(), flight);
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "Deadlock in Resource Manager");
        }
    }

    private void unreserveRoom(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        try {
            Hotel hotel = (Hotel) rmHotels.query(xid, TABLE_NAME_HOTEL, location);
            hotel.setNumAvail(hotel.getNumAvail() + 1);
            rmHotels.update(xid, TABLE_NAME_HOTEL, hotel.getLocation(), hotel);
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "Deadlock in Resource Manager");
        }
    }

    private void unreserveCar(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {

        try {
            Car car = (Car) rmCars.query(xid, TABLE_NAME_CAR, location);
            car.setNumAvail(car.getNumAvail() + 1);
            rmCars.update(xid, TABLE_NAME_CAR, car.getLocation(), car);
        } catch (DeadlockException e) {
            throw new TransactionAbortedException(xid, "Deadlock in Resource Manager");
        }
    }

    // TECHNICAL/TESTING INTERFACE
    public boolean reconnect()
            throws RemoteException {
        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            rmFlights =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameFlights);
            System.out.println("WC bound to RMFlights");
            rmHotels =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameRooms);
            System.out.println("WC bound to RMRooms");
            rmCars =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameCars);
            System.out.println("WC bound to RMCars");
            rmCustomers =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameCustomers);
            System.out.println("WC bound to RMCustomers");
            rmReservations =
                    (ResourceManager) Naming.lookup(rmiPort +
                            ResourceManager.RMINameReservations);
            System.out.println("WC bound to RMReservations");
            tm =
                    (TransactionManager) Naming.lookup(rmiPort +
                            TransactionManager.RMIName);
            System.out.println("WC bound to TM");
        } catch (Exception e) {
            System.err.println("WC cannot bind to some component:" + e);
            return false;
        }

        try {
            if (rmFlights.reconnect() && rmHotels.reconnect() &&
                    rmCars.reconnect() && rmCustomers.reconnect() && rmReservations.reconnect()) {
                return true;
            }
        } catch (Exception e) {
            System.err.println("Some RM cannot reconnect:" + e);
            return false;
        }

        return false;
    }

    public boolean dieNow(String who)
            throws RemoteException {
        if (who.equals(TransactionManager.RMIName) ||
                who.equals("ALL")) {
            try {
                tm.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameFlights) ||
                who.equals("ALL")) {
            try {
                rmFlights.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameRooms) ||
                who.equals("ALL")) {
            try {
                rmHotels.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameCars) ||
                who.equals("ALL")) {
            try {
                rmCars.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameCustomers) ||
                who.equals("ALL")) {
            try {
                rmCustomers.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(ResourceManager.RMINameReservations) ||
                who.equals("ALL")) {
            try {
                rmReservations.dieNow();
            } catch (RemoteException e) {
            }
        }
        if (who.equals(WorkflowController.RMIName) ||
                who.equals("ALL")) {
            System.exit(1);
        }
        return true;
    }

    public boolean dieRM(String who, String dieTime)
            throws RemoteException {

        // validate the dieTime first
        if (!dieTime.equals(RM_DIE_TIME_AFTER_ENLIST) && !dieTime.equals(RM_DIE_TIME_AFTER_PREPARE)
                && !dieTime.equals(RM_DIE_TIME_BEFORE_PREPARE) && !dieTime.equals(RM_DIE_TIME_BEFORE_COMMIT)
                && !dieTime.equals(RM_DIE_TIME_BEFORE_ABORT)) {
            System.out.println("Invalid die time:" + dieTime);
            return false;
        }

        // Set die time according to who
        switch (who) {
            case ResourceManager.RMINameFlights:
                rmFlights.setDieTime(dieTime);
                break;
            case ResourceManager.RMINameRooms:
                rmHotels.setDieTime(dieTime);
                break;
            case ResourceManager.RMINameCars:
                rmCars.setDieTime(dieTime);
                break;
            case ResourceManager.RMINameCustomers:
                rmCustomers.setDieTime(dieTime);
                break;
            case ResourceManager.RMINameReservations:
                rmReservations.setDieTime(dieTime);
                break;
            default:
                System.out.println("Invalid who string: " + who);
                return false;
        }

        return true;
    }

    public boolean dieTM(String dieTime)
            throws RemoteException {

        if (!dieTime.equals(TM_DIE_TIME_BEFORE_COMMIT) && !dieTime.equals(TM_DIE_TIME_AFTER_COMMIT)) {
            System.out.println("Invalid die time: " + dieTime);
            return false;
        }
        tm.setDieTime(dieTime);
        return true;
    }
}
