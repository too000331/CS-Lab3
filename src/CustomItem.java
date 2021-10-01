import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

public class CustomItem {
    private boolean selected;
    private String status;
    private String systemValue;
    private JSONObject json;
    private String reference;
    private String valueType;
    private String solution;
    private String right_type;
    private String description;
    private String valueData;
    private String type;
    private String seeAlso;
    private String info;
    private String regItem;
    private String regOption;
    private String regKey;
    private String lockoutPolicy;

    private static List<String> ALL_KEYS = Arrays.asList("type", "description", "info", "solution", "see_also", "value_type", "value_data",
            "right_type", "reg_key", "reg_item", "reg_ignore_hku_users", "reg_option",
            "lockout_policy", "audit_policy_subcategory", "key_item", "password_policy",
            "wmi_namespace", "wmi_request", "wmi_attribute", "wmi_key", "reference");

    private static List<String> UNQUOTED_KEYS = Arrays.asList("type", "value_type", "right_type", "reg_option", "lockout_policy", "password_policy", "wmi_request");

    private static List<String> UNQUOTED_VALUE_TYPES = Arrays.asList("USER_RIGHT", "TIME_MINUTE");

    public CustomItem(JSONObject json) {
        setJson(json);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getStatus() {
        if(status != null) {
            return status;
        } else {
            return "";
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSystemValue() {
        if(systemValue != null) {
            return systemValue;
        } else {
            return "";
        }
    }

    public void setSystemValue(String systemValue) {
        this.systemValue = systemValue;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
        setSelected(true);
        if(json.has("reference")) {
            this.setReference(json.getString("reference"));
        }
        if(json.has("value_type")) {
            this.setValueType(json.getString("value_type"));;
        }
        if(json.has("solution")) {
            this.setSolution(json.getString("solution"));
        }
        if(json.has("right_type")) {
            this.setRight_type(json.getString("right_type"));
        }
        if(json.has("description")) {
            this.setDescription(json.getString("description"));
        }
        if(json.has("value_data")) {
            this.setValueData(json.getString("value_data"));
        }
        if(json.has("type")) {
            this.setType(json.getString("type"));
        }
        if(json.has("see_also")) {
            this.setSeeAlso(json.getString("see_also"));
        }
        if(json.has("info")) {
            this.setInfo(json.getString("info"));
        }
        if(json.has("reg_item")) {
            this.setRegItem(json.getString("reg_item"));
        }
        if(json.has("reg_option")) {
            this.setRegOption(json.getString("reg_option"));
        }
        if(json.has("reg_key")) {
            this.setRegKey(json.getString("reg_key"));
        }
        if(json.has("lockout_policy")) {
            this.setLockoutPolicy(json.getString("lockout_policy"));
        }
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getRight_type() {
        return right_type;
    }

    public void setRight_type(String right_type) {
        this.right_type = right_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getValueData() {
        return valueData;
    }

    public void setValueData(String valueData) {
        this.valueData = valueData;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(String seeAlso) {
        this.seeAlso = seeAlso;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getRegItem() {
        return regItem;
    }

    public void setRegItem(String regItem) {
        this.regItem = regItem;
    }

    public String getRegOption() {
        return regOption;
    }

    public void setRegOption(String regOption) {
        this.regOption = regOption;
    }

    public String getRegKey() {
        return regKey;
    }

    public void setRegKey(String regKey) {
        this.regKey = regKey;
    }

    public String getLockoutPolicy() {
        return lockoutPolicy;
    }

    public void setLockoutPolicy(String lockoutPolicy) {
        this.lockoutPolicy = lockoutPolicy;
    }

    public String toAuditFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("    <custom_item>\n");
        int maxKeyLength = getMaxKeyLength();
        for(String key : ALL_KEYS) {
            if(this.getJson().has(key)) {
                addKeyAndValue(key, maxKeyLength, sb);
            }
        }
        Iterator<String> iter = this.getJson().keys();
        while(iter.hasNext()) {
            String key = iter.next();
            if(!ALL_KEYS.contains(key)) {
                addKeyAndValue(key, maxKeyLength, sb);
            }
        }
        sb.append("    </custom_item>\n");
        return sb.toString();
    }

    private void addKeyAndValue(String key, int maxKeyLength, StringBuilder sb) {
        String paddedKey = padKey(key, maxKeyLength);
        String valueType = getValueType();
        String value = this.getJson().getString(key).replace("\\n", "\n");

        sb.append("      ").append(paddedKey).append(" : ");

        if(shouldAddQuotes(key, valueType, value)) {
            sb.append("\"");
        }

        if(key.equalsIgnoreCase("wmi_request")) {
            sb.append("'");
        }

        sb.append(value);

        if(shouldAddQuotes(key, valueType, value)) {
            sb.append("\"");
        }

        if(key.equalsIgnoreCase("wmi_request")) {
            sb.append("'");
        }

        sb.append("\n");
    }

    private boolean shouldAddQuotes(String key, String valueType, String value) {
        return (!"value_data".equalsIgnoreCase(key) && !UNQUOTED_KEYS.contains(key.toLowerCase()))
                || ("value_data".equalsIgnoreCase(key)
                && !(valueData.contains("[") && !valueData.contains(" "))
                && (!UNQUOTED_VALUE_TYPES.contains(valueType.toUpperCase())
                || (value.contains(" ") && !value.contains("\""))
                || ("USER_RIGHT".equalsIgnoreCase(valueType) && !value.contains("\""))));
    }

    private int getMaxKeyLength() {
        Optional<String> maxKey = this.getJson().keySet().stream().max((k1, k2) -> k1.length() - k2.length());
        return maxKey.orElse("").length();
    }

    private String padKey(String key, int keyLength) {
        int length = key.length();
        if(length < keyLength) {
            StringBuilder sb = new StringBuilder();
            sb.append(key);
            for(int i = length; i < keyLength; i++) {
                sb.append(" ");
            }
            return sb.toString();
        } else {
            return key;
        }
    }
}