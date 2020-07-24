package test;

import org.junit.Test;

import java.rmi.RemoteException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static transaction.ResourceManager.*;

public class SystemFailureTest extends ReservationSystemTest {

    /**
     * When the whole system is failed, the ACID should also be held after the system was restarted.
     */
    @Test
    public void testSystemFailure() {
        int xid;
        //because rmKey1 will be committed, it should be different in every test
        String rmKey1 = "committed-transaction" + UUID.randomUUID();
        String rmKey2 = "aborted-transaction";
        String rmKey3 = "un-finished-transaction";
        int num = 100;
        int price = 999;

        try {
            //commit a transaction
            xid = wc.start();
            wc.newCustomer(xid, rmKey1);
            wc.addFlight(xid, rmKey1, num, price);
            wc.reserveFlight(xid, rmKey1, rmKey1);
            wc.addRooms(xid, rmKey1, num, price);
            wc.reserveRoom(xid, rmKey1, rmKey1);
            wc.addCars(xid, rmKey1, num, price);
            wc.reserveCar(xid, rmKey1, rmKey1);
            wc.commit(xid);

            //abort a transaction
            xid = wc.start();
            wc.newCustomer(xid, rmKey2);
            wc.addFlight(xid, rmKey2, num, price);
            wc.reserveFlight(xid, rmKey2, rmKey2);
            wc.addRooms(xid, rmKey2, num, price);
            wc.reserveRoom(xid, rmKey2, rmKey2);
            wc.addCars(xid, rmKey2, num, price);
            wc.reserveCar(xid, rmKey2, rmKey2);
            wc.abort(xid);

            // execute a part of a transaction and kill the whole system
            xid = wc.start();
            wc.newCustomer(xid, rmKey3);
            wc.addFlight(xid, rmKey3, num, price);
            wc.reserveFlight(xid, rmKey3, rmKey3);
            wc.addRooms(xid, rmKey3, num, price);
            wc.dieNow("ALL");
            wc.reserveRoom(xid, rmKey3, rmKey3);
            fail("The whole system should fail and throw RemoteException");
        } catch (RemoteException remoteException) {
            try {
                System.out.printf("\nFailed Components: %s %s %s %s %s %s %s\n",
                        "TM", RMINameFlights, RMINameRooms, RMINameCars, RMINameCustomers, RMINameReservations, "WC");
                countDown(60);

                // re-get the wc
                getWorkflowController();

                // rmKey1 should exist in the system
                xid = wc.start();
                assertEquals(wc.queryFlight(xid, rmKey1), num - 1);
                assertEquals(wc.queryRooms(xid, rmKey1), num - 1);
                assertEquals(wc.queryCars(xid, rmKey1), num - 1);
                assertEquals(wc.queryCustomerBill(xid, rmKey1), price * 3);

                // rmKey2 & rmKey3 should not exist in the system
                assertEquals(wc.queryFlight(xid, rmKey2), -1);
                assertEquals(wc.queryRooms(xid, rmKey2), -1);
                assertEquals(wc.queryCars(xid, rmKey2), -1);
                assertEquals(wc.queryCustomerBill(xid, rmKey2), -1);

                assertEquals(wc.queryFlight(xid, rmKey3), -1);
                assertEquals(wc.queryRooms(xid, rmKey3), -1);
                assertEquals(wc.queryCars(xid, rmKey3), -1);
                assertEquals(wc.queryCustomerBill(xid, rmKey3), -1);

                wc.abort(xid);
            } catch (Exception e) {
                e.printStackTrace();
                fail("Unexpected Exception happens...");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception happens...");
        }
    }
}
