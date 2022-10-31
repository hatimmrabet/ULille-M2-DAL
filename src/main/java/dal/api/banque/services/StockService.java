package dal.api.banque.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import dal.api.banque.models.Stock;

@Service
public class StockService {

    public List<Stock> getStocks() {
        List<Stock> stocks = new ArrayList<>();
        stocks.add(new Stock("chaise", 0, 0));
        stocks.add(new Stock("banc", 0, 0));
        stocks.add(new Stock("table", 0, 0));
        stocks.add(new Stock("bois", 0, 0));
        stocks.add(new Stock("metal", 0, 0));
        stocks.add(new Stock("peinture", 0, 0));
        stocks.add(new Stock("plastique", 0, 0));
        stocks.add(new Stock("verre", 0, 0));
        stocks.add(new Stock("papier", 0, 0));
        stocks.add(new Stock("tissu", 0, 0));
        return stocks;
    }

    public HashMap<String, List<Stock>> getRules() {
        HashMap<String, List<Stock>> rules = new HashMap<>();
        rules.put("chaise", List.of(new Stock("bois", 3, 0), 
                                        new Stock("metal", 3, 0), 
                                        new Stock("peinture", 3, 0)));
        rules.put("banc", List.of(new Stock("bois", 1, 0), 
                                        new Stock("metal", 1, 0), 
                                        new Stock("peinture", 1, 0),
                                        new Stock("plastique", 3, 0),
                                        new Stock("verre", 1, 0),
                                        new Stock("papier", 1, 0),
                                        new Stock("tissu", 1, 0)));
        rules.put("table", List.of(new Stock("plastique", 1, 0),
                                        new Stock("verre", 3, 0),
                                        new Stock("papier", 3, 0),
                                        new Stock("tissu", 3, 0)));
        return rules;
    }

    public List<Stock> getRulesForProduct(String name) {
        return getRules().get(name);
    }

    
}
