package console.actor;

import akka.camel.Message;
import akka.camel.UntypedConsumerActor;
import akka.persistence.common.PersistentVector;
import akka.persistence.couchdb.CouchDBStorage;
import akka.stm.Atomic;
import console.util.QueryStringMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;


public class MyConsumerActor extends UntypedConsumerActor {

    QueryStringMapper QSMapper = new QueryStringMapper();

    PersistentVector<byte[]> messageLog;

    Logger log = LoggerFactory.getLogger(MyConsumerActor.class);

    Boolean persist = false;

    public MyConsumerActor(Boolean persist) {
        if (persist) {
            messageLog = CouchDBStorage.newVector("messageLog");
            this.persist = true;
        }
    }

    public String getEndpointUri() {
        return "jetty:http://localhost:8012/camel/default";

    }

    public void onReceive(Object message) {
        Message msg = (Message) message;
        String b = msg.getBodyAs(String.class);
        final String body = b.replaceAll("\\n", "");

//        System.out.println(String.format("received %s", body));

        Map<String, List<String>> params = null;
        try {
            params = QSMapper.convertUrlToMap(body);
            processParams(params);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // Write to persistent STM messageLog on CouchDB
        if (persist) {
            new Atomic() {
                public Object atomically() {
                    try {
                        messageLog.add(body.getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        log.info("Failed to add to messageLog");
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        }

        getContext().replySafe(String.format("Affirmative! %s\n", body));

    }

    public void processParams(Map<String, List<String>> body) {
    }

}
