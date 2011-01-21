package console;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.camel.CamelContextManager;
import akka.camel.UntypedConsumerActor;
import akka.camel.UntypedProducerActor;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static akka.camel.CamelServiceManager.startCamelService;

public class GateWay {


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

                    public void onReceive(Object message) {
                        System.out.println("Forwarding message:" + message);
                        jmsProducer.sendOneWay(message);
                    }
                };
            }
        });

        jmsConsumer.start();


    }

}

