package ast;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import interpreter.Interpreter;
import java.util.Scanner;
import java.util.Stack;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
//TODO: whileState store ident and type
public class TypeCheck {
    private HashMap<String, VariableInfo> symbolTable;
    private Queue<VariableInfo> values = new LinkedList<>();
    private Scanner s = new Scanner(System.in);
    private Stack<String> stack = new Stack<>();
    //private Hashtable<String, VariableInfo> whileState = new Hashtable<>();
    //private Hashtable<String, VariableInfo> table1 = new Hashtable<>();
    //private Hashtable<String, VariableInfo> table2 = new Hashtable<>();
    public TypeCheck() {
        this.symbolTable = new HashMap<String, VariableInfo>();
    }
    

    // Method to check if a variable has been declared
    public boolean isDeclared(String ident) {
        return this.symbolTable.containsKey(ident);
    }
    
    //----declare-----  Decl, VarDecl, IntVarDecl, FloatVarDecl
    public void checkDecl(Decl decl){
        String exprType = exprType(decl.expr);
        String declType = checkVarDecl(decl.varDecl);
        if (!exprType.equals("null")  && ((declType.equals("int") && exprType.contains("Float")) || (declType.equals("float") && exprType.contains("Int")))){
            Interpreter.fatalError(decl.varDecl.ident + " is invalid declaration!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        VariableInfo exprVal = getExprValue(decl.expr);
        if (decl.expr != null && ( exprVal != null && exprVal.getIntValue() == null && exprVal.getFloatValue() == null)){
            Interpreter.fatalError("Variable " + exprVal.getIdent() + " has not been initialized yet!", Interpreter.EXIT_UNINITIALIZED_VAR_ERROR);
        }
        if (declType.equals("Int") && exprType.contains("Int")){
            //VariableInfo exprVal = getExprValue(decl.expr);
            if (decl.expr instanceof ReadIntExpr ){
                VariableInfo val = values.poll();
                this.symbolTable.put(decl.varDecl.ident,val);
            }
            else{
                VariableInfo val = exprVal.copyWithIdentInt(decl.varDecl.ident);
                //System.out.println(val.getIdent()+":"+val.getType().name());
                this.symbolTable.put(decl.varDecl.ident,val);
            }
           
        }
        if (declType.equals("Float") && exprType.contains("Float")){
            //VariableInfo exprVal = getExprValue(decl.expr);
            if (decl.expr instanceof ReadFloatExpr){
                VariableInfo val = values.poll();
                this.symbolTable.put(decl.varDecl.ident,val);
            }
            else{
                VariableInfo val = exprVal.copyWithIdentFloat(decl.varDecl.ident);
                this.symbolTable.put(decl.varDecl.ident,val);
            }
        }
        
    }

    public String checkVarDecl(VarDecl varDecl){
        if (varDecl instanceof IntVarDecl){
            //call checkIntVarDecl
            IntVarDecl intVar = (IntVarDecl) varDecl;
            return checkIntVarDecl(intVar);
        }
        else if (varDecl instanceof FloatVarDecl){
            //call checkFloatVarDecl
            FloatVarDecl floatVar = (FloatVarDecl) varDecl;
            return checkFloatVarDecl(floatVar);
        }
        return "null";
    }

    public String checkIntVarDecl(IntVarDecl var){
        if(isDeclared(var.ident)){
            //Error message: "already declared"
            Interpreter.fatalError("Variable " + var.ident + " has already been declared!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
            return "null";
        }
        else{
            this.symbolTable.put(var.ident, new VariableInfo(var.ident,VariableInfo.VarType.NoneInt));
            // for debugging
            //System.out.println("Declared variable: " + var.ident);
            return "Int";
        }
        
    }

    public String checkFloatVarDecl(FloatVarDecl var){
        if(isDeclared(var.ident)){
            //Error message: "already declared"
            Interpreter.fatalError("Variable " + var.ident + " has already been declared!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
            return "null";
        }
        else{
            this.symbolTable.put(var.ident, new VariableInfo(var.ident,VariableInfo.VarType.NoneFloat));
            // for debugging
            //System.out.println("Declared variable: " + var.ident);
            return "Float";
        }
    }

    //------Expr-------- ExprValue, IdentExpr, IntConstExpr, FloatConstExpr, exprType, BinaryExpr, DivByZero, UnaryMinusExpr
    public VariableInfo getExprValue(Expr expr) {
        if (expr instanceof IntConstExpr) {
            IntConstExpr intConstExpr = (IntConstExpr) expr;
            if (intConstExpr.ival > 0)
                return new VariableInfo(null, VariableInfo.VarType.PosInt, intConstExpr.ival);
            else if (intConstExpr.ival == 0)
                return new VariableInfo(null, VariableInfo.VarType.ZeroInt, intConstExpr.ival);
            else
                return new VariableInfo(null, VariableInfo.VarType.NegInt, intConstExpr.ival);
        }
        if (expr instanceof FloatConstExpr) {
            FloatConstExpr floatConstExpr = (FloatConstExpr) expr;
            if (floatConstExpr.fval > 0.0)
                return new VariableInfo(null, VariableInfo.VarType.PosFloat, floatConstExpr.fval);
            else if (floatConstExpr.fval == 0.0)
                return new VariableInfo(null, VariableInfo.VarType.ZeroFloat, floatConstExpr.fval);
            else
                return new VariableInfo(null, VariableInfo.VarType.NegFloat, floatConstExpr.fval);
        }
        if (expr instanceof IdentExpr) {
            IdentExpr identExpr = (IdentExpr) expr;
            VariableInfo varInfo = this.symbolTable.get(identExpr.ident);
            return varInfo;
        }
        if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            String type = exprType(binaryExpr);
            VariableInfo expr1 = getExprValue(binaryExpr.expr1);
            if (binaryExpr.expr1 instanceof ReadIntExpr || binaryExpr.expr1 instanceof ReadFloatExpr) expr1 = values.poll();
            VariableInfo expr2 = getExprValue(binaryExpr.expr2);
            if (binaryExpr.expr2 instanceof ReadIntExpr || binaryExpr.expr2 instanceof ReadFloatExpr) expr2 = values.poll();
            //System.out.println(expr1.getIntValue()+ ", "+expr2.getIntValue());
            if ((expr1.getIntValue() == null && expr1.getFloatValue() == null) || expr2.getIntValue() == null && expr2.getFloatValue() == null)
                Interpreter.fatalError("There are uninitialized variables ",Interpreter.EXIT_UNINITIALIZED_VAR_ERROR);
            if (binaryExpr.op == 1){
                
                if (type.contains("Int")) {
                    //System.out.println("here");
                    if (type.equals("PosInt"))
                        return new VariableInfo(null, VariableInfo.VarType.PosInt, expr1.getIntValue() + expr2.getIntValue());
                    else if (type.equals("ZeroInt"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroInt, expr1.getIntValue() + expr2.getIntValue());
                    else if (type.equals("NegInt"))
                        return new VariableInfo(null, VariableInfo.VarType.NegInt, expr1.getIntValue() + expr2.getIntValue());
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyInt, expr1.getIntValue() + expr2.getIntValue());
                }
                if (type.contains("Float")) {
                    if (type.equals("PosFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.PosFloat, expr1.getFloatValue() + expr2.getFloatValue());
                    else if (type.equals("ZeroFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroFloat, expr1.getFloatValue() + expr2.getFloatValue());
                    else if (type.equals("NegFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.NegFloat, expr1.getFloatValue() + expr2.getFloatValue());
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyFloat, expr1.getFloatValue() + expr2.getFloatValue());
                }
            }
            else if (binaryExpr.op == 2){
                if (type.contains("Int")) {
                    if (type.equals("PosInt"))
                        return new VariableInfo(null, VariableInfo.VarType.PosInt, expr1.getIntValue() - expr2.getIntValue());
                    else if (type.equals("ZeroInt"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroInt, expr1.getIntValue() - expr2.getIntValue());
                    else if (type.equals("NegInt"))
                        return new VariableInfo(null, VariableInfo.VarType.NegInt, expr1.getIntValue() - expr2.getIntValue());
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyInt, expr1.getIntValue() - expr2.getIntValue());
                }
                if (type.contains("Float")) {
                    if (type.equals("PosFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.PosFloat, expr1.getFloatValue() - expr2.getFloatValue());
                    else if (type.equals("ZeroFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroFloat, expr1.getFloatValue() - expr2.getFloatValue());
                    else if (type.equals("NegFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.NegFloat, expr1.getFloatValue() - expr2.getFloatValue());
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyFloat, expr1.getFloatValue() - expr2.getFloatValue());
                }
            }
            else if (binaryExpr.op == 3){
                if (type.contains("Int")) {
                    if (type.equals("PosInt"))
                        return new VariableInfo(null, VariableInfo.VarType.PosInt, expr1.getIntValue() * expr2.getIntValue());
                    else if (type.equals("ZeroInt"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroInt, expr1.getIntValue() * expr2.getIntValue());
                    else if (type.equals("NegInt"))
                        return new VariableInfo(null, VariableInfo.VarType.NegInt, expr1.getIntValue() * expr2.getIntValue());
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyInt, expr1.getIntValue() * expr2.getIntValue());
                }
                if (type.contains("Float")) {
                    if (type.equals("PosFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.PosFloat, expr1.getFloatValue() * expr2.getFloatValue());
                    else if (type.equals("ZeroFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroFloat, expr1.getFloatValue() * expr2.getFloatValue());
                    else if (type.equals("NegFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.NegFloat, expr1.getFloatValue() * expr2.getFloatValue());
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyFloat, expr1.getFloatValue() * expr2.getFloatValue());
                }
            }
            else if (binaryExpr.op == 4){
                if (type.contains("Int")) {
                    if (type.equals("ZeroInt"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroInt, expr1.getIntValue() / expr2.getIntValue());
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyInt, expr1.getIntValue() / expr2.getIntValue());
                }
                if (type.contains("Float")) {
                    if (type.equals("PosFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.PosFloat, expr1.getFloatValue() / expr2.getFloatValue());
                    else if (type.equals("ZeroFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroFloat, expr1.getFloatValue() / expr2.getFloatValue());
                    else if (type.equals("NegFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.NegFloat, expr1.getFloatValue() / expr2.getFloatValue());
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyFloat, expr1.getFloatValue() / expr2.getFloatValue());
                }
            }
        }
        if (expr instanceof UnaryMinusExpr){
            UnaryMinusExpr unaryMinusExpr = (UnaryMinusExpr) expr;
            if(unaryMinusExpr.expr instanceof IntConstExpr){
                IntConstExpr intConstExpr = (IntConstExpr) unaryMinusExpr.expr;
                if (intConstExpr.ival > 0L)
                    return new VariableInfo(null, VariableInfo.VarType.NegInt, intConstExpr.ival*(-1));
                if (intConstExpr.ival == 0L)
                    return new VariableInfo(null, VariableInfo.VarType.ZeroInt, intConstExpr.ival*(-1)); 
                else
                    return new VariableInfo(null, VariableInfo.VarType.PosInt, intConstExpr.ival*(-1));
            }
            if(unaryMinusExpr.expr instanceof FloatConstExpr){
                FloatConstExpr floatConstExpr = (FloatConstExpr) unaryMinusExpr.expr;
                if (floatConstExpr.fval > 0.0)
                    return new VariableInfo(null, VariableInfo.VarType.NegFloat, floatConstExpr.fval*(-1.0));
                if (floatConstExpr.fval == 0.0)
                    return new VariableInfo(null, VariableInfo.VarType.ZeroFloat, floatConstExpr.fval*(-1.0));
                else
                    return new VariableInfo(null, VariableInfo.VarType.PosFloat, floatConstExpr.fval*(-1.0));
            }
            if(unaryMinusExpr.expr instanceof IdentExpr){
                IdentExpr identExpr = (IdentExpr) unaryMinusExpr.expr;
                if (symbolTable.get(identExpr.ident).getType().name().contains("Int")) {
                    //System.out.println(symbolTable.get(identExpr.ident).getType().name());
                    if (symbolTable.get(identExpr.ident).getType().name().contains("Pos"))
                        return new VariableInfo(null, VariableInfo.VarType.NegInt, symbolTable.get(identExpr.ident).getIntValue()*(-1));
                    if (symbolTable.get(identExpr.ident).getType().name().contains("Zero"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroInt, symbolTable.get(identExpr.ident).getIntValue()*(-1));
                    if (symbolTable.get(identExpr.ident).getType().name().contains("Neg"))
                        return new VariableInfo(null, VariableInfo.VarType.PosInt, symbolTable.get(identExpr.ident).getIntValue()*(-1));
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyInt, symbolTable.get(identExpr.ident).getIntValue()*(-1));
                }
                else {
                    if (symbolTable.get(identExpr.ident).getType().name().contains("Pos"))
                        return new VariableInfo(null, VariableInfo.VarType.NegFloat, symbolTable.get(identExpr.ident).getFloatValue()*(-1.0));
                    if (symbolTable.get(identExpr.ident).getType().name().contains("Zero"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroFloat, symbolTable.get(identExpr.ident).getFloatValue()*(-1.0));
                    if (symbolTable.get(identExpr.ident).getType().name().contains("Neg"))
                        return new VariableInfo(null, VariableInfo.VarType.PosFloat, symbolTable.get(identExpr.ident).getFloatValue()*(-1.0));
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyFloat, symbolTable.get(identExpr.ident).getFloatValue()*(-1.0));
                }
            }
            if (unaryMinusExpr.expr instanceof BinaryExpr){
                BinaryExpr binaryExpr = (BinaryExpr) unaryMinusExpr.expr;
                String type = exprType(binaryExpr);
                VariableInfo res = getExprValue(binaryExpr);
                //use copywith
                if (type.contains("Int")){
                    if (type.equals("PosInt"))
                        return new VariableInfo(null, VariableInfo.VarType.NegInt, res.getIntValue()*(-1));
                    else if (type.equals("NegInt"))
                        return new VariableInfo(null, VariableInfo.VarType.PosInt, res.getIntValue()*(-1));
                    else if (type.equals("ZeroInt"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroInt, res.getIntValue()*(-1));
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyInt, res.getIntValue()*(-1));
                }
                else{
                    if (type.equals("PosFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.NegFloat, res.getFloatValue()*(-1.0));
                    else if (type.equals("NegFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.PosFloat, res.getFloatValue()*(-1.0));
                    else if (type.equals("ZeroFloat"))
                        return new VariableInfo(null, VariableInfo.VarType.ZeroFloat, res.getFloatValue()*(-1.0));
                    else
                        return new VariableInfo(null, VariableInfo.VarType.AnyFloat, res.getFloatValue()*(-1.0));
                }
            }
            if (unaryMinusExpr.expr instanceof ReadIntExpr){
                return new VariableInfo(null, VariableInfo.VarType.AnyInt, 0L);
            }
            if (unaryMinusExpr.expr instanceof ReadFloatExpr){
                return new VariableInfo(null, VariableInfo.VarType.AnyFloat, 0.0);
            }
        }
        if (expr instanceof ReadIntExpr || expr instanceof ReadFloatExpr){
            while(s.hasNext()){
                if(s.hasNextInt()){
                    values.add(VariableInfo.createInt(null, Long.valueOf(s.nextInt())));
                }
                else if(s.hasNextFloat()){
                    values.add(VariableInfo.createFloat(null, Double.valueOf(s.nextFloat())));
                }
            }
        }
        return null;
    }


    public String checkIdentExpr(IdentExpr idExpr){
        VariableInfo exprInfo = symbolTable.get(idExpr.ident);
        //System.out.println(idExpr.ident+": "+exprType);
        if (exprInfo == null){
            Interpreter.fatalError("Variable " + idExpr.ident + " has not been declared yet!", Interpreter.EXIT_UNINITIALIZED_VAR_ERROR);
        }
        return exprInfo.getType().name();
    }

    public String checkIntConstExpr(IntConstExpr intConstExpr){
        if (intConstExpr.ival > 0)
            return "PosInt";
        else if (intConstExpr.ival == 0)
            return "ZeroInt";
        else
            return "NegInt";
    }

    public String checkFloatConstExpr(FloatConstExpr floatConstExpr){
        if (floatConstExpr.fval > 0.0)
            return "PosFloat";
        else if (floatConstExpr.fval == 0.0)
            return "ZeroFloat";
        else 
            return "NegFloat";
    }

    public String checkReadIntExpr(ReadIntExpr readIntExpr){
        return "AnyInt";
    }

    public String checkReadFloatExpr(ReadFloatExpr readFloatExpr){
        return "AntFloat";
    }

    //Retrieve Expr's type
    public String exprType(Expr expr){
        if(expr instanceof IntConstExpr){
            IntConstExpr intConstExpr = (IntConstExpr) expr;
            return checkIntConstExpr(intConstExpr);
        }
        if(expr instanceof ReadIntExpr){
            ReadIntExpr readIntExpr = (ReadIntExpr) expr;
            return checkReadIntExpr(readIntExpr);
        }
        if(expr instanceof FloatConstExpr){
            FloatConstExpr floatConstExpr = (FloatConstExpr) expr;
            return checkFloatConstExpr(floatConstExpr);
        }
        if(expr instanceof ReadFloatExpr){
            ReadFloatExpr readFloatExpr = (ReadFloatExpr) expr;
            return checkReadFloatExpr(readFloatExpr);
        }
        if(expr instanceof IdentExpr){
            IdentExpr identExpr = (IdentExpr) expr;
            return checkIdentExpr(identExpr);
            
        }
        if(expr instanceof BinaryExpr){
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            return checkBinaryExpr(binaryExpr.expr1,binaryExpr.expr2, binaryExpr.op);
        }
        if(expr instanceof UnaryMinusExpr){
            UnaryMinusExpr unaryMinusExpr = (UnaryMinusExpr) expr;
            return checkUnaryMinusExpr(unaryMinusExpr.expr);
        }
        return "null";
    }
    // PlusExpr both side should be the same type
    public String checkBinaryExpr(Expr expr1, Expr expr2, int op){
        String ex1 = exprType(expr1);
        String ex2 = exprType(expr2);
        if ((ex1.contains("Int") && ex2.contains("Float")) || (ex1.contains("Float") && ex2.contains("Int"))){
            //System.out.println(ex1);
            //System.out.println(ex2);
            Interpreter.fatalError(expr1+" and "+expr2+ " are two different types. The binary expression is invalid!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        if(ex1.contains("Int") && ex2.contains("Int")){
            if (op == 1)
                return plus(ex1,ex2)+"Int";
            else if (op == 2)
                return minus(ex1,ex2)+"Int";
            else if (op == 3)
                return multiply(ex1,ex2)+"Int";
            else
                return divideInt(ex1,ex2);
        }
        if(ex1.contains("Float") && ex2.contains("Float")){
            if (op == 1)
                return plus(ex1,ex2)+"Float";
            else if (op == 2)
                return minus(ex1,ex2)+"Float";
            else if (op == 3)
                return multiply(ex1,ex2)+"Float";
            else
                return divideFloat(ex1,ex2);
        }
        
        
        return "null";
    }
    public String plus(String ex1, String ex2){
        if ((ex1.contains("Neg") && ex2.contains("Neg")) || (ex1.contains("Neg") && ex2.contains("Zero")) || (ex1.contains("Zero") && ex2.contains("Neg")))
            return "Neg"; //3
        if (ex1.contains("Zero") && ex2.contains("Zero"))
            return "Zero"; //1
        if ((ex1.contains("Neg") && ex2.contains("Pos")) || ex1.contains("Pos") && ex2.contains("Neg"))
            return "Any"; //2
        if ((ex1.contains("Zero") && ex2.contains("Pos")) || ex1.contains("Pos") && ex2.contains("Zero") || (ex1.contains("Pos") && ex2.contains("Pos")))
            return "Pos"; //3
        if (ex1.contains("Any") || ex2.contains("Any"))
            return "Any"; //7
        return "null";
        
    }
    public String minus(String ex1, String ex2){
        if ((ex1.contains("Neg") && ex2.contains("Neg")) || (ex1.contains("Pos") && ex2.contains("Pos")))
            return "Any"; //2
        if ((ex1.contains("Zero") && ex2.contains("Neg")) || (ex1.contains("Pos") && ex2.contains("Neg")) || (ex1.contains("Pos") && ex2.contains("Zero")))
            return "Pos"; //3
        if ((ex1.contains("Neg") && ex2.contains("Zero")) || (ex1.contains("Neg") && ex2.contains("Pos")) || (ex1.contains("Zero") && ex2.contains("Pos")))
            return "Neg"; //3
        if ((ex1.contains("Zero") && ex2.contains("Zero")))
            return "Zero"; //1
        if (ex1.contains("Any") || ex2.contains("Any"))
            return "Any"; //7
        return "null";
    }
    public String multiply(String ex1, String ex2){
        if ((ex1.contains("Neg") && ex2.contains("Neg")) || (ex1.contains("Pos") && ex2.contains("Pos")))
            return "Pos"; //2
        if ((ex1.contains("Pos") && ex2.contains("Neg")) || (ex1.contains("Neg") && ex2.contains("Pos")))
            return "Neg"; //2
        if ((ex1.contains("Zero") || ex2.contains("Zero")))
            return "Zero"; //7
        if (ex1.contains("Any") || ex2.contains("Any"))
            return "Any"; //5
        return "null";
        
    }

    public String divideInt(String ex1, String ex2){
        if ((ex2.contains("Zero") || ex2.contains("Any")))
            Interpreter.fatalError("Division-by-zero error!",Interpreter.EXIT_DIV_BY_ZERO_ERROR); //8
        if (ex1.contains("Zero"))
            return "ZeroInt"; //2
        else
            return "AnyInt"; //6
    }

    public String divideFloat(String ex1, String ex2){
        if ((ex2.contains("Zero") || ex2.contains("Any")))
            Interpreter.fatalError("Division-by-zero error!",Interpreter.EXIT_DIV_BY_ZERO_ERROR); //8
        if ((ex1.contains("Neg") && ex2.contains("Neg")) || (ex1.contains("Pos") && ex2.contains("Pos")))
            return "PosFloat"; //2
        if ((ex1.contains("Pos") && ex2.contains("Neg")) || (ex1.contains("Neg") && ex2.contains("Pos")))
            return "NegFloat"; //2
        if (ex1.contains("Zero"))
            return "ZeroFloat"; //2
        if (ex1.contains("Any"))
            return "AnyFloat"; //2
        return "null";
    }
    public void checkDivByZero(Expr expr){
        if (expr instanceof IntConstExpr){
            IntConstExpr intExpr = (IntConstExpr) expr;
            if (intExpr.ival == 0) {
                Interpreter.fatalError("It's pointless to devide 0",Interpreter.EXIT_DIV_BY_ZERO_ERROR);
            }
        }
        if (expr instanceof ReadIntExpr)

        if (expr instanceof FloatConstExpr){
            FloatConstExpr floatExpr = (FloatConstExpr) expr;
            if (floatExpr.fval == 0.0) {
                Interpreter.fatalError("It's pointless to devide 0",Interpreter.EXIT_DIV_BY_ZERO_ERROR);
            }
        }
        if (expr instanceof ReadFloatExpr)

        if (expr instanceof IdentExpr){
            IdentExpr identExpr = (IdentExpr) expr;
            //System.out.println(op+", "+ identExpr.ident);
            if(checkIdentExpr(identExpr).contains("Zero"))
                Interpreter.fatalError("It's pointless to devide zero",Interpreter.EXIT_DIV_BY_ZERO_ERROR);
        }
    }
    //UnaryMinusExpr
    public String checkUnaryMinusExpr(Expr expr){
        if (exprType(expr).equals("PosInt"))
            return "NegInt";
        if (exprType(expr).equals("NegInt"))
            return "PosInt";
        if (exprType(expr).equals("ZeroInt"))
            return "ZeroInt";
        if (exprType(expr).equals("AnyInt"))
            return "AnyInt"; 
        if (exprType(expr).equals("PosFloat"))
            return "NegFloat";
        if (exprType(expr).equals("NegFloat"))
            return "PosFloat";
        if (exprType(expr).equals("ZeroFloat"))
            return "ZeroFloat";
        if (exprType(expr).equals("AnyFloat"))
            return "AnyFloat";
        return "null";
    }

    //----CondExpr------------ CompExpr, LogicalExpr
    public String condExpr(CondExpr expr){
        if (expr instanceof LogicalExpr){
            LogicalExpr logicalExpr = (LogicalExpr) expr;
            return checkLogicalExpr(logicalExpr.expr1,logicalExpr.expr2,logicalExpr.op);
        }
        if(expr instanceof CompExpr){
            CompExpr compExpr = (CompExpr) expr;
            return checkCompExpr(compExpr.expr1, compExpr.expr2, compExpr.op);
        }
        return "False";
    }
    public String checkCompExpr(Expr expr1, Expr expr2, int op){
        String ex1 = exprType(expr1);
        String ex2 = exprType(expr2);
        if (((ex1.contains("Int") && ex2.contains("Float")) || (ex1.contains("Float") && ex2.contains("Int"))) && (!ex1.equals("null") && !ex2.equals("null"))){
            //System.out.println(ex1);
            Interpreter.fatalError(expr1+" and "+expr2+ " are two different types. The comparison expression is invalid!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        if(ex1.contains("Int") && ex2.contains("Int")){
            //Long val1,val2;
            String type1, type2;
            if (expr1 instanceof ReadIntExpr){
                getExprValue(expr1);
                VariableInfo readVal1 = values.poll();
                //val1 = readVal1.getIntValue();
                type1 = readVal1.getType().name();
            } 
            else type1 = getExprValue(expr1).getType().name();//val1 = getExprValue(expr1).getIntValue();
            if (expr2 instanceof ReadIntExpr){
                getExprValue(expr2);
                VariableInfo readVal2 = values.poll();
                //val2 = readVal2.getIntValue();
                type2 = readVal2.getType().name();
            } 
            else type2 = getExprValue(expr2).getType().name();//val2 = getExprValue(expr2).getIntValue();
            return checkCond(type1, type2, op);
        }
        if(ex1.contains("Float") && ex2.contains("Float")){
            //Double val1,val2;
            String type1, type2;
            if (expr1 instanceof ReadFloatExpr){
                getExprValue(expr1);
                VariableInfo readVal1 = values.poll();
                //val1 = readVal1.getFloatValue();
                type1 = readVal1.getType().name();
            } 
            else type1 = getExprValue(expr1).getType().name();//val1 = getExprValue(expr1).getFloatValue();
            if (expr2 instanceof ReadFloatExpr){
                getExprValue(expr2);
                VariableInfo readVal2 = values.poll();
                //val2 = readVal2.getFloatValue();
                type2 = readVal2.getType().name();
            } 
            else type2 = getExprValue(expr2).getType().name();//val2 = getExprValue(expr2).getFloatValue();
            return checkCond(type1, type2, op);
        }
        
        return "False";

    }

    public String checkCond(String type1, String type2, int op){
        //System.out.println(type1+"and"+type2);
        if (op == 1 || op == 2){ // >= or >
            if ((type1.contains("Neg") && type2.contains("Neg")) || (type1.contains("Pos") && type2.contains("Pos")) || (type1.contains("Any") || type2.contains("Any")))
                return "AnyBool"; // 9
            if ((type1.contains("Neg") && type2.contains("Zero")) || (type1.contains("Neg") && type2.contains("Pos")) || (type1.contains("Zero") && type2.contains("Pos")))
                return "False"; // 3
            if ((type1.contains("Zero") && type2.contains("Neg")) || (type1.contains("Pos") && type2.contains("Neg")) || (type1.contains("Pos") && type2.contains("Zero")))
                return "True"; // 3
            if (type1.contains("Zero") && type2.contains("Zero")){
                if (op == 1)
                    return "True";
                else return "False"; // 1
            }
        }
        if (op == 3 || op == 4){ // <= or <
            if ((type1.contains("Neg") && type2.contains("Neg")) || (type1.contains("Pos") && type2.contains("Pos")) || (type1.contains("Any") || type2.contains("Any")))
                return "AnyBool"; // 9
            if ((type1.contains("Neg") && type2.contains("Zero")) || (type1.contains("Neg") && type2.contains("Pos")) || (type1.contains("Zero") && type2.contains("Pos")))
                return "True"; // 3
            if ((type1.contains("Zero") && type2.contains("Neg")) || (type1.contains("Pos") && type2.contains("Neg")) || (type1.contains("Pos") && type2.contains("Zero")))
                return "False"; // 3
            if (type1.contains("Zero") && type2.contains("Zero")){
                if (op == 3)
                    return "True";
                else return "False"; // 1
            }
        }
        if (op == 5){ // ==
            if (type1.contains("Zero") && type2.contains("Zero"))
                return "True"; // 1
            if ((type1.contains("Neg") && (type2.contains("Zero") || type2.contains("Pos"))) || (type1.contains("Zero") && (type2.contains("Neg") || type2.contains("Pos"))) || (type1.contains("Pos") && (type2.contains("Neg") ||type2.contains("Zero"))))
                return "False"; // 6
            if ((type1.contains("Neg") && type2.contains("Neg")) || (type1.contains("Pos") && type2.contains("Pos")) || ((type1.contains("Any") || type2.contains("Any"))))
                return "AnyBool"; // 9
        }
        if (op == 6){ // !=
            if (type1.contains("Zero") && type2.contains("Zero"))
                return "False"; // 1
            if ((type1.contains("Neg") && (type2.contains("Zero") || type2.contains("Pos"))) || (type1.contains("Zero") && (type2.contains("Neg") || type2.contains("Pos"))) || (type1.contains("Pos") && (type2.contains("Neg") ||type2.contains("Zero"))))
                return "True"; // 6
            if ((type1.contains("Neg") && type2.contains("Neg")) || (type1.contains("Pos") && type2.contains("Pos")) || ((type1.contains("Any") || type2.contains("Any"))))
                return "AnyBool"; // 9
        }
        return "False";
    }


    public String checkLogicalExpr(CondExpr condExpr1, CondExpr condExpr2,int op){
        String cond1 = condExpr(condExpr1);
        if(op == 1){
            if(cond1.equals("False")) return "False";
            String cond2 = condExpr(condExpr2);
            if(cond1.equals("True") && cond2.equals("True")) return "True";
            return "False";
        }
        if(op == 2){
            if(cond1.equals("True")) return "True";
            String cond2 = condExpr(condExpr2);
            if(cond2.equals("True")) return "True";
            return "False";
        }
        if(op == 3){
            if(cond1.equals("True")) return "False";
            return "True";
        }
        return "False";
    }
    
    //----------uniList-----------
    public void checkUnitList(UnitList unitList){
        checkUnit(unitList.unit);
        if(unitList.unitList != null){
            checkUnitList(unitList.unitList);
        }
    }
    //-------------unit------------
    public void checkUnit(Unit unit){
        if (unit instanceof AssignStmt){
            AssignStmt assignStmt = (AssignStmt) unit;
            checkAssignStmt(assignStmt.ident, assignStmt.expr);
        }
        if(unit instanceof BlockStmt){
            BlockStmt blockStmt = (BlockStmt) unit;
            checkBlockStmt(blockStmt);
        }

        if(unit instanceof IfStmt){
            IfStmt ifStmt = (IfStmt) unit;
            checkIfStmt(ifStmt);
        }

        if(unit instanceof PrintStmt){
            PrintStmt printStmt = (PrintStmt) unit;
            checkPrintStmt(printStmt.expr);
        }
        
        if(unit instanceof WhileStmt){
            WhileStmt whileStmt = (WhileStmt) unit;
            checkWhileStmt(whileStmt);
        }
        if(unit instanceof Decl){
            Decl decl = (Decl) unit;
            checkDecl(decl);
        }
    }

    //-----------Stmt-----------
    public void checkStmt(Stmt stmt){
        if(stmt instanceof AssignStmt){
            AssignStmt assignStmt = (AssignStmt) stmt;
            checkAssignStmt(assignStmt.ident, assignStmt.expr);
        }

        if(stmt instanceof BlockStmt){
            BlockStmt blockStmt = (BlockStmt) stmt;
            checkBlockStmt(blockStmt);
        }

        if(stmt instanceof IfStmt){
            IfStmt ifStmt = (IfStmt) stmt;
            checkIfStmt(ifStmt);
        }

        if(stmt instanceof PrintStmt){
            PrintStmt printStmt = (PrintStmt) stmt;
            checkPrintStmt(printStmt.expr);
        }
        
        if(stmt instanceof WhileStmt){
            WhileStmt whileStmt = (WhileStmt) stmt;
            checkWhileStmt(whileStmt);
        }
    }

    //AssignStmt
    public void checkAssignStmt(String ident, Expr expr){
        if(!isDeclared(ident)){
            //System.out.println("assignment");
            Interpreter.fatalError("Variable " + ident + " has not been declared yet!", Interpreter.EXIT_UNINITIALIZED_VAR_ERROR);
        }
        VariableInfo.VarType identType = symbolTable.get(ident).getType();
        String exprType = exprType(expr);
        //update ident's val
        VariableInfo newVal = getExprValue(expr);
        if (newVal != null && newVal.getIntValue() == null && newVal.getFloatValue() == null){
            Interpreter.fatalError("Variable " + newVal.getIdent() + " has not been initialized yet!", Interpreter.EXIT_UNINITIALIZED_VAR_ERROR);
        }
        if ((identType.name().contains("Int") && exprType.contains("Float"))  || (identType.name().contains("Float") && exprType.contains("Int"))) {
            //System.out.println(newVal.getType());
            Interpreter.fatalError("Invalid assignment!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        if (exprType.contains("Int")){
            if (expr instanceof ReadIntExpr){
                VariableInfo val = values.poll();
                this.symbolTable.put(ident,val);
            }
            else {
                newVal = newVal.copyWithIdentInt(ident);
                symbolTable.put(ident,newVal);
                //System.out.println(ident+": "+newVal.getIntValue());
            }
        }
        else{
            if (expr instanceof ReadFloatExpr){
                VariableInfo val = values.poll();
                this.symbolTable.put(ident,val);
            }
            else {
                newVal = newVal.copyWithIdentFloat(ident);
                symbolTable.put(ident,newVal);
                //System.out.println(ident+": "+newVal.getFloatValue());
            }
        }
    }  
        

    //BlockStmt
    public void checkBlockStmt(BlockStmt blockStmt){
        // code 0 -> if-else | 1-> while
        checkUnitList(blockStmt.block);
    }

    //IfStmt
    public void checkIfStmt(IfStmt is){ 
        Hashtable<String, VariableInfo> originalTable = new Hashtable<>(symbolTable);
        String cond = condExpr(is.expr);
        if (cond.equals("True")){
            if (is.elsestmt != null)
                Interpreter.fatalError("Else part is Dead Code!",Interpreter.EXIT_DEAD_CODE);
            checkStmt(is.thenstmt);
            return;
        }
        if (cond.equals("False"))
            Interpreter.fatalError("Then part is Dead Code!",Interpreter.EXIT_DEAD_CODE);
        checkStmt(is.thenstmt);
        Hashtable<String, VariableInfo> ifTable = new Hashtable<>(symbolTable);
        symbolTable.clear();
        symbolTable.putAll(originalTable);
        Set<String> commonElements = new HashSet<>(ifTable.keySet());
        if (is.elsestmt != null){
            checkStmt(is.elsestmt);
            Hashtable<String, VariableInfo> elseTable = new Hashtable<>(symbolTable);
            commonElements.retainAll(elseTable.keySet());
            for (String key : commonElements)
                merge(key,ifTable.get(key).getType().name(),elseTable.get(key).getType().name());
            /*for (Map.Entry<String, VariableInfo> entry : table2.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().getType().name());
            }*/
        }
        else{
            commonElements.retainAll(originalTable.keySet());
            for (String key : commonElements){
                merge(key,ifTable.get(key).getType().name(),originalTable.get(key).getType().name());
                //System.out.println(key+":"+symbolTable.get(key).getType().name());
                //System.out.println(table1.get(key).getType().name()+" "+table2.get(key).getType().name());
            }
        }
        
        
    }

    public void merge(String ident, String type1, String type2){
        if (type1.contains("None") || type2.contains("None"))
            Interpreter.fatalError("There are uninitialized variables", Interpreter.EXIT_UNINITIALIZED_VAR_ERROR);
        VariableInfo.VarType identType = symbolTable.get(ident).getType();
        String resType = integrate(type1,type2);
        Long longVal = 0L;
        Double doubleVal = 0.0;
        if (identType.name().contains("Int")){
            if (resType.equals("Pos"))
                symbolTable.put(ident, new VariableInfo(ident,VariableInfo.VarType.PosInt,longVal));
            else if (resType.equals("Neg"))
                symbolTable.put(ident, new VariableInfo(ident,VariableInfo.VarType.NegInt,longVal));
            else if (resType.equals("Zero"))
                symbolTable.put(ident, new VariableInfo(ident,VariableInfo.VarType.ZeroInt,longVal));
            else
                symbolTable.put(ident, new VariableInfo(ident,VariableInfo.VarType.AnyInt,longVal));
        }
        else{
            if (resType.equals("Pos"))
                symbolTable.put(ident, new VariableInfo(ident,VariableInfo.VarType.PosFloat,doubleVal));
            else if (resType.equals("Neg"))
                symbolTable.put(ident, new VariableInfo(ident,VariableInfo.VarType.NegFloat,doubleVal));
            else if (resType.equals("Zero"))
                symbolTable.put(ident, new VariableInfo(ident,VariableInfo.VarType.ZeroFloat,doubleVal));
            else
                symbolTable.put(ident, new VariableInfo(ident,VariableInfo.VarType.AnyFloat,doubleVal));
        }
    }
    
    public String integrate(String type1, String type2){
        if (type1.contains("Neg") && type2.contains("Neg"))
            return "Neg";
        if (type1.contains("Zero") && type2.contains("Zero"))
            return "Zero";
        if (type1.contains("Pos") && type2.contains("Pos"))
            return "Pos";
        else
            return "Any";
        
    }
    //PrintStmt
    //set { NegInt,ZeroInt, PosInt, AnyInt, NegFloat, ZeroFloat, PosFloat, AnyFloat}
    public void checkPrintStmt(Expr expr){
        String type = exprType(expr);
        //System.out.println(expr);
        VariableInfo val = getExprValue(expr);
        //System.out.println(val.getType().name()+" " + val.getIntValue());
        if (val.getIntValue() == null && val.getFloatValue() == null)
            Interpreter.fatalError("Variable " + val.getIdent() + " has not been initialized yet!", Interpreter.EXIT_UNINITIALIZED_VAR_ERROR);
        if(type.contains("Int") && !(expr instanceof ReadIntExpr)) {
            System.out.println(val.getType());
        }
        if(expr instanceof ReadIntExpr) {
            System.out.println("AnyInt");
        }
        if(type.contains("Float") && !(expr instanceof ReadFloatExpr)) {
            System.out.println(val.getType());
        }
        if(expr instanceof ReadFloatExpr) {
            System.out.println("AnyFloat");
        }
    }

    //WhileStmt
    public void checkWhileStmt(WhileStmt ws){
        //while(condExpr(ws.expr))
        //VariableInfo in whileState stores the original type
        String temp, originalType, newType, postMerge;
        int size, count;
        if (condExpr(ws.expr).equals("False"))
            Interpreter.fatalError("The loop is Dead Code!",Interpreter.EXIT_DEAD_CODE);
        Hashtable<String, VariableInfo> whileState = new Hashtable<>(symbolTable);
        while(true){
            checkStmt(ws.body);
            size = whileState.size();
            count = 0;
            for (Map.Entry<String, VariableInfo> entry : whileState.entrySet()) {
                //System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue().getType().name());
                temp = entry.getKey();
                originalType = entry.getValue().getType().name();
                newType = symbolTable.get(temp).getType().name();
                merge(temp, originalType, newType);
                postMerge = symbolTable.get(temp).getType().name();
                if (postMerge == newType){
                    count += 1;
                    if (count == size)
                        return;
                }
            }
        }
        
    }
}
