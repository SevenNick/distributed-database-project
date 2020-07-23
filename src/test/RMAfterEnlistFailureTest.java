package test;


import org.junit.Test;

import java.rmi.RemoteException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static transaction.ResourceManager.RMINameRooms;
import static transaction.WorkflowController.RM_DIE_TIME_AFTER_ENLIST;

/**
 * This file tests the failure: RM dies after enlist.
 * <p>
 * When such a situation happens, the corresponding transaction should be aborted.
 * <p>
 * TODO: 比如xid=1失败后恢复了，在TM命令行看不到TX 1 is terminated
 */
public class RMAfterEnlistFailureTest extends ReservationSystemTest {

    @Test
    public void testFailureAfterEnlist() {
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
}
