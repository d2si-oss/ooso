package mapper_wrapper;

import java.util.List;

public class MapperWrapperInfo {
    private List<String> batch;
    private String inputBucket;
    private String outputBucket;
    private long id;

    public MapperWrapperInfo() {
    }

    public MapperWrapperInfo(List<String> batch, String inputBucket, String outputBucket, long id) {
        this.batch = batch;
        this.inputBucket = inputBucket;
        this.outputBucket = outputBucket;
        this.id = id;
    }

    public List<String> getBatch() {
        return batch;
    }

    public void setBatch(List<String> batch) {
        this.batch = batch;
    }

    public String getInputBucket() {
        return inputBucket;
    }

    public void setInputBucket(String inputBucket) {
        this.inputBucket = inputBucket;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOutputBucket() {
        return outputBucket;
    }

    public void setOutputBucket(String outputBucket) {
        this.outputBucket = outputBucket;
    }
}
