package console;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.camel.CamelContextManager;
import akka.camel.UntypedProducerActor;
import akka.persistence.common.PersistentVector;
import akka.persistence.couchdb.CouchDBStorage;
import name.pachler.nio.file.*;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.List;

import static akka.camel.CamelServiceManager.startCamelService;

public class Neufs {


    PersistentVector<byte[]> messageLog;

    Logger log = LoggerFactory.getLogger(Neufs.class);


    public Neufs() {
        messageLog = CouchDBStorage.newVector("messageLog");
    }

    public void start() {

        // Create CamelContext with Spring-based registry and custom route builder
        CamelContextManager.init(new DefaultCamelContext(
                new ApplicationContextRegistry(
                        new ClassPathXmlApplicationContext("../appContext.xml", Neufs.class)
                ))
        );
        startCamelService();


        final ActorRef jmsProducer = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {

                return new UntypedProducerActor() {
                    public String getEndpointUri() {
                        return "jms:topic:neufs";
                    }

                    @Override
                    public boolean isOneway() {
                        return true;
                    }

                };
            }
        });


        jmsProducer.start();


        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path watchedPath = Paths.get("/Users/tom/neufs");

        WatchKey key = null;
        try {
            key = watchedPath.register(watchService, StandardWatchEventKind.ENTRY_CREATE, StandardWatchEventKind.ENTRY_DELETE);
            System.err.println("file watching ok !");
        } catch (UnsupportedOperationException uox) {
            System.err.println("file watching not supported!");
            // handle this error here
        } catch (IOException iox) {
            System.err.println("I/O errors");
            // handle this error here
        }

        for (; ;) {

            // take() will block until a file has been created/deleted
            WatchKey signalledKey;
            try {
                signalledKey = watchService.take();
            } catch (InterruptedException ix) {
                // we'll ignore being interrupted
                continue;
            } catch (ClosedWatchServiceException cwse) {
                // other thread closed watch service
                System.out.println("watch service closed, terminating.");
                break;
            }

            // get list of events from key
            List<WatchEvent<?>> list = signalledKey.pollEvents();

            // VERY IMPORTANT! call reset() AFTER pollEvents() to allow the
            // key to be reported again by the watch service
            signalledKey.reset();

            // we'll simply print what has happened; real applications
            // will do something more sensible here
            for (WatchEvent e : list) {
                String message = "";
                if (e.kind() == StandardWatchEventKind.ENTRY_CREATE) {
                    Path context = (Path) e.context();
                    message = context.toString() + " created";
                } else if (e.kind() == StandardWatchEventKind.ENTRY_DELETE) {
                    Path context = (Path) e.context();
                    message = context.toString() + " deleted";
                } else if (e.kind() == StandardWatchEventKind.OVERFLOW) {
                    message = "OVERFLOW: more changes happened than we could retreive";
                }

                jmsProducer.sendOneWay(message);

                System.out.println(message);
            }
        }

    }


}

