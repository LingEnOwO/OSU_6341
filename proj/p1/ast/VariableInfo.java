package ast;

public class VariableInfo {
    public enum VarType{
        INT, FLOAT
    }
    private VarType type;
    private  Integer intVal;
    private Double floatVal;  

    public VariableInfo(VarType type, Integer intVal) {
        this.type = type;
        this.intVal = intVal;
    }

    public VariableInfo(VarType type, Double floatVal) {
        this.type = type;
        this.floatVal = floatVal;
    }

    public VarType getType() {
        return this.type;
    }


    public Integer getIntValue() {
        return this.intVal;
    }

    public Double getFloatValue() {
        return this.floatVal;
    }
}
