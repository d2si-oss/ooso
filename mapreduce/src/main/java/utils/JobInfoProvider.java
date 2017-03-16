package utils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JobInfoProvider {
    public static JobInfo getJobInfo() throws IOException {
        Gson gson = new Gson();
        InputStream driverInfoStream = JobInfoProvider.class.getClassLoader()
                .getResourceAsStream("jobInfo.json");
        BufferedReader driverInfoReader = new BufferedReader(new InputStreamReader(driverInfoStream));
        return gson.fromJson(driverInfoReader, JobInfo.class);
    }
}
