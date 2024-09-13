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
        checker.checkIntVarDecl(this);
    }
}
