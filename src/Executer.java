import java.util.Objects;

public class Executer {
    public static ExecutionResult check(CustomItem item) {
        switch(item.getType().toUpperCase()) {
            case "REGISTRY_SETTING":
                return checkRegistryValue(item);
            case "USER_RIGHTS_POLICY":
            case "LOCKOUT_POLICY":
            case "REG_CHECK":
            default:
                return new ExecutionResult(ExecutionStatus.UNKNOWN, null);
        }
    }

    private static ExecutionResult checkRegistryValue(CustomItem item) {
        try {
            //WindowsRegistry.overwriteValue("HKLM\\Software\\DVA", "Data", "REG_DWORD",  "77");
            String value = WindowsRegistry.getValue(item.getRegKey(), item.getRegItem(), polycyValueTypeToRegType(item.getValueType()));
            ExecutionStatus status;
            if(Objects.equals(item.getValueData(), value)) {
                status = ExecutionStatus.PASSED;
            } else {
                status = ExecutionStatus.FAILED;
            }
            return new ExecutionResult(status, value);
        } catch (Exception e) {
            e.printStackTrace();
            return new ExecutionResult(ExecutionStatus.ERROR, e.getMessage());
        }
    }

    private static String polycyValueTypeToRegType(String dataType) {
        switch(dataType) {
            case "POLICY_DWORD":
                return "REG_DWORD";
            case "POLICY_TEXT":
                return "REG_SZ";
            default:
                return "REG_SZ";
        }
    }

}
