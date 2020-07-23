package test;

import org.junit.Test;
import transaction.WorkflowController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static transaction.ResourceManager.RMINameFlights;

/**
 * This file tests the failure: RM dies before abort.
 * <p>
 * When such a situation happens, the corresponding transaction should be aborted.
 */
public class RMBeforeAbortFailureTest extends ReservationSystemTest {
    @Test
    public void testFailureBeforeAbort() {
        int xid;
        String RMKey = "failure-before-abort-test";
        int num = 100;
        int price = 999;
        try {
            xid = wc.start();
            wc.addFlight(xid, RMKey, num, price);
            wc.addRooms(xid, RMKey, num, price);
            wc.dieRM(RMINameFlights, WorkflowController.RM_DIE_TIME_BEFORE_ABORT);

            // RMFlights will die, but abort should success
            wc.abort(xid);

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
