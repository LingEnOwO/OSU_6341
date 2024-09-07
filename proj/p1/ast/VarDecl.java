package ast;
import java.io.PrintStream;
import java.util.Map;
public abstract class VarDecl extends ASTNode {
    public final String ident;

    public VarDecl(String i, Location loc) {
        super(loc);
        ident = i;
    }
    
    public abstract void check(TypeCheck checker);
}
