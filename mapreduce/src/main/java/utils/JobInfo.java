package utils;

public class JobInfo {
    private String jobInputBucket;
    private String mapperOutputBucket;
    private String statusBucket;
    private String mappersInfoName;
    private int mapperMemory;

    public JobInfo() {
    }

    public JobInfo(String jobInputBucket, String mapperOutputBucket, String statusBucket, String mappersInfoName, int mapperMemory) {
        this.jobInputBucket = jobInputBucket;
        this.mapperOutputBucket = mapperOutputBucket;
        this.statusBucket = statusBucket;
        this.mappersInfoName = mappersInfoName;
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

    public String getMappersInfoName() {
        return mappersInfoName;
    }

    public void setMappersInfoName(String mappersInfoName) {
        this.mappersInfoName = mappersInfoName;
    }
}
