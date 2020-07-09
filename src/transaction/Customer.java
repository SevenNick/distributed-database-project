package transaction;

import java.io.Serializable;

public class Customer implements ResourceItem, Serializable {
    private static final String INDEX_CUSTOMER_NAME = "customerName";

    private boolean isDeleted = false;

    private String customerName;

    public Customer(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{INDEX_CUSTOMER_NAME};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{customerName};
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        if (indexName.equals(INDEX_CUSTOMER_NAME))
            return customerName;
        else
            throw new InvalidIndexException(indexName);
    }

    @Override
    public Object getKey() {
        return customerName;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void delete() {
        this.isDeleted = true;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public Object clone() {
        Customer o = new Customer(getCustomerName());
        o.isDeleted = this.isDeleted;
        return o;
    }
}
