package ast;
import java.io.PrintStream;
import interpreter.Interpreter;

public class IntVarDecl extends VarDecl {
    public IntVarDecl(String i, Location loc) {
	super(i,loc);
    }
    public void print(PrintStream ps) {
	ps.print("int " + ident);
    }

    public void check(TypeCheck checker) {
        // Check if the variable has been declared
        if (checker.isDeclared(ident)){
            Interpreter.fatalError("Variable " + ident + " has already been declared ", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        String type = checker.getType(ident);
        Object value = checker.getValue(ident);
        // Add the variable to the symbol table as an 'int' type
        checker.declareVariable(ident,type, value);
    }
}
