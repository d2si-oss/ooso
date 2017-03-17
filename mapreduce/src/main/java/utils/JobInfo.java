package utils;

public class JobInfo {
    private String jobInputBucket;
    private String mapperOutputBucket;
    private String reducerOutputBucket;
    private String reducersInfoName;
    private String statusBucket;
    private String mappersInfoName;
    private int mapperMemory;
    private int reducerMemory;

    public JobInfo() {
    }

    public JobInfo(String jobInputBucket, String mapperOutputBucket, String reducerOutputBucket, String reducersInfoName, String statusBucket, String mappersInfoName, int mapperMemory, int reducerMemory) {
        this.jobInputBucket = jobInputBucket;
        this.mapperOutputBucket = mapperOutputBucket;
        this.reducerOutputBucket = reducerOutputBucket;
        this.reducersInfoName = reducersInfoName;
        this.statusBucket = statusBucket;
        this.mappersInfoName = mappersInfoName;
        this.mapperMemory = mapperMemory;
        this.reducerMemory = reducerMemory;
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

    public String getReducerOutputBucket() {
        return reducerOutputBucket;
    }

    public void setReducerOutputBucket(String reducerOutputBucket) {
        this.reducerOutputBucket = reducerOutputBucket;
    }

    public String getReducersInfoName() {
        return reducersInfoName;
    }

    public void setReducersInfoName(String reducersInfoName) {
        this.reducersInfoName = reducersInfoName;
    }

    public int getReducerMemory() {
        return reducerMemory;
    }

    public void setReducerMemory(int reducerMemory) {
        this.reducerMemory = reducerMemory;
    }
}

