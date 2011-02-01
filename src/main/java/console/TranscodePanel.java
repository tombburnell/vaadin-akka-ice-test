package console;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.camel.UntypedProducerActor;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import console.ExampleComponents.RichTextExample;
import console.ExampleComponents.TextExample;
import console.ExampleComponents.TranscodeTableStyle;
import console.actor.MyConsumerActor;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.artur.icepush.ICEPush;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class TranscodePanel extends VerticalLayout {

    Logger log = LoggerFactory.getLogger(TranscodePanel.class);

    // For displaying our tasks
    Table taskTable = new Table("Task Table");

    ICEPush pusher;


    public TranscodePanel(ICEPush icepush) {
        //ICEPush icepush) {

        pusher = icepush;

        // Add a button for starting some example background work
        this.addComponent(new Button("Do stuff in the background", new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent
                    event) {
                TranscodePanel.this.addComponent(new Label("Waiting for background process to complete..."));
                // trigger thread when we click it
                new BackgroundThread().start();
            }
        }));

        HorizontalLayout transcodeTables = new HorizontalLayout();
        transcodeTables.setSpacing(true);
        transcodeTables.setMargin(true);
        this.addComponent(transcodeTables);

        taskTable.setWidth(400, Sizeable.UNITS_PIXELS);
        taskTable.setSizeFull();
        // increase from default of 2 to smooth out scrolling
        // taskTable.setCacheRate(5);
        transcodeTables.addComponent(taskTable);

        // Create table headings
        taskTable.addContainerProperty("id", Integer.class, null);
        taskTable.addContainerProperty("vpid", String.class, null);
        taskTable.addContainerProperty("bpid", String.class, null);
        taskTable.addContainerProperty("title", String.class, null);
        taskTable.addContainerProperty("status", String.class, null);
        taskTable.addContainerProperty("jobid", String.class, null);
        taskTable.addContainerProperty("percent", Integer.class, null);

        //Add some initial transcode data
        for (int i = 0; i < 1; i++) {
            taskTable.addItem(new Object[]{
                    //           new Integer(i), "v00" + i, "b00" + i, "Eastenders ep: " + i, "InProgress", "123", "0"}, new Integer(i)
                    new Integer(i), "-", "-", "untitled", "pending", "-", 0}, new Integer(i)
            );
        }
        taskTable.setCellStyleGenerator(new TranscodeTableStyle() {
            public Table getTable() {
                return taskTable;
            }
        });

        Table targetTable = new Table("Target Table");
        transcodeTables.addComponent(targetTable);
        String[] targetFields = {"id", "wfe_profile", "title"};
        for (String p : targetFields) {
            targetTable.addContainerProperty(p, String.class, null);
        }
        for (int i = 0; i < 10; i++) {
            log.info("Adding target item " + i);
            targetTable.addItem(new Object[]{i, "flv_avc1_med", "Eastenders"}, new Integer(i));
        }

        //
        // Done with PageLayout
        //


        // Lets create an Actor to Monitor for updates via REST

        ActorRef restActor = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {

                return new MyConsumerActor() {

                    public String getEndpointUri() {
                        return "jetty:http://localhost:8012/camel/default";
                    }

                    public void processParams(Map<String, List<String>> params) {
                        updateTable(params);
                    }
                };
            }
        });

        restActor.start();

        // Lets create an Actor to Monitor for updates via REST

        ActorRef fileActor = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {

                return new MyConsumerActor() {

                    public String getEndpointUri() {
                        return "file:/tmp/input";
                    }

                    public void processParams(Map<String, List<String>> params) {
                        updateTable(params);
                    }
                };
            }
        });

        fileActor.start();

        // Create an actor to listen to monitor for updates via JMS topic

        ActorRef jmsActor = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {

                return new MyConsumerActor() {

                    public String getEndpointUri() {
                        return "jms:topic:forge";
                    }

                    public void processParams(Map<String, List<String>> params) {
                        updateTable(params);
                    }
                };
            }
        });

        jmsActor.start();


        // actor to fire off messages to queue
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


        // add a text box and forward any messages to forge queue
        TextExample textArea = new TextExample() {
            public void processText(String text) {
                jmsProducer.sendOneWay(text);
            }
        };
        transcodeTables.addComponent(textArea);

        // add a richtextbox
        RichTextExample richText = new RichTextExample();
        this.addComponent(richText);

    }


    /*
    update table based on message params

    */

    DateTime lastTime = new DateTime();
    Integer interval = 0;

    public void updateTable(Map<String, List<String>> params) {

        List<String> housekeep = params.get("housekeep");
        if (housekeep != null && housekeep.size() > 0) {
            log.info("housekeep = " + housekeep);
            for (String taskId : housekeep) {
                Integer tId = Integer.parseInt(taskId);
                log.info("Removing housekept id:" + tId);

                synchronized (taskTable) {
                    taskTable.removeItem(tId);
                }

                pusher.push();
            }
            return;

        }

        List<String> taskIds = params.get("id");

        if (taskIds == null) {
            log.info("No taskIds provideds");
            return;
        }

        for (String taskId : taskIds) {
            Integer tId = Integer.parseInt(taskId);

            Item row = taskTable.getItem(tId);

            if (null == row) {
                int i = tId;
                taskTable.addItem(new Object[]{
                        new Integer(i), "-", "-", "untitled", "pending", "-", 0}, new Integer(i)
                );
                row = taskTable.getItem(tId);
            }

            for (String key : params.keySet()) {
                if (key.equals("id")) {
                    continue;
                }

                String value = params.get(key).get(0);
                log.info("Setting " + key + " to " + value);

                Property p = taskTable.getContainerProperty(tId, key);
                if (p != null) {
                    p.setValue(value);
                } else {
                    log.info("No field called " + key);
                }
            }
        }

        DateTime now = new DateTime();
        if (now.getSecondOfDay() >= lastTime.getSecondOfDay() + interval) {
            // push the updates to the view
            pusher.push();
            lastTime = now;

        }


    }


    // simulate updates to percent complete column

    public class BackgroundThread extends Thread {

        @Override
        public void run() {

            for (int i = 0; i < 25; i++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {

                }
                for (Iterator ii = taskTable.getItemIds().iterator(); ii.hasNext();) {

                    // Get the current item identifier, which is an integer.
                    int iid = (Integer) ii.next();
                    Property p = taskTable.getContainerProperty(iid, "percent");

                    // increase percent value
                    p.setValue((Integer) p.getValue() + 2);

                    // increate the Status if complete
                    if ((Integer) p.getValue() == 100) {
                        taskTable.getContainerProperty(iid, "status").setValue("Completed");
                    }

                    // push changes as we go
                    pusher.push();
                }
            }

            // Tell them we are done
            synchronized (this) {
                TranscodePanel.this.addComponent(new Label("All done"));
            }

            // Push the changes
            pusher.push();

        }

    }
}
