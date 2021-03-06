package transaction;

import java.io.Serializable;

public class Car implements ResourceItem, Serializable {
    private static final String INDEX_LOCATION = "location";
    private static final String INDEX_PRICE = "price";
    private static final String INDEX_NUM_CARS = "numCars";
    private static final String INDEX_NUM_AVAIL = "numAvail";

    private boolean isDeleted = false;

    private String location;
    private int price;
    private int numCars;
    private int numAvail;

    public Car(String location, int price, int numCars, int numAvail) {
        this.location = location;
        this.price = price;
        this.numCars = numCars;
        this.numAvail = numAvail;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{INDEX_LOCATION, INDEX_PRICE, INDEX_NUM_CARS, INDEX_NUM_AVAIL};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{location, "" + price, "" + numCars, "" + numAvail};
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        switch (indexName) {
            case INDEX_LOCATION:
                return location;
            case INDEX_PRICE:
                return price;
            case INDEX_NUM_CARS:
                return numCars;
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

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumCars() {
        return numCars;
    }

    public void setNumCars(int numCars) {
        this.numCars = numCars;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void setNumAvail(int numAvail) {
        this.numAvail = numAvail;
    }

    @Override
    public Object clone() {
        Car o = new Car(getLocation(), getPrice(), getNumCars(), getNumAvail());
        o.isDeleted = this.isDeleted;
        return o;
    }
}
