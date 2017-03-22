package mapper_logic;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapperLogic {

    public static String mapResultCalculator(BufferedReader objectBufferedReader) throws IOException {
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
    }
}
