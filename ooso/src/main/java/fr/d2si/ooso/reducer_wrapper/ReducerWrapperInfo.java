package fr.d2si.ooso.reducer_wrapper;

import fr.d2si.ooso.utils.ObjectInfoSimple;

import java.util.List;

public class ReducerWrapperInfo {
    private int id;
    private List<ObjectInfoSimple> batch;
    private int step;
    private boolean last;

    public ReducerWrapperInfo() {
    }

    public ReducerWrapperInfo(int id, List<ObjectInfoSimple> batch, int step, boolean last) {
        this.id = id;
        this.batch = batch;
        this.step = step;
        this.last = last;
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
}
