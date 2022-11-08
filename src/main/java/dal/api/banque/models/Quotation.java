package dal.api.banque.models;

import org.springframework.data.annotation.Id;

import java.util.Map;

public class Quotation
{
    @Id
    private String id;
    private String seller;
    private String buyer;
    private Map<String, Integer> cart;
    private double totalHT;
    private double totalTTC;
    private Integer fee;
    private Status status;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getSeller() {
        return seller;
    }
    public void setSeller(String seller) {
        this.seller = seller;
    }
    public String getBuyer() {
        return buyer;
    }
    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }
    public Map<String, Integer> getCart() {
        return cart;
    }
    public void setCart(Map<String, Integer> cart) {
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
    public Integer getFee() {
        return fee;
    }
    public void setFee(Integer fee) {
        this.fee = fee;
    }
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    


}
