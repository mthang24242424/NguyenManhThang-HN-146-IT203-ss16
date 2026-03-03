package ThucHanh;

import java.util.*;

public class Main {
    static void main() {
        IRepository<Product> productRepository = new ProductRepository();
        productRepository.add(new FoodProduct("F1", "Bánh mì", 20000, 10));
        productRepository.add(new FoodProduct("F2", "Bún chả", 30000, 15));
        productRepository.add(new ElectronicProduct("E1", "Điện thoại", 5000000, 12));
        productRepository.add(new ElectronicProduct("E2", "Laptop", 15000000, 18));

        System.out.println("Danh sách sản phẩm");
        displayAllProducts(productRepository.findAll());

        System.out.println("\nTìm kiếm theo id");
        findAndDisplayProductById(productRepository, "E1");
        findAndDisplayProductById(productRepository, "F2");

        System.out.println("\nSắp xêp");
        displaySortedByPrice(productRepository.findAll());

    }

    //  Hiển thị toàn bộ danh sách sản phẩm
    static void displayAllProducts(List<Product> products) {
        for (Product product : products) {
            product.displayInfo();
            System.out.println("Thành tiền: " + product.calculateFinalPrice());
            System.out.println("---");
        }
    }

    //  Tìm sản phẩm theo id và hiển thị kết quả
    static void findAndDisplayProductById(IRepository<Product> repository, String id) {
        Product product = repository.findById(id);
        if (product != null) {
            System.out.println("Tìm thấy sản phẩm với ID: " + id);
            product.displayInfo();
            System.out.println("Thành tiền: " + product.calculateFinalPrice());
        } else {
            System.out.println("Không tìm thấy sản phẩm với ID: " + id);
        }
    }

    //  Sắp xếp danh sách theo giá tăng dần
    static void displaySortedByPrice(List<Product> products) {
        List<Product> sorted = new ArrayList<>(products);
        Collections.sort(sorted, new Comparator<Product>() {
            @Override
            public int compare(Product p1, Product p2) {
                return Double.compare(p1.calculateFinalPrice(), p2.calculateFinalPrice());
            }
        });

        System.out.println("Danh sách sắp xếp theo giá (thành tiền) tăng dần:");
        for (Product product : sorted) {
            System.out.println("ID: " + product.id + ", Tên: " + product.name +
                             ", Thành tiền: " + product.calculateFinalPrice());
        }
    }

}
