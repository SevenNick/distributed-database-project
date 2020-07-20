package transaction;

import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import static transaction.WorkflowController.TM_DIE_TIME_AFTER_COMMIT;
import static transaction.WorkflowController.TM_DIE_TIME_BEFORE_COMMIT;

/**
 * Transaction Manager for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl
        extends java.rmi.server.UnicastRemoteObject
        implements TransactionManager, Serializable {
    private static final String TM_LOG_FILENAME = "TM.log";

    private String dieTime;
    private int curXid;
    private Map<Integer, Transaction> txs;

    enum TransactionState {
        proceeding,
        commit,
        abort
    }

    class Transaction {
        int xid;
        Set<ResourceManager> rms;
        TransactionState state;

        Transaction(int xid) {
            this.xid = xid;
            this.rms = new HashSet<>();
            this.state = TransactionState.proceeding;
        }

        synchronized void enlist(ResourceManager rm) throws InvalidTransactionException, RemoteException {
            if (this.state == TransactionState.proceeding) {
                rms.add(rm);
            } else if (this.state == TransactionState.commit && rms.contains(rm)) {
                rm.commit(this.xid);
                rms.remove(rm);
                checkDone();
            } else if (this.state == TransactionState.abort && rms.contains(rm)) {
                rm.abort(this.xid);
                rms.remove(rm);
                checkDone();
            }
        }

        synchronized void checkDone() { // TODO: need rename
            if (rms.isEmpty())
                txs.remove(xid);
        }

        synchronized boolean prepare() throws InvalidTransactionException, RemoteException {
            boolean ret = true;
            for (ResourceManager rm : rms)
                ret &= rm.prepare(xid);
            return ret;
        }

        synchronized void commit() {
            this.state = TransactionState.commit;
            terminate();
        }

        synchronized void terminate() {
            Set<ResourceManager> toRemove = new HashSet<>();
            for (ResourceManager rm : rms) {
                try {
                    if (this.state == TransactionState.abort)
                        rm.abort(xid);
                    else if (this.state == TransactionState.commit)
                        rm.commit(xid);
                    toRemove.add(rm);
                } catch (RemoteException | InvalidTransactionException ignored) {
                }
            }
            rms.removeAll(toRemove);
            checkDone();
        }

        synchronized void abort() {
            this.state = TransactionState.abort;
            terminate();
        }
    }

    private Transaction getTx(int xid) throws InvalidTransactionException {
        if (xid < 0)
            throw new InvalidTransactionException(xid, "Xid must be positive");
        if (txs.containsKey(xid))
            throw new InvalidTransactionException(xid, "Xid is not exist");
        return txs.get(xid);
    }

    public TransactionManagerImpl() throws RemoteException {
        curXid = 1;
        txs = new Hashtable<>();
        loadLog();
        recover();
    }

    // load from tm log;
    private void loadLog() {
        File logFile = new File(TM_LOG_FILENAME);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(logFile))) {
            TransactionManagerImpl tmLog = (TransactionManagerImpl) ois.readObject();
            this.dieTime = tmLog.dieTime;
            this.txs = tmLog.txs;
            this.curXid = tmLog.curXid;
        } catch (IOException | ClassNotFoundException ignore) {
        }
    }

    private void recover() {
        for (Transaction tx : txs.values()) {
            tx.terminate();
        }
    }

    private void storeLog() {
        File logFile = new File(TM_LOG_FILENAME);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(logFile))) {
            oos.writeObject(this);
            oos.flush();
        } catch (IOException ignore) {
        }
    }

    @Override
    public synchronized int start() throws RemoteException {
        int xid = curXid++;
        txs.put(xid, new Transaction(xid));
        storeLog();
        return xid;
    }

    @Override
    public void enlist(int xid, ResourceManager rm) throws RemoteException, InvalidTransactionException {
        Transaction tx = getTx(xid);
        tx.enlist(rm);
        storeLog();
    }

    // TODO: must transaction immediately abort if it is failed to commit?

    @Override
    public boolean commit(int xid) throws RemoteException, TransactionAbortedException, InvalidTransactionException {
        if (dieTime.equals(TM_DIE_TIME_BEFORE_COMMIT))
            dieNow();

        Transaction tx = getTx(xid);
        boolean prepared = tx.prepare();
        if (prepared)
            tx.commit();
        else
            tx.abort();
        storeLog();
//        else throw new TransactionAbortedException(xid, "tx aborted");

        if (dieTime.equals(TM_DIE_TIME_AFTER_COMMIT))
            dieNow();

        return prepared;
    }


    @Override
    public void abort(int xid) throws RemoteException, InvalidTransactionException {
        Transaction tx = getTx(xid);
        tx.abort();
        storeLog();
    }

    public boolean dieNow() throws RemoteException {
        System.exit(1);
        // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
        return true;
    }

    @Override
    public void setDieTime(String time) throws RemoteException {
        this.dieTime = time;
        System.out.println("Die time set to : " + time);
    }

    public void ping() throws RemoteException {
    }

    public static void main(String[] args) {
        String rmiPort = System.getProperty("rmiPort");
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        try {
            TransactionManagerImpl obj = new TransactionManagerImpl();
            Naming.rebind(rmiPort + TransactionManager.RMIName, obj);
            System.out.println("TM bound");
        } catch (Exception e) {
            System.err.println("TM not bound:" + e);
            System.exit(1);
        }
    }
}
