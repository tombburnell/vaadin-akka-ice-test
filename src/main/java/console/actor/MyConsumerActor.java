package console.actor;

import akka.camel.Message;
import akka.camel.UntypedConsumerActor;
import console.QueryStringMapper;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: Jan 18, 2011
 * Time: 1:04:23 AM
 * To change this template use File | Settings | File Templates.
 */

public class MyConsumerActor extends UntypedConsumerActor {

    QueryStringMapper QSMapper = new QueryStringMapper();

    public String getEndpointUri() {
        return "jetty:http://localhost:8012/camel/default";

    }

    public void onReceive(Object message) {
        Message msg = (Message) message;
        String body = msg.getBodyAs(String.class);

        System.out.println(String.format("received %s", body));

        Map<String, List<String>> params = null;
        try {
            params = QSMapper.convertUrlToMap(body);

            System.out.println(String.format("received %s", params));

            doSomething(params);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        getContext().replySafe(String.format("Hello %s", body));

    }

    public void doSomething(Map<String, List<String>> body) {
    }

}
