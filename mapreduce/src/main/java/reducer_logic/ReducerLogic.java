package reducer_logic;

import com.google.gson.Gson;
import utils.Commons;
import utils.ObjectInfoSimple;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReducerLogic {

    public static String reduceResultCalculator(List<ObjectInfoSimple> batch) throws IOException {
        Gson gson = new Gson();

        Map<String, Double> reduceTransactionCountPerProduct = new HashMap<>();

        for (ObjectInfoSimple objectInfo : batch) {

            BufferedReader objectBufferedReader = Commons.getReaderFromObjectInfo(objectInfo);

            Map<String, Double> transactionCountPerProduct = gson.fromJson(objectBufferedReader, HashMap.class);
            transactionCountPerProduct.forEach((k, v) -> reduceTransactionCountPerProduct.merge(k, v, (v1, v2) -> v1 + v2));

            objectBufferedReader.close();
        }

        return gson.toJson(reduceTransactionCountPerProduct);

    }
}
