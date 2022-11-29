package dal.api.banque.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "quotations")
public class Quotation
{   
    @Id 
    private long id;
    @DBRef
    private Account seller;
    @DBRef
    private Account buyer;
    private List<Stock> cart;
    private double HT;
    private double TTC;
    private int fee;
    private Status status;


    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getSeller() {
        return seller;
    }

    public void setSeller(Account seller) {
        this.seller = seller;
    }

    public Account getBuyer() {
        return buyer;
    }

    public void setBuyer(Account buyer) {
        this.buyer = buyer;
    }

    public List<Stock> getCart() {
        return cart;
    }

    public void setCart(List<Stock> cart) {
        this.cart = cart;
    }

    public double getHT() {
        return HT;
    }

    public void setHT(double HT) {
        this.HT = HT;
    }

    public double getTTC() {
        return TTC;
    }

    public void setTTC(double TTC) {
        this.TTC = TTC;
    }

    public int getFee() {
        return fee;
    }

    public void setFee(int fee) {
        this.fee = fee;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
