package console.ExampleComponents;


import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

@SuppressWarnings("serial")
public class TextExample extends HorizontalLayout implements
        Property.ValueChangeListener {

    private static final String initialText = "id=";//"The quick brown fox jumps over the lazy dog.";

    private Label plainText;
    private final TextField editor;

    public TextExample() {
        setSpacing(true);
        setWidth("100%");

        editor = new TextField(null, initialText);
        editor.setRows(5); // this will make it an 'area', i.e multiline
        editor.setColumns(14);
        editor.addListener(this);
        editor.setImmediate(true);
        addComponent(editor);

        // the TextArea is immediate, and it's valueCahnge updates the Label,
        // so this button actually does nothing
        addComponent(new Button(">"));

        //plainText = new Label(initialText);
        //plainText.setContentMode(Label.CONTENT_XHTML);
        //addComponent(plainText);
        //setExpandRatio(plainText, 1);
    }

    /*
    * Catch the valuechange event of the textfield and update the value of the
    * label component
    */
    public void valueChange(ValueChangeEvent event) {
        String text = (String) editor.getValue();

        processText(text);
    }

    public void processText(String text) {
    }


};