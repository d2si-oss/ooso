package fr.d2si.serverless_mapreduce.mapper;

import java.io.BufferedReader;

public abstract class MapperAbstract {
    public abstract String map(BufferedReader objectBufferedReader);
}
