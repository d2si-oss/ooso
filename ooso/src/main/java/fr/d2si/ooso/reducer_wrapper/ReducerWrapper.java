package fr.d2si.ooso.reducer_wrapper;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import fr.d2si.ooso.reducer.ReducerAbstract;
import fr.d2si.ooso.utils.JobInfo;
import fr.d2si.ooso.utils.ObjectInfoSimple;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static fr.d2si.ooso.utils.Commons.*;

public class ReducerWrapper implements RequestHandler<ReducerWrapperInfo, String> {
    private ReducerAbstract reducerLogic;

    private JobInfo jobInfo;

    private String jobId;

    private ReducerWrapperInfo reducerWrapperInfo;

    @Override
    public String handleRequest(ReducerWrapperInfo reducerWrapperInfo, Context context) {

        try {
            this.reducerWrapperInfo = reducerWrapperInfo;

            this.reducerLogic = instantiateReducerClass();

            this.jobInfo = this.reducerWrapperInfo.getJobInfo();

            this.jobId = this.jobInfo.getJobId();

            List<ObjectInfoSimple> batch = reducerWrapperInfo.getBatch();

            String reduceResult = processBatch(batch);

            if (this.reducerWrapperInfo.isLast())
                storeFinalResult(reduceResult);
            else
                storeIntermediateResult(reduceResult);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return IGNORED_RETURN_VALUE;
    }

    private ReducerAbstract instantiateReducerClass() throws ClassNotFoundException, IOException {
        return (ReducerAbstract) base64ToObject(this.reducerWrapperInfo.getReducerInBase64());
    }

    private String processBatch(List<ObjectInfoSimple> batch) {
        return this.reducerLogic.reduce(batch);
    }

    private void storeFinalResult(String reduceResult) throws UnsupportedEncodingException {
        storeObject(TEXT_TYPE,
                reduceResult,
                jobInfo.getReducerOutputBucket(),
                this.jobId + "/result");
    }

    private void storeIntermediateResult(String reduceResult) throws UnsupportedEncodingException {
        storeObject(TEXT_TYPE,
                reduceResult,
                jobInfo.getReducerOutputBucket(),
                this.jobId + "/" + this.reducerWrapperInfo.getStep() + "-reducer-" + this.reducerWrapperInfo.getId());
    }
}
