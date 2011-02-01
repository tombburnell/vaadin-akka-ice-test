package console;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.camel.CamelContextManager;
import akka.camel.Message;
import akka.camel.UntypedConsumerActor;
import akka.camel.UntypedProducerActor;
import akka.dispatch.Future;
import akka.japi.Procedure;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static akka.camel.CamelServiceManager.startCamelService;

public class TranscodePlugin {

    Logger log = LoggerFactory.getLogger(TranscodePlugin.class);

    ActorRef jmsProducer;

    public void start() {

        // Create CamelContext with Spring-based registry and custom route builder
        CamelContextManager.init(new DefaultCamelContext(
                new ApplicationContextRegistry(
                        new ClassPathXmlApplicationContext("../appContext.xml", TranscodePlugin.class)
                ))
        );
        startCamelService();


        jmsProducer = Actors.actorOf(new UntypedActorFactory() {
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
                        return "jms:topic:neufs";
                    }

                    public void onReceive(final Object message) throws InterruptedException {
                        Message msg = (Message) message;
                        final String neufsMessage = msg.getBodyAs(String.class);

                        log.info("Got message: " + neufsMessage);

                        String[] pair = neufsMessage.split(" ");
                        String action = pair[1];
                        String[] file_id = pair[0].split("_");
                        String id = file_id[1];

                        log.info("action='" + action + "' id=" + id);

                        if (action.equals("created")) {

                            startTranscode(id);
                            log.info("Started Transcode of " + id);

                        } else if (action.equals("deleted")) {
                            jmsProducer.sendOneWay("housekeep=" + id);

                        } else {
                            log.info("action '" + action + "' not recognised");
                        }
                    }
                };
            }
        });

        jmsConsumer.start();

    }

    public void startTranscode(Object id) {
        final ActorRef transcodeActor = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {

                return new UntypedActor() {
                    public void onReceive(Object id) throws InterruptedException {

                        int i;
                        String status = "inprogress";
                        for (i = 0; i <= 100; i += 10) {
                            if (i == 100) status = "completed";

                            String sendMessage = "id=" + id + "&status=" + status + "&percent=" + i;
                            log.info("Sending message " + sendMessage);

                            jmsProducer.sendOneWay(sendMessage);

                            Thread.sleep(400);

                        }
                        getContext().replyUnsafe(" finished! " + getContext().getUuid());

                    }

                    public void postStop() {
                        log.info("I've been told to stop..");
                    }
                };
            }
        });
        transcodeActor.start();

        Future f = transcodeActor.sendRequestReplyFuture(id);
        Procedure<Future> procedure = new Procedure<Future>() {

            public void apply(Future future) {
                log.info("got reply - stopping actor");
                transcodeActor.stop();
            }
        };

        f.onComplete(procedure);

    }


}

