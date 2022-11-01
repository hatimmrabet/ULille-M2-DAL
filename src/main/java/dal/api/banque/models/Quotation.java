package dal.api.banque.models;

import java.util.Map;

public class Quotation
{
    private String Id;
    private String Seller;
    private String Buyer;
    private Map<String, Integer> cart;
    private double totalHT;
    private double totalTTC;
    private Integer fee;
    private Status status;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getSeller() {
        return Seller;
    }

    public void setSeller(String seller) {
        Seller = seller;
    }

    public String getBuyer() {
        return Buyer;
    }

    public void setBuyer(String buyer) {
        Buyer = buyer;
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
