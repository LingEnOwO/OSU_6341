package ast;
import java.io.PrintStream;
import java.util.HashMap;

import ast.BlockStmt;
import ast.IdentExpr;
import ast.IfStmt;
import ast.LogicalExpr;
import ast.WhileStmt;
import interpreter.Interpreter;

public class TypeCheck {
    // A symbol table to track declared variables, types
    //private HashMap<String, String> symbolTable;
    private SymbolTable currentTable;

    // Constructor to initialize the symbol table
    public TypeCheck() {
        //this.symbolTable = new HashMap<String, String>();
        this.currentTable = new SymbolTable(null);
    }
    

    // Method to check if a variable has been declared
    public boolean isDeclared(String ident) {
        //return this.symbolTable.containsKey(ident);
        return this.currentTable.lookup(ident);
    }
    
    //----declare-----  Decl, VarDecl, IntVarDecl, FloatVarDecl
    public void checkDecl(Decl decl){
        String exprType = exprType(decl.expr);
        String decltype = checkVarDecl(decl.varDecl);
        if (!exprType.equals("null")  && exprType != decltype){
            Interpreter.fatalError(decl.varDecl.ident + " is invalid declaration!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
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
            //this.symbolTable.put(var.ident, "int");
            this.currentTable.put(var.ident, "int");
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
            //this.symbolTable.put(var.ident, "float");
            this.currentTable.put(var.ident, "float");
            // for debugging
            //System.out.println("Declared variable: " + var.ident);
            return "float";
        }
    }

    //------Expr-------- IdentExpr, IntConstExpr, FloatConstExpr, exprType, PlusExpr
    public String checkIdentExpr(IdentExpr idExpr){
        //Check idExpr is int or float => call table's type
        //String exprType = symbolTable.get(idExpr.ident);
        String exprType = currentTable.get(idExpr.ident);
        //System.out.println(idExpr.ident+": "+exprType);
        if (exprType == "null"){
            Interpreter.fatalError("Variable " + idExpr.ident + " has not been declared yet!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        if (exprType.equals("int")) {
            return "int";
        } 
        else if (exprType.equals("float")) {
            return "float";
        } 
        else {
            return "null";
        }

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
            return checkBinaryExpr(binaryExpr.expr1,binaryExpr.expr2);
        }
        if(expr instanceof UnaryMinusExpr){
            UnaryMinusExpr unaryMinusExpr = (UnaryMinusExpr) expr;
            return checkUnaryMinusExpr(unaryMinusExpr.expr);
        }
        return "null";
    }
    // PlusExpr both side should be the same type
    public String checkBinaryExpr(Expr expr1, Expr expr2){
        String ex1 = exprType(expr1);
        String ex2 = exprType(expr2);
        
        if(ex1.equals("int") && ex2.equals("int")){
            return "int";
        }
        if(ex1.equals("float") && ex2.equals("float")){
            return "float";
        }
        if (!ex1.equals(ex2) && (!ex1.equals("null") && !ex2.equals("null"))){
            //System.out.println(ex1);
            //System.out.println(ex2);
            Interpreter.fatalError(expr1+" and "+expr2+ " are two different types. The binary expression is invalid!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        return "null";
    }
    //UnaryMinusExpr
    public String checkUnaryMinusExpr(Expr expr){
        return exprType(expr);
    }

    //----CondExpr------------ CompExpr, LogicalExpr
    public void condExpr(CondExpr expr){
        if (expr instanceof LogicalExpr){
            LogicalExpr logicalExpr = (LogicalExpr) expr;
            checkLogicalExpr(logicalExpr.expr1,logicalExpr.expr2);
        }
        if(expr instanceof CompExpr){
            CompExpr compExpr = (CompExpr) expr;
            checkCompExpr(compExpr.expr1, compExpr.expr2);
        }
        //return "null";
    }
    public String checkCompExpr(Expr expr1, Expr expr2){
        String ex1 = exprType(expr1);
        String ex2 = exprType(expr2);
        //System.out.println(ex1);
        if(ex1.equals("int") && ex2.equals("int")){
            return "int";
        }
        if(ex1.equals("float") && ex2.equals("float")){
            return "float";
        }
        if (!ex1.equals(ex2) && (!ex1.equals("null") && !ex2.equals("null"))){
            //System.out.println(ex1);
            Interpreter.fatalError(expr1+" and "+expr2+ " are two different types. The comparison expression is invalid!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        return "null";
    }

    public void checkLogicalExpr(CondExpr expr1, CondExpr expr2){
        condExpr(expr1);
        condExpr(expr2);
        
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
        if(currentTable.get(ident) == "null"){
        //if(!isDeclared(ident)){
            //System.out.println("assignment");
            Interpreter.fatalError("Variable " + ident + " has not been declared yet!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        //String identType = symbolTable.get(ident);
        String identType = currentTable.get(ident);
        String exprType = exprType(expr);
        if(!identType.equals(exprType) ){
            //System.out.println(exprType);
            Interpreter.fatalError("Invalid assignment!", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }

    }

    //BlockStmt
    public void checkBlockStmt(UnitList ul){
        SymbolTable previousTable = this.currentTable; 
        this.currentTable = new SymbolTable(previousTable); 
        this.checkUnitList(ul);
        this.currentTable = previousTable;
    }

    //IfStmt
    public void checkIfStmt(IfStmt is){ 
        condExpr(is.expr);
        checkStmt(is.thenstmt);
        if(is.elsestmt != null){
            this.checkStmt(is.elsestmt);
        }
    }
    
    //PrintStmt
    public void checkPrintStmt(Expr expr){}

    //WhileStmt
    public void checkWhileStmt(WhileStmt ws){
        condExpr(ws.expr);
        checkStmt(ws.body);
    }
    


}
