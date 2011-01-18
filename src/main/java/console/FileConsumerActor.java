package console;

import akka.camel.Message;
import akka.camel.UntypedConsumerActor;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: Jan 18, 2011
 * Time: 1:04:23 AM
 * To change this template use File | Settings | File Templates.
 */
    public class FileConsumerActor extends UntypedConsumerActor {
        public String getEndpointUri() {
//            return "file:data/input/actor";
            return "jetty:http://localhost:8012/camel/default";

        }

        public void onReceive(Object message) {
            Message msg = (Message) message;
            String body = msg.getBodyAs(String.class);
            System.out.println(String.format("received %s", body));
            doSomething();
        }

        public void doSomething()  {


        }


    }
