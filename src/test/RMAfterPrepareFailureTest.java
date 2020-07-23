package test;

import org.junit.Test;
import transaction.WorkflowController;

import static org.junit.Assert.*;
import static transaction.ResourceManager.RMINameFlights;

/**
 * This file tests the failure: RM dies after prepare.
 * <p>
 * Because TM does not receive the "prepare OK" message from the RM,
 * we decide to abort the corresponding transaction.
 * <p>
 * When such a situation happens, the corresponding transaction should be aborted.
 */
public class RMAfterPrepareFailureTest extends ReservationSystemTest {

    @Test
    public void testFailureAfterPrepare() {
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

            countDown(30);
            wc.reconnect();

            xid = wc.start();
            // ensure corresponding operations are aborted
            assertEquals(wc.queryFlight(xid, RMKey), -1);
            assertEquals(wc.queryRooms(xid, RMKey), -1);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception happens...");
        }
    }
}
