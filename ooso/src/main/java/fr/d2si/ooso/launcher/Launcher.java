package fr.d2si.ooso.launcher;

import com.google.gson.Gson;
import fr.d2si.ooso.mapper.MapperAbstract;
import fr.d2si.ooso.mappers_driver.MappersDriverInfo;
import fr.d2si.ooso.reducer.ReducerAbstract;
import fr.d2si.ooso.utils.Commons;
import fr.d2si.ooso.utils.JobInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class Launcher {
    private JobInfo jobInfo;

    private MapperAbstract mapper;
    private ReducerAbstract reducer;

    private Logger logger;

    public Launcher withMapper(MapperAbstract mapper) {
        this.mapper = mapper;
        return this;
    }

    public Launcher withReducer(ReducerAbstract reducer) {
        this.reducer = reducer;
        return this;
    }

    public void launchJob() {
        try {
            loadJobInfo();
            configureLogger();

            if (this.mapper == null || (!this.jobInfo.getDisableReducer() && reducer == null)) {
                throw new RuntimeException("You must set the mapper and reducer classes");
            }

            launchMappersDriver();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void configureLogger() {
        this.logger = Logger.getAnonymousLogger();
    }

    private void loadJobInfo() throws IOException {
        this.logger.info("Loading Job Configuration...");

        Gson gson = new Gson();
        InputStream driverInfoStream = getClass().getClassLoader().getResourceAsStream("jobInfo.json");
        BufferedReader driverInfoReader = new BufferedReader(new InputStreamReader(driverInfoStream));
        this.jobInfo = gson.fromJson(driverInfoReader, JobInfo.class);
        driverInfoReader.close();
    }

    private void launchMappersDriver() throws IOException {
        this.logger.info("Serializing mapper and reducer...");

        String mapperInBase64 = Commons.objectToBase64(this.mapper);
        String reducerInBase64 = Commons.objectToBase64(this.reducer);

        this.logger.info("Launching job...");

        MappersDriverInfo mappersDriverInfo = new MappersDriverInfo(mapperInBase64, reducerInBase64, this.jobInfo);

        Commons.invokeLambdaAsync(this.jobInfo.getMappersDriverFunctionName(), mappersDriverInfo);
    }
}
