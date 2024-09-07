package ast;
import java.io.PrintStream;
import interpreter.Interpreter;

public class IdentExpr extends Expr {
    public final String ident; 
    public IdentExpr(String i, Location loc) {
	super(loc);
	ident = i;
    }
    public void print(PrintStream ps) {
	ps.print(ident);
    }

    public void check(TypeCheck checker) {
        // Check if the identifier has been declared
        if (!checker.isDeclared(ident)) {
            Interpreter.fatalError("Variable " + ident + " has not been declared yet", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        // Identifier is null -> the variable is used before assignment !!maybe use "or" u dont need two if
        if (checker.getValue(ident) == null && checker.isDeclared(ident)){
            Interpreter.fatalError("Variable " + ident + " has not been declared yet", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
    }
}
