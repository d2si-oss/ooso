package mapper_wrapper;

import java.util.List;
import java.util.Map;

public class MapperWrapperInfo {
    private List<Map<String, String>> batch;
    private long id;

    public MapperWrapperInfo() {
    }

    public MapperWrapperInfo(List<Map<String, String>> batch, long id) {
        this.batch = batch;
        this.id = id;
    }

    public List<Map<String, String>> getBatch() {
        return batch;
    }

    public void setBatch(List<Map<String, String>> batch) {
        this.batch = batch;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
