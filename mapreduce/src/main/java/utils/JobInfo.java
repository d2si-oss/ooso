package utils;

public class JobInfo {
    private String jobId;
    private String jobInputBucket;
    private String mapperOutputBucket;
    private String reducerOutputBucket;
    private String statusBucket;
    private String mappersInfoName;
    private String mapperFunctionName;
    private String reducerFunctionName;
    private String statusTable;
    private int mapperMemory;
    private int reducerMemory;

    public JobInfo() {
    }

    public JobInfo(String jobId, String jobInputBucket, String mapperOutputBucket, String reducerOutputBucket, String statusBucket, String mappersInfoName, String mapperFunctionName, String reducerFunctionName, String statusTable, int mapperMemory, int reducerMemory) {
        this.jobId = jobId;
        this.jobInputBucket = jobInputBucket;
        this.mapperOutputBucket = mapperOutputBucket;
        this.reducerOutputBucket = reducerOutputBucket;
        this.statusBucket = statusBucket;
        this.mappersInfoName = mappersInfoName;
        this.mapperFunctionName = mapperFunctionName;
        this.reducerFunctionName = reducerFunctionName;
        this.statusTable = statusTable;
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

    public int getReducerMemory() {
        return reducerMemory;
    }

    public void setReducerMemory(int reducerMemory) {
        this.reducerMemory = reducerMemory;
    }

    public String getMapperFunctionName() {
        return mapperFunctionName;
    }

    public void setMapperFunctionName(String mapperFunctionName) {
        this.mapperFunctionName = mapperFunctionName;
    }

    public String getReducerFunctionName() {
        return reducerFunctionName;
    }

    public void setReducerFunctionName(String reducerFunctionName) {
        this.reducerFunctionName = reducerFunctionName;
    }

    public String getStatusTable() {
        return statusTable;
    }

    public void setStatusTable(String statusTable) {
        this.statusTable = statusTable;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}

