package transaction;

import java.io.Serializable;

public class Customer implements ResourceItem, Serializable {
    private static final String INDEX_CUSTOMER_NAME = "custName";

    private boolean isDeleted = false;

    private String custName;

    public Customer(String custName) {
        this.custName = custName;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{INDEX_CUSTOMER_NAME};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{custName};
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        if (indexName.equals(INDEX_CUSTOMER_NAME))
            return custName;
        else
            throw new InvalidIndexException(indexName);
    }

    @Override
    public Object getKey() {
        return custName;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void delete() {
        this.isDeleted = true;
    }

    public String getCustName() {
        return custName;
    }

    @Override
    public Object clone() {
        Customer o = new Customer(getCustName());
        o.isDeleted = this.isDeleted;
        return o;
    }
}
