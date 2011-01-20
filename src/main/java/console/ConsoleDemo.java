package console;

import akka.actor.ActorRef;
import akka.actor.Actors;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import akka.camel.CamelContextManager;
import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import console.actor.MyConsumerActor;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.vaadin.artur.icepush.ICEPush;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static akka.camel.CamelServiceManager.startCamelService;

public class ConsoleDemo extends Application {

    // For handling long-polling/push to browser
    private ICEPush pusher = new ICEPush();

    // Track if we have already started Camel service
    static boolean startedCamel = false;

    // start the camel service
    static public void startCamel() {

        if (startedCamel == false) {

            // Create CamelContext with Spring-based registry and custom route builder
            CamelContextManager.init(new DefaultCamelContext(
                    new ApplicationContextRegistry(
                            new ClassPathXmlApplicationContext("/appContext.xml", ConsoleDemo.class)
                    ))
            );

            startCamelService();
            startedCamel = true;
            System.out.println("Starting Camel service");
        } else {
            System.out.println("Camel service started already");
        }
    }


    // For displaying our tasks
    Table taskTable = new Table("Task Table");


    @Override
    public void init() {


        startCamel();


        Window mainWindow = new Window("Icepushaddon Application");
        setMainWindow(mainWindow);


        VerticalLayout root = new VerticalLayout();

        //turn on spacing and margin (configure in css)
        root.setSpacing(true);
        root.setMargin(true);

        mainWindow.setContent(root);

        // Add the push component - so we can push stuff to it
        // MUST happen after setContent()
        mainWindow.addComponent(pusher);

        // Add the topmost component.
        Label title = new Label("WFE console");
        title.setWidth(400, Sizeable.UNITS_PIXELS);
        root.addComponent(title);


        // Create a sample menu bar
        final MenuBar menubar = new MenuBar();
        root.addComponent(menubar);
        menubar.addItem("Ingest", null, null);
        menubar.addItem("Transcode", null, null);
        menubar.addItem("QC", null, null);
        menubar.addItem("Delta", null, null);
        menubar.addItem("Subtitles", null, null);
        menubar.addItem("Publication", null, null);


        // Add a button for starting some example background work
        root.addComponent(new Button("Do stuff in the background", new ClickListener() {
            public void buttonClick(ClickEvent event) {
                getMainWindow().addComponent(new Label("Waiting for background process to complete..."));

                // trigger thread when we click it
                new BackgroundThread().start();
            }
        }));


        HorizontalLayout transcodeTables = new HorizontalLayout();
        transcodeTables.setSpacing(true);
        transcodeTables.setMargin(true);
        root.addComponent(transcodeTables);

        taskTable.setWidth(400, Sizeable.UNITS_PIXELS);
        taskTable.setSizeFull();

        // increase from default of 2 to smooth out scrolling
//        taskTable.setCacheRate(5);
        transcodeTables.addComponent(taskTable);

        // Create table headings
        taskTable.addContainerProperty("id", Integer.class, null);
        taskTable.addContainerProperty("vpid", String.class, null);
        taskTable.addContainerProperty("bpid", String.class, null);
        taskTable.addContainerProperty("title", String.class, null);
        taskTable.addContainerProperty("status", String.class, null);
        taskTable.addContainerProperty("jobid", Integer.class, null);
        taskTable.addContainerProperty("percent", Integer.class, null);

        //Add some initial rows
        for (int i = 0; i < 10; i++) {
            taskTable.addItem(new Object[]{new Integer(i), "v00" + i, "b00" + i, "Neighbours ep: " + i, "InProgress", "123", "50"}, new Integer(i));
        }


        Table targetTable = new Table("Target Table");
        transcodeTables.addComponent(targetTable);

        String[] targetFields = {"id", "wfe_profile", "title"};
        for (String p : targetFields) {
            targetTable.addContainerProperty(p, String.class, null);
        }
        for (int i = 0; i < 10; i++) {
            System.out.println("Adding target item " + i);
            targetTable.addItem(new Object[]{i, "flv_avc1", "Neighbours"}, new Integer(i));
        }


        // Done with PageLayout


        // Lets create an Actor to Monitor for updates via REST

        ActorRef restActor = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {

                return new MyConsumerActor() {

                    public void doSomething(Map<String, List<String>> params) {
                        updateTable(params);
                    }
                };
            }
        });

        restActor.start();


        // Create an actor to listen to monitor for updates via JMS topic

        ActorRef jmsActor = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {

                return new MyConsumerActor() {

                    public String getEndpointUri() {
                        return "jms:topic:yo";
                    }

                    public void doSomething(Map<String, List<String>> params) {
                        updateTable(params);
                    }
                };
            }
        });

        jmsActor.start();


    }


    /*
    update table based on message params

    */

    public void updateTable(Map<String, List<String>> params) {
        List<String> taskIds = params.get("id");

        if (taskIds == null) {
            System.out.println("No taskIds provideds");
            return;
        }

        for (String taskId : taskIds) {
            Integer tId = Integer.parseInt(taskId);

            Item row = taskTable.getItem(tId);

            for (String key : params.keySet()) {
                if (key.equals("id")) {
                    continue;
                }

                String value = params.get(key).get(0);
                System.out.println("Setting " + key + " to " + value);

                Property p = taskTable.getContainerProperty(tId, key);
                if (p != null) {
                    p.setValue(value);
                } else {
                    System.out.println("No field called " + key);
                }
            }
        }

        // push the updates to the view
        pusher.push();

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
            synchronized (ConsoleDemo.this) {
                getMainWindow().addComponent(new Label("All done"));
            }

            // Push the changes
            pusher.push();

        }

    }


}
