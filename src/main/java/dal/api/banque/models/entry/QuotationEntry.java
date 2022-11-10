package dal.api.banque.models.entry;

import dal.api.banque.models.Stock;

import java.util.List;

public class QuotationEntry {

    private List<Stock> cart;

    public List<Stock> getCart() {
        return cart;
    }
    public void setCart(List<Stock> cart) {
        this.cart = cart;
    }
}
