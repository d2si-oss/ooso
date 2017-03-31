package reducer;

import utils.ObjectInfoSimple;

import java.util.List;

public abstract class ReducerAbstract {
    public abstract String reduce(List<ObjectInfoSimple> batch);
}
