package reducer;

import com.google.gson.Gson;
import commons.Record;
import fr.d2si.ooso.reducer.ReducerAbstract;
import fr.d2si.ooso.utils.Commons;
import fr.d2si.ooso.utils.ObjectInfoSimple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Reducer extends ReducerAbstract {

    public String reduce(List<ObjectInfoSimple> batch) {
        try {
            Gson gson = new Gson();

            List<Record> records = new ArrayList<>();

            for (ObjectInfoSimple objectInfo : batch) {
                try (BufferedReader objectBufferedReader = Commons.getReaderFromObjectInfo(objectInfo)) {
                    records.addAll(readerToArray(objectBufferedReader, Record[].class));
                }
            }

            records.sort(Comparator.comparing(Record::getPassengerCount));

            return gson.toJson(records);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static <T> List<T> readerToArray(Reader s, Class<T[]> clazz) {
        return Arrays.asList(new Gson().fromJson(s, clazz));
    }
}
