package job;

import fr.d2si.ooso.launcher.Launcher;
import mapper.Mapper;
import reducer.Reducer;

public class LaunchJob {
    public static void main(String[] args) {
        new Launcher()
                .withMapper(new Mapper())
                .withReducer(new Reducer())
                .launchJob();
    }
}