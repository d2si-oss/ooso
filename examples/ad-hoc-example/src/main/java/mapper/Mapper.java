package mapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import commons.Record;
import fr.d2si.ooso.mapper.MapperAbstract;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Mapper extends MapperAbstract {

    public String map(BufferedReader objectBufferedReader) {

        try {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

            Map<String, Long> result = new HashMap<>();

            String line;
            while (((line = objectBufferedReader.readLine()) != null)) {
                Record currentRecord = new Record(line);

                String key = String.join(
                        "-",
                        currentRecord.getPassengerCount(),
                        String.valueOf(dateTimeFormatter.parseDateTime(currentRecord.getTpepPickupDatetime()).getYear()),
                        String.valueOf(Double.valueOf(currentRecord.getTripDistance()).intValue()));

                Long count = result.get(key);

                result.put(key, count == null ? 1 : count + 1);
            }

            return gson.toJson(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
