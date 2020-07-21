package transaction;

import java.rmi.Naming;

/**
 * A toy client of the Distributed Travel Reservation System.
 */

public class Client {

    public static void main(String args[]) {
        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        WorkflowController wc = null;
        try {
            wc = (WorkflowController) Naming.lookup(rmiPort + WorkflowController.RMIName);
            System.out.println("Bound to WC");
        } catch (Exception e) {
            System.err.println("Cannot bind to WC:" + e);
            System.exit(1);
        }

        try {
            int xid = wc.start();

            String flightNum = "347";
            String customerName = "John";

            if (!wc.newCustomer(xid, customerName)) {
                System.err.println("Add customer failed");
            }

            System.out.println("Flight 347 has " + wc.queryFlight(xid, flightNum) + " seats.");
            if (!wc.reserveFlight(xid, customerName, flightNum)) {
                System.err.println("Reserve flight failed");
            }
            System.out.println("Flight 347 now has " + wc.queryFlight(xid, flightNum) + " seats.");
            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

        } catch (Exception e) {
            System.err.println("Received exception:" + e);
            System.exit(1);
        }

    }
}
