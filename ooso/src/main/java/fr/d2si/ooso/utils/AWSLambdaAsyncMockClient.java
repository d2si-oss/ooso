package fr.d2si.ooso.utils;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.regions.Region;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class AWSLambdaAsyncMockClient implements AWSLambda {
    private Map<String, String> lambdaHandlerMapping;
    private ErrorDetectingThreadPool threadPool;

    public AWSLambdaAsyncMockClient() {
        lambdaHandlerMapping = new HashMap<>(6);
        threadPool = new ErrorDetectingThreadPool();

        JobInfo jobInfo = JobInfoProvider.getJobInfo();

        lambdaHandlerMapping.put(jobInfo.getMappersDriverFunctionName(), "fr.d2si.ooso.mappers_driver.MappersDriver");
        lambdaHandlerMapping.put(jobInfo.getReducersDriverFunctionName(), "fr.d2si.ooso.reducers_driver.ReducersDriver");
        lambdaHandlerMapping.put(jobInfo.getMapperFunctionName(), "fr.d2si.ooso.mapper_wrapper.MapperWrapper");
        lambdaHandlerMapping.put(jobInfo.getReducerFunctionName(), "fr.d2si.ooso.reducer_wrapper.ReducerWrapper");
        lambdaHandlerMapping.put(jobInfo.getMappersListenerFunctionName(), "fr.d2si.ooso.mappers_listener.MappersListener");
        lambdaHandlerMapping.put(jobInfo.getReducersListenerFunctionName(), "fr.d2si.ooso.reducers_listener.ReducersListener");
    }

    @Override
    public InvokeResult invoke(InvokeRequest invokeRequest) {
        try {
            String invocationType = invokeRequest.getInvocationType();

            if (!invocationType.equals(InvocationType.Event.toString()))
                throw new RuntimeException("This client supports async invocations only.");

            String functionName = invokeRequest.getFunctionName();
            ByteBuffer payload = invokeRequest.getPayload();

            String functionClassName = lambdaHandlerMapping.get(functionName);

            Class<?> functionClass = getLambdaClass(functionClassName);
            invokeLambda(functionClass, payload);

        } catch (Exception e) {
            return new InvokeResult().withStatusCode(500);
        }
        return new InvokeResult().withStatusCode(200);
    }

    private Class<?> getLambdaClass(String functionClassName) throws ClassNotFoundException {
        return getClass().getClassLoader().loadClass(functionClassName);
    }

    private void invokeLambda(Class<?> functionClass, ByteBuffer payload) throws IllegalAccessException, InstantiationException, InvocationTargetException, ExecutionException, InterruptedException {
        Gson gson = new GsonBuilder().serializeNulls().setLenient().create();
        Class<?> lambdaPayloadClass = getLambdaPayloadClass(functionClass);
        Object lambdaPayload = gson.fromJson(new String(payload.array()), lambdaPayloadClass);

        RequestHandler functionClassInstance = ((RequestHandler) functionClass.newInstance());
        threadPool.submit(() -> functionClassInstance.handleRequest(lambdaPayload, new MockContext()));

    }

    private Class<?> getLambdaPayloadClass(Class<?> functionClass) {
        Class<?> payloadClass = null;

        for (Method method : functionClass.getDeclaredMethods()) {
            if (method.getName().equals("handleRequest")) {
                payloadClass = method.getParameterTypes()[0];
            }
        }
        return payloadClass;
    }

    public void awaitWorkflowEnd() throws Exception {
        boolean exit = false;
        while (!exit) {
            Thread.sleep(100);
            exit = threadPool.getActiveCount() == 0;
            if (threadPool.isExceptionOccured()) {
                throw threadPool.getException();
            }
        }
    }

    @Override
    public void setEndpoint(String s) {

    }

    @Override
    public void setRegion(Region region) {

    }

    @Override
    public AddPermissionResult addPermission(AddPermissionRequest addPermissionRequest) {
        return null;
    }

    @Override
    public CreateAliasResult createAlias(CreateAliasRequest createAliasRequest) {
        return null;
    }

    @Override
    public CreateEventSourceMappingResult createEventSourceMapping(CreateEventSourceMappingRequest createEventSourceMappingRequest) {
        return null;
    }

    @Override
    public CreateFunctionResult createFunction(CreateFunctionRequest createFunctionRequest) {
        return null;
    }

    @Override
    public DeleteAliasResult deleteAlias(DeleteAliasRequest deleteAliasRequest) {
        return null;
    }

    @Override
    public DeleteEventSourceMappingResult deleteEventSourceMapping(DeleteEventSourceMappingRequest deleteEventSourceMappingRequest) {
        return null;
    }

    @Override
    public DeleteFunctionResult deleteFunction(DeleteFunctionRequest deleteFunctionRequest) {
        return null;
    }

    @Override
    public GetAccountSettingsResult getAccountSettings(GetAccountSettingsRequest getAccountSettingsRequest) {
        return null;
    }

    @Override
    public GetAliasResult getAlias(GetAliasRequest getAliasRequest) {
        return null;
    }

    @Override
    public GetEventSourceMappingResult getEventSourceMapping(GetEventSourceMappingRequest getEventSourceMappingRequest) {
        return null;
    }

    @Override
    public GetFunctionResult getFunction(GetFunctionRequest getFunctionRequest) {
        return null;
    }

    @Override
    public GetFunctionConfigurationResult getFunctionConfiguration(GetFunctionConfigurationRequest getFunctionConfigurationRequest) {
        return null;
    }

    @Override
    public GetPolicyResult getPolicy(GetPolicyRequest getPolicyRequest) {
        return null;
    }

    @Override
    public InvokeAsyncResult invokeAsync(InvokeAsyncRequest invokeAsyncRequest) {
        return null;
    }

    @Override
    public ListAliasesResult listAliases(ListAliasesRequest listAliasesRequest) {
        return null;
    }

    @Override
    public ListEventSourceMappingsResult listEventSourceMappings(ListEventSourceMappingsRequest listEventSourceMappingsRequest) {
        return null;
    }

    @Override
    public ListEventSourceMappingsResult listEventSourceMappings() {
        return null;
    }

    @Override
    public ListFunctionsResult listFunctions(ListFunctionsRequest listFunctionsRequest) {
        return null;
    }

    @Override
    public ListFunctionsResult listFunctions() {
        return null;
    }

    @Override
    public ListVersionsByFunctionResult listVersionsByFunction(ListVersionsByFunctionRequest listVersionsByFunctionRequest) {
        return null;
    }

    @Override
    public PublishVersionResult publishVersion(PublishVersionRequest publishVersionRequest) {
        return null;
    }

    @Override
    public RemovePermissionResult removePermission(RemovePermissionRequest removePermissionRequest) {
        return null;
    }

    @Override
    public UpdateAliasResult updateAlias(UpdateAliasRequest updateAliasRequest) {
        return null;
    }

    @Override
    public UpdateEventSourceMappingResult updateEventSourceMapping(UpdateEventSourceMappingRequest updateEventSourceMappingRequest) {
        return null;
    }

    @Override
    public UpdateFunctionCodeResult updateFunctionCode(UpdateFunctionCodeRequest updateFunctionCodeRequest) {
        return null;
    }

    @Override
    public UpdateFunctionConfigurationResult updateFunctionConfiguration(UpdateFunctionConfigurationRequest updateFunctionConfigurationRequest) {
        return null;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public ResponseMetadata getCachedResponseMetadata(AmazonWebServiceRequest amazonWebServiceRequest) {
        return null;
    }

    private class ErrorDetectingThreadPool extends ThreadPoolExecutor {

        private boolean exceptionOccured = false;
        private Exception exception;

        ErrorDetectingThreadPool() {
            super(0, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<>());
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (t == null && r instanceof Future<?>) {
                try {
                    ((Future<?>) r).get();
                } catch (CancellationException ce) {
                    t = ce;
                } catch (ExecutionException ee) {
                    t = ee.getCause();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // ignore/reset
                }
            }
            if (t != null) {
                this.exceptionOccured = true;
                this.exception = (Exception) t;
                this.shutdownNow();
            }
        }

        boolean isExceptionOccured() {
            return exceptionOccured;
        }

        Exception getException() {
            return exception;
        }
    }


}
