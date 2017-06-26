package fr.d2si.ooso.mapper;

import java.io.BufferedReader;
import java.io.Serializable;

public abstract class MapperAbstract implements Serializable {
    public abstract String map(BufferedReader objectBufferedReader);
}
