package ThucHanh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProductRepository implements IRepository<Product> {
    private ArrayList<Product> products;
    private HashMap<String, Product> productMap;

    public ProductRepository(){
        products = new ArrayList<>();
        productMap = new HashMap<>();
    }

    @Override
    public boolean add(Product item) {
        if (item == null || item.id == null || productMap.containsKey(item.id)) {
            return false;
        }
        boolean added = products.add(item);
        if (added) {
            productMap.put(item.id, item);
        }
        return added;
    }

    @Override
    public boolean removeById(String id) {
        Product removed = productMap.remove(id);
        if (removed != null) {
            return products.remove(removed);
        }
        return false;
    }

    @Override
    public Product findById(String id) {
        return productMap.get(id);
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products);
    }
}
