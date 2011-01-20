package console.actor;

import akka.camel.UntypedProducerActor;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: Jan 20, 2011
 * Time: 7:47:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class JMSProducerActor extends UntypedProducerActor {

    public String getEndpointUri() {
        return "jms:queue:test";
    }

    @Override
    public boolean isOneway() {
        return true;
    }
}
