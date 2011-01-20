package console;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: tom
 * Date: Jan 20, 2011
 * Time: 7:41:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryStringMapper {

    public QueryStringMapper() {

    }

    //convert provided query string to a map

    Map<String, List<String>> convertUrlToMap(String query) throws UnsupportedEncodingException {

        Map<String, List<String>> params = new HashMap<String, List<String>>();

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            String key = URLDecoder.decode(pair[0], "UTF-8");
            String value = URLDecoder.decode(pair[1], "UTF-8");
            List<String> values = params.get(key);
            if (values == null) {
                values = new ArrayList<String>();
                params.put(key, values);
            }
            values.add(value);
        }

        return params;
    }
}
