package fr.d2si.ooso.mapper_wrapper;

import fr.d2si.ooso.utils.ObjectInfoSimple;

import java.util.List;

public class MapperWrapperInfo {
    private List<ObjectInfoSimple> batch;
    private long id;

    public MapperWrapperInfo() {
    }

    public MapperWrapperInfo(List<ObjectInfoSimple> batch, long id) {
        this.batch = batch;
        this.id = id;
    }

    public List<ObjectInfoSimple> getBatch() {
        return batch;
    }

    public void setBatch(List<ObjectInfoSimple> batch) {
        this.batch = batch;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
