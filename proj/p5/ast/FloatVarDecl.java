package ast;
import java.io.PrintStream;

public class FloatVarDecl extends VarDecl {
    public FloatVarDecl(String i, Location loc) {
	super(i,loc);
    }
    public void print(PrintStream ps) {
	ps.print("float " + ident);
    }

    public void check(TypeCheck checker) { 
        checker.checkFloatVarDecl(this);
    }
}
