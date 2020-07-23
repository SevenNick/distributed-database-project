package test;

import org.junit.Test;
import transaction.InvalidTransactionException;
import transaction.TransactionAbortedException;

import java.rmi.RemoteException;

import static org.junit.Assert.*;

/**
 * This file tests the normal logic of the whole system without failure.
 * <p>
 * Abort is invoked at the end of each test, which ensures the test won't
 * affect the whole system.
 */

public class NormalTest extends ReservationSystemTest {

    @Test
    public void testAddFlightAndQueryFlight() {
        try {
            int xid = wc.start();
            String validFlightNumber = "flight-test";
            String invalidFlightNumber = null;
            int validNumSeats = 500;
            int invalidNumSeats = -1;
            int price = 999;

            // Before addFlight, queryFlight should return -1
            assertEquals(wc.queryFlight(xid, validFlightNumber), -1);
            assertEquals(wc.queryFlight(xid, invalidFlightNumber), -1);

            // If params are invalid, addFlight should fail (i.e., return false).
            assertFalse(wc.addFlight(xid, invalidFlightNumber, validNumSeats, price));
            assertFalse(wc.addFlight(xid, validFlightNumber, invalidNumSeats, price));

            // If params are valid, addFlight should success
            assertTrue(wc.addFlight(xid, validFlightNumber, validNumSeats, price));
            assertEquals(wc.queryFlight(xid, validFlightNumber), validNumSeats);

            // test add numSeats can work correctly
            assertTrue(wc.addFlight(xid, validFlightNumber, validNumSeats, price));
            assertEquals(wc.queryFlight(xid, validFlightNumber), 2 * validNumSeats);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test
    public void testQueryFlightPrice() {
        try {
            int xid = wc.start();
            String validFlightNumber = "flight-test";
            String invalidFlightNumber = null;
            int validNumSeats = 500;
            int price = 999;

            // query non-exist flight should return -1
            assertEquals(wc.queryFlightPrice(xid, validFlightNumber), -1);
            assertEquals(wc.queryFlightPrice(xid, invalidFlightNumber), -1);

            wc.addFlight(xid, validFlightNumber, validNumSeats, price);
            assertEquals(wc.queryFlightPrice(xid, validFlightNumber), price);

            //ensure price was updated correctly
            int newPrice = price + 100;
            wc.addFlight(xid, validFlightNumber, 0, newPrice);
            assertEquals(wc.queryFlightPrice(xid, validFlightNumber), newPrice);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test
    public void testDeleteFlight() {
        try {
            int xid = wc.start();
            String validFlightNumber = "flight-test";
            String invalidFlightNumber = null;
            int validNumSeats = 500;
            int price = 999;

            // delete non-exist flight should fail (i.e., return false)
            assertFalse(wc.deleteFlight(xid, validFlightNumber));
            assertFalse(wc.deleteFlight(xid, invalidFlightNumber));

            wc.addFlight(xid, validFlightNumber, validNumSeats, price);
            assertTrue(wc.deleteFlight(xid, validFlightNumber));
            assertEquals(wc.queryFlight(xid, validFlightNumber), -1);

            // deleteFlight should fail if a customer has a reservation on this flight
            String validCustomerName = "flight-test-customer";
            wc.newCustomer(xid, validCustomerName);
            wc.addFlight(xid, validFlightNumber, validNumSeats, price);
            wc.reserveFlight(xid, validCustomerName, validFlightNumber);
            assertFalse(wc.deleteFlight(xid, validFlightNumber));

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test
    public void testReserveFlight() {
        try {
            int xid = wc.start();
            String validFlightNumber = "flight-test";
            String validCustomerName = "flight-test-customer";
            int validNumSeats = 0;
            int price = 999;

            //reserve non-exist flight should fail (i.e., return false)
            assertFalse(wc.reserveFlight(xid, validCustomerName, validFlightNumber));

            // reserve should fail when there is no seats left
            wc.newCustomer(xid, validCustomerName);
            wc.addFlight(xid, validFlightNumber, validNumSeats, price);
            assertFalse(wc.reserveFlight(xid, validCustomerName, validFlightNumber));

            //add seats to the flight, reserve should success now
            validNumSeats = 100;
            wc.addFlight(xid, validFlightNumber, validNumSeats, price);
            assertTrue(wc.reserveFlight(xid, validCustomerName, validFlightNumber));
            assertEquals(wc.queryFlight(xid, validFlightNumber), validNumSeats - 1);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test(expected = InvalidTransactionException.class)
    public void testAddFlightException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.addFlight(invalidXid, "flight-test", 500, 500);
    }

    @Test(expected = InvalidTransactionException.class)
    public void testDeleteFlightException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.deleteFlight(invalidXid, "flight-test");
    }

    @Test(expected = InvalidTransactionException.class)
    public void testQueryFlightException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.queryFlight(invalidXid, "flight-test");
    }

    @Test(expected = InvalidTransactionException.class)
    public void testQueryFlightPriceException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.queryFlightPrice(invalidXid, "flight-test");
    }

    @Test(expected = InvalidTransactionException.class)
    public void testReserveFlightException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.reserveFlight(invalidXid, "flight-test-customer", "flight-test");
    }

    @Test
    public void testAddRoomsAndQueryRooms() {
        try {
            int xid = wc.start();
            String validLocation = "rooms-test";
            String invalidLocation = null;
            int validNumRooms = 500;
            int invalidNumRooms = -1;
            int price = 999;

            // Before addRooms, queryRooms should return -1
            assertEquals(wc.queryRooms(xid, validLocation), -1);
            assertEquals(wc.queryRooms(xid, invalidLocation), -1);

            // If params are invalid, addRooms should fail (i.e., return false).
            assertFalse(wc.addRooms(xid, invalidLocation, validNumRooms, price));
            assertFalse(wc.addRooms(xid, validLocation, invalidNumRooms, price));

            // If params are valid, addRooms should success
            assertTrue(wc.addRooms(xid, validLocation, validNumRooms, price));
            assertEquals(wc.queryRooms(xid, validLocation), validNumRooms);

            // test add numRooms can work correctly
            assertTrue(wc.addRooms(xid, validLocation, validNumRooms, price));
            assertEquals(wc.queryRooms(xid, validLocation), 2 * validNumRooms);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test
    public void testQueryRoomsPrice() {
        try {
            int xid = wc.start();
            String validLocation = "rooms-test";
            String invalidLocation = null;
            int validNumRooms = 500;
            int price = 999;

            // query non-exist Rooms should return -1
            assertEquals(wc.queryRoomsPrice(xid, validLocation), -1);
            assertEquals(wc.queryRoomsPrice(xid, invalidLocation), -1);

            wc.addRooms(xid, validLocation, validNumRooms, price);
            assertEquals(wc.queryRoomsPrice(xid, validLocation), price);

            //ensure price was updated correctly
            int newPrice = price + 100;
            wc.addRooms(xid, validLocation, 0, newPrice);
            assertEquals(wc.queryRoomsPrice(xid, validLocation), newPrice);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test
    public void testDeleteRooms() {
        try {
            int xid = wc.start();
            String validLocation = "rooms-test";
            String invalidLocation = null;
            int validNumRooms = 500;
            int price = 999;

            // delete non-exist rooms should fail (i.e., return false)
            assertFalse(wc.deleteRooms(xid, validLocation, validNumRooms));
            assertFalse(wc.deleteRooms(xid, invalidLocation, validNumRooms));

            wc.addRooms(xid, validLocation, validNumRooms, price);

            // if deleteNumRooms > totalNumRooms, deleteRooms should fail
            int deleteNumRooms = validNumRooms + 1;
            assertFalse(wc.deleteRooms(xid, validLocation, deleteNumRooms));

            // delete all the rooms
            assertTrue(wc.deleteRooms(xid, validLocation, validNumRooms));
            assertEquals(wc.queryRooms(xid, validLocation), 0);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test
    public void testReserveRoom() {
        try {
            int xid = wc.start();
            String validLocation = "rooms-test";
            String validCustomerName = "rooms-test-customer";
            int validNumRooms = 0;
            int price = 999;

            //reserve non-exist rooms should fail (i.e., return false)
            assertFalse(wc.reserveRoom(xid, validCustomerName, validLocation));

            // reserve should fail when there is no rooms left
            wc.newCustomer(xid, validCustomerName);
            wc.addRooms(xid, validLocation, validNumRooms, price);
            assertFalse(wc.reserveRoom(xid, validCustomerName, validLocation));

            //add rooms, reserve should success now
            validNumRooms = 100;
            wc.addRooms(xid, validLocation, validNumRooms, price);
            assertTrue(wc.reserveRoom(xid, validCustomerName, validLocation));
            assertEquals(wc.queryRooms(xid, validLocation), validNumRooms - 1);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test(expected = InvalidTransactionException.class)
    public void testAddRoomsException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.addRooms(invalidXid, "rooms-test", 500, 500);
    }

    @Test(expected = InvalidTransactionException.class)
    public void testDeleteRoomsException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.deleteRooms(invalidXid, "rooms-test", 500);
    }

    @Test(expected = InvalidTransactionException.class)
    public void testQueryRoomsException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.queryRooms(invalidXid, "rooms-test");
    }

    @Test(expected = InvalidTransactionException.class)
    public void testQueryRoomPriceException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.queryRoomsPrice(invalidXid, "rooms-test");
    }

    @Test(expected = InvalidTransactionException.class)
    public void testReserveRoomException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.reserveRoom(invalidXid, "rooms-test-customer", "rooms-test");
    }

    @Test
    public void testAddCarsAndQueryCars() {
        try {
            int xid = wc.start();
            String validLocation = "cars-test";
            String invalidLocation = null;
            int validNumCars = 500;
            int invalidNumCars = -1;
            int price = 999;

            // Before addCars, queryCars should return -1
            assertEquals(wc.queryCars(xid, validLocation), -1);
            assertEquals(wc.queryCars(xid, invalidLocation), -1);

            // If params are invalid, addCars should fail (i.e., return false).
            assertFalse(wc.addCars(xid, invalidLocation, validNumCars, price));
            assertFalse(wc.addCars(xid, validLocation, invalidNumCars, price));

            // If params are valid, addCars should success
            assertTrue(wc.addCars(xid, validLocation, validNumCars, price));
            assertEquals(wc.queryCars(xid, validLocation), validNumCars);

            // test add numCars can work correctly
            assertTrue(wc.addCars(xid, validLocation, validNumCars, price));
            assertEquals(wc.queryCars(xid, validLocation), 2 * validNumCars);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test
    public void testQueryCarsPrice() {
        try {
            int xid = wc.start();
            String validLocation = "cars-test";
            String invalidLocation = null;
            int validNumCars = 500;
            int price = 999;

            // query non-exist Cars should return -1
            assertEquals(wc.queryCarsPrice(xid, validLocation), -1);
            assertEquals(wc.queryCarsPrice(xid, invalidLocation), -1);

            wc.addCars(xid, validLocation, validNumCars, price);
            assertEquals(wc.queryCarsPrice(xid, validLocation), price);

            //ensure price was updated correctly
            int newPrice = price + 100;
            wc.addCars(xid, validLocation, 0, newPrice);
            assertEquals(wc.queryCarsPrice(xid, validLocation), newPrice);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test
    public void testDeleteCars() {
        try {
            int xid = wc.start();
            String validLocation = "cars-test";
            String invalidLocation = null;
            int validNumCars = 500;
            int price = 999;

            // delete non-exist cars should fail (i.e., return false)
            assertFalse(wc.deleteCars(xid, validLocation, validNumCars));
            assertFalse(wc.deleteCars(xid, invalidLocation, validNumCars));

            wc.addCars(xid, validLocation, validNumCars, price);

            // if deleteNumCars > totalNumCars, deleteCars should fail
            int deleteNumCars = validNumCars + 1;
            assertFalse(wc.deleteCars(xid, validLocation, deleteNumCars));

            // delete all the cars
            assertTrue(wc.deleteCars(xid, validLocation, validNumCars));
            assertEquals(wc.queryCars(xid, validLocation), 0);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test
    public void testReserveCar() {
        try {
            int xid = wc.start();
            String validLocation = "cars-test";
            String validCustomerName = "cars-test-customer";
            int validNumCars = 0;
            int price = 999;

            //reserve non-exist cars should fail (i.e., return false)
            assertFalse(wc.reserveCar(xid, validCustomerName, validLocation));

            // reserve should fail when there is no cars left
            wc.newCustomer(xid, validCustomerName);
            wc.addCars(xid, validLocation, validNumCars, price);
            assertFalse(wc.reserveCar(xid, validCustomerName, validLocation));

            //add cars, reserve should success now
            validNumCars = 100;
            wc.addCars(xid, validLocation, validNumCars, price);
            assertTrue(wc.reserveCar(xid, validCustomerName, validLocation));
            assertEquals(wc.queryCars(xid, validLocation), validNumCars - 1);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test(expected = InvalidTransactionException.class)
    public void testAddCarsException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.addCars(invalidXid, "cars-test", 500, 500);
    }

    @Test(expected = InvalidTransactionException.class)
    public void testDeleteCarsException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.deleteCars(invalidXid, "cars-test", 500);
    }

    @Test(expected = InvalidTransactionException.class)
    public void testQueryCarsException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.queryCars(invalidXid, "cars-test");
    }

    @Test(expected = InvalidTransactionException.class)
    public void testQueryCarPriceException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.queryCarsPrice(invalidXid, "cars-test");
    }

    @Test(expected = InvalidTransactionException.class)
    public void testReserveCarException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.reserveCar(invalidXid, "cars-test-customer", "cars-test");
    }

    @Test
    public void testNewCustomer() {
        try {
            int xid = wc.start();
            String validCustomerName = "customer-test";
            String invalidCustomerName = null;

            assertFalse(wc.newCustomer(xid, invalidCustomerName));
            assertTrue(wc.newCustomer(xid, validCustomerName));
            assertTrue(wc.newCustomer(xid, validCustomerName));

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test
    public void testDeleteCustomer() {
        try {
            int xid = wc.start();
            String validCustomerName = "customer-test";
            String invalidCustomerName = null;

            //delete non-exist customer should fail (i.e., return false)
            assertFalse(wc.deleteCustomer(xid, validCustomerName));
            assertFalse(wc.deleteCustomer(xid, invalidCustomerName));

            wc.newCustomer(xid, validCustomerName);
            assertTrue(wc.deleteCustomer(xid, validCustomerName));
            assertFalse(wc.deleteCustomer(xid, validCustomerName));

            // when delete customer, associated reservations should be un-reserved
            String validFlightNumber = "customer-test-flight";
            String validLocation = "customer-test-room-location";
            int num = 100;
            int price = 999;
            wc.newCustomer(xid, validCustomerName);
            wc.addFlight(xid, validFlightNumber, num, price);
            wc.addRooms(xid, validLocation, num, price);
            wc.reserveFlight(xid, validCustomerName, validFlightNumber);
            wc.reserveRoom(xid, validCustomerName, validLocation);

            assertEquals(wc.queryFlight(xid, validFlightNumber), num - 1);
            assertEquals(wc.queryRooms(xid, validLocation), num - 1);

            assertTrue(wc.deleteCustomer(xid, validCustomerName));

            assertEquals(wc.queryFlight(xid, validFlightNumber), num);
            assertEquals(wc.queryRooms(xid, validLocation), num);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test
    public void testQueryCustomerBill() {
        try {
            int xid = wc.start();
            String validCustomerName = "customer-test";
            String invalidCustomerName = null;
            String validFlightNumber = "customer-test-flight";
            String validRoomsAndCarsLocation = "customer-test-rooms-and-cars";
            int num = 100;
            int price = 100;

            // if customer does not exist, queryCustomerBill should return -1
            assertEquals(wc.queryCustomerBill(xid, validCustomerName), -1);
            assertEquals(wc.queryCustomerBill(xid, invalidCustomerName), -1);

            wc.newCustomer(xid, validCustomerName);
            wc.addFlight(xid, validFlightNumber, num, price);
            wc.addRooms(xid, validRoomsAndCarsLocation, num, price);
            wc.addCars(xid, validRoomsAndCarsLocation, num, price);

            wc.reserveFlight(xid, validCustomerName, validFlightNumber);
            assertEquals(wc.queryCustomerBill(xid, validCustomerName), price);

            wc.reserveRoom(xid, validCustomerName, validRoomsAndCarsLocation);
            assertEquals(wc.queryCustomerBill(xid, validCustomerName), 2 * price);

            wc.reserveCar(xid, validCustomerName, validRoomsAndCarsLocation);
            assertEquals(wc.queryCustomerBill(xid, validCustomerName), 3 * price);

            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    @Test(expected = InvalidTransactionException.class)
    public void testNewCustomerException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.newCustomer(invalidXid, "customer-test");
    }

    @Test(expected = InvalidTransactionException.class)
    public void testDeleteCustomerException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.deleteCustomer(invalidXid, "customer-test");
    }

    @Test(expected = InvalidTransactionException.class)
    public void testQueryCustomerBillException()
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        int invalidXid = -1;
        wc.queryCustomerBill(invalidXid, "customer-test");
    }
}
