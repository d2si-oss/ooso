package reducer_wrapper;

import utils.ObjectInfoSimple;

import java.util.List;
import java.util.Map;

public class ReducerWrapperInfo {
    private int id;
    private List<ObjectInfoSimple> batch;
    private int step;

    public ReducerWrapperInfo() {
    }

    public ReducerWrapperInfo(int id, List<ObjectInfoSimple> batch, int step) {
        this.id = id;
        this.batch = batch;
        this.step = step;
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
}
