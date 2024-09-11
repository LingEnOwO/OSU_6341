package ast;
import java.io.PrintStream;
import java.util.Map;

public class Decl extends Unit {
    public final VarDecl varDecl;
    public final Expr expr;
    public Decl(VarDecl d, Location loc) {
	super(loc);
	varDecl = d;
	expr = null;
    }
    public Decl(VarDecl d, Expr e, Location loc) {
	super(loc);
	varDecl = d;
	expr = e;
    }

    /*public void check(Map<String, VarDecl> context) throws Exception {
        varDecl.check(context);  // Check and add the variable declaration to the context

        if (expr != null) {
            expr.check(context);  // Check the expression for valid variable usage
        }
    }*/

    public void print(PrintStream ps) { 
	varDecl.print(ps); 
	if (expr != null) {
	    ps.print(" = ");
	    expr.print(ps);
	}
	ps.print(";");
    }

    public void check(TypeCheck checker){
        checker.checkDecl(this);
    }
}
