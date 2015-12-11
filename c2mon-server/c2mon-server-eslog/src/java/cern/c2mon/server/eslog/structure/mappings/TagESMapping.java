package cern.c2mon.server.eslog.structure.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Allows to create dynamic mappings for the different types that exist in ElasticSearch.
 * Look at the Mapping Interface for more details.
 * @author Alban Marguet.
 */
@Slf4j
public class TagESMapping implements Mapping {
    Routing _routing;
    Properties properties;

    public TagESMapping() {
        _routing = new Routing();
    }

    @Override
    public String getMapping() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);
        log.info(json);
        return json;
    }

    @Override
    public void setProperties(String tagValueType) {
        properties = new Properties(tagValueType);
    }
}
