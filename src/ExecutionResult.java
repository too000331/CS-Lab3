public class ExecutionResult {
    private String status;
    private String value;

    public ExecutionResult(ExecutionStatus status, String value) {
        if(status != null) {
            setStatus(status.name());
        } else {
            setStatus("");
        }

        setValue(value);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}