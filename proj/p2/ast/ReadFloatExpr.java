package ast;
import java.io.PrintStream;

public class ReadFloatExpr extends Expr {
    public ReadFloatExpr(Location loc) {
	super(loc);
    }
    public void print(PrintStream ps) {
	ps.print("readfloat");
    }

    public void check(TypeCheck checker){
        checker.checkReadFloatExpr(this);
    }
}
