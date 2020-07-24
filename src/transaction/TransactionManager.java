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
    /**
     * The RMI name a TransactionManager binds to.
     */
    public static final String RMIName = "TM";

    public boolean dieNow()
            throws RemoteException;

    public void setDieTime(String time) throws RemoteException;

    public void ping() throws RemoteException;


    // Below is add by xsh because TM need them!

    public void enlist(int xid, ResourceManager rm) throws RemoteException, InvalidTransactionException;

    int start() throws RemoteException;

    boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException;

    void abort(int xid) throws RemoteException, InvalidTransactionException;

}
