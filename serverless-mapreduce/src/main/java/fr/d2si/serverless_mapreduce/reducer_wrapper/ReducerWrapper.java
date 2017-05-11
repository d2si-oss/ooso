package fr.d2si.serverless_mapreduce.reducer_wrapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import fr.d2si.serverless_mapreduce.reducer.ReducerAbstract;
import fr.d2si.serverless_mapreduce.utils.Commons;
import fr.d2si.serverless_mapreduce.utils.JobInfo;
import fr.d2si.serverless_mapreduce.utils.JobInfoProvider;
import fr.d2si.serverless_mapreduce.utils.ObjectInfoSimple;

import java.io.IOException;
import java.util.List;

public class ReducerWrapper implements RequestHandler<ReducerWrapperInfo, String> {
    private ReducerAbstract reducerLogic;

    private JobInfo jobInfo;

    private String jobId;

    private ReducerWrapperInfo reducerWrapperInfo;

    @Override
    public String handleRequest(ReducerWrapperInfo reducerWrapperInfo, Context context) {

        try {

            this.reducerLogic = instantiateReducerClass();

            this.jobInfo = JobInfoProvider.getJobInfo();

            this.jobId = this.jobInfo.getJobId();

            this.reducerWrapperInfo = reducerWrapperInfo;

            List<ObjectInfoSimple> batch = reducerWrapperInfo.getBatch();

            String reduceResult = processBatch(batch);

            storeResult(reduceResult, this.reducerWrapperInfo.isLast());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "OK";
    }

    private ReducerAbstract instantiateReducerClass() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return (ReducerAbstract) getClass().getClassLoader().loadClass("reducer.Reducer").newInstance();
    }

    private String processBatch(List<ObjectInfoSimple> batch) throws Exception {
        return this.reducerLogic.reduce(batch);
    }


    private void storeResult(String result, Boolean last) throws IOException {

        Commons.storeObject(Commons.TEXT_TYPE,
                result,
                jobInfo.getReducerOutputBucket(),
                last ?
                        this.jobId + "/result" :
                        this.jobId + "/" + this.reducerWrapperInfo.getStep() + "-reducer-" + this.reducerWrapperInfo.getId());
    }
}
