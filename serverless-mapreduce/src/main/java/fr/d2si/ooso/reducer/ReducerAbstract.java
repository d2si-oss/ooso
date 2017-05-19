package fr.d2si.ooso.reducer;

import fr.d2si.ooso.utils.ObjectInfoSimple;

import java.util.List;

public abstract class ReducerAbstract {
    public abstract String reduce(List<ObjectInfoSimple> batch);
}
