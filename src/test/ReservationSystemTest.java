package test;

import org.junit.BeforeClass;
import transaction.WorkflowController;

import java.rmi.Naming;

public class ReservationSystemTest {
    static WorkflowController wc;

    @BeforeClass
    public static void getWorkflowController() {
        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            wc = (WorkflowController) Naming.lookup(rmiPort + WorkflowController.RMIName);
        } catch (Exception e) {
            System.exit(1);
        }
    }

    void countDown(int seconds) {
        try {
            while (seconds >= 0) {
                System.out.print("\r");
                System.out.printf("Please restart the failed components! Time left: %3ds", seconds--);
                Thread.sleep(1000);
            }
            System.out.println();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
