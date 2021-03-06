package transaction;

import java.io.Serializable;

public class Flight implements ResourceItem, Serializable {
    private static final String INDEX_FLIGHT_NUMBER = "flightNum";
    private static final String INDEX_PRICE = "price";
    private static final String INDEX_NUM_SEATS = "numSeats";
    private static final String INDEX_NUM_AVAIL = "numAvail";

    private boolean isDeleted = false;

    private String flightNum;
    private int price;
    private int numSeats;
    private int numAvail;


    public Flight(String flightNum, int price, int numSeats, int numAvail) {
        this.flightNum = flightNum;
        this.price = price;
        this.numSeats = numSeats;
        this.numAvail = numAvail;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{INDEX_FLIGHT_NUMBER, INDEX_PRICE, INDEX_NUM_SEATS, INDEX_NUM_AVAIL};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{flightNum, "" + price, "" + numSeats, "" + numAvail};
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        switch (indexName) {
            case INDEX_FLIGHT_NUMBER:
                return flightNum;
            case INDEX_PRICE:
                return price;
            case INDEX_NUM_SEATS:
                return numSeats;
            case INDEX_NUM_AVAIL:
                return numAvail;
            default:
                throw new InvalidIndexException(indexName);
        }
    }

    @Override
    public Object getKey() {
        return flightNum;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void delete() {
        this.isDeleted = true;
    }

    public String getFlightNum() {
        return flightNum;
    }

    public void setFlightNum(String flightNum) {
        this.flightNum = flightNum;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumSeats() {
        return numSeats;
    }

    public void setNumSeats(int numSeats) {
        this.numSeats = numSeats;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void setNumAvail(int numAvail) {
        this.numAvail = numAvail;
    }

    @Override
    public Object clone() {
        Flight o = new Flight(getFlightNum(), getPrice(), getNumSeats(), getNumAvail());
        o.isDeleted = this.isDeleted;
        return o;
    }
}
