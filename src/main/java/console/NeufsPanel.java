package console;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.camel.Message;
import akka.camel.UntypedConsumerActor;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.artur.icepush.ICEPush;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: Jan 26, 2011
 * Time: 10:59:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class NeufsPanel extends VerticalLayout {

    Logger log = LoggerFactory.getLogger(NeufsPanel.class);

    Table fileChangeTable;

    ICEPush pusher;

    public NeufsPanel(ICEPush icepush) {
        pusher = icepush;

        Label subtitle = new Label("Neufs Plugin");
        subtitle.setStyleName("H2");
        addComponent(subtitle);

        fileChangeTable = new Table("Target Table");
        fileChangeTable.setCacheRate(5);
        addComponent(fileChangeTable);

        fileChangeTable.addContainerProperty("id", Integer.class, null);
        fileChangeTable.addContainerProperty("change", String.class, null);

        fileChangeTable.setWidth("300");

        fileChangeTable.sort(new Object[]{"id"}, new boolean[]{false});

//        for (int i = 0; i < 10; i++) {
//            String f = "init-"+i;
//            System.out.println("Adding filename item " + f);
//            fileChangeTable.addItem(new Object[]{ f }, new Integer(i));
//        }


        // Create an actor to listen to monitor for updates via JMS topic

        ActorRef jmsActor = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {

                return new UntypedConsumerActor() {

                    public String getEndpointUri() {
                        return "jms:topic:neufs";
//                        return "file:/tmp/neufs";
                    }

                    public void onReceive(Object message) {
                        Message msg = (Message) message;
                        log.info("Got message about file: " + msg);
                        final String body = msg.getBodyAs(String.class);
                        //                       String filename = (String) msg.getHeader("CamelFileName");
                        String filename = body;

                        Integer y;
                        if (null != fileChangeTable) {
                            y = fileChangeTable.size();
                        } else {
                            y = 0;
                        }

                        log.info("Got message about file: " + filename + "adding as id" + y);

//                        Integer lastId = (Integer) fileChangeTable.lastItemId();
//                        if (null == lastId) lastId= 0;
                        synchronized (getApplication()) {
                            System.out.println("table=" + fileChangeTable + " y=" + y + "file = " + filename);
                            fileChangeTable.addItem(new Object[]{y, filename}, new Integer(y));
                            fileChangeTable.sort();

                        }

                        log.info("Now there are " + fileChangeTable.size() + " rows");
                        pusher.push();
                    }

                };
            }
        });

        jmsActor.start();

    }
}
