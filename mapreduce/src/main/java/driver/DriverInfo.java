package driver;

public class DriverInfo {
    private String jobInputBucket;
    private String mapperOutputBucket;
    private String statusBucket;
    private String jobInfoName;
    private int mapperMemory;

    public DriverInfo() {
    }

    public DriverInfo(String jobInputBucket, String mapperOutputBucket, String statusBucket, String jobInfoName, int mapperMemory) {
        this.jobInputBucket = jobInputBucket;
        this.mapperOutputBucket = mapperOutputBucket;
        this.statusBucket = statusBucket;
        this.jobInfoName = jobInfoName;
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

    public String getStatusBucket() {
        return statusBucket;
    }

    public void setStatusBucket(String statusBucket) {
        this.statusBucket = statusBucket;
    }

    public String getJobInfoName() {
        return jobInfoName;
    }

    public void setJobInfoName(String jobInfoName) {
        this.jobInfoName = jobInfoName;
    }
}
