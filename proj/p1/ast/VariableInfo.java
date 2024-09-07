package ast;

public class VariableInfo {
    private String type;  // The type of the variable (e.g., int, float)
    private Object value; // The value of the variable (e.g., 1 for int, 3.0 for float, null if uninitialized)

    public VariableInfo(String type, Object value) {
        this.type = type;
        this.value = value;
    }

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
