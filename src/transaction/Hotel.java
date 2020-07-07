package transaction;

import java.io.Serializable;

public class Hotel implements ResourceItem, Serializable {
    private static final String INDEX_LOCATION = "location";
    private static final String INDEX_PRICE = "price";
    private static final String INDEX_NUM_ROOMS = "numRooms";
    private static final String INDEX_NUM_AVAIL = "numAvail";

    private boolean isDeleted = false;

    private String location;
    private int price;
    private int numRooms;
    private int numAvail;

    public Hotel(String location, int price, int numRooms, int numAvail) {
        this.location = location;
        this.price = price;
        this.numRooms = numRooms;
        this.numAvail = numAvail;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{INDEX_LOCATION, INDEX_PRICE, INDEX_NUM_ROOMS, INDEX_NUM_AVAIL};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{location, "" + price, "" + numRooms, "" + numAvail};
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        switch (indexName) {
            case INDEX_LOCATION:
                return location;
            case INDEX_PRICE:
                return price;
            case INDEX_NUM_ROOMS:
                return numRooms;
            case INDEX_NUM_AVAIL:
                return numAvail;
            default:
                throw new InvalidIndexException(indexName);
        }
    }

    @Override
    public Object getKey() {
        return location;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void delete() {
        this.isDeleted = true;
    }

    public String getLocation() {
        return location;
    }

    public int getPrice() {
        return price;
    }

    public int getNumRooms() {
        return numRooms;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setNumRooms(int numRooms) {
        this.numRooms = numRooms;
    }

    public void setNumAvail(int numAvail) {
        this.numAvail = numAvail;
    }

    @Override
    public Object clone() {
        Hotel o = new Hotel(getLocation(), getPrice(), getNumRooms(), getNumAvail());
        o.isDeleted = this.isDeleted;
        return o;
    }
}
