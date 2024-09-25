package ast;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class SymbolTable {
    private SymbolTable parent;
    private HashMap<String, String> symbols; 
    //private List<SymbolTable> children;

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
        this.symbols = new HashMap<>();
        /*this.children = new ArrayList<>();
        if (parent != null) {
            parent.addChild(this); 
        }*/
    }

    public void put(String name, String type) {
        symbols.put(name, type);
    }
    // Recursively find a symbol in the current or parent tables to get the type
    public String get(String name){
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        } 
        else if (parent != null) {
            return parent.get(name);
        } 
        else {
            return "null"; 
        }
    }

    // For declaration to see if the variable is declared or not
    public boolean lookup(String name) {
        if (symbols.containsKey(name)) {
            return true;
        } 
        else {
            return false; 
        }
    }
/* 
    // Add a child table
    public void addChild(SymbolTable child) {
        children.add(child);
    }

    // Get the parent of this table (useful for returning to outer scope)
    public SymbolTable getParent() {
        return parent;
    }*/
}
