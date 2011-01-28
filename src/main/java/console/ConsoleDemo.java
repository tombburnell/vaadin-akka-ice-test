package console;

import akka.camel.CamelContextManager;
import com.vaadin.Application;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spring.spi.ApplicationContextRegistry;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.vaadin.artur.icepush.ICEPush;

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


    VerticalLayout main;
    Layout currentView;

    TranscodePanel transcodePanel;
    NeufsPanel neufsPanel;


    @Override
    public void init() {

        startCamel();

        Window mainWindow = new Window("Console Demo");
        setMainWindow(mainWindow);
        VerticalLayout root = new VerticalLayout();
        root.setSpacing(true);
        root.setMargin(true);
        mainWindow.setContent(root);

        // Add the push component - so we can push stuff to it
        // MUST happen after setContent()
        mainWindow.addComponent(pusher);

        // Add the topmost component.
        Label title = new Label("WFE console ");
        title.setStyleName("h1");
        title.setWidth(400, Sizeable.UNITS_PIXELS);
        root.addComponent(title);

        // Create a sample menu bar
        final MenuBar menubar = new MenuBar();


        root.addComponent(menubar);
        menubar.addItem("Transcode", null, new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                changeView(getTranscodePanel());
            }
        });
        menubar.addItem("Neufs", null, new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                changeView(getNeufsPanel());
            }
        });

        transcodePanel = getTranscodePanel();
        currentView = transcodePanel;

        main = new VerticalLayout();
        main.addComponent(transcodePanel);

        root.addComponent(main);

    }


    public TranscodePanel getTranscodePanel() {
        if (null == transcodePanel) transcodePanel = new TranscodePanel(pusher);
        return transcodePanel;
    }

    public NeufsPanel getNeufsPanel() {
        if (null == neufsPanel) neufsPanel = new NeufsPanel(pusher);
        return neufsPanel;
    }

    public void changeView(Layout view) {
        main.replaceComponent(currentView, view);
        currentView = view;

    }


}
