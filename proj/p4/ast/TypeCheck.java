package ast;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;
import interpreter.Interpreter;
import java.util.Scanner;
//TODO: Refactoring -> in each java class 
public class TypeCheck {
    private HashMap<String, VariableInfo> symbolTable;
    private Queue<VariableInfo> values = new LinkedList<>();
    private Scanner s = new Scanner(System.in);
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
        if (declType.equals("Int") && exprType.contains("Int")){
            VariableInfo exprVal = getExprValue(decl.expr);
            if (decl.expr instanceof ReadIntExpr ){
                VariableInfo val = values.poll();
                //if (val.getType() != VariableInfo.VarType.INT) Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
                this.symbolTable.put(decl.varDecl.ident,val);
            }
            else{
                VariableInfo val = exprVal.copyWithIdentInt(decl.varDecl.ident);
                this.symbolTable.put(decl.varDecl.ident,val);
            }
           
        }
        if (declType.equals("Float") && exprType.contains("Float")){
            VariableInfo exprVal = getExprValue(decl.expr);
            if (decl.expr instanceof ReadFloatExpr){
                VariableInfo val = values.poll();
                //if (val.getType() != VariableInfo.VarType.FLOAT) Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
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
            this.symbolTable.put(var.ident, new VariableInfo(var.ident,VariableInfo.VarType.AnyInt));
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
            this.symbolTable.put(var.ident, new VariableInfo(var.ident,VariableInfo.VarType.AnyFloat));
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
                //Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
            }
            else if (binaryExpr.op == 2){
                if (type.contains("Int")) {
                    //System.out.println("here");
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
                //Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
            }
            else if (binaryExpr.op == 3){
                if (type.contains("Int")) {
                    //System.out.println("here");
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
                //Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
            }
            else if (binaryExpr.op == 4){
                if (type.contains("Int")) {
                    //System.out.println("here");
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
                //Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
            }
        }
        if (expr instanceof UnaryMinusExpr){
            UnaryMinusExpr unaryMinusExpr = (UnaryMinusExpr) expr;
            if(unaryMinusExpr.expr instanceof IntConstExpr){
                IntConstExpr intConstExpr = (IntConstExpr) unaryMinusExpr.expr;
                return new VariableInfo(null, VariableInfo.VarType.NegInt, intConstExpr.ival*(-1));
            }
            if(unaryMinusExpr.expr instanceof FloatConstExpr){
                FloatConstExpr floatConstExpr = (FloatConstExpr) unaryMinusExpr.expr;
                return new VariableInfo(null, VariableInfo.VarType.NegFloat, floatConstExpr.fval*(-1.0));
            }
            if(unaryMinusExpr.expr instanceof IdentExpr){
                IdentExpr identExpr = (IdentExpr) unaryMinusExpr.expr;
                if (symbolTable.get(identExpr.ident).getType().name().contains("Int")) return new VariableInfo(null, VariableInfo.VarType.NegInt, symbolTable.get(identExpr.ident).getIntValue()*(-1));
                else return new VariableInfo(null, VariableInfo.VarType.NegFloat, symbolTable.get(identExpr.ident).getFloatValue()*(-1.0));
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
        /*else if (exprInfo.getType() == VariableInfo.VarType.INT){
            return "int";
        } 
        else if (exprInfo.getType() == VariableInfo.VarType.FLOAT) {
            return "float";
        } 
        
        return "null";*/
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
                //checkDivByZero(expr2);
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
                //checkDivByZero(expr2);
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
        return exprType(expr);
    }

    //----CondExpr------------ CompExpr, LogicalExpr
    public boolean condExpr(CondExpr expr){
        if (expr instanceof LogicalExpr){
            LogicalExpr logicalExpr = (LogicalExpr) expr;
            return checkLogicalExpr(logicalExpr.expr1,logicalExpr.expr2,logicalExpr.op);
        }
        if(expr instanceof CompExpr){
            CompExpr compExpr = (CompExpr) expr;
            return checkCompExpr(compExpr.expr1, compExpr.expr2, compExpr.op);
        }
        return false;
    }
    public boolean checkCompExpr(Expr expr1, Expr expr2, int op){
        String ex1 = exprType(expr1);
        String ex2 = exprType(expr2);
        if (((ex1.contains("Int") && ex2.contains("Float")) || (ex1.contains("Float") && ex2.contains("Int"))) && (!ex1.equals("null") && !ex2.equals("null"))){
            //System.out.println(ex1);
            Interpreter.fatalError(expr1+" and "+expr2+ " are two different types. The comparison expression is invalid!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        if(ex1.contains("Int") && ex2.contains("Int")){
            Long val1,val2;
            if (expr1 instanceof ReadIntExpr){
                getExprValue(expr1);
                VariableInfo readVal1 = values.poll();
                val1 = readVal1.getIntValue();
            } 
            else val1 = getExprValue(expr1).getIntValue();
            if (expr2 instanceof ReadIntExpr){
                getExprValue(expr2);
                VariableInfo readVal2 = values.poll();
                val2 = readVal2.getIntValue();
            } 
            else val2 = getExprValue(expr2).getIntValue();
            return checkIntCond(val1, val2, op);
        }
        if(ex1.contains("Float") && ex2.contains("Float")){
            Double val1,val2;
            if (expr1 instanceof ReadFloatExpr){
                getExprValue(expr1);
                VariableInfo readVal1 = values.poll();
                val1 = readVal1.getFloatValue();
            } 
            else val1 = getExprValue(expr1).getFloatValue();
            if (expr2 instanceof ReadFloatExpr){
                getExprValue(expr2);
                VariableInfo readVal2 = values.poll();
                val2 = readVal2.getFloatValue();
            } 
            else val2 = getExprValue(expr2).getFloatValue();
            return checkFloatCond(val1, val2, op);
        }
        
        return false;

    }

    public boolean checkIntCond(Long val1, Long val2, int op){
        switch(op){
            case 1:
                if (val1 >= val2) return true;
                else return false;
            case 2:
                if (val1 > val2) return true;
                else return false;
            case 3:
                if (val1 <= val2) return true;
                else return false;
            case 4:
                if (val1 < val2) return true;
                else return false;
            case 5:
                if (val1 == val2) return true;
                else return false;
            case 6:
                if (val1 != val2) return true;
                else return false;
        }
        return false;
    }

    public boolean checkFloatCond(Double val1, Double val2, int op){
        switch(op){
            case 1:
                if (val1 >= val2) return true;
                else return false;
            case 2:
                if (val1 > val2) return true;
                else return false;
            case 3:
                if (val1 <= val2) return true;
                else return false;
            case 4:
                if (val1 < val2) return true;
                else return false;
            case 5:
                if (val1 == val2) return true;
                else return false;
            case 6:
                if (val1 != val2) return true;
                else return false;
        }
        return false;
    }

    public boolean checkLogicalExpr(CondExpr condExpr1, CondExpr condExpr2,int op){
        boolean cond1 = condExpr(condExpr1);
        //boolean cond2 = condExpr(condExpr2);
        if(op == 1){
            if(cond1 == false) return false;
            boolean cond2 = condExpr(condExpr2);
            if(cond1 == true && cond2 == true) return true;
            return false;
        }
        if(op == 2){
            if(cond1 == true) return true;
            boolean cond2 = condExpr(condExpr2);
            if(cond2 == true) return true;
            return false;
        }
        if(op == 3){
            if(cond1 == true) return false;
            return true;
        }
        return false;
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
            checkBlockStmt(blockStmt.block);
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
            checkBlockStmt(blockStmt.block);
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
    public String checkAssignStmt(String ident, Expr expr){
        //if(currentTable.get(ident) == "null"){
        if(!isDeclared(ident)){
            //System.out.println("assignment");
            Interpreter.fatalError("Variable " + ident + " has not been declared yet!", Interpreter.EXIT_UNINITIALIZED_VAR_ERROR);
        }
        VariableInfo.VarType identType = symbolTable.get(ident).getType();
        //String identType = currentTable.get(ident);
        String exprType = exprType(expr);
        //VariableInfo.VarType exType;
        //update ident's val
        VariableInfo newVal = getExprValue(expr);
        if ((identType.name().contains("Int") && exprType.contains("Float"))  || (identType.name().contains("Float") && exprType.contains("Int"))) {
            //System.out.println(newVal.getType());
            Interpreter.fatalError("Invalid assignment!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        if (exprType.contains("Int")){
            //exType = VariableInfo.VarType.INT;
            newVal = newVal.copyWithIdentInt(ident);
            symbolTable.put(ident,newVal);
            //System.out.println(ident+": "+newVal.getIntValue());
        }
        else{
            //exType = VariableInfo.VarType.FLOAT;
            newVal = newVal.copyWithIdentFloat(ident);
            symbolTable.put(ident,newVal);
            //System.out.println(ident+": "+newVal.getFloatValue());
        }
        return ident;
    
    }  
        

    //BlockStmt
    public void checkBlockStmt(UnitList ul){
        //SymbolTable previousTable = this.currentTable; 
        //this.currentTable = new SymbolTable(previousTable); 
        this.checkUnitList(ul);
        //this.currentTable = previousTable;
    }

    //IfStmt
    public void checkIfStmt(IfStmt is){ 
        /*checkStmt(is.thenstmt);

        if (is.elsestmt != null)
            checkStmt(is.elsestmt);*/
        
        if(condExpr(is.expr) == true){
            checkStmt(is.thenstmt);
        }
        else{
            if(is.elsestmt != null){
                this.checkStmt(is.elsestmt);
            }
        }
        // merge part
        /*if(is.elsestmt != null){
            System.out.println("merge");
            if (is.thenstmt instanceof BlockStmt ){ 
                System.out.println("merge");
                merge(is.thenstmt,is.elsestmt); //do plus() to if'expr and else's expr
            }
            
        }*/
    
    }

    public void merge(Stmt thenstmt, Stmt elsestmt){
        AssignStmt thenSt = (AssignStmt) thenstmt;
        AssignStmt elseSt = (AssignStmt) elsestmt;

        if (thenSt.ident == elseSt.ident){
            VariableInfo.VarType identType = symbolTable.get(thenSt.ident).getType();
            VariableInfo expr1Info = getExprValue(thenSt.expr);
            VariableInfo expr2Info = getExprValue(elseSt.expr);
            
            String resType = plus(expr1Info.getType().name(),expr2Info.getType().name());
            if (identType.name().contains("Int")){
                if (resType.equals("Pos"))
                    symbolTable.put(thenSt.ident, new VariableInfo(thenSt.ident,VariableInfo.VarType.PosInt));
                else if (resType.equals("Neg"))
                    symbolTable.put(thenSt.ident, new VariableInfo(thenSt.ident,VariableInfo.VarType.NegInt));
                else if (resType.equals("Zero"))
                    symbolTable.put(thenSt.ident, new VariableInfo(thenSt.ident,VariableInfo.VarType.ZeroInt));
                else
                    symbolTable.put(thenSt.ident, new VariableInfo(thenSt.ident,VariableInfo.VarType.AnyInt));
            }
            else{
                if (resType.equals("Pos"))
                    symbolTable.put(thenSt.ident, new VariableInfo(thenSt.ident,VariableInfo.VarType.PosFloat));
                else if (resType.equals("Neg"))
                    symbolTable.put(thenSt.ident, new VariableInfo(thenSt.ident,VariableInfo.VarType.NegFloat));
                else if (resType.equals("Zero"))
                    symbolTable.put(thenSt.ident, new VariableInfo(thenSt.ident,VariableInfo.VarType.ZeroFloat));
                else
                    symbolTable.put(thenSt.ident, new VariableInfo(thenSt.ident,VariableInfo.VarType.AnyFloat));
            }
                
        }
    }
    
    //PrintStmt
    //set { NegInt,ZeroInt, PosInt, AnyInt, NegFloat, ZeroFloat, PosFloat, AnyFloat}
    public void checkPrintStmt(Expr expr){
        String type = exprType(expr);
        //System.out.println(expr);
        VariableInfo val = getExprValue(expr);
        if(type.contains("Int") && !(expr instanceof ReadIntExpr)) {
            /*if (val.getIntValue() > 0)
                System.out.println("PosInt");
            else if (val.getIntValue() < 0)
                System.out.println("NegInt");
            else
                System.out.println("ZeroInt");*/
            System.out.println(val.getType());
        }
        if(expr instanceof ReadIntExpr) {
            System.out.println("AnyInt");
            
            //else Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
        }
        if(type.contains("Float") && !(expr instanceof ReadFloatExpr)) {
            System.out.println(val.getType());
            /*if (val.getFloatValue() > 0)
                System.out.println("PosFloat");
            else if (val.getFloatValue() < 0)
                System.out.println("NegFloat");
            else
                System.out.println("ZeroFloat");*/
        }
        if(expr instanceof ReadFloatExpr) {
            System.out.println("AnyFloat");
  
            //else Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
        }
    }

    //WhileStmt
    public void checkWhileStmt(WhileStmt ws){
        while(condExpr(ws.expr))
            checkStmt(ws.body);
    }
    


}
