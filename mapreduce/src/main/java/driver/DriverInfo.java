package driver;

public class DriverInfo {
    private String jobInputBucket;
    private String mapperOutputBucket;
    private int mapperMemory;

    public DriverInfo() {
    }

    public DriverInfo(String jobInputBucket, String mapperOutputBucket, int mapperMemory) {
        this.jobInputBucket = jobInputBucket;
        this.mapperOutputBucket = mapperOutputBucket;
        this.mapperMemory = mapperMemory;
    }

    public String getJobInputBucket() {
        return jobInputBucket;
    }

    public void setJobInputBucket(String jobInputBucket) {
        this.jobInputBucket = jobInputBucket;
    }

    public int getMapperMemory() {
        return mapperMemory;
    }

    public void setMapperMemory(int mapperMemory) {
        this.mapperMemory = mapperMemory;
    }

    public String getMapperOutputBucket() {
        return mapperOutputBucket;
    }

    public void setMapperOutputBucket(String mapperOutputBucket) {
        this.mapperOutputBucket = mapperOutputBucket;
    }
}
