package fr.d2si.serverless_mapreduce.reducer;

import fr.d2si.serverless_mapreduce.utils.ObjectInfoSimple;

import java.util.List;

public abstract class ReducerAbstract {
    public abstract String reduce(List<ObjectInfoSimple> batch);
}
