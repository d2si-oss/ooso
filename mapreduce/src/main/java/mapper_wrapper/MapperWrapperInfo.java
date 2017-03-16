package mapper_wrapper;

import java.util.List;

public class MapperWrapperInfo {
    private List<String> batch;
    private long id;

    public MapperWrapperInfo() {
    }

    public MapperWrapperInfo(List<String> batch, long id) {
        this.batch = batch;
        this.id = id;
    }

    public List<String> getBatch() {
        return batch;
    }

    public void setBatch(List<String> batch) {
        this.batch = batch;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
