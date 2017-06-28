package mapper;

import commons.Rate;
import commons.Record;
import fr.d2si.ooso.mapper.MapperAbstract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class Mapper extends MapperAbstract {
    private final static String EXTERNAL_DATA_URL = "https://s3-eu-west-1.amazonaws.com/ooso-misc/rateCodeMapping.csv";
    private Map<String, String> idAndRate;

    @Override
    public String map(BufferedReader objectReader) {

        try (BufferedReader reader = getReaderFromUrl(EXTERNAL_DATA_URL)) {
            idAndRate = getRateMappingFromReader(reader);
            return processLines(objectReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedReader getReaderFromUrl(String url) throws IOException {
        return new BufferedReader(
                new InputStreamReader(
                        new URL(url)
                                .openStream()));
    }

    private Map<String, String> getRateMappingFromReader(BufferedReader reader) {
        return reader
                .lines()
                .map(Rate::new)
                .collect(toMap(Rate::getId, Rate::getRate));
    }

    private String processLines(BufferedReader objectBufferedReader) throws IOException {
        List<String> records = objectBufferedReader
                .lines()
                .map(this::mapRateCode)
                .collect(toList());
        return String.join("\n", records);
    }

    private String mapRateCode(String line) {
        Record record = new Record(line);
        String rate = idAndRate.get(record.getRatecodeID());
        record.setRatecodeID(rate == null ? "NA" : rate);
        return record.toString();
    }
}
