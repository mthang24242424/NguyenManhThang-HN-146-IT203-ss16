package ThucHanh;

public class ElectronicProduct extends Product{
    protected int warrantyMonths;

    public ElectronicProduct(String id, String name, double price, int warrantyMonths){
        super(id, name, price);
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    double calculateFinalPrice(){
        if(warrantyMonths > 12){
            return super.price + 1000000;
        }else{
            return super.price;
        }
    }
    @Override
    void displayInfo(){
        super.displayInfo();
        System.out.println("Số tháng bảo hành: " + warrantyMonths);
    }
}
