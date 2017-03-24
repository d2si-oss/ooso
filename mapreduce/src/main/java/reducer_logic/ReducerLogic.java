package reducer_logic;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.gson.Gson;
import utils.AmazonS3Provider;
import utils.ObjectInfoSimple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReducerLogic {

    public static String reduceResultCalculator(List<ObjectInfoSimple> batch) throws IOException {
        Gson gson = new Gson();

        Map<String, Double> reduceTransactionCountPerProduct = new HashMap<>();

        AmazonS3 s3Client = AmazonS3Provider.getInstance();

        for (ObjectInfoSimple objectInfo : batch) {
            S3Object object = s3Client.getObject(objectInfo.getBucket(), objectInfo.getKey());
            S3ObjectInputStream objectContentRawStream = object.getObjectContent();
            BufferedReader objectBufferedReader = new BufferedReader(new InputStreamReader(objectContentRawStream));

            Map<String, Double> transactionCountPerProduct = gson.fromJson(objectBufferedReader, HashMap.class);
            transactionCountPerProduct.forEach((k, v) -> reduceTransactionCountPerProduct.merge(k, v, (v1, v2) -> v1 + v2));
        }

        return gson.toJson(reduceTransactionCountPerProduct);

    }
}
