package ast;

public class VariableInfo {
    public enum VarType{
        INT, FLOAT
    }
    private String ident;
    private VarType type;
    private Long intVal;
    private Double floatVal;  

    public VariableInfo(String ident,VarType type) {
        this.ident = ident;
        this.type = type;
    }

    public VariableInfo(String ident,VarType type, Long intVal) {
        this.ident = ident;
        this.type = type;
        this.intVal = intVal;
    }

    public VariableInfo(String ident,VarType type, Double floatVal) {
        this.ident = ident;
        this.type = type;
        this.floatVal = floatVal;
    }

    public String getIdent(){
        return ident;
    }

    public VarType getType() {
        return type;
    }

    public Long getIntValue() {
        return intVal;
    }

    public Double getFloatValue() {
        return floatVal;
    }

    public VariableInfo copyWithIdentInt(String ident) {
        return new VariableInfo(ident, this.type, this.intVal);
    }

    public VariableInfo copyWithIdentFloat(String ident) {
        return new VariableInfo(ident, this.type, this.floatVal);
    }

    public static VariableInfo createInt(String ident, Long intValue) {
        VariableInfo value = new VariableInfo(ident, VarType.INT, intValue);
        return value;
    }

    public static VariableInfo createFloat(String ident, Double floatValue) {
        VariableInfo value = new VariableInfo(ident, VarType.FLOAT, floatValue);
        return value;
    }
}

