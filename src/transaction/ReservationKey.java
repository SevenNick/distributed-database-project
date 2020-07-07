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
public class ReservationKey implements Serializable {
    protected String customerName;

    protected int reservationType;

    protected String reservationKey;

    public ReservationKey(String customerName, int reservationType, String reservationKey) {
        this.customerName = customerName;
        this.reservationKey = reservationKey;
        this.reservationType = reservationType;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof ReservationKey))
            return false;
        if (this == o)
            return true;
        ReservationKey k = (ReservationKey) o;
        if (k.customerName.equals(customerName) && k.reservationKey.equals(reservationKey) && k.reservationType == reservationType)
            return true;
        return false;
    }

    public int hashCode() {
        return customerName.hashCode() + reservationType + reservationKey.hashCode();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[");
        buf.append("customer name=");
        buf.append(customerName);
        buf.append(";");
        buf.append("reservationKey=");
        buf.append(reservationKey);
        buf.append(";");
        buf.append("reservationType=");
        buf.append(reservationType);
        buf.append("]");

        return buf.toString();
    }
}
