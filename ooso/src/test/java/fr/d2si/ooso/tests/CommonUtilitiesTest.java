package fr.d2si.ooso.tests;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import fr.d2si.ooso.utils.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.*;

public class CommonUtilitiesTest {
    private static JobInfo jobInfo;

    private static AmazonS3 s3Client;
    private static final String DUMMY_BUCKET_NAME = "dummy-bucket";

    private static final Map<String, String> KEY_CONTENT_MAPPING = Collections.unmodifiableMap(
            Stream.of(
                    new SimpleEntry<>("pref1/dummy1", "dummy1"),
                    new SimpleEntry<>("pref1/dummy2", "dummy2"),
                    new SimpleEntry<>("pref1/dummy3", "dummy3"),
                    new SimpleEntry<>("pref1/dummy4", "dummy4"),
                    new SimpleEntry<>("pref1/dummy5", "dummy5"),
                    new SimpleEntry<>("pref1/dummy6", "dummy6"),
                    new SimpleEntry<>("pref1/dummy7", "dummy7"),

                    new SimpleEntry<>("pref2/dummy1", "dummy1"),
                    new SimpleEntry<>("pref2/dummy2", "dummy2"),
                    new SimpleEntry<>("pref2/dummy3", "dummy3"),
                    new SimpleEntry<>("pref2/dummy4", "dummy4"),
                    new SimpleEntry<>("pref2/dummy5", "dummy5"),
                    new SimpleEntry<>("pref2/dummy6", "dummy6"),
                    new SimpleEntry<>("pref2/dummy7", "dummy7")
            ).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue)));

    @BeforeClass
    public static void setUp() throws Exception {
        jobInfo = JobInfoProvider.getJobInfo();

        s3Client = AmazonS3Provider.getS3Client();

        s3Client.createBucket(DUMMY_BUCKET_NAME);

        //putting test data in local s3 bucket
        File dataDir = new File(new File("").getAbsoluteFile().getParent() + "/test-data/250mb");
        for (File file : dataDir.listFiles()) {
            Commons.storeObject(Commons.TEXT_TYPE, file, DUMMY_BUCKET_NAME, "data/" + file.getName());
        }
        for (Map.Entry<String, String> entry : KEY_CONTENT_MAPPING.entrySet()) {
            Commons.storeObject(Commons.TEXT_TYPE, entry.getValue(), DUMMY_BUCKET_NAME, entry.getKey());
        }
    }

    @Test
    public void jobInfoNotNull() throws Exception {
        assertNotNull(jobInfo);
    }

    @Test
    public void jobInfoContainsTrueData() throws Exception {
        assertEquals(jobInfo.getJobId(), "f4a88b72-357d-11e7-9140-34f39a0a3e44");
        assertEquals(jobInfo.getMapperMemory(), 1536);
        assertEquals(jobInfo.getReducerMemory(), 1536);
        assertEquals(jobInfo.getJobInputBucket(), "my-dataset/500mb");
        assertEquals(jobInfo.getMapperOutputBucket(), "mappers-bucket");
        assertEquals(jobInfo.getReducerOutputBucket(), "reducers-bucket");
        assertEquals(jobInfo.getMapperFunctionName(), "mapper");
        assertEquals(jobInfo.getReducerFunctionName(), "reducer");
        assertEquals(jobInfo.getMapperForceBatchSize(), -1);
        assertEquals(jobInfo.getReducerForceBatchSize(), -1);
        assertFalse(jobInfo.getDisableReducer());
    }

    @Test
    public void s3SingletonTest() throws Exception {
        AmazonS3 firstInstance = AmazonS3Provider.getS3Client();
        AmazonS3 secondInstance = AmazonS3Provider.getS3Client();

        assertTrue(firstInstance == secondInstance);
    }

    @Test
    public void lambdaSingletonTest() throws Exception {
        AWSLambda firstInstance = AWSLambdaProvider.getLambdaClient();
        AWSLambda secondInstance = AWSLambdaProvider.getLambdaClient();

        assertTrue(firstInstance == secondInstance);
    }

    @Test
    public void bucketCreationTest() throws Exception {
        s3Client.createBucket(DUMMY_BUCKET_NAME);
        assertTrue(s3Client.doesBucketExist(DUMMY_BUCKET_NAME));
    }

    @Test
    public void storeObjectTest() throws Exception {
        S3Object object = s3Client.getObject(DUMMY_BUCKET_NAME, "pref1/dummy1");

        ObjectMetadata objectMetadata = object.getObjectMetadata();
        BufferedReader reader = new BufferedReader(new InputStreamReader(object.getObjectContent()));
        String storedContent = reader.readLine();
        reader.close();

        assertEquals(objectMetadata.getContentType(), Commons.TEXT_TYPE);
        assertEquals(objectMetadata.getContentLength(), KEY_CONTENT_MAPPING.get("pref1/dummy1").getBytes().length);
        assertEquals(storedContent, KEY_CONTENT_MAPPING.get("pref1/dummy1"));
    }

    @Test
    public void objectInfoSimpleToReaderTest() throws Exception {
        ObjectInfoSimple infoSimple = new ObjectInfoSimple(DUMMY_BUCKET_NAME, "pref1/dummy1");

        BufferedReader readerFromObjectInfo = Commons.getReaderFromObjectInfo(infoSimple);
        String storedContent = readerFromObjectInfo.readLine();
        readerFromObjectInfo.close();

        assertEquals(storedContent, KEY_CONTENT_MAPPING.get("pref1/dummy1"));
    }

    @Test
    public void pathUtilitiesTest() throws Exception {
        String pathWithoutSlashes = "bucket";
        String pathWithOneSlash = "bucket/";
        String pathWithMultipleSlashesAndTailSlash = "bucket/haha/xd/lol/";
        String pathWithMultipleSlashesWithoutTailSlash = "bucket/haha/xd/lol";

        assertEquals(Commons.getBucketFromFullPath(pathWithoutSlashes), "bucket");
        assertEquals(Commons.getPrefixFromFullPath(pathWithoutSlashes), "");

        assertEquals(Commons.getBucketFromFullPath(pathWithOneSlash), "bucket");
        assertEquals(Commons.getPrefixFromFullPath(pathWithOneSlash), "");

        assertEquals(Commons.getBucketFromFullPath(pathWithMultipleSlashesAndTailSlash), "bucket");
        assertEquals(Commons.getPrefixFromFullPath(pathWithMultipleSlashesAndTailSlash), "haha/xd/lol");

        assertEquals(Commons.getBucketFromFullPath(pathWithMultipleSlashesWithoutTailSlash), "bucket");
        assertEquals(Commons.getPrefixFromFullPath(pathWithMultipleSlashesWithoutTailSlash), "haha/xd/lol");
    }

    @Test
    public void getBucketObjectSummariesTest() throws Exception {
        List<S3ObjectSummary> bucketObjectSummariesWithPrefix = Commons.getBucketObjectSummaries(DUMMY_BUCKET_NAME, "pref1/");
        Set<String> storedPrefixedKeys = bucketObjectSummariesWithPrefix.stream().map(S3ObjectSummary::getKey).collect(toSet());
        Set<String> expectedPrefixedKeys = KEY_CONTENT_MAPPING.keySet().stream().filter(o -> o.startsWith("pref1/")).collect(toSet());
        assertEquals(storedPrefixedKeys, expectedPrefixedKeys);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Commons.emptyBucket(DUMMY_BUCKET_NAME);
        s3Client.deleteBucket(DUMMY_BUCKET_NAME);
    }
}