package utils;

public class JobInfo {
    private String jobId;
    private String jobInputBucket;
    private String mapperOutputBucket;
    private String reducerOutputBucket;
    private String mapperFunctionName;
    private String reducerFunctionName;
    private String statusTable;
    private String mapperMemory;
    private String reducerMemory;
    private String mapperForceBatchSize;
    private String reducerForceBatchSize;
    private String disableReducer;

    public JobInfo() {
    }

    public JobInfo(String jobId, String jobInputBucket, String mapperOutputBucket, String reducerOutputBucket, String mapperFunctionName, String reducerFunctionName, String statusTable, String mapperMemory, String reducerMemory, String mapperForceBatchSize, String reducerForceBatchSize, String disableReducer) {
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
        this.disableReducer = disableReducer;
    }

    public String getJobInputBucket() {
        return jobInputBucket;
    }

    public void setJobInputBucket(String jobInputBucket) {
        this.jobInputBucket = jobInputBucket;
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

    public int getMapperMemory() {
        return Integer.parseInt(mapperMemory);
    }

    public void setMapperMemory(String mapperMemory) {
        this.mapperMemory = mapperMemory;
    }

    public int getReducerMemory() {
        return Integer.parseInt(reducerMemory);
    }

    public void setReducerMemory(String reducerMemory) {
        this.reducerMemory = reducerMemory;
    }

    public int getMapperForceBatchSize() {
        return Integer.parseInt(mapperForceBatchSize);
    }

    public void setMapperForceBatchSize(String mapperForceBatchSize) {
        this.mapperForceBatchSize = mapperForceBatchSize;
    }

    public int getReducerForceBatchSize() {
        return Integer.parseInt(reducerForceBatchSize);
    }

    public void setReducerForceBatchSize(String reducerForceBatchSize) {
        this.reducerForceBatchSize = reducerForceBatchSize;
    }

    public boolean getDisableReducer() {
        return Boolean.parseBoolean(disableReducer);
    }

    public void setDisableReducer(String disableReducer) {
        this.disableReducer = disableReducer;
    }
}

