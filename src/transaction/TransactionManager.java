package transaction;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for the Transaction Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */

public interface TransactionManager extends Remote {

    public boolean dieNow()
            throws RemoteException;

    public void ping() throws RemoteException;

    public void enlist(int xid, ResourceManager rm) throws RemoteException;


    /**
     * The RMI name a TransactionManager binds to.
     */
    public static final String RMIName = "TM";

    // Below is add by xsh because TM need them!

    int start() throws RemoteException;

    boolean commit() throws RemoteException, TransactionAbortedException, InvalidTransactionException;

    void abort() throws RemoteException, InvalidTransactionException;

}
