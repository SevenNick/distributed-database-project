package transaction;

import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

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
    private static final String TM_LOG_FILENAME = "data/TM.log";

    private transient String dieTime;
    private int curXid;
    private Map<Integer, Transaction> txs;

    public TransactionManagerImpl() throws RemoteException {
        curXid = 1;
        txs = new Hashtable<>();

        loadLog();
        recover();

        dieTime = "NoDie";

        System.out.println(this);
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

    private Transaction getTx(int xid) throws InvalidTransactionException {
        if (xid < 0) throw new InvalidTransactionException(xid, "Xid must be positive");
        if (!txs.containsKey(xid)) throw new InvalidTransactionException(xid, "Xid is not exist");
        return txs.get(xid);
    }

    @Override
    public String toString() {
        return "TransactionManagerImpl{" +
                "dieTime='" + dieTime + '\'' +
                ", curXid=" + curXid +
                ", txs=" + txs +
                '}';
    }

    private void loadLog() {
        File logFile = new File(TM_LOG_FILENAME);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(logFile))) {
            TransactionManagerImpl tmLog = (TransactionManagerImpl) ois.readObject();
            this.dieTime = tmLog.dieTime;
            this.txs = tmLog.txs;
            this.curXid = tmLog.curXid;
        } catch (IOException ignored) {
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void recover() {
        for (Transaction tx : txs.values())
            tx.recover();
    }

    private synchronized void storeLog() {
        File logFile = new File(TM_LOG_FILENAME);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(logFile))) {
            oos.writeObject(this);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized int start() {
        int xid = curXid++;
        txs.put(xid, new Transaction(xid));
        storeLog();
        System.out.println("Start TX " + xid);
        return xid;
    }

    @Override
    public void enlist(int xid, ResourceManager rm) throws RemoteException, InvalidTransactionException {
        Transaction tx = getTx(xid);
        tx.enlist(rm);
        storeLog();
    }

    @Override
    public boolean commit(int xid) throws RemoteException, InvalidTransactionException {
        Transaction tx = getTx(xid);
        boolean prepared = tx.prepare();

        if (prepared) tx.setState(TransactionState.commit);
        else tx.setState(TransactionState.abort);

        if (dieTime.equals(TM_DIE_TIME_BEFORE_COMMIT))
            dieNow();

        storeLog();

        if (dieTime.equals(TM_DIE_TIME_AFTER_COMMIT)) {
            dieNow();
        }

        tx.terminate();

        return prepared;
    }

    @Override
    public void abort(int xid) throws InvalidTransactionException {
        Transaction tx = getTx(xid);
        tx.setState(TransactionState.abort);
        storeLog();
        tx.terminate();
    }

    public boolean dieNow() {
        System.exit(1);
        // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
        return true;
    }

    @Override
    public void setDieTime(String time) {
        this.dieTime = time;
        System.out.println("Die time set to : " + time);
    }

    public void ping() {
    }

    enum TransactionState implements Serializable {
        proceeding,
        commit,
        abort
    }

    private class Transaction implements Serializable {
        int xid;
        Map<String, ResourceManager> rms;
        TransactionState state;

        Transaction(int xid) {
            this.xid = xid;
            this.rms = new HashMap<>();
            this.state = TransactionState.proceeding;
        }

        @Override
        public String toString() {
            return "Transaction{" + "xid=" + xid + ", rms=" + rms.size() + ", state=" + state + '}';
        }

        synchronized void enlist(ResourceManager rm) throws InvalidTransactionException, RemoteException {
            if (this.state == TransactionState.proceeding) {
                rms.put(rm.getID(), rm);
            } else if (this.state == TransactionState.commit && rms.containsKey(rm.getID())) {
                rm.commit(this.xid);
                rms.remove(rm.getID());
                checkTerminated();
            } else if (this.state == TransactionState.abort && rms.containsKey(rm.getID())) {
                rm.abort(this.xid);
                rms.remove(rm.getID());
                checkTerminated();
            }
        }

        synchronized void checkTerminated() {
            if (rms.isEmpty()) {
                System.out.println("TX " + xid + " is terminated.");
                txs.remove(xid);
            }
        }

        synchronized boolean prepare() {
            try {
                for (ResourceManager rm : rms.values())
                    if (!rm.prepare(xid))
                        return false;
            } catch (RemoteException | InvalidTransactionException e) {
                return false;
            }
            return true;
        }

        synchronized void setState(TransactionState state) {
            this.state = state;
        }

        synchronized void terminate() {
            System.out.format("%s starts terminating.\n", this.toString());
            Iterator<Map.Entry<String, ResourceManager>> iterator = rms.entrySet().iterator();
            ResourceManager rm;
            while (iterator.hasNext()) {
                try {
                    rm = iterator.next().getValue();
                    if (this.state == TransactionState.abort) rm.abort(xid);
                    else if (this.state == TransactionState.commit) rm.commit(xid);
                    iterator.remove();
                } catch (RemoteException | InvalidTransactionException ignored) {
                }
            }
            checkTerminated();
        }

        // TX recover only needs to do one thing:
        // set any proceeding transactions to abort
        synchronized void recover() {
            if (state == TransactionState.proceeding)
                state = TransactionState.abort;
        }
    }
}
