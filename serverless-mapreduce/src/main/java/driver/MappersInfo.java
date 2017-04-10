package driver;

import java.util.Map;

public class MappersInfo {
    private int mapperCount;
    private Map<Integer, Integer> batchCountPerMapper;

    public MappersInfo() {
    }

    public MappersInfo(int mapperCount, Map<Integer, Integer> batchCountPerMapper) {
        this.mapperCount = mapperCount;
        this.batchCountPerMapper = batchCountPerMapper;
    }

    public int getMapperCount() {
        return mapperCount;
    }

    public void setMapperCount(int mapperCount) {
        this.mapperCount = mapperCount;
    }

    public Map<Integer, Integer> getBatchCountPerMapper() {
        return batchCountPerMapper;
    }

    public void setBatchCountPerMapper(Map<Integer, Integer> batchCountPerMapper) {
        this.batchCountPerMapper = batchCountPerMapper;
    }
}
