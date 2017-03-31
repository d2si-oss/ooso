package mapper;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Mapper extends MapperAbstract {

    public String map(BufferedReader objectBufferedReader) {

        try {
            Map<String, Long> transactionsPerProduct = new HashMap<>();

            String line;
            while (((line = objectBufferedReader.readLine()) != null)) {
                String[] fields = line.split(",");
                String productId = fields[3];
                Long currentCount = transactionsPerProduct.get(productId);
                transactionsPerProduct.put(productId, currentCount != null ? currentCount + 1 : 1);
            }

            Gson gson = new Gson();
            return gson.toJson(transactionsPerProduct);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
