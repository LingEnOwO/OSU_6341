package ast;
import java.io.PrintStream;
import interpreter.Interpreter;

public class FloatVarDecl extends VarDecl {
    public FloatVarDecl(String i, Location loc) {
	super(i,loc);
    }
    public void print(PrintStream ps) {
	ps.print("float " + ident);
    }

    public void check(TypeCheck checker) {
        // Check if the variable has been declared
        if (checker.isDeclared(ident)){
            Interpreter.fatalError("Variable " + ident + " has already been declared ", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        String type = checker.getType(ident);
        Object value = checker.getValue(ident);
        // Add the variable to the symbol table as an 'float' type
        checker.declareVariable(ident, type,value);
    }
}
