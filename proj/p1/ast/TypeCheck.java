package ast;
import java.io.PrintStream;
import java.util.HashMap;

public class TypeCheck {
    // A symbol table to track declared variables, types, and values
    private HashMap<String, VariableInfo> symbolTable;

    // Constructor to initialize the symbol table
    public TypeCheck() {
        this.symbolTable = new HashMap<>();
    }

    // Method to declare a variable with its type and value
    public void declareVariable(String name, String type, Object value) {
        symbolTable.put(name, new VariableInfo(type, value));
    }

    // Decl
    // Method to check if a variable has been declared
    public boolean isDeclared(String name) {
        return symbolTable.containsKey(name);
    }



    // Method to retrieve the type of a variable
    public String getType(String name) {
        VariableInfo info = symbolTable.get(name);
        return info != null ? info.getType() : null;
    }

    // Method to retrieve the value of a variable
    public Object getValue(String name) {
        VariableInfo info = symbolTable.get(name);
        return info != null ? info.getValue() : null;
    }

    // Method to update the value of a variable
    public void updateValue(String name, Object value) {
        VariableInfo info = symbolTable.get(name);
        if (info != null) {
            info.setValue(value);
        } else {
            throw new RuntimeException("Variable " + name + " not declared.");
        }
    }

}
