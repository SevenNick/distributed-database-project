package test;

import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static transaction.TransactionManager.RMIName;
import static transaction.WorkflowController.TM_DIE_TIME_AFTER_COMMIT;
import static transaction.WorkflowController.TM_DIE_TIME_BEFORE_COMMIT;

public class TMFailureTest extends ReservationSystemTest {
    private int xid;
    private String rmKey;
    private int num;
    private int price;

    @Before
    public void reserve() {
        try {
            xid = wc.start();
            rmKey = "TM-failure-test-" + UUID.randomUUID(); //ensure every time rmKey is different
            num = 100;
            price = 999;
            wc.newCustomer(xid, rmKey);
            wc.addFlight(xid, rmKey, num, price);
            wc.reserveFlight(xid, rmKey, rmKey);
            wc.addRooms(xid, rmKey, num, price);
            wc.reserveRoom(xid, rmKey, rmKey);
            wc.addCars(xid, rmKey, num, price);
            wc.reserveCar(xid, rmKey, rmKey);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception happens...");
        }
    }

    /**
     * When TM failed before commit, all proceeding transactions should be aborted eventually.
     */
    @Test
    public void testFailureBeforeCommit() {
        System.out.printf("\nTesting TM Failure...(DieTime: %s)\n", TM_DIE_TIME_BEFORE_COMMIT);

        try {
            wc.dieTM(TM_DIE_TIME_BEFORE_COMMIT);
            wc.commit(xid);
            fail("TM should fail and throw RemoteException");
        } catch (RemoteException remoteException) {
            try {
                System.out.printf("Failed Component: %s\n", RMIName);
                countDown(-1);
                wc.reconnect();

                xid = wc.start();

                //ensure corresponding operations are aborted
                assertEquals(wc.queryFlight(xid, rmKey), -1);
                assertEquals(wc.queryRooms(xid, rmKey), -1);
                assertEquals(wc.queryCars(xid, rmKey), -1);
                assertEquals(wc.queryCustomerBill(xid, rmKey), -1);
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

    /**
     * When TM failed after commit, all proceeding transactions should be committed eventually.
     */
    @Test
    public void testFailureAfterCommit() {
        System.out.printf("\nTesting TM Failure...(DieTime: %s)\n", TM_DIE_TIME_AFTER_COMMIT);

        try {
            wc.dieTM(TM_DIE_TIME_AFTER_COMMIT);
            wc.commit(xid);
            fail("TM should fail and throw RemoteException");
        } catch (RemoteException remoteException) {
            try {
                System.out.printf("Failed Component: %s\n", RMIName);
                countDown(-1);
                wc.reconnect();

                xid = wc.start();

                //ensure corresponding operations are committed
                assertEquals(wc.queryFlight(xid, rmKey), num - 1);
                assertEquals(wc.queryRooms(xid, rmKey), num - 1);
                assertEquals(wc.queryCars(xid, rmKey), num - 1);
                assertEquals(wc.queryCustomerBill(xid, rmKey), price * 3);
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
