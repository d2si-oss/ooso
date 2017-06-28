package reducer;

import com.google.gson.Gson;
import fr.d2si.ooso.reducer.ReducerAbstract;
import fr.d2si.ooso.utils.Commons;
import fr.d2si.ooso.utils.ObjectInfoSimple;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reducer extends ReducerAbstract {

    public String reduce(List<ObjectInfoSimple> batch) {
        try {
            Gson gson = new Gson();

            Map<String, Double> reduceTransactionCountPerProduct = new HashMap<>();

            for (ObjectInfoSimple objectInfo : batch) {

                BufferedReader objectBufferedReader = Commons.getReaderFromObjectInfo(objectInfo);

                Map<String, Double> transactionCountPerProduct = gson.fromJson(objectBufferedReader, HashMap.class);
                transactionCountPerProduct.forEach((k, v) -> reduceTransactionCountPerProduct.merge(k, v, (v1, v2) -> v1 + v2));

                objectBufferedReader.close();
            }

            return gson.toJson(reduceTransactionCountPerProduct);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
