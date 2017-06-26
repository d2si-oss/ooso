package fr.d2si.ooso.reducer_wrapper;

import fr.d2si.ooso.utils.JobInfo;
import fr.d2si.ooso.utils.ObjectInfoSimple;

import java.util.List;

public class ReducerWrapperInfo {
    private int id;
    private List<ObjectInfoSimple> batch;
    private int step;
    private String reducerInBase64;
    private boolean last;
    private JobInfo jobInfo;

    public ReducerWrapperInfo() {
    }

    public ReducerWrapperInfo(int id, List<ObjectInfoSimple> batch, int step, String reducerInBase64, boolean last, JobInfo jobInfo) {
        this.id = id;
        this.batch = batch;
        this.step = step;
        this.reducerInBase64 = reducerInBase64;
        this.last = last;
        this.jobInfo = jobInfo;
    }

    public List<ObjectInfoSimple> getBatch() {
        return batch;
    }

    public void setBatch(List<ObjectInfoSimple> batch) {
        this.batch = batch;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isLast() {
        return last;
    }

    public boolean getLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
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
