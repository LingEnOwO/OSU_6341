package ast;
import java.io.PrintStream;
import interpreter.Interpreter;

public class AssignStmt extends Stmt {
    public final String ident;
    public final Expr expr;
    public AssignStmt(String i, Expr e, Location loc) {
	super(loc);
	ident = i;
	expr = e;
    }
    public void print(PrintStream ps) { 
	ps.print(ident + " = ");
	expr.print(ps);
	ps.print(";");
    }

    public void check(TypeCheck checker) {
        // Check if the variable on the left-hand side is declared
        if (!checker.isDeclared(ident)) {
            Interpreter.fatalError("Variable " + ident + " not declared before assignment", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }

        // Check if the right-hand side expression uses declared variables
        //expr.check(checker);
    }
}
