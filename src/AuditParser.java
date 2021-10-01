import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

public class AuditParser {

    private static final String INDENT = "    ";
    private boolean insideValue;
    private boolean insideArray;
    private char quot;


    public JSONObject parseFile(String fileName) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(fileName));
        removeComments(lines);

        StringBuilder sb = new StringBuilder();
        String indent = "";
        insideValue = false;
        insideArray = false;
        for(String line : lines) {
            if(!insideValue) {
                String trLine = line.trim();
                if(trLine.startsWith("<") && trLine.endsWith(">")) {
                    indent = transformTag(trLine, sb, indent);
                } else if(!trLine.isEmpty()) {
                    transformProperties(trLine, sb, indent);
                }
            } else {
                appendRestOfValue(line, sb);
            }
        }

        //Files.write(Paths.get(fileName + ".json"), sb.toString().getBytes());

        JSONObject obj = new JSONObject("{" + sb.toString() + "}");

        return obj;
    }

    private void removeComments(List<String> lines) {
        Iterator<String> iter = lines.iterator();
        while (iter.hasNext()) {
            String current = iter.next().trim();
            if(current.isEmpty() || current.startsWith("#")) {
                iter.remove();
            } else {
                break;
            }
        }
    }

    private void transformProperties(String line, StringBuilder sb, String indent) {
        List<PropertyValue> properties = parseProperties(line);
        for(PropertyValue prop : properties) {
            if(sb.length() > 0 &&  sb.charAt(sb.length() - 1) != '{') {
                sb.append(",");
            }
            sb.append("\n");
            sb.append(indent).append(prop.name).append(":");
            sb.append("\"").append(prop.value);
            if(!insideValue) {
                sb.append("\"");
            }
        }
    }

    private void appendRestOfValue(String line, StringBuilder sb) {
        ParseResult pr = new ParseResult();
        parseValue(quot + line, pr);
        sb.append("\\\\n").append(sanitiseValue(pr.val));
        if(!insideValue) {
            sb.append("\"");
        }
    }

    public static String sanitiseValue(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String transformTag(String line, StringBuilder sb, String indent) {
        String tagContent = line.substring(1, line.length() - 1).trim();
        if(tagContent.startsWith("/")) {
            return transformEndTag(tagContent, sb, indent);
        } else {
            return transformStartTag(line, sb, indent);
        }
    }

    static class ParseResult {
        public int pos = 0;
        public String val = "";
    }

    static class PropertyValue {
        public PropertyValue(String name, String value) {
            this.name = name;
            this.value = sanitiseValue(value);
        }
        public String name;
        public String value;
    }

    private void parseName(String tagContent, ParseResult pr) {
        tagContent = tagContent.trim();
        char[] chars = tagContent.toCharArray();
        for(int i=0; i< tagContent.length(); i++) {
            char cc = chars[i];
            if(!Character.isJavaIdentifierPart(cc)) {
                pr.val = tagContent.substring(0, i);
                pr.pos = i;
                return;
            }
        }
        pr.val = tagContent;
        pr.pos = tagContent.length();

    }

    private void parseValue(String tagContent, ParseResult pr) {
        char[] chars = tagContent.toCharArray();
        quot = 0;
        boolean hasQuotes = false;
        insideValue = false;
        int i = 0;
        if(chars.length > 0) {
            if(chars[0] == '\'' || chars[0] == '"') {
                quot = chars[0];
                insideValue = true;
                hasQuotes = true;
                i++;
            }
        }
        for(;i< tagContent.length(); i++) {
            char cc = chars[i];
            if(hasQuotes) {
                if(cc == quot) {
                    pr.val = tagContent.substring(1, i);
                    pr.pos = i + 1;
                    insideValue = false;
                    return;
                }
            } else {
                if(Character.isWhitespace(cc)) {
                    pr.val = tagContent.substring(0, i);
                    pr.pos = i;
                    return;
                }
            }
        }
        if(hasQuotes) {
            pr.val = tagContent.substring(1);
        } else {
            pr.val = tagContent;
        }
        pr.pos = tagContent.length();
    }

    private List<PropertyValue> parseProperties(String content) {
        List<PropertyValue> ret = new ArrayList<>();
        ParseResult pr = new ParseResult();
        String name = "";
        String value = "";
        while(!content.trim().isEmpty()) {
            char[] chars = content.toCharArray();
            int i = 0;
            for(; i< content.length(); i++) {
                char cc = chars[i];
                if(!Character.isWhitespace(cc)) {
                    break;
                }
            }
            char cc = chars[i];
            content = content.substring(i);
            if(chars.length > i + 1 && ( (cc == '&') && (chars[i+1] == '&') || (cc == '|') && (chars[i+1] == '|'))) {
                String op = content.substring(0, 2);
                content = content.substring(2).trim();
                parseValue(content, pr);
                if(!value.endsWith("\"")) {
                    value = "\"" + value + "\"";
                }
                value = value + " " + op + " \"" + pr.val + "\"";
            } else if(cc != ':') {
                if(!name.isEmpty()) {
                    ret.add(new PropertyValue(name, value));
                }
                parseName(content, pr);
                name = pr.val;
                value = "";
            } else {
                content = content.substring(1).trim();
                parseValue(content, pr);
                value = pr.val;
            }
            if(content.length() > pr.pos + 1) {
                content = content.substring(pr.pos);
            } else {
                break;
            }
        }
        if(!name.isEmpty()) {
            ret.add(new PropertyValue(name, value));
        }

        return ret;
    }

    private String transformStartTag(String line, StringBuilder sb, String indent) {
        String tagContent = line.substring(1, line.length() - 1).trim();

        List<PropertyValue> parts = parseProperties(tagContent);
        if(parts.isEmpty()) {
            return indent;
        }

        PropertyValue part = parts.get(0);
        if(sb.length() > 0 &&  sb.charAt(sb.length() - 1) != '{') {
            sb.append(",");
        }
        sb.append("\n");

        if(part.name.equalsIgnoreCase("custom_item")) {
            if(!insideArray) {
                sb.append(indent).append("custom_item : [\n");
                indent += INDENT;
            }
            sb.append(indent).append("{");
            insideArray = true;
        } else {
            sb.append(indent).append(part.name).append(": {");
        }
        indent += INDENT;
        boolean firstItem = true;
        if(!part.value.isEmpty()) {
            firstItem = false;
            sb.append("\n").append(indent).append(part.name).append(":\"").append(part.value).append("\"");
        }
        for(int i=1; i < parts.size(); i++) {
            part = parts.get(i);
            if(!firstItem) {
                sb.append(",");
            }
            sb.append("\n");
            sb.append(indent).append(part.name).append(":");
            sb.append("\"").append(part.value).append("\"");
        }
        return indent;
    }

    private String transformEndTag(String line, StringBuilder sb, String indent) {
        String tagName = line.substring(1);
        if(insideArray && !tagName.equalsIgnoreCase("custom_item")) {
            insideArray = false;
            indent = indent.substring(INDENT.length());
            sb.append("\n").append(indent).append("]");
        }
        indent = indent.substring(INDENT.length());
        sb.append("\n").append(indent).append("}");
        return indent;
    }
}