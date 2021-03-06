package fr.d2si.ooso.reducers_driver;

import fr.d2si.ooso.utils.JobInfo;

public class ReducersDriverInfo {
    private int step;
    private String reducerInBase64;
    private JobInfo jobInfo;

    public ReducersDriverInfo() {
    }

    public ReducersDriverInfo(int step, String reducerInBase64, JobInfo jobInfo) {
        this.step = step;
        this.reducerInBase64 = reducerInBase64;
        this.jobInfo = jobInfo;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
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
