package ThucHanh;

public abstract class Product {
    protected String id;
    protected String name;
    protected double price;

    public Product(String id, String name, double price){
        this.id = id;
        this.name = name;
        this.price = price;
    }

    abstract double calculateFinalPrice();
    void displayInfo(){
        System.out.println("ID:" + id);
        System.out.println("Name:" + name);
        System.out.println("Price:" + price);
    }
}
