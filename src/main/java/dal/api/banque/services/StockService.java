package dal.api.banque.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import dal.api.banque.models.Stock;

@Service
public class StockService {

    public HashMap<String, List<Stock>> loadData() {
        HashMap<String, List<Stock>> data = new HashMap<String, List<Stock>>();
        try {
            String json = IOUtils.toString(new ClassPathResource("static-data.json").getInputStream(), StandardCharsets.UTF_8);
            JSONObject obj = new JSONObject(json);
            for (String key : obj.keySet()) {
                List<Stock> stocks = new ArrayList<Stock>();
                JSONObject stock = obj.getJSONObject(key);
                for (String key2 : stock.keySet()) {
                    Stock s = new Stock(key2, 0, stock.getInt(key2));
                    stocks.add(s);
                }
                data.put(key, stocks);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while loading static data");
        }
        return data;
    }

    public List<Stock> getFornisseurStocks(String fournisseur) {
        return loadData().get(fournisseur);
    }

    public List<Stock> getStocks() {
        List<Stock> stocks = new ArrayList<>();
        stocks.add(new Stock("chaise", 0, 1000));
        stocks.add(new Stock("banc", 0, 1000));
        stocks.add(new Stock("table", 0, 1000));
        stocks.add(new Stock("bois", 0, 1000));
        stocks.add(new Stock("metal", 0, 1000));
        stocks.add(new Stock("peinture", 0, 1000));
        stocks.add(new Stock("plastique", 0, 1000));
        stocks.add(new Stock("verre", 0, 1000));
        stocks.add(new Stock("papier", 0, 1000));
        stocks.add(new Stock("tissu", 0, 1000));
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
