package dal.api.banque.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import dal.api.banque.models.Stock;
import dal.api.banque.utils.FileManager;

@Service
public class StockService  {

    private HashMap<String, List<Stock>> clientsStocks = new HashMap<String, List<Stock>>();
    private HashMap<String, Integer> frais = new HashMap<String, Integer>();
    private final String DATA_FILE = "static-data.json";
    private final String FEE_FILE = "static-frais.json";

    public StockService() {
        this.clientsStocks = loadClientsData();
        this.frais = loadFraisData();
    }

    public HashMap<String, List<Stock>> loadClientsData() {
        HashMap<String, List<Stock>> data = new HashMap<String, List<Stock>>();
        JSONObject obj = new JSONObject(FileManager.getFileContent(DATA_FILE));
        for (String key : obj.keySet()) {
            List<Stock> stocks = new ArrayList<Stock>();
            JSONObject stock = obj.getJSONObject(key);
            for (String key2 : stock.keySet()) {
                Stock s = new Stock(key2, 0, stock.getInt(key2));
                stocks.add(s);
            }
            data.put(key, stocks);
        }
        return data;
    }

    public HashMap<String, Integer> loadFraisData() {
        HashMap<String, Integer> data = new HashMap<String, Integer>();
        JSONObject obj = new JSONObject(FileManager.getFileContent(FEE_FILE));
        for (String key : obj.keySet()) {
            data.put(key, obj.getInt(key));
        }
        return data;
    }

    public List<Stock> getFornisseurStocks(String fournisseur) {
        if(clientsStocks.containsKey(fournisseur)) {
            return clientsStocks.get(fournisseur);
        }
        return null;
    }

    public int getFournisseurFrais(String fournisseur) {
        if(frais.containsKey(fournisseur)) {
            return frais.get(fournisseur);
        }
        return -1;
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
