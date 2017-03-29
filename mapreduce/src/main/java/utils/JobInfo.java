package utils;

public class JobInfo {
    private String jobId;
    private String jobInputBucket;
    private String mapperOutputBucket;
    private String reducerOutputBucket;
    private String mapperFunctionName;
    private String reducerFunctionName;
    private String statusTable;
    private int mapperMemory;
    private int reducerMemory;
    private int mapperForceBatchSize;
    private int reducerForceBatchSize;

    public JobInfo() {
    }

    public JobInfo(String jobId, String jobInputBucket, String mapperOutputBucket, String reducerOutputBucket, String mapperFunctionName, String reducerFunctionName, String statusTable, int mapperMemory, int reducerMemory, int mapperForceBatchSize, int reducerForceBatchSize) {
        this.jobId = jobId;
        this.jobInputBucket = jobInputBucket;
        this.mapperOutputBucket = mapperOutputBucket;
        this.reducerOutputBucket = reducerOutputBucket;
        this.mapperFunctionName = mapperFunctionName;
        this.reducerFunctionName = reducerFunctionName;
        this.statusTable = statusTable;
        this.mapperMemory = mapperMemory;
        this.reducerMemory = reducerMemory;
        this.mapperForceBatchSize = mapperForceBatchSize;
        this.reducerForceBatchSize = reducerForceBatchSize;
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

    public int getMapperForceBatchSize() {
        return mapperForceBatchSize;
    }

    public void setMapperForceBatchSize(int mapperForceBatchSize) {
        this.mapperForceBatchSize = mapperForceBatchSize;
    }

    public int getReducerForceBatchSize() {
        return reducerForceBatchSize;
    }

    public void setReducerForceBatchSize(int reducerForceBatchSize) {
        this.reducerForceBatchSize = reducerForceBatchSize;
    }
}

