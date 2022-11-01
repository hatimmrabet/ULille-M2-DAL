package dal.api.banque.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Generated;

@Document(collection = "accounts")
public class Account {

    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    private String password;
    private List<Stock> stocks;

    private int fee;

    private double balance;
    // private List<Operation> operations;

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
    
    

}
