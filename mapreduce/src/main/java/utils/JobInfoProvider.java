package utils;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JobInfoProvider {
    private static JobInfo jobInfo;

    static {
        Gson gson = new Gson();
        InputStream driverInfoStream = JobInfoProvider.class.getClassLoader()
                .getResourceAsStream("jobInfo.json");
        BufferedReader driverInfoReader = new BufferedReader(new InputStreamReader(driverInfoStream));
        jobInfo = gson.fromJson(driverInfoReader, JobInfo.class);

    }

    public static JobInfo getJobInfo() {
        return jobInfo;
    }

    private JobInfoProvider() {
    }
}
