package fr.d2si.ooso.mappers_driver;

import fr.d2si.ooso.utils.JobInfo;

public class MappersDriverInfo {
    private String mapperInBase64;
    private String reducerInBase64;
    private JobInfo jobInfo;

    public MappersDriverInfo() {
    }

    public MappersDriverInfo(String mapperInBase64, String reducerInBase64, JobInfo jobInfo) {
        this.mapperInBase64 = mapperInBase64;
        this.reducerInBase64 = reducerInBase64;
        this.jobInfo = jobInfo;
    }

    public String getMapperInBase64() {
        return mapperInBase64;
    }

    public void setMapperInBase64(String mapperInBase64) {
        this.mapperInBase64 = mapperInBase64;
    }

    public String getReducerInBase64() {
        return reducerInBase64;
    }

    public void setReducerInBase64(String reducerInBase64) {
        this.reducerInBase64 = reducerInBase64;
    }

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }
}
