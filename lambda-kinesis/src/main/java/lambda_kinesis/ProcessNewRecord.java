package lambda_kinesis;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;

import java.nio.ByteBuffer;

/**
 * Basic lambda and kinesis integration. One would normally validate the stream etc..
 * Only for experimentation purposes
 */
public class ProcessNewRecord implements RequestHandler<KinesisEvent, String> {
    public String handleRequest(KinesisEvent kinesisEvent, Context context) {
        try {
            for (KinesisEvent.KinesisEventRecord record : kinesisEvent.getRecords()) {
                ByteBuffer rawInt = record.getKinesis().getData();
                rawInt.rewind();
                int myInt = Integer.parseInt(new String(rawInt.array()));
                System.out.println(myInt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }
}
