package test;

import org.junit.Test;
import transaction.WorkflowController;

import static org.junit.Assert.*;
import static transaction.ResourceManager.RMINameFlights;

/**
 * This file tests the failure: RM dies before commit.
 * <p>
 * When such a situation happens, the corresponding transaction should be committed.
 * <p>
 * TODO: 这个Test不通过
 */
public class RMBeforeCommitFailureTest extends ReservationSystemTest {

    @Test
    public void testBeforeCommitFailure() {
        int xid;
        String RMKey = "failure-before-commit-test";
        int num = 100;
        int price = 999;
        try {
            xid = wc.start();
            wc.addFlight(xid, RMKey, num, price);
            wc.newCustomer(xid, RMKey);
            wc.reserveFlight(xid, RMKey, RMKey);
            wc.dieRM(RMINameFlights, WorkflowController.RM_DIE_TIME_BEFORE_COMMIT);
            // RMFlights will die, but commit should success (i.e., return true)
            assertTrue(wc.commit(xid));

            countDown(30);
            wc.reconnect();

            xid = wc.start();
            // ensure corresponding operations are commit
            assertEquals(wc.queryFlight(xid, RMKey), num - 1);
            assertEquals(wc.queryCustomerBill(xid, RMKey), price);
        } catch (Exception e) {
            fail("Unexpected Exception happens...\n  " + e.getMessage());
        }
    }
}
