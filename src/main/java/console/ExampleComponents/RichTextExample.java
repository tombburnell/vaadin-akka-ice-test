package console.ExampleComponents;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: Jan 21, 2011
 * Time: 8:18:24 AM
 * To change this template use File | Settings | File Templates.
 */

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.RichTextArea;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class RichTextExample extends VerticalLayout implements ClickListener {

    private Button b;
    private Label richText;

    private final RichTextArea editor = new RichTextArea();

    public RichTextExample() {
        setSpacing(true);

        richText = new Label(
                "<h1>Rich text example</h1>"
                        + "<p>The <b>quick</b> brown fox jumps <sup>over</sup> the <b>lazy</b> dog.</p>"
                        + "<p>This text can be edited with the <i>Edit</i> -button</p>");
        richText.setContentMode(Label.CONTENT_XHTML);

        addComponent(richText);

        b = new Button("Edit");
        b.addListener(this);
        addComponent(b);

        editor.setWidth("100%");
    }

    public void buttonClick(ClickEvent event) {
        if (getComponentIterator().next() == richText) {
            editor.setValue(richText.getValue());
            processText(richText.getValue().toString());
            replaceComponent(richText, editor);
            b.setCaption("Apply");
        } else {
            richText.setValue(editor.getValue());
            replaceComponent(editor, richText);
            b.setCaption("Edit");
        }
    }

    public void processText(String value) {

    }

}
