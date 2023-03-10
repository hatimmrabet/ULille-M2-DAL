package dal.api.banque.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Document(collection = "accounts")
public class Account {
    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    @JsonIgnore
    private String password;
    private List<Stock> stock;

    private int fee;

    private double money;
    // private List<Operation> operations;

    public Account() {
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getFee() {
        return fee;
    }
    public void setFee(int fee) {
        this.fee = fee;
    }
    public double getMoney() {
        return money;
    }
    public void setMoney(double money) {
        this.money = money;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public List<Stock> getStock() {
        return stock;
    }
    public void setStock(List<Stock> stocks) {
        this.stock = stocks;
    }

    public Stock getStock(String name) {
        for (Stock stock : this.stock) {
            if (stock.getType().equals(name)) {
                return stock;
            }
        }
        return null;
    }

    /**
     * Ajouter un stock à la liste des stocks de l'account
     * Si le stock existe déjà, on augmente la quantité, sinon, on l'ajoute
     */
    public void addStock(Stock stock) {
        this.stock.stream()
                    .filter(s -> s.getType().equals(stock.getType()))
                    .findFirst()
                    .ifPresentOrElse(s -> { s.setQuantity(s.getQuantity() + stock.getQuantity()); },
                                    () -> { this.stock.add(stock); });
    }

    public void removeStocks(Stock stock) {
        this.stock.stream()
                    .filter(s -> s.getType().equals(stock.getType()))
                    .findFirst()
                    .ifPresent(s -> { s.setQuantity(s.getQuantity() - stock.getQuantity()); });

    }
}
