package console;

import org.vaadin.artur.icepush.ICEPush;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class PushApp extends Application {

    private ICEPush pusher = new ICEPush();

    @Override
    public void init() {
        Window mainWindow = new Window("Icepushaddon Application");
        setMainWindow(mainWindow);

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
                        new BackgroundThread().start();
                    }
                }));

    }

    public class BackgroundThread extends Thread {

        @Override
        public void run() {
            // Simulate background work
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }

            // Update UI
            synchronized (PushApp.this) {
                getMainWindow().addComponent(new Label("All done"));
            }

            // Push the changes
            pusher.push();
        }

    }
}
