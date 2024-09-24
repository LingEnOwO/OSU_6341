package ast;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class SymbolTable {
    private SymbolTable parent;
    private HashMap<String, String> symbols; // Stores variable name and type
    private List<SymbolTable> children;

    // Constructor for creating a new table, with a link to the parent
    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
        this.symbols = new HashMap<>();
        this.children = new ArrayList<>();
        if (parent != null) {
            parent.addChild(this); // Add this table as a child of the parent
        }
    }

    // Add a symbol to the current table
    public void put(String name, String type) {
        symbols.put(name, type);
    }
    // get
    public String get(String name){
        if (symbols.containsKey(name)) {
            return symbols.get(name);
        } else if (parent != null) {
            return parent.get(name);
        } else {
            return "null"; // Symbol not found
        }
    }

    // Recursively lookup a symbol in the current or parent tables
    public boolean lookup(String name) {
        if (symbols.containsKey(name)) {
            return true;
        } else if (parent != null) {
            return parent.lookup(name);
        } else {
            return false; // Symbol not found
        }
    }

    // Add a child table
    public void addChild(SymbolTable child) {
        children.add(child);
    }

    // Get the parent of this table (useful for returning to outer scope)
    public SymbolTable getParent() {
        return parent;
    }
}
