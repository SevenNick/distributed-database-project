/*
 * Created on 2005-5-29
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package transaction;

import java.rmi.*;

/**
 * @author Administrator
 * <p>
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class RMManagerCars {
    public static void main(String[] args) {
        String rmiName = ResourceManager.RMINameCars;
        if (rmiName == null || rmiName.equals("")) {
            System.err.println("No RMI name given");
            System.exit(1);
        }

        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        ResourceManagerImpl obj = null;
        try {
            obj = new ResourceManagerImpl(rmiName);
            Naming.rebind(rmiPort + rmiName, obj);
            System.out.println(rmiName + " bound");
        } catch (Exception e) {
            System.err.println(rmiName + " not bound:" + e);
            System.exit(1);
        }

    }
}
