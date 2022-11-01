package dal.api.banque.models.entry;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

public class QuotationEntry {
    @NotEmpty(message = "name is required")
    private String Seller;
    @NotEmpty(message = "name is required")
    private String Buyer;

    private Map<String, Integer> cart;

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


}
