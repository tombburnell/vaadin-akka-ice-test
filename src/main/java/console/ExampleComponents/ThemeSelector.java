package console.ExampleComponents;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")

public class ThemeSelector extends VerticalLayout implements
        Property.ValueChangeListener {

    private static final String[] themes = new String[]{"reindeer", "mytheme"};

    public ThemeSelector() {
        setSpacing(true);

        NativeSelect l = new NativeSelect("Change themes");
        for (int i = 0; i < themes.length; i++) {
            l.addItem(themes[i]);
        }

        l.setNullSelectionAllowed(false);
        l.setValue("reindeer");
        l.setImmediate(true);
        l.addListener(this);

        addComponent(l);
    }

    /*
     * Shows a notification when a selection is made.
     */
    public void valueChange(ValueChangeEvent event) {
        getWindow().showNotification("Change theme to : " + event.getProperty());
        getApplication().setTheme(event.getProperty().toString());

    }
}