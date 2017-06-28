package fr.d2si.ooso.reducer;

import fr.d2si.ooso.utils.ObjectInfoSimple;

import java.io.Serializable;
import java.util.List;

public abstract class ReducerAbstract implements Serializable {
    public abstract String reduce(List<ObjectInfoSimple> batch);
}
