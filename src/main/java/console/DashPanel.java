package console;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import console.ExampleComponents.TranscodePopup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.artur.icepush.ICEPush;


public class DashPanel extends VerticalLayout {

    Logger log = LoggerFactory.getLogger(DashPanel.class);

    Table dashTable;

    ICEPush pusher;

    public DashPanel(ICEPush icepush) {
        pusher = icepush;


        Label subtitle = new Label("Dashboard");
        subtitle.setStyleName("h2");
        addComponent(subtitle);

        HorizontalLayout dash = new HorizontalLayout();
        addComponent(dash);
        dash.setSpacing(true);
        dash.setMargin(true);

        dashTable = new Table("Dashboard");
//        dashTable.setCacheRate(5);
        dash.addComponent(dashTable);

        dashTable.addContainerProperty("id", Integer.class, null);
        dashTable.addContainerProperty("title", String.class, null);
        dashTable.addContainerProperty("platform", String.class, null);
        dashTable.addContainerProperty("ingest", String.class, null);
        dashTable.addContainerProperty("transcode", String.class, null);
        dashTable.addContainerProperty("", VerticalLayout.class, null);
        dashTable.addContainerProperty("qc", String.class, null);
        dashTable.addContainerProperty("delta", String.class, null);
        dashTable.addContainerProperty("wmv", String.class, null);
        dashTable.addContainerProperty("published", String.class, null);
        dashTable.addContainerProperty("Due", String.class, null);
        dashTable.addContainerProperty("ETA", String.class, null);
        dashTable.setWidth("1000");
        dashTable.sort(new Object[]{"id"}, new boolean[]{true});

        //Add some initial transcode data
        for (int i = 0; i < 10; i++) {
            dashTable.addItem(new Object[]{
                    new Integer(i),
                    "Eastenders ep: " + i,
                    "ipad",
                    "ingested",
                    "transcoded",
                    new TranscodePopup(),
                    "qc-passed",
                    "on-delta",
                    "n/a",
                    "publishing",
                    "19:10:00",
                    "19:20:01",
            }, new Integer(i));
        }


        final Table queueTable = new Table("Queues");
        dash.addComponent(queueTable);

        queueTable.addContainerProperty("id", Integer.class, null);
        queueTable.addContainerProperty("Queue", String.class, null);
        queueTable.addContainerProperty("Depth", Integer.class, null);
        queueTable.addContainerProperty("Rate of change", String.class, null);

        queueTable.addItem(new Object[]{new Integer(0), "ingest", 100, "+1/min"}, new Integer(0));
        queueTable.addItem(new Object[]{new Integer(1), "transcode", 200, "+10/min"}, new Integer(1));
        queueTable.addItem(new Object[]{new Integer(2), "qc", 20, "+10/min"}, new Integer(2));
        queueTable.addItem(new Object[]{new Integer(3), "delta", 20, "+10/min"}, new Integer(3));

        queueTable.addItem(new Object[]{new Integer(4), "publish2mad", 420, "+40/min"}, new Integer(4));

        queueTable.addItem(new Object[]{new Integer(5), "publish2pips", 220, "-15/min"}, new Integer(5));

        queueTable.setCellStyleGenerator(new Table.CellStyleGenerator() {
            public String getStyle(Object itemId, Object propertyId) {

                int row = ((Integer) itemId).intValue();

                if (null != propertyId) {
                    String col = (String) propertyId;

                    if (col == "Depth") {
                        Integer val = (Integer) queueTable.getContainerProperty(itemId, propertyId).getValue();
                        if (val > 100) {
                            return "red-background";
                        }

                    }
                }

                return "";
            }
        });


    }
}
