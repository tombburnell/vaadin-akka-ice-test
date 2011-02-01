package console.ExampleComponents;

import com.vaadin.ui.Table;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: Jan 31, 2011
 * Time: 11:55:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class TranscodeTableStyle implements Table.CellStyleGenerator {

    public Table getTable() {
        throw new NotImplementedException();
    }

    public String getStyle(Object itemId, Object propertyId) {

        int row = ((Integer) itemId).intValue();

        if (null != propertyId) {
            String col = (String) propertyId;

            if (col == "percent") {
                Integer val = (Integer) getTable().getContainerProperty(itemId, propertyId).getValue();
                if (val == 100) {

                    return "green-background";
                }

            }
        }

        return "";
    }
}
