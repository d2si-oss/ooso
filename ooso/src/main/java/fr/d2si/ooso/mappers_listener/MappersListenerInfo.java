package fr.d2si.ooso.mappers_listener;

import fr.d2si.ooso.utils.JobInfo;

public class MappersListenerInfo {
    private String reducerInBase64;
    private JobInfo jobInfo;

    public MappersListenerInfo() {
    }

    public MappersListenerInfo(String reducerInBase64, JobInfo jobInfo) {
        this.reducerInBase64 = reducerInBase64;
        this.jobInfo = jobInfo;
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
