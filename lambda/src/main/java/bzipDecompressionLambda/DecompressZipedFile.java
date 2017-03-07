package bzipDecompressionLambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import java.io.BufferedInputStream;
import java.net.URLDecoder;

public class DecompressZipedFile implements RequestHandler<S3Event, String> {
    public String handleRequest(S3Event s3Event, Context context) {
        String destBucket = "othmane-output-bucket";
        try {
            AmazonS3 client = AmazonS3ClientBuilder.standard().build();

            S3EventNotificationRecord record = s3Event.getRecords().get(0);
            String srcBucket = record.getS3().getBucket().getName();
            String srcKey = record.getS3().getObject().getKey().replace("+", " ");
            srcKey = URLDecoder.decode(srcKey, "UTF-8");

            S3Object object = client.getObject(srcBucket, srcKey);

            S3ObjectInputStream objectContentRawStream = object.getObjectContent();
            BufferedInputStream objectBufferedInputStream = new BufferedInputStream(objectContentRawStream);
            BZip2CompressorInputStream objectBzipInputStream = new BZip2CompressorInputStream(objectBufferedInputStream);

            client.putObject(destBucket, srcKey.replace(".bz2", "") + "-unzipped", objectBzipInputStream, new ObjectMetadata());

            objectBzipInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }
}
