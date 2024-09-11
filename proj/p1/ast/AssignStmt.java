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
        checker.checkAssignment(ident, expr);
    }
}
