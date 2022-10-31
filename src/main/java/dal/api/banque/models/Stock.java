package dal.api.banque.models;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stocks")
public class Stock {

    private String name;
    private int quantity;
    private double price;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
