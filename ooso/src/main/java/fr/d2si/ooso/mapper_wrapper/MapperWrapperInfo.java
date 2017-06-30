package fr.d2si.ooso.mapper_wrapper;

import fr.d2si.ooso.utils.JobInfo;
import fr.d2si.ooso.utils.ObjectInfoSimple;

import java.util.List;

public class MapperWrapperInfo {
    private long id;
    private List<ObjectInfoSimple> batch;
    private String mapperLogicInBase64;
    private JobInfo jobInfo;

    public MapperWrapperInfo() {
    }

    public MapperWrapperInfo(List<ObjectInfoSimple> batch, long id, String mapperLogicInBase64, JobInfo jobInfo) {
        this.batch = batch;
        this.id = id;
        this.mapperLogicInBase64 = mapperLogicInBase64;
        this.jobInfo = jobInfo;
    }

    public List<ObjectInfoSimple> getBatch() {
        return batch;
    }

    public void setBatch(List<ObjectInfoSimple> batch) {
        this.batch = batch;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMapperLogicInBase64() {
        return mapperLogicInBase64;
    }

    public void setMapperLogicInBase64(String mapperLogicInBase64) {
        this.mapperLogicInBase64 = mapperLogicInBase64;
    }

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }
}
