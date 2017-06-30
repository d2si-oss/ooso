package fr.d2si.ooso.reducers_listener;

import fr.d2si.ooso.utils.JobInfo;

public class ReducersListenerInfo {
    private int step;
    private int expectedFilesCount;
    private String reducerInBase64;
    private JobInfo jobInfo;

    public JobInfo getJobInfo() {
        return jobInfo;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    public ReducersListenerInfo() {
    }

    public ReducersListenerInfo(int step, int expectedFilesCount, String reducerInBase64, JobInfo jobInfo) {
        this.step = step;
        this.expectedFilesCount = expectedFilesCount;
        this.reducerInBase64 = reducerInBase64;
        this.jobInfo = jobInfo;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getExpectedFilesCount() {
        return expectedFilesCount;
    }

    public void setExpectedFilesCount(int expectedFilesCount) {
        this.expectedFilesCount = expectedFilesCount;
    }

    public String getReducerInBase64() {
        return reducerInBase64;
    }

    public void setReducerInBase64(String reducerInBase64) {
        this.reducerInBase64 = reducerInBase64;
    }
}
