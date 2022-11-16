package dal.api.banque.models.response;

import dal.api.banque.models.Status;

public class QuotationDTO {
    
    private String id;
    private String seller;
    private String buyer;
    private double HT;
    private double TTC;
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
