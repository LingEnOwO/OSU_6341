package ast;
import java.io.PrintStream;
import java.util.HashMap;
import interpreter.Interpreter;
import ast.VariableInfo.VarType;

public class TypeCheck {
    // A symbol table to track declared variables, types
    private HashMap<String, String> symbolTable;

    // Constructor to initialize the symbol table
    public TypeCheck() {
        this.symbolTable = new HashMap<String, String>();
    }
    
    /* Method to retrieve the type of a variable
    public String getType(String ident) {
        String type = symbolTable.get(ident);
        return type.getType();
    }

    Method to retrieve the value of a variable
    public Integer getIntValue(String ident) {
        VariableInfo intVar = symbolTable.get(ident);
        return intVar.getIntValue();
    }

    public Double getFloatValue(String ident) {
        VariableInfo floatVar = symbolTable.get(ident);
        return floatVar.getFloatValue();
    }
    

    Method to update the value of a variable
    public void updateValue(String ident, Object value) {
        VariableInfo info = symbolTable.get(ident);
        if (info != null) {
            info.setValue(value);
        } else {
            throw new RuntimeException("Variable " + ident + " not declared.");
        }
    }*/

    // Method to check if a variable has been declared
    public boolean isDeclared(String ident) {
        return this.symbolTable.containsKey(ident);
    }
    
    //----declare-----  Decl, VarDecl, IntVarDecl, FloatVarDecl
    public void checkDecl(Decl decl){
        String exprType = exprType(decl.expr);
        String decltype = checkVarDecl(decl.varDecl);
        if (!exprType.equals("null")  && exprType != decltype){
            Interpreter.fatalError(decl.varDecl.ident + " is invalid declaration", Interpreter.EXIT_STATIC_CHECKING_ERROR);
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
            Interpreter.fatalError("Variable " + var.ident + " has already been declared ", Interpreter.EXIT_STATIC_CHECKING_ERROR);
            return "null";
        }
        else{
            this.symbolTable.put(var.ident, "int");
            // for debugging
            System.out.println("Declared variable: " + var.ident);
            return "int";
        }
        
    }

    public String checkFloatVarDecl(FloatVarDecl var){
        if(isDeclared(var.ident)){
            //Error message: "already declared"
            Interpreter.fatalError("Variable " + var.ident + " has already been declared ", Interpreter.EXIT_STATIC_CHECKING_ERROR);
            return "null";
        }
        else{
            this.symbolTable.put(var.ident, "float");
            // for debugging
            System.out.println("Declared variable: " + var.ident);
            return "float";
        }
    }

    /*  Method to declare a variable with an initial integer value
    public void declareVariable(String ident, Integer intVal) {
        symbolTable.put(ident, new VariableInfo(VariableInfo.VarType.INT, intVal));
    }

    // Method to declare a variable with an initial float value
    public void declareVariable(String ident, Double floatVal) {
        symbolTable.put(ident, new VariableInfo(VariableInfo.VarType.FLOAT, floatVal));
    }*/


    //------Expr-------- Expr, IdentExpr, IntConstExpr, FloatConstExpr, PlusExpr
    public void checkExpr(Expr expr){
        if(expr instanceof IdentExpr){
            //call Ident
        }
        else if (expr instanceof IntConstExpr){
            //call intConst
        }
        else if(expr instanceof FloatConstExpr){
            //call FloatConst
        }
        else if(expr instanceof PlusExpr){
            //call PlusConst
        }
        else{
            // null?
        }
    }

    public String checkIdentExpr(IdentExpr idExpr){
        //Check idExpr is int or float => call table's type
        String exprType = symbolTable.get(idExpr.ident);
        if (exprType == null){
            Interpreter.fatalError("Variable " + idExpr.ident + " has not been declared yet", Interpreter.EXIT_STATIC_CHECKING_ERROR);
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

    //Retrieve Expr's type
    //public boolean ()
    // PlusExpr both side should be the same type
    public String exprType(Expr expr){
        if(expr instanceof IntConstExpr){
            return "int";
        }
        if(expr instanceof FloatConstExpr){
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
        if(expr instanceof PlusExpr){
            PlusExpr plusExpr = (PlusExpr) expr;
            checkPlusExpr(plusExpr.expr1,plusExpr.expr2);
        }
        /*if(expr instanceof Expr){
            exprType(expr);
        }*/
        return "null";//?? not sure it is right
    }
    public void checkPlusExpr(Expr expr1, Expr expr2){
        if (this.exprType(expr1) != this.exprType(expr2)){
            Interpreter.fatalError("You cannot add two different type variables", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
    }    
    
    //uniList
    public void checkUnitList(UnitList unitList){
        checkUnit(unitList.unit);
        if(unitList.unitList != null){
            checkUnitList(unitList.unitList);
        }
    }
    // unit
    public void checkUnit(Unit unit){
        if (unit instanceof AssignStmt){
            AssignStmt assignStmt = (AssignStmt) unit;
            checkAssignment(assignStmt.ident, assignStmt.expr);
        }
        else if(unit instanceof Decl){
            Decl decl = (Decl) unit;
            checkDecl(decl);
        }
    }

    //Stmt
    public void checkStmt(AssignStmt assignStmt){
        checkAssignment(assignStmt.ident, assignStmt.expr);
    }

    //Assignment
    public void checkAssignment(String ident, Expr expr){
        if(!isDeclared(ident)){
            System.out.println("assignment");
            Interpreter.fatalError("Variable " + ident + " has not been declared yet", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }
        String identType = symbolTable.get(ident);
        String exprType = exprType(expr);
        if(!identType.equals(exprType)){
            Interpreter.fatalError("Invalid assignment", Interpreter.EXIT_STATIC_CHECKING_ERROR);
        }

    }
    // printStmt
    public void checkPrintStmt(Expr expr){}

}
