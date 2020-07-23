package test;

import org.junit.Test;
import transaction.WorkflowController;

import static org.junit.Assert.*;
import static transaction.ResourceManager.RMINameCars;

/**
 * This file tests the failure: RM dies before prepare.
 * <p>
 * When such a situation happens, the corresponding transaction should be aborted.
 */
public class RMBeforePrepareFailureTest extends ReservationSystemTest {

    @Test
    public void testFailureBeforePrepare() {
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

            countDown(30);
            wc.reconnect();

            xid = wc.start();
            // ensure corresponding operations are aborted
            assertEquals(wc.queryFlight(xid, RMKey), -1);
            assertEquals(wc.queryCars(xid, RMKey), -1);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception happens...");
        }
    }
}
