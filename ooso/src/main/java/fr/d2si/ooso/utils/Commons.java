package fr.d2si.ooso.utils;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringInputStream;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.Base64;
import java.util.List;

import static java.util.stream.Collectors.toList;

public final class Commons {
    public final static String IGNORED_RETURN_VALUE = "";

    public final static String JSON_TYPE = "application/json";
    public final static String TEXT_TYPE = "text/plain";
    private final static Gson GSON = new GsonBuilder().serializeNulls().setLenient().create();

    public static List<S3ObjectSummary> getBucketObjectSummaries(String bucket) {
        return getBucketObjectSummaries(bucket, "");
    }

    public static List<S3ObjectSummary> getBucketObjectSummaries(String bucket, String prefix) {
        String realBucket = getBucketFromFullPath(bucket);
        String preprefix = getPrefixFromFullPath(bucket);

        AmazonS3 s3Client = AmazonS3Provider.getS3Client();
        ListObjectsRequest req = new ListObjectsRequest().withBucketName(realBucket).withPrefix(!preprefix.equals("") ? preprefix + "/" + prefix : prefix);
        ObjectListing objectListing = s3Client.listObjects(req);
        List<S3ObjectSummary> summaries = objectListing.getObjectSummaries();

        while (objectListing.isTruncated()) {
            objectListing = s3Client.listNextBatchOfObjects(objectListing);
            summaries.addAll(objectListing.getObjectSummaries());
        }

        return summaries.stream().filter(obj -> !obj.getKey().equals("") && !obj.getKey().endsWith("/")).collect(toList());
    }

    public static String getBucketFromFullPath(String path) {
        return path.substring(0,
                !path.contains("/") ?
                        path.length() :
                        path.indexOf("/"));
    }

    public static String getPrefixFromFullPath(String path) {
        String realBucket = getBucketFromFullPath(path);
        String prefix = realBucket.equals(path) ? "" : path.substring(path.indexOf("/") + 1, path.length());
        prefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        return prefix;
    }

    public static void storeObject(String contentType,
                                   String content,
                                   String destBucket,
                                   String destKey) throws UnsupportedEncodingException {
        AmazonS3 s3Client = AmazonS3Provider.getS3Client();

        ObjectMetadata metadata = prepareObjectMetadata(contentType, content);

        s3Client.putObject(
                destBucket,
                destKey,
                new StringInputStream(content),
                metadata);
    }

    private static ObjectMetadata prepareObjectMetadata(String contentType, String content) {
        ObjectMetadata metadata = prepareObjectMetadata(contentType);
        metadata.setContentLength(content.getBytes().length);
        return metadata;
    }

    private static ObjectMetadata prepareObjectMetadata(String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        return metadata;
    }

    public static void storeObject(String contentType,
                                   File content,
                                   String destBucket,
                                   String destKey) {
        AmazonS3 s3Client = AmazonS3Provider.getS3Client();

        ObjectMetadata metadata = prepareObjectMetadata(contentType);
        s3Client.putObject(new PutObjectRequest(destBucket, destKey, content).withMetadata(metadata));
    }

    public static List<List<ObjectInfoSimple>> getBatches(String bucket, int memory, int desiredBatchSize) {
        return getBatches(bucket, memory, "", desiredBatchSize);
    }

    public static List<List<ObjectInfoSimple>> getBatches(String bucket, int memory, String prefix, int desiredBatchSize) {
        List<S3ObjectSummary> objectSummaries = Commons.getBucketObjectSummaries(bucket, prefix);
        int batchSize;
        if (desiredBatchSize <= 0)
            batchSize = getBatchSize(objectSummaries, memory);
        else
            batchSize = desiredBatchSize;
        List<ObjectInfoSimple> unPartitionedList = objectSummaries.stream().map(ObjectInfoSimple::new).collect(toList());
        return Lists.partition(unPartitionedList, batchSize);
    }

    public static int getBatchSize(List<S3ObjectSummary> objectSummaries, int availableMemory) {
        int maxUsableMemory = (int) (0.6 * 1024 * 1024 * availableMemory);
        long totalSize = 0;
        for (S3ObjectSummary summary : objectSummaries)
            totalSize += summary.getSize();
        double averageSize = totalSize / objectSummaries.size();
        return (int) (maxUsableMemory / averageSize);
    }

    public static void emptyBucket(String bucket) {
        emptyBucketWithPrefix(bucket, "");
    }

    public static void emptyBucketWithPrefix(String bucket, String prefix) {
        AmazonS3 s3Client = AmazonS3Provider.getS3Client();

        List<S3ObjectSummary> files = Commons.getBucketObjectSummaries(bucket, prefix);

        for (S3ObjectSummary object : files)
            s3Client.deleteObject(object.getBucketName(), object.getKey());
    }

    public static void invokeLambdaAsync(String function, Object payload) {
        invokeLambda(function, payload, true);
    }

    public static void invokeLambda(String function, Object payload, boolean async) {
        String payloadString = GSON.toJson(payload);
        AWSLambda lambda = AWSLambdaProvider.getLambdaClient();

        InvokeRequest request = new InvokeRequest()
                .withFunctionName(function)
                .withInvocationType(async ? InvocationType.Event : InvocationType.RequestResponse)
                .withPayload(payloadString);

        lambda.invoke(request);
    }

    public static BufferedReader getReaderFromObjectInfo(ObjectInfoSimple objectInfo) {
        AmazonS3 s3Client = AmazonS3Provider.getS3Client();

        S3Object object = s3Client.getObject(objectInfo.getBucket(), objectInfo.getKey());
        S3ObjectInputStream objectContentRawStream = object.getObjectContent();

        return new BufferedReader(new InputStreamReader(objectContentRawStream));
    }

    public static JobInfo loadJobInfo() {
        try (InputStream driverInfoStream = Commons.class.getClassLoader().getResourceAsStream("jobInfo.json");
             BufferedReader driverInfoReader = new BufferedReader(new InputStreamReader(driverInfoStream))) {
            Gson gson = new Gson();
            return gson.fromJson(driverInfoReader, JobInfo.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String objectToBase64(Serializable objectToSerialize) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(objectToSerialize);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    public static Object base64ToObject(String objectInBase64) throws IOException, ClassNotFoundException {
        byte[] data = Base64.getDecoder().decode(objectInBase64);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }
}
