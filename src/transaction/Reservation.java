/*
 * Created on 2005-5-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package transaction;

import java.io.Serializable;

/**
 * @author RAdmin
 * <p>
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Reservation implements ResourceItem, Serializable {

    public static final String INDEX_CUSTOMER_NAME = "customerName";
    public static final String INDEX_RESERVATION_TYPE = "reservationType";
    public static final String INDEX_RESERVATION_KEY = "reservationKey";

    public static final int RESERVATION_TYPE_FLIGHT = 1;

    public static final int RESERVATION_TYPE_HOTEL = 2;

    public static final int RESERVATION_TYPE_CAR = 3;

    protected String customerName;

    protected int reservationType;

    protected String reservationKey;

    protected boolean isDeleted = false;

    public Reservation(String customerName, int reservationType, String reservationKey) {
        this.customerName = customerName;
        this.reservationType = reservationType;
        this.reservationKey = reservationKey;
    }

    public String[] getColumnNames() {
        return new String[]{INDEX_CUSTOMER_NAME, INDEX_RESERVATION_TYPE, INDEX_RESERVATION_KEY};
    }

    public String[] getColumnValues() {
        return new String[]{customerName, "" + reservationType, "" + reservationKey};
    }

    public Object getIndex(String indexName) throws InvalidIndexException {
        switch (indexName) {
            case INDEX_CUSTOMER_NAME:
                return customerName;
            case INDEX_RESERVATION_TYPE:
                return reservationType;
            case INDEX_RESERVATION_KEY:
                return reservationKey;
            default:
                throw new InvalidIndexException(indexName);
        }
    }

    public Object getKey() {
        return new ReservationKey(customerName, reservationType, reservationKey);
    }

    /**
     * @return Returns the customerName.
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @return Returns the reservationKey.
     */
    public String getReservationKey() {
        return reservationKey;
    }

    /**
     * @return Returns the reservationType.
     */
    public int getReservationType() {
        return reservationType;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void delete() {
        isDeleted = true;
    }

    public Object clone() {
        Reservation o = new Reservation(getCustomerName(), getReservationType(),
                getReservationKey());
        o.isDeleted = isDeleted;
        return o;
    }
}
