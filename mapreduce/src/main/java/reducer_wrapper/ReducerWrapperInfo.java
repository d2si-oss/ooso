package reducer_wrapper;

import java.util.List;
import java.util.Map;

public class ReducerWrapperInfo {
    private int id;
    private List<Map<String, String>> batch;
    private int step;

    public ReducerWrapperInfo() {
    }

    public ReducerWrapperInfo(int id, List<Map<String, String>> batch, int step) {
        this.id = id;
        this.batch = batch;
        this.step = step;
    }

    public List<Map<String, String>> getBatch() {
        return batch;
    }

    public void setBatch(List<Map<String, String>> batch) {
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
