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
        if (!exprType.equals("null")  && exprType != declType){
            Interpreter.fatalError(decl.varDecl.ident + " is invalid declaration!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        if (declType.equals("int") && exprType.equals("int")){
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
        if (declType.equals("float") && !exprType.equals("null")){
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
            this.symbolTable.put(var.ident, new VariableInfo(var.ident,VariableInfo.VarType.INT));
            // for debugging
            //System.out.println("Declared variable: " + var.ident);
            return "int";
        }
        
    }

    public String checkFloatVarDecl(FloatVarDecl var){
        if(isDeclared(var.ident)){
            //Error message: "already declared"
            Interpreter.fatalError("Variable " + var.ident + " has already been declared!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
            return "null";
        }
        else{
            this.symbolTable.put(var.ident, new VariableInfo(var.ident,VariableInfo.VarType.FLOAT));
            // for debugging
            //System.out.println("Declared variable: " + var.ident);
            return "float";
        }
    }

    //------Expr-------- ExprValue, IdentExpr, IntConstExpr, FloatConstExpr, exprType, BinaryExpr, DivByZero, UnaryMinusExpr
    public VariableInfo getExprValue(Expr expr) {
        if (expr instanceof IntConstExpr) {
            IntConstExpr intConstExpr = (IntConstExpr) expr;
            return new VariableInfo(null, VariableInfo.VarType.INT, intConstExpr.ival);
        }
        if (expr instanceof FloatConstExpr) {
            FloatConstExpr floatConstExpr = (FloatConstExpr) expr;
            return new VariableInfo(null, VariableInfo.VarType.FLOAT, floatConstExpr.fval);
        }
        if (expr instanceof IdentExpr) {
            IdentExpr identExpr = (IdentExpr) expr;
            VariableInfo varInfo = this.symbolTable.get(identExpr.ident);
            if (varInfo.getIntValue() == null && varInfo.getFloatValue() == null) {
                return null;
            }
            return varInfo;
        }
        if (expr instanceof BinaryExpr) {
            BinaryExpr binaryExpr = (BinaryExpr) expr;
            VariableInfo expr1 = getExprValue(binaryExpr.expr1);
            if (binaryExpr.expr1 instanceof ReadIntExpr || binaryExpr.expr1 instanceof ReadFloatExpr) expr1 = values.poll();
            VariableInfo expr2 = getExprValue(binaryExpr.expr2);
            if (binaryExpr.expr2 instanceof ReadIntExpr || binaryExpr.expr2 instanceof ReadFloatExpr) expr2 = values.poll();
            if (binaryExpr.op == 1){
                
                if (expr1.getType() == VariableInfo.VarType.INT && expr2.getType() == VariableInfo.VarType.INT) {
                    //System.out.println("here");
                    return new VariableInfo(null, VariableInfo.VarType.INT, expr1.getIntValue() + expr2.getIntValue());
                }
                if (expr1.getType() == VariableInfo.VarType.FLOAT && expr2.getType() == VariableInfo.VarType.FLOAT) {
                    return new VariableInfo(null, VariableInfo.VarType.FLOAT, expr1.getFloatValue() + expr2.getFloatValue());
                }
                //Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
            }
            else if (binaryExpr.op == 2){
                if (expr1.getType() == VariableInfo.VarType.INT && expr2.getType() == VariableInfo.VarType.INT) {
                    return new VariableInfo(null, VariableInfo.VarType.INT, expr1.getIntValue() - expr2.getIntValue());
                }
                if (expr1.getType() == VariableInfo.VarType.FLOAT && expr2.getType() == VariableInfo.VarType.FLOAT) {
                    return new VariableInfo(null, VariableInfo.VarType.FLOAT, expr1.getFloatValue() - expr2.getFloatValue());
                }
                //Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
            }
            else if (binaryExpr.op == 3){
                if (expr1.getType() == VariableInfo.VarType.INT && expr2.getType() == VariableInfo.VarType.INT) {
                    return new VariableInfo(null, VariableInfo.VarType.INT, expr1.getIntValue() * expr2.getIntValue());
                }
                if (expr1.getType() == VariableInfo.VarType.FLOAT && expr2.getType() == VariableInfo.VarType.FLOAT) {
                    return new VariableInfo(null, VariableInfo.VarType.FLOAT, expr1.getFloatValue() * expr2.getFloatValue());
                }
                //Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
            }
            else if (binaryExpr.op == 4){
                if (expr1.getType() == VariableInfo.VarType.INT && expr2.getType() == VariableInfo.VarType.INT) {
                    return new VariableInfo(null, VariableInfo.VarType.INT, expr1.getIntValue() / expr2.getIntValue());
                }
                if (expr1.getType() == VariableInfo.VarType.FLOAT && expr2.getType() == VariableInfo.VarType.FLOAT) {
                    return new VariableInfo(null, VariableInfo.VarType.FLOAT, expr1.getFloatValue() / expr2.getFloatValue());
                }
                //Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
            }
        }
        if (expr instanceof UnaryMinusExpr){
            UnaryMinusExpr unaryMinusExpr = (UnaryMinusExpr) expr;
            if(unaryMinusExpr.expr instanceof IntConstExpr){
                IntConstExpr intConstExpr = (IntConstExpr) unaryMinusExpr.expr;
                return new VariableInfo(null, VariableInfo.VarType.INT, intConstExpr.ival*(-1));
            }
            if(unaryMinusExpr.expr instanceof FloatConstExpr){
                FloatConstExpr floatConstExpr = (FloatConstExpr) unaryMinusExpr.expr;
                return new VariableInfo(null, VariableInfo.VarType.FLOAT, floatConstExpr.fval*(-1.0));
            }
            if(unaryMinusExpr.expr instanceof IdentExpr){
                IdentExpr identExpr = (IdentExpr) unaryMinusExpr.expr;
                if (symbolTable.get(identExpr.ident).getType() == VariableInfo.VarType.INT) return new VariableInfo(null, VariableInfo.VarType.INT, symbolTable.get(identExpr.ident).getIntValue()*(-1));
                else return new VariableInfo(null, VariableInfo.VarType.FLOAT, symbolTable.get(identExpr.ident).getFloatValue()*(-1.0));
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
        //Check idExpr is int or float => call table's type
        VariableInfo exprType = symbolTable.get(idExpr.ident);
        //String exprType = currentTable.get(idExpr.ident);
        //System.out.println(idExpr.ident+": "+exprType);
        if (exprType == null){
            Interpreter.fatalError("Variable " + idExpr.ident + " has not been declared yet!", Interpreter.EXIT_UNINITIALIZED_VAR_ERROR);
        }
        else if (exprType.getType() == VariableInfo.VarType.INT){
            return "int";
        } 
        else if (exprType.getType() == VariableInfo.VarType.FLOAT) {
            return "float";
        } 
        
        return "null";
        

    }

    public boolean checkIntConstExpr(IntConstExpr intConstExpr){
        return true;
    }

    public boolean checkFloatConstExpr(FloatConstExpr floatConstExpr){
        return true;
    }

    public boolean checkReadIntExpr(ReadIntExpr readIntExpr){
        return true;
    }

    public boolean checkReadFloatExpr(ReadFloatExpr readFloatExpr){
        return true;
    }

    //Retrieve Expr's type
    public String exprType(Expr expr){
        if(expr instanceof IntConstExpr || expr instanceof ReadIntExpr){
            return "int";
        }
        if(expr instanceof FloatConstExpr || expr instanceof ReadFloatExpr){
            return "float";
        }
        if(expr instanceof IdentExpr){
            IdentExpr identExpr = (IdentExpr) expr;
            if (this.checkIdentExpr(identExpr) == "int"){
                return "int";
            }
            else{
                return "float";
            }
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
        //checkDivByZero(op,expr2);
        if(ex1.equals("int") && ex2.equals("int")){
            checkDivByZero(op,expr2);
            return "int";
        }
        if(ex1.equals("float") && ex2.equals("float")){
            checkDivByZero(op,expr2);
            return "float";
        }
        if (!ex1.equals(ex2) && (!ex1.equals("null") && !ex2.equals("null"))){
            //System.out.println(ex1);
            //System.out.println(ex2);
            Interpreter.fatalError(expr1+" and "+expr2+ " are two different types. The binary expression is invalid!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        
        return "null";
    }

    public void checkDivByZero(int op, Expr expr){
        if (op == 4 && expr instanceof IntConstExpr){
            IntConstExpr intExpr = (IntConstExpr) expr;
            if (intExpr.ival == 0) {
                Interpreter.fatalError("It's pointless to devide 0",Interpreter.EXIT_DIV_BY_ZERO_ERROR);
            }
        }
        if (op == 4 && expr instanceof FloatConstExpr){
            FloatConstExpr floatExpr = (FloatConstExpr) expr;
            if (floatExpr.fval == 0.0) {
                Interpreter.fatalError("It's pointless to devide 0",Interpreter.EXIT_DIV_BY_ZERO_ERROR);
            }
        }
        if (op == 4 && expr instanceof IdentExpr){
            IdentExpr identExpr = (IdentExpr) expr;
            //System.out.println(op+", "+ identExpr.ident);
            if (symbolTable.get(identExpr.ident).getType() == VariableInfo.VarType.INT && symbolTable.get(identExpr.ident).getIntValue() == 0){
                Interpreter.fatalError("It's pointless to devide 0",Interpreter.EXIT_DIV_BY_ZERO_ERROR);
            }
            if (symbolTable.get(identExpr.ident).getType() == VariableInfo.VarType.FLOAT && symbolTable.get(identExpr.ident).getFloatValue() == 0.0) {
                Interpreter.fatalError("It's pointless to devide 0",Interpreter.EXIT_DIV_BY_ZERO_ERROR);
            }
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
        if (!ex1.equals(ex2) && (!ex1.equals("null") && !ex2.equals("null"))){
            //System.out.println(ex1);
            Interpreter.fatalError(expr1+" and "+expr2+ " are two different types. The comparison expression is invalid!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        if(ex1.equals("int") && ex2.equals("int")){
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
        if(ex1.equals("float") && ex2.equals("float")){
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
    public void checkAssignStmt(String ident, Expr expr){
        //if(currentTable.get(ident) == "null"){
        if(!isDeclared(ident)){
            //System.out.println("assignment");
            Interpreter.fatalError("Variable " + ident + " has not been declared yet!", Interpreter.EXIT_UNINITIALIZED_VAR_ERROR);
        }
        VariableInfo.VarType identType = symbolTable.get(ident).getType();
        //String identType = currentTable.get(ident);
        String exprType = exprType(expr);
        VariableInfo.VarType exType;
        //update ident's val
        VariableInfo newVal = getExprValue(expr);
        if (exprType.equals("int")){
            exType = VariableInfo.VarType.INT;
            newVal = newVal.copyWithIdentInt(ident);
            symbolTable.put(ident,newVal);
            //System.out.println(ident+": "+newVal.getIntValue());
        }
        else{
            exType = VariableInfo.VarType.FLOAT;
            newVal = newVal.copyWithIdentFloat(ident);
            symbolTable.put(ident,newVal);
            //System.out.println(ident+": "+newVal.getFloatValue());
        }
        if(identType != exType){
            //System.out.println(exType);
            Interpreter.fatalError("Invalid assignment!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
    
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
        if(condExpr(is.expr) == true){
            checkStmt(is.thenstmt);
        }
        else{
            if(is.elsestmt != null){
                this.checkStmt(is.elsestmt);
            }
        }
        
    }
    
    //PrintStmt
    public void checkPrintStmt(Expr expr){
        String type = exprType(expr);
        //System.out.println(expr);
        VariableInfo val = getExprValue(expr);
        if(type == "int" && !(expr instanceof ReadIntExpr)) System.out.println(val.getIntValue());
        if(expr instanceof ReadIntExpr) {
            if (values.peek().getType() == VariableInfo.VarType.INT) System.out.println(values.poll().getIntValue());
            //else Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
        }
        if(type == "float" && !(expr instanceof ReadFloatExpr)) System.out.println(val.getFloatValue());
        if(expr instanceof ReadFloatExpr) {
            if (values.peek().getType() == VariableInfo.VarType.FLOAT) System.out.println(values.poll().getFloatValue());
            //else Interpreter.fatalError("Failed to read from stdin", Interpreter.EXIT_FAILED_STDIN_READ);
        }
    }

    //WhileStmt
    public void checkWhileStmt(WhileStmt ws){
        while(condExpr(ws.expr))
            checkStmt(ws.body);
    }
    


}
