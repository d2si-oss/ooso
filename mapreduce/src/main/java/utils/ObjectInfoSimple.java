package utils;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class ObjectInfoSimple {
    private String bucket;
    private String key;

    public ObjectInfoSimple() {
    }

    public ObjectInfoSimple(String bucket, String key) {
        this.bucket = bucket;
        this.key = key;
    }

    public ObjectInfoSimple(S3ObjectSummary summary) {
        this.bucket = summary.getBucketName();
        this.key = summary.getKey();
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
