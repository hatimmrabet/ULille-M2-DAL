package dal.api.banque.models;

import java.util.List;
import java.util.Map;

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
    private List<Stock> stocks;

    private int fee;

    private double balance;
    // private List<Operation> operations;

    public Account() {
    }

    public String getId() {
        return id;
    }
    public int getFee() {
        return fee;
    }
    public void setFee(int fee) {
        this.fee = fee;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    void setId(String id) {
        this.id = id;
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
    public List<Stock> getStocks() {
        return stocks;
    }
    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    /**
     * Ajouter un stock à la liste des stocks de l'account
     * Si le stock existe déjà, on augmente la quantité, sinon, on l'ajoute
     */
    public void addStock(Stock stock) {
        this.stocks.stream()
                    .filter(s -> s.getName().equals(stock.getName()))
                    .findFirst()
                    .ifPresentOrElse(s -> { s.setQuantity(s.getQuantity() + stock.getQuantity()); },
                                    () -> { this.stocks.add(stock); });
    }

    public boolean removeStocks(Map<String, Integer> cart) {
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String stockName = entry.getKey();
            int quantity = entry.getValue();
            Stock stock = stocks.stream().filter(s -> s.getName().equals(stockName)).findFirst().orElse(null);
            if (stock == null) {
                return false;
            }
            stock.setQuantity(stock.getQuantity() - quantity);
        }
        return true;
    }
}
