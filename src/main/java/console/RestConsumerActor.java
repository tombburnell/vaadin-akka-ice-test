package console;

import akka.camel.Message;
import akka.camel.UntypedConsumerActor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: Jan 18, 2011
 * Time: 1:04:23 AM
 * To change this template use File | Settings | File Templates.
 */

public class RestConsumerActor extends UntypedConsumerActor {
    public String getEndpointUri() {
//     return "file:data/input/actor";
        return "jetty:http://localhost:8012/camel/default";

    }

    public void onReceive(Object message) {
        Message msg = (Message) message;
        String body = msg.getBodyAs(String.class);

        System.out.println(String.format("received %s", body));

        Map<String, List<String>> params = null;
        try {
            params = convertUrlToMap(body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(String.format("received %s", params));

        doSomething(params);

        getContext().replySafe(String.format("Hello %s", body));

    }


    public void doSomething(Map<String, List<String>> body)  {  }



    //convert provided query string to a map
    Map<String, List<String>> convertUrlToMap(String query) throws UnsupportedEncodingException {

        Map<String, List<String>> params = new HashMap<String, List<String>>();

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            String key = URLDecoder.decode(pair[0], "UTF-8");
            String value = URLDecoder.decode(pair[1], "UTF-8");
            List<String> values = params.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                params.put(key, values);
            }
            values.add(value);
        }
        return params;
    }


}
