package driver;

import java.util.Map;

public class MappersInfo {
    private long mapperCount;
    private Map<Long, Integer> batchCountPerMapper;

    public MappersInfo() {
    }

    public MappersInfo(long mapperCount, Map<Long, Integer> batchCountPerMapper) {
        this.mapperCount = mapperCount;
        this.batchCountPerMapper = batchCountPerMapper;
    }

    public long getMapperCount() {
        return mapperCount;
    }

    public void setMapperCount(long mapperCount) {
        this.mapperCount = mapperCount;
    }

    public Map<Long, Integer> getBatchCountPerMapper() {
        return batchCountPerMapper;
    }

    public void setBatchCountPerMapper(Map<Long, Integer> batchCountPerMapper) {
        this.batchCountPerMapper = batchCountPerMapper;
    }
}
