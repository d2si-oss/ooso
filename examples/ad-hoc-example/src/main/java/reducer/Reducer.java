package reducer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

            Map<String, Long> reduceTransactionCountPerKey = new HashMap<>();

            for (ObjectInfoSimple objectInfo : batch) {

                BufferedReader objectBufferedReader = Commons.getReaderFromObjectInfo(objectInfo);
                Map<String, Long> transactionCountPerKey = gson.fromJson(objectBufferedReader, new TypeToken<Map<String, Long>>() {
                }.getType());
                transactionCountPerKey.forEach((k, v) -> reduceTransactionCountPerKey.merge(k, v, (v1, v2) -> v1 + v2));

                objectBufferedReader.close();
            }

            return gson.toJson(reduceTransactionCountPerKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
