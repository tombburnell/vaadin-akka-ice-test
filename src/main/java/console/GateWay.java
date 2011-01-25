package console;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.camel.CamelContextManager;
import akka.camel.Message;
import akka.camel.UntypedConsumerActor;
import akka.camel.UntypedProducerActor;
import akka.persistence.common.PersistentMap;
import akka.persistence.common.PersistentVector;
import akka.persistence.couchdb.CouchDBStorage;
import akka.stm.Atomic;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.UnsupportedEncodingException;

import static akka.camel.CamelServiceManager.startCamelService;

public class GateWay {


    PersistentVector<byte[]> messageLog;

    Logger log = LoggerFactory.getLogger(GateWay.class);


    public GateWay() {
        messageLog = CouchDBStorage.newVector("messageLog");

    }

    public void start() {

        // Create CamelContext with Spring-based registry and custom route builder
        CamelContextManager.init(new DefaultCamelContext(
                new ApplicationContextRegistry(
                        new ClassPathXmlApplicationContext("/appContext.xml", ConsoleDemo.class)
                ))
        );

        startCamelService();


        final ActorRef jmsProducer = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {

                return new UntypedProducerActor() {
                    public String getEndpointUri() {
                        return "jms:topic:forge";
                    }

                    @Override
                    public boolean isOneway() {
                        return true;
                    }

                };
            }
        });


        jmsProducer.start();


        // Create an actor to listen to monitor for updates via JMS topic

        ActorRef jmsConsumer = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {

                return new UntypedConsumerActor() {

                    public String getEndpointUri() {
                        return "jms:topic:yo";
                    }

                    public void onReceive(final Object message) {
                        Object returnMessage = message;

                        // if message contains 'large' save it and forward a modified message
                        if (message.toString().contains("large")) {
                            returnMessage = "id=0&title=" + saveMessage(message);
                        }

                        System.out.println("Forwarding message:" + returnMessage);
                        jmsProducer.sendOneWay(returnMessage);
                    }
                };
            }
        });

        jmsConsumer.start();


    }


    /*
    save message to couchdb and return an id string
    */

    public String saveMessage(Object message) {
        Message msg = (Message) message;
        final String body = msg.getBodyAs(String.class);
        Object returnMessage = message;

        DateTime dt = new DateTime();
        String id = "myid:" + dt.toString();
        final PersistentMap<byte[], byte[]> persistentMap = CouchDBStorage.newMap(id);

        new Atomic() {
            public Object atomically() {
                try {
                    persistentMap.put("data".getBytes("UTF-8"), body.getBytes("UTF-8"));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

        return id;

    }


}

