package ast;
import java.io.PrintStream;

public abstract class Stmt extends Unit {
    public Stmt(Location loc) {
	super(loc);
    }

    public abstract void check(TypeCheck checker);
}
