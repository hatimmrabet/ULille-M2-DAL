package dal.api.banque.models;

public class Stock {

    private String type;
    private int quantity;
    private double price;
    
    public Stock(String type, int quantity, double price) {
        this.type = type;
        this.quantity = quantity;
        this.price = price;
    }
    
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    
}
