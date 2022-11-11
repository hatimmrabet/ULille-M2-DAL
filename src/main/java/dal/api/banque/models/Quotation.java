package dal.api.banque.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;

public class Quotation
{
    @Id
    private String id;
    @DBRef
    private Account seller;
    @DBRef
    private Account buyer;
    private List<Stock> cart;
    private double totalHT;
    private double totalTTC;
    private int fee;
    private Status status;


    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public double getTotalHT() {
        return totalHT;
    }

    public void setTotalHT(double totalHT) {
        this.totalHT = totalHT;
    }

    public double getTotalTTC() {
        return totalTTC;
    }

    public void setTotalTTC(double totalTTC) {
        this.totalTTC = totalTTC;
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
