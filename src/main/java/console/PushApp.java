package console;

import akka.actor.*;
import akka.japi.Creator;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.*;
import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.Application;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

import akka.camel.Message;
import akka.camel.UntypedConsumerActor;
import static akka.camel.CamelServiceManager.*;

public class PushApp extends Application {



    private static String[] fields = {"First Name", "Last Name", "Company",
            "Mobile Phone", "Work Phone", "Home Phone", "Work Email",
            "Home Email", "Street", "Zip", "City", "State", "Country"};
    private static String[] visibleCols = new String[]{"Last Name",
            "First Name", "Company"};

    String[] fnames = {"Peter", "Alice", "Joshua", "Mike", "Olivia",
            "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik", "Rene",
            "Lisa", "Marge"};
    String[] lnames = {"Smith", "Gordon", "Simpson", "Brown", "Clavel",
            "Simons", "Verne", "Scott", "Allison", "Gates", "Rowling",
            "Barks", "Ross", "Schneider", "Tate"};

    static boolean startedCamel = false;

    static public void startCamel() {
        
        if (startedCamel == false) {

            startCamelService();
            startedCamel = true;
            System.out.println("Starting Camel service");
        } else {
            System.out.println("Camel service started already");
        }

    }

    IndexedContainer ic = new IndexedContainer();

    private ICEPush pusher = new ICEPush();

    @Override
    public void init() {
        Window mainWindow = new Window("Icepushaddon Application");// new SplitPanel());
        setMainWindow(mainWindow);

        startCamel();


        // Add the push component
        mainWindow.addComponent(pusher);

        // Add a button for starting background work
        getMainWindow().addComponent(
                new Button("Do stuff in the background", new ClickListener() {

                    public void buttonClick(ClickEvent event) {
                        getMainWindow()
                                .addComponent(
                                        new Label(
                                                "Waiting for background process to complete..."));

                        //new BackgroundThread().start();
                        //ActorRef files = Actors.actorOf(FileConsumer.class);
//                        UntypedConsumerActor fc = new FileConsumerActor() {
//                            public void doSomething() {
//                                Object id = ic.addItem();
//                                ic.getContainerProperty(id, "First Name").setValue(
//                                        fnames[(int) (fnames.length * Math.random())]);
//                                ic.getContainerProperty(id, "Last Name").setValue(
//                                        lnames[(int) (lnames.length * Math.random())]);
//
//                                pusher.push();
//                            }
//                        };
//                        ActorRef actor = Actors.actorOf(new UntypedActorFactory() {
//                            public UntypedActor create() {
//
//                                System.out.println("Creating FileConsumerActor");
//
//                                return new FileConsumerActor() {
//                                    public void doSomething() {
//                                        System.out.println("doSomething");
//                                        Object id = ic.addItem();
//                                        ic.getContainerProperty(id, "First Name").setValue(
//                                                fnames[(int) (fnames.length * Math.random())]);
//                                        ic.getContainerProperty(id, "Last Name").setValue(
//                                                lnames[(int) (lnames.length * Math.random())]);
//
//                                        pusher.push();
//                                    }
//                                };
//                            }
//                        });
//                        actor.start();

//                        ActorRef files = Actors.actorOf( FileConsumerActor.class);
//                        files.start();
                    }
                }));

        Table table = new Table();
        mainWindow.addComponent(table);


        for (String p : fields) {
            ic.addContainerProperty(p, String.class, "");
        }

        for (int i = 0; i < 1; i++) {
            Object id = ic.addItem();
            ic.getContainerProperty(id, "First Name").setValue(
                    fnames[(int) (fnames.length * Math.random())]);
            ic.getContainerProperty(id, "Last Name").setValue(
                    lnames[(int) (lnames.length * Math.random())]);
        }

        table.setContainerDataSource(ic);

//        ActorRef files = Actors.actorOf( FileConsumerActor.class);
//        files.start();

        ActorRef actor = Actors.actorOf(new UntypedActorFactory() {
            public UntypedActor create() {

                System.out.println("Creating FileConsumerActor");

                return new FileConsumerActor() {
                    public void doSomething() {
                        System.out.println("doSomething");
                        Object id = ic.addItem();
                        ic.getContainerProperty(id, "First Name").setValue(
                                fnames[(int) (fnames.length * Math.random())]);
                        ic.getContainerProperty(id, "Last Name").setValue(
                                lnames[(int) (lnames.length * Math.random())]);

                        pusher.push();
                    }
                };
            }
        });
        actor.start();



    }

    public class BackgroundThread extends Thread {

        @Override
        public void run() {
            // Simulate background work
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//
//            }
//

            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {

                }
                Object id = ic.addItem();
                ic.getContainerProperty(id, "First Name").setValue(
                        fnames[(int) (fnames.length * Math.random())]);
                ic.getContainerProperty(id, "Last Name").setValue(
                        lnames[(int) (lnames.length * Math.random())]);

                pusher.push();
            }

            // Update UI
            synchronized (PushApp.this) {
                getMainWindow().addComponent(new Label("All done"));
            }

            // Push the changes
            pusher.push();

        }

    }


//    public class FileConsumer extends UntypedConsumerActor {
//        public String getEndpointUri() {
//            return "file:data/input/actor";
//        }
//
//        public void onReceive(Object message) {
//            Message msg = (Message) message;
//            String body = msg.getBodyAs(String.class);
//            System.out.println(String.format("received %s", body));
//
//            Object id = ic.addItem();
//            ic.getContainerProperty(id, "First Name").setValue(
//                    fnames[(int) (fnames.length * Math.random())]);
//            ic.getContainerProperty(id, "Last Name").setValue(
//                    lnames[(int) (lnames.length * Math.random())]);
//
//            pusher.push();
//
//        }
//    }
}
