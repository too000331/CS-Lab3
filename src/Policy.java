import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class Policy {
    private JSONObject json;
    List<CustomItem> items = new ArrayList<>();

    public Policy(JSONObject json) {
        setJson(json);
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
        JSONArray customItemsJson = json.getJSONObject("check_type").getJSONObject("group_policy").getJSONObject("if").getJSONObject("then").getJSONArray("custom_item");
        customItemsJson.forEach(customItem -> {
            CustomItem item = new CustomItem((JSONObject)customItem);
            addItem(item);
        });
    }

    public List<CustomItem> getItems() {
        return items;
    }

    public void setItems(List<CustomItem> items) {
        this.items = items;
    }

    private void addItem(CustomItem item) {
        getItems().add(item);
    }

}
