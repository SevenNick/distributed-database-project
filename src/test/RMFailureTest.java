package test;

import org.junit.After;
import org.junit.Test;
import transaction.WorkflowController;

import java.rmi.RemoteException;

import static org.junit.Assert.*;
import static transaction.ResourceManager.*;
import static transaction.WorkflowController.*;

/**
 * This file tests the whole system under various RM failures.
 * <p>
 */
public class RMFailureTest extends ReservationSystemTest {


    @After
    public void printMsg() {
        System.out.println("Test Complete...\n\n");
    }

    /**
     * When RM failed after enlist, the corresponding transaction should be aborted eventually.
     */
    @Test
    public void testFailureAfterEnlist() {
        System.out.printf("\nTesting RM Failure...(DieTime: %s)\n", RM_DIE_TIME_AFTER_ENLIST);

        int xid = -1;
        String RMKey = "failure-after-enlist-test";
        int num = 100;
        int price = 999;
        try {
            xid = wc.start();
            wc.addFlight(xid, RMKey, num, price);
            wc.dieRM(RMINameRooms, RM_DIE_TIME_AFTER_ENLIST);
            // should throw RemoteException because RMRooms die
            wc.addRooms(xid, RMKey, num, price);
            fail("RMRooms should fail and throw RemoteException");
        } catch (RemoteException remoteException) {
            try {
                wc.abort(xid);
                System.out.printf("Failed Component: %s\n", RMINameRooms);
                countDown(30);
                wc.reconnect();

                xid = wc.start();
                // ensure corresponding operations are aborted
                assertEquals(wc.queryFlight(xid, RMKey), -1);
                assertEquals(wc.queryRooms(xid, RMKey), -1);
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
     * When RM failed before prepare, the corresponding transaction should be aborted eventually.
     */
    @Test
    public void testFailureBeforePrepare() {
        System.out.printf("\nTesting RM Failure...(DieTime: %s)\n", RM_DIE_TIME_BEFORE_PREPARE);

        int xid;
        String RMKey = "failure-before-prepare-test";
        int num = 100;
        int price = 999;
        try {
            xid = wc.start();
            wc.addFlight(xid, RMKey, num, price);
            wc.addCars(xid, RMKey, num, price);
            wc.dieRM(RMINameCars, WorkflowController.RM_DIE_TIME_BEFORE_PREPARE);
            // RMCars will die, commit should fail (i.e., return false)
            assertFalse(wc.commit(xid));

            System.out.printf("Failed Component: %s\n", RMINameCars);
            countDown(30);
            wc.reconnect();

            xid = wc.start();
            // ensure corresponding operations are aborted
            assertEquals(wc.queryFlight(xid, RMKey), -1);
            assertEquals(wc.queryCars(xid, RMKey), -1);
            wc.abort(xid);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception happens...");
        }
    }

    /**
     * Because TM does not receive the "prepare OK" message from the RM,
     * we decide to abort the corresponding transaction when RM failed after prepare.
     */
    @Test
    public void testFailureAfterPrepare() {
        System.out.printf("\nTesting RM Failure...(DieTime: %s)\n", RM_DIE_TIME_AFTER_PREPARE);

        int xid;
        String RMKey = "failure-after-prepare-test";
        int num = 100;
        int price = 999;
        try {
            xid = wc.start();
            wc.addFlight(xid, RMKey, num, price);
            wc.addRooms(xid, RMKey, num, price);
            wc.dieRM(RMINameFlights, WorkflowController.RM_DIE_TIME_AFTER_PREPARE);
            // RMFlights will die, commit should fail (i.e., return false)
            assertFalse(wc.commit(xid));

            System.out.printf("Failed Component: %s\n", RMINameFlights);
            countDown(30);
            wc.reconnect();

            xid = wc.start();
            // ensure corresponding operations are aborted
            assertEquals(wc.queryFlight(xid, RMKey), -1);
            assertEquals(wc.queryRooms(xid, RMKey), -1);
            wc.abort(xid);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception happens...");
        }
    }

    /**
     * when RM failed before commit., the corresponding transaction should be committed eventually.
     */
    @Test
    public void testBeforeCommitFailure() {
        System.out.printf("\nTesting RM Failure...(DieTime: %s)\n", RM_DIE_TIME_BEFORE_COMMIT);

        int xid;
        String RMKey = "failure-before-commit-test";
        int num = 100;
        int price = 999;
        try {
            xid = wc.start();
            wc.addFlight(xid, RMKey, num, price);
            wc.newCustomer(xid, RMKey);
            wc.reserveFlight(xid, RMKey, RMKey);
            wc.dieRM(RMINameReservations, WorkflowController.RM_DIE_TIME_BEFORE_COMMIT);
            // RMFlights will die, but commit should success (i.e., return true)
            assertTrue(wc.commit(xid));

            System.out.printf("Failed Component: %s\n", RMINameReservations);
            countDown(30);
            wc.reconnect();

            xid = wc.start();
            // ensure corresponding operations are commit
            assertEquals(wc.queryFlight(xid, RMKey), num - 1);
            assertEquals(wc.queryCustomerBill(xid, RMKey), price);
            wc.abort(xid);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }

    /**
     * when RM failed before abort, the corresponding transaction should be aborted eventually.
     */
    @Test
    public void testFailureBeforeAbort() {
        System.out.printf("\nTesting RM Failure...(DieTime: %s)\n", RM_DIE_TIME_BEFORE_ABORT);

        int xid;
        String RMKey = "failure-before-abort-test";
        try {
            xid = wc.start();
            wc.newCustomer(xid, RMKey);
            wc.dieRM(RMINameCustomers, WorkflowController.RM_DIE_TIME_BEFORE_ABORT);
            // RMFlights will die, but abort should success
            wc.abort(xid);

            System.out.printf("Failed Component: %s\n", RMINameCustomers);
            countDown(30);
            wc.reconnect();

            xid = wc.start();
            // ensure corresponding operations are aborted
            assertEquals(wc.queryCustomerBill(xid, RMKey), -1);
            wc.abort(xid);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception happens...");
        }
    }
}
