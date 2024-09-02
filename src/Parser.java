import jdk.nashorn.internal.ir.Block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

public class Parser {
    private final ArrayList<SymTab> symTab;
    private ArrayList<String> out;
    private ArrayList<String> error;
    private ArrayList<Quaternion> IR;
    private ArrayList<Quaternion> IR_real;
    private ArrayList<Quaternion> data;
    private int t = 0;
    private int col_var = 0;
    private int col_tmp = 0;
    private int col_str = 0;
    private int col_func = 0;
    private boolean isGlobal = true;
    private boolean isMain = false;
    private boolean isFunc = false;
    private boolean isVoidFunc = false;
    private boolean isIntFunc = false;
    private boolean haveReturn = false;
    private boolean isAssign = false;
    private ArrayList<SymbolTable> symbolTables = new ArrayList<>();
    private SymbolTable currentSymbolTable;
    private int num_st = 0;
    private int cycleDepth = 0;

    public Parser(ArrayList<SymTab> symTabs) {
        symTab = symTabs;
        out = new ArrayList<>();
        IR = new ArrayList<>();
        IR_real = new ArrayList<>();
        data = new ArrayList<>();
        error = new ArrayList<>();
    }

    public ArrayList<String> getError() {
        return error;
    }

    public String findVar(String name) {
        ////// ////////System.out.println("??? "+name);
        for (int i = IR.size() - 1; i >= 0; i--) {
            if (IR.get(i).getSyxtaxKind().equals("VAR") || IR.get(i).getSyxtaxKind().equals("PARA")) {
                if (IR.get(i).getArg2() != null) {
                    if (IR.get(i).getArg2().equals(name)) {
                        return IR.get(i).getResult();
                    }
                }
            } else if (IR.get(i).getSyxtaxKind().equals("FUNC") || IR.get(i).getSyxtaxKind().equals("MAINFUNC")) {
                break;
            }
        }
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getSyxtaxKind().equals("VAR")) {
                if (data.get(i).getOp() != null) {
                    if (data.get(i).getArg2().equals(name)) {
                        //IR.add(new Quaternion("LOADWORD",null,null,data.get(i).getResult(),"tmp_"+col_tmp));
                        //col_tmp++;
                        return IR.get(IR.size() - 1).getResult();
                    }
                }
            }
        }
        return name;
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public void analyse() {
        for (int i = 0; i < symTab.size(); i++) {
            ////////System.out.println(symTab.get(i).getIdCode() + " " + symTab.get(i).getName());
        }
        CompUnit();
        //IR.remove(IR.size()-1);
        //IR.remove(IR.size()-1);
        for (int i = 0; i < data.size(); i++) {
            //// ////////System.out.println(data.get(i).toString());
        }
        //// ////////System.out.println("-------------------");
        for (int i = 0; i < IR.size(); i++) {
            if (!IR.get(i).getSyxtaxKind().equals("NUMBER") &&
                    !IR.get(i).getSyxtaxKind().equals("LVAL")) {
                IR_real.add(IR.get(i));
                ////// ////////System.out.println(IR.get(i).toString());
            }
        }
        for (int i = 0; i < error.size() - 1; i++) {
            ////System.out.println("SWAP " + error.get(i).split(" ")[0] + " " + error.get(i).split(" ")[1].charAt(0)
                    //+ " " + error.get(i + 1).split(" ")[1].charAt(0));
            for (int k = 0; k < error.size() - 1 - i; k++) {
                if (Integer.valueOf(error.get(k).split(" ")[0]) > Integer.valueOf(error.get(k + 1).split(" ")[0]) ||
                        (Objects.equals(error.get(k).split(" ")[0],error.get(k + 1).split(" ")[0]) &&
                                error.get(k).split(" ")[1].charAt(0) > error.get(k + 1).split(" ")[1].charAt(0))) {
                    /*//System.out.println("swap "+error.get(k).split(" ")[0]+ " "
                    +error.get(k).split(" ")[1].charAt(0)+" " +error.get(k+1).split(" ")[0]+ " "
                                    +error.get(k+1).split(" ")[1].charAt(0)
                            );*/
                    Collections.swap(error, k, k + 1);
                }
            }
        }
    }

    public ArrayList<String> getOut() {
        return out;
    }

    private void addToken(String idcode) {
        // //System.out.print(symTab.get(t).getIdCode() + " " + idcode + " " + symTab.get(t).getName() + " ");
        if (!Objects.equals(symTab.get(t).getIdCode(), idcode)) {
            //System.out.print(t);
            //// ////////System.out.println("  !!!!!  ");
        } else {
            ////// ////////System.out.println(t);
        }
        if (Objects.equals(symTab.get(t).getIdCode(), idcode)) {
            out.add(symTab.get(t).getIdCode() + " " + symTab.get(t).getName());
            t++;
        } else {

            if (Objects.equals(idcode, "SEMICN")) {
                //////System.out.println("???!! " + symTab.get(t).getN() + " " + symTab.get(t).getName() + " " + idcode);
                symTab.add(t, new SymTab(";", "SEMICN", symTab.get(t - 1).getN()));
                error.add(symTab.get(t - 1).getN() + " i");
            } else if (Objects.equals(idcode, "RPARENT")) {
                symTab.add(t, new SymTab(")", "RPAREN", symTab.get(t - 1).getN()));
                //////System.out.println("???&& " + symTab.get(t).getName());
                error.add(symTab.get(t - 1).getN() + " j");
            } else if (Objects.equals(idcode, "RBRACK")) {
                symTab.add(t, new SymTab("]", "RBRACK", symTab.get(t - 1).getN()));
                error.add(symTab.get(t - 1).getN() + " k");
            }
            t++;
        }

    }

    private SymTab nowToken() {
        return symTab.get(t);
    }

    private void CompUnit() { //CompUnit → {Decl} {FuncDef} MainFuncDef
        symbolTables.add(new SymbolTable(num_st, -1));
        currentSymbolTable = symbolTables.get(num_st++);
        data.add(new Quaternion("STR", "=", "str_0", "str", "\\n"));
        col_str++;
        while (isDecl()) {
            Decl();
        }
        isGlobal = false;
        while (isFuncDef()) {
            isFunc = true;
            FuncDef();

            isFunc = false;
            isVoidFunc = false;
            isIntFunc = false;
        }
        isMain = true;
        MainFuncDef();
        out.add("<CompUnit>");
    }

    private boolean isDecl() {
        if (Objects.equals(symTab.get(t).getIdCode(), "CONSTTK")) {
            return true;
        } else if (Objects.equals(symTab.get(t).getIdCode(), "INTTK")) {
            if (Objects.equals(symTab.get(t + 2).getIdCode(), "LPARENT")) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean isFuncDef() {
        //////System.out.println("MIAN? " +symTab.get(t + 1).getIdCode()+" "+symTab.get(t + 1).getName()+" "+symTab.get(t + 1).getN());
        if (Objects.equals(symTab.get(t + 1).getIdCode(), "MAINTK")) {
            return false;
        } else {
            return true;
        }
    }

    private void Decl() { //Decl → ConstDecl | VarDecl
        if (Objects.equals(symTab.get(t).getIdCode(), "CONSTTK")) {
            //////// ////////System.out.println("???ConstDecl");
            ConstDecl();
            //////// ////////System.out.println("???ConstDecl END");
        } else {
            ////// ////////System.out.println("???VarDecl");
            VarDecl();
            ////// ////////System.out.println("???VarDecl end");
        }
        //out.add("<Decl>");
    }

    private void ConstDecl() { //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        addToken("CONSTTK");
        //////System.out.println("1");
        BType();
        ConstDef();
        while (Objects.equals(symTab.get(t).getIdCode(), "COMMA")) {
            addToken("COMMA");
            ConstDef();
        }
        addToken("SEMICN");
        out.add("<ConstDecl>");
    }

    private void BType() { //BType → 'int'
        //////System.out.println("BType()");
        addToken("INTTK");
        //out.add("<BType>");
    }

    private void ConstDef() { //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        String op, arg1, arg2, result;
        arg1 = nowToken().getName();
        int line = nowToken().getN();
        addToken("IDENFR");
        int dim = 0;
        while (Objects.equals(symTab.get(t).getIdCode(), "LBRACK")) {
            addToken("LBRACK");
            ConstExp();
            addToken("RBRACK");
            dim++;
        }
        op = nowToken().getName();
        addToken("ASSIGN");
        ConstInitVal();
        arg2 = IR.get(IR.size() - 1).getResult();
        result = "int";
        data.add(new Quaternion("CONST", op, result, arg2, arg1));
        out.add("<ConstDef>");
        if (!currentSymbolTable.getSymboltable().containsKey(arg1)) {
            ////System.out.println("addSymbol " + arg1 + " " + dim);
            currentSymbolTable.addSymbol(arg1, new Symbol(arg1, dim, line, 0));
        } else {
            ////System.out.println("error b " + arg1 + " " + line);
            error.add(line + " b");
        }
    }

    private void ConstExp() { //ConstExp → AddExp
        AddExp();
        out.add("<ConstExp>");
    }

    private void AddExp() { //AddExp → MulExp | AddExp ('+' | '−') MulExp
        String op, arg1, arg2, result;
        MulExp();
        arg1 = IR.get(IR.size() - 1).getResult();
        out.add("<AddExp>");
        while (Objects.equals(symTab.get(t).getIdCode(), "PLUS") || Objects.equals(symTab.get(t).getIdCode(), "MINU")) {
            if (Objects.equals(symTab.get(t).getIdCode(), "PLUS")) {
                addToken("PLUS");
                op = "+";
            } else {
                addToken("MINU");
                op = "-";
            }
            MulExp();
            arg2 = IR.get(IR.size() - 1).getResult();
            result = "tmp_" + String.valueOf(col_tmp);
            col_tmp++;
            if (isInteger(arg1) && isInteger(arg2)) {
                int a1 = Integer.valueOf(arg1);
                int a2 = Integer.valueOf(arg2);
                if (op.equals("+")) {
                    IR.add(new Quaternion("NUMBER", null, null, null, String.valueOf(a1 + a2)));
                } else {
                    IR.add(new Quaternion("NUMBER", null, null, null, String.valueOf(a1 - a2)));
                }
            } else {
                boolean isLoad = false;
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).getSyxtaxKind().equals("VAR")) {
                        if (data.get(i).getArg2().equals(arg1) || data.get(i).getArg2().equals(arg2)) {
                            IR.add(new Quaternion("LOADWORD", null, null,data.get(i).getResult(), "tmp_" + col_tmp));
                            isLoad = true;

                        }
                    }
                }
                if (isLoad) {
                    if (op.equals("+")) {
                        IR.add(new Quaternion("ADD", op, arg1, arg2, "tmp_" + col_tmp));
                    } else {
                        IR.add(new Quaternion("SUB", op, arg1, arg2, "tmp_" + col_tmp));
                    }
                    col_tmp++;
                } else {
                    if (op.equals("+")) {
                        IR.add(new Quaternion("ADD", op, arg1, arg2, result));
                    } else {
                        IR.add(new Quaternion("SUB", op, arg1, arg2, result));
                    }
                }

            }
            arg1 = result;
            out.add("<AddExp>");
        }

    }

    private void MulExp() { //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
        String op, arg1, arg2, result;
        UnaryExp();
        arg1 = IR.get(IR.size() - 1).getResult();
        out.add("<MulExp>");
        while (Objects.equals(symTab.get(t).getIdCode(), "MULT") ||
                Objects.equals(symTab.get(t).getIdCode(), "DIV") ||
                Objects.equals(symTab.get(t).getIdCode(), "MOD")) {
            if (Objects.equals(symTab.get(t).getIdCode(), "MULT")) {
                addToken("MULT");
                op = "*";
            } else if (Objects.equals(symTab.get(t).getIdCode(), "DIV")) {
                addToken("DIV");
                op = "/";
            } else {
                addToken("MOD");
                op = "%";
            }
            UnaryExp();
            arg2 = IR.get(IR.size() - 1).getResult();
            result = "tmp_" + String.valueOf(col_tmp);
            col_tmp++;
            if (isInteger(arg1) && isInteger(arg2)) {
                int a1 = Integer.valueOf(arg1);
                int a2 = Integer.valueOf(arg2);
                if (op.equals("*")) {
                    IR.add(new Quaternion("NUMBER", null, null, null, String.valueOf(a1 * a2)));
                } else if (op.equals("/")) {
                    if(a2!=0) {
                        IR.add(new Quaternion("NUMBER", null, null, null, String.valueOf(a1 / a2)));
                    }
                } else {
                    if(a2!=0) {
                        IR.add(new Quaternion("NUMBER", null, null, null, String.valueOf(a1 % a2)));
                    }
                }
            } else {
                boolean isLoad = false;
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).getSyxtaxKind().equals("VAR")) {
                        if (data.get(i).getArg2().equals(arg1) || data.get(i).getArg2().equals(arg2)) {
                            IR.add(new Quaternion("LOADWORD", null, null,data.get(i).getResult(), "tmp_" + col_tmp));
                            isLoad = true;

                        }
                    }
                }
                if (isLoad) {
                    if (op.equals("*")) {
                        IR.add(new Quaternion("MUL", op, arg1, arg2, "tmp_" + col_tmp));
                    } else if (op.equals("/")) {
                        IR.add(new Quaternion("DIV", op, arg1, arg2, "tmp_" + col_tmp));
                    } else {
                        IR.add(new Quaternion("MOD", op, arg1, arg2, "tmp_" + col_tmp));
                    }
                    col_tmp++;
                } else {
                    if (op.equals("*")) {
                        IR.add(new Quaternion("MUL", op, arg1, arg2, result));
                    } else if (op.equals("/")) {
                        IR.add(new Quaternion("DIV", op, arg1, arg2, result));
                    } else {
                        IR.add(new Quaternion("MOD", op, arg1, arg2, result));
                    }
                }

            }
            arg1 = result;
            out.add("<MulExp>");
        }

    }

    /*
    UnaryExp → PrimaryExp
    | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖, 函数调用也需要覆盖FuncRParams的不同情况
     | UnaryOp UnaryExp // 存在即可
     */

    private void UnaryExp() {
        if (isPrimaryExp()) {
            ////// ////////System.out.println("1");
            PrimaryExp();
        } else if (Objects.equals(symTab.get(t).getIdCode(), "PLUS") ||
                Objects.equals(symTab.get(t).getIdCode(), "MINU") ||
                Objects.equals(symTab.get(t).getIdCode(), "NOT")) {
            String op, arg1, arg2, result;
            if (Objects.equals(symTab.get(t).getIdCode(), "PLUS")) {
                op = "+";
            } else if (Objects.equals(symTab.get(t).getIdCode(), "MINU")) {
                op = "-";
            } else {
                op = "!";
            }
            arg1 = "0";
            UnaryOp();
            UnaryExp();
            arg2 = IR.get(IR.size() - 1).getResult();
            result = "tmp_" + String.valueOf(col_tmp);
            col_tmp++;
            if (isInteger(arg1) && isInteger(arg2)) {
                int a2 = Integer.valueOf(arg2);
                if (op.equals("+")) {
                    IR.add(new Quaternion("NUMBER", null, null, null, String.valueOf(a2)));
                } else if (op.equals("-")) {
                    IR.add(new Quaternion("NUMBER", null, null, null, String.valueOf(-a2)));
                }
            } else {
                boolean isLoad = false;
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).getSyxtaxKind().equals("VAR")) {
                        if (data.get(i).getArg2().equals(arg1) || data.get(i).getArg2().equals(arg2)) {
                            IR.add(new Quaternion("LOADWORD", null, null,data.get(i).getResult(), "tmp_" + col_tmp));
                            isLoad = true;
                        }
                    }
                }
                if (isLoad) {
                    if (op.equals("+")) {
                        IR.add(new Quaternion("ADD", op, arg1, arg2, "tmp_" + col_tmp));
                    } else if (op.equals("-")) {
                        IR.add(new Quaternion("SUB", op, arg1, arg2, "tmp_" + col_tmp));
                    }
                    col_tmp++;
                } else {
                    if (op.equals("+")) {
                        IR.add(new Quaternion("ADD", op, arg1, arg2, result));
                    } else if (op.equals("-")) {
                        IR.add(new Quaternion("SUB", op, arg1, arg2, result));
                    }
                }
            }
        } else {
            ////// ////////System.out.println("3");
            String result = nowToken().getName();
            int line = nowToken().getN();
            SymbolTable tmpSymbolTable = currentSymbolTable;
            ////////System.out.println("???? "+currentSymbolTable.getParent());
            while (!tmpSymbolTable.getSymboltable().containsKey(result)) {
                if (tmpSymbolTable.getParent() != -1) {
                    tmpSymbolTable = symbolTables.get(tmpSymbolTable.getParent());
                } else {
                    error.add(line + " c");
                    break;
                }
            }
            String func_name = nowToken().getName();
            line = nowToken().getN();
            addToken("IDENFR");
            addToken("LPARENT");
            int para_n = 0;
            if (!Objects.equals(symTab.get(t).getIdCode(), "RPARENT")) {
                if (!Objects.equals(symTab.get(t).getIdCode(), "SEMICN")) {
                    para_n = FuncRParams(func_name, line);
                }
            }
            IR.add(new Quaternion("CALL", null, null, null, result));
            result = "tmp_" + String.valueOf(col_tmp);
            col_tmp++;
            IR.add(new Quaternion("ASSIGN", "=", "ret", null, result));
            addToken("RPARENT");
            tmpSymbolTable = currentSymbolTable;
            while (!tmpSymbolTable.getSymboltable().containsKey(func_name)) {
                if (tmpSymbolTable.getParent() != -1) {
                    tmpSymbolTable = symbolTables.get(tmpSymbolTable.getParent());
                } else {
                    break;
                }
            }
            if (tmpSymbolTable.getSymboltable().containsKey(func_name)) {
                if (tmpSymbolTable.getSymboltable().get(func_name).getPara_n() != para_n) {
                    ////System.out.println("?!?! " + func_name + " " +
                    //tmpSymbolTable.getSymboltable().get(func_name).getPara_n() + " " + para_n);
                    error.add(line + " d");
                }
            }
        }
        out.add("<UnaryExp>");
    }

    private boolean isPrimaryExp() {
        if (Objects.equals(symTab.get(t).getIdCode(), "IDENFR") &&
                Objects.equals(symTab.get(t + 1).getIdCode(), "LPARENT")) {
            return false;
        }
        if (Objects.equals(symTab.get(t).getIdCode(), "PLUS") ||
                Objects.equals(symTab.get(t).getIdCode(), "MINU") ||
                Objects.equals(symTab.get(t).getIdCode(), "NOT")) {
            return false;
        }
        return true;
    }

    private void UnaryOp() {
        if (Objects.equals(symTab.get(t).getIdCode(), "PLUS")) {
            addToken("PLUS");
        } else if (Objects.equals(symTab.get(t).getIdCode(), "MINU")) {
            addToken("MINU");
        } else {
            addToken("NOT");
        }
        out.add("<UnaryOp>");
    }

    private void PrimaryExp() { //PrimaryExp → '(' Exp ')' | LVal | Number
        if (Objects.equals(symTab.get(t).getIdCode(), "LPARENT")) {
            addToken("LPARENT");
            Exp();
            addToken("RPARENT");
        } else if (Objects.equals(symTab.get(t).getIdCode(), "INTCON")) {
            Number_();
        } else {
            LVal();
        }
        out.add("<PrimaryExp>");
    }

    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static boolean isNumber(String str) {
        return str != null && NUMBER_PATTERN.matcher(str).matches();
    }

    private void Number_() {
        IR.add(new Quaternion("NUMBER", null, null, null, nowToken().getName()));
        addToken("INTCON");
        out.add("<Number>");
    }

    private int FuncRParams(String func_name, int func_ident_n) {//Exp { ',' Exp }
        ////System.out.println("FuncRParams " + func_name + " " + func_ident_n);
        int para_n = 0;
        boolean error_e = false;
        boolean undefine = false;
        ArrayList<Integer> para_dim = new ArrayList<>();
        SymbolTable tmpSymbolTable = currentSymbolTable;
        while (!tmpSymbolTable.getSymboltable().containsKey(func_name)) {
            if (tmpSymbolTable.getParent() != -1) {
                tmpSymbolTable = symbolTables.get(tmpSymbolTable.getParent());
            } else {
                undefine = true;
                break;
            }
        }

        //System.out.println("^^^ " + func_name + " " + undefine + " " + tmpSymbolTable.getSymboltable().containsKey(func_name));
        if (!undefine) {
            para_dim = tmpSymbolTable.getSymboltable().get(func_name).getPara_dim();
        }
        int tt = t;
        Exp();
        ////System.out.println("new para");
        if (!undefine) {
            //错误处理-e
            int q = t - 1;
            //////System.out.println("??tt "+symTab.get(tt).getName());
            String para_name = null;
            while (q >= tt && !Objects.equals(symTab.get(q).getIdCode(), "COMMA")) {
                int dim1 = 0;
                int dim2 = 1;
                ////System.out.println("new part");
                while (Objects.equals(symTab.get(q).getIdCode(), "RBRACK") ||
                        Objects.equals(symTab.get(q).getIdCode(), "IDENFR")) {
                    dim1 = 0;
                    if (Objects.equals(symTab.get(q).getIdCode(), "RBRACK")) {
                        while (!Objects.equals(symTab.get(q).getIdCode(), "LBRACK")) {
                            q--;
                        }
                        q--;
                        dim1 = 1;
                        //////System.out.println("DIM 1: "+dim1+ " " + symTab.get(q).getName());
                        if (Objects.equals(symTab.get(q).getIdCode(), "RBRACK")) {
                            dim1 = 2;
                            while (!Objects.equals(symTab.get(q).getIdCode(), "LBRACK")) {
                                q--;
                            }
                            q--;
                        }
                    } else {
                        dim1 = 0;
                    }
                    ////System.out.println("DIM1: " + dim1 + " " + symTab.get(q).getName());
                    dim2 = 1;
                    if (Objects.equals(symTab.get(q).getIdCode(), "IDENFR")) {
                        String ident_name = symTab.get(q).getName();
                        ////////System.out.println("PARANAME "+ident_name);
                        tmpSymbolTable = currentSymbolTable;
                        while (!tmpSymbolTable.getSymboltable().containsKey(ident_name)) {
                            if (tmpSymbolTable.getParent() != -1) {
                                tmpSymbolTable = symbolTables.get(tmpSymbolTable.getParent());
                            } else {
                                break;
                            }
                        }
                        ////System.out.println("!@!@!@ " + ident_name + " " +
                        //tmpSymbolTable.getSymboltable().containsKey(ident_name));
                        if (tmpSymbolTable.getSymboltable().containsKey(ident_name)) {
                            dim2 = tmpSymbolTable.getSymboltable().get(ident_name).getDim();
                        } else {
                            dim2 = 0;
                        }
                        ////System.out.println("!@!@ " + ident_name + " " + dim2);
                    }
                    int dim = -dim1 + dim2;
                    if (para_n < para_dim.size()) {
                        if (dim != para_dim.get(para_n)) {
                            ////System.out.println("dim? " + func_name + " " + dim1 + " " + dim2 + " " + para_dim.get(para_n));
                            error_e = true;
                        }
                    }
                    q--;
                }
                if (Objects.equals(symTab.get(q).getIdCode(), "INTCON")) {
                    if (para_n < para_dim.size()) {
                        if (para_dim.get(para_n) != 0) {
                            ////System.out.println("dim? intcon " + para_dim.get(para_n));
                            error_e = true;
                        }
                    }
                }
                q--;
            }
        }
        para_n++;

        IR.add(new Quaternion("PUSH", null, null, null, IR.get(IR.size() - 1).getResult()));
        while (Objects.equals(symTab.get(t).getIdCode(), "COMMA")) {
            addToken("COMMA");
            tt = t;
            Exp();
            ////System.out.println("new para");
            if (!undefine) {
                //错误处理-e
                int q = t - 1;
                //////System.out.println("??tt "+symTab.get(tt).getName());
                String para_name = null;
                while (q >= tt && !Objects.equals(symTab.get(q).getIdCode(), "COMMA")) {
                    int dim1 = 0;
                    int dim2 = 1;
                    ////System.out.println("new part");
                    while (Objects.equals(symTab.get(q).getIdCode(), "RBRACK") ||
                            Objects.equals(symTab.get(q).getIdCode(), "IDENFR")) {
                        dim1 = 0;
                        if (Objects.equals(symTab.get(q).getIdCode(), "RBRACK")) {
                            while (!Objects.equals(symTab.get(q).getIdCode(), "LBRACK")) {
                                q--;
                            }
                            q--;
                            dim1 = 1;
                            //////System.out.println("DIM 1: "+dim1+ " " + symTab.get(q).getName());
                            if (Objects.equals(symTab.get(q).getIdCode(), "RBRACK")) {
                                dim1 = 2;
                                while (!Objects.equals(symTab.get(q).getIdCode(), "LBRACK")) {
                                    q--;
                                }
                                q--;
                            }
                        } else {
                            dim1 = 0;
                        }
                        ////System.out.println("DIM1: " + dim1 + " " + symTab.get(q).getName());
                        dim2 = 1;
                        if (Objects.equals(symTab.get(q).getIdCode(), "IDENFR")) {
                            String ident_name = symTab.get(q).getName();
                            ////////System.out.println("PARANAME "+ident_name);
                            tmpSymbolTable = currentSymbolTable;
                            while (!tmpSymbolTable.getSymboltable().containsKey(ident_name)) {
                                if (tmpSymbolTable.getParent() != -1) {
                                    tmpSymbolTable = symbolTables.get(tmpSymbolTable.getParent());
                                } else {
                                    break;
                                }
                            }
                            ////System.out.println("!@!@!@ " + ident_name + " " +
                            //tmpSymbolTable.getSymboltable().containsKey(ident_name));
                            if (tmpSymbolTable.getSymboltable().containsKey(ident_name)) {
                                dim2 = tmpSymbolTable.getSymboltable().get(ident_name).getDim();
                            } else {
                                dim2 = 0;
                            }
                            ////System.out.println("!@!@ " + ident_name + " " + dim2);
                        }
                        int dim = -dim1 + dim2;
                        if (para_n < para_dim.size()) {
                            if (dim != para_dim.get(para_n)) {
                                ////System.out.println("dim? " + func_name + " " + dim1 + " " + dim2 + " " + para_dim.get(para_n));
                                error_e = true;
                            }
                        }
                        q--;
                    }
                    if (Objects.equals(symTab.get(q).getIdCode(), "INTCON")) {
                        if (para_n < para_dim.size()) {
                            if (para_dim.get(para_n) != 0) {
                                ////System.out.println("dim? intcon " + para_dim.get(para_n));
                                error_e = true;
                            }
                        }
                    }
                    q--;
                }
            }
            para_n++;
            IR.add(new Quaternion("PUSH", null, null, null, IR.get(IR.size() - 1).getResult()));
        }
        out.add("<FuncRParams>");
        if (error_e) {
            ////System.out.println("??? e " + func_name);
            error.add(func_ident_n + " e");
        }
        return para_n;
    }

    private void ConstInitVal() {//ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        if (!Objects.equals(symTab.get(t).getIdCode(), "LBRACE")) {
            ConstExp();
        } else {
            addToken("LBRACE");
            if (!Objects.equals(symTab.get(t).getIdCode(), "RBRACE")) {
                ConstInitVal();
                while (Objects.equals(symTab.get(t).getIdCode(), "COMMA")) {
                    addToken("COMMA");
                    ConstInitVal();
                }
            }
            addToken("RBRACE");
        }
        out.add("<ConstInitVal>");
    }

    private void VarDecl() { //VarDecl → BType VarDef { ',' VarDef } ';'
        //////System.out.println("2");
        BType();
        ////// ////////System.out.println("???VarDef");
        VarDef();
        ////// ////////System.out.println("???VarDef END");
        while (Objects.equals(symTab.get(t).getIdCode(), "COMMA")) {
            addToken("COMMA");
            VarDef();
        }
        addToken("SEMICN");
        out.add("<VarDecl>");

    }

    /*
    VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    | Ident { '[' ConstExp ']' } '=' InitVal
     */
    private void VarDef() {
        String op, arg1, arg2, result;
        arg1 = nowToken().getName();
        int line = nowToken().getN();
        addToken("IDENFR");
        int dim = 0;
        while (Objects.equals(symTab.get(t).getIdCode(), "LBRACK")) {
            addToken("LBRACK");
            ConstExp();
            addToken("RBRACK");
            dim++;
        }
        if (Objects.equals(symTab.get(t).getIdCode(), "ASSIGN")) {
            op = nowToken().getName();
            addToken("ASSIGN");
            InitVal();
            arg2 = IR.get(IR.size() - 1).getResult();
        } else {
            op = null;
            arg2 = null;
        }
        if (isGlobal) {
            data.add(new Quaternion("VAR", op, arg2, arg1, "val_" + arg1 + "_" + col_var));
        } else {
            IR.add(new Quaternion("VAR", op, arg2, arg1, "val_" + arg1 + "_" + col_var));
        }
        col_var++;
        out.add("<VarDef>");
        if (!currentSymbolTable.getSymboltable().containsKey(arg1)) {
            currentSymbolTable.addSymbol(arg1, new Symbol(arg1, dim, line, 1));
        } else {
            ////System.out.println("error b " + arg1 + " " + line);
            error.add(line + " b");
        }
    }

    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    private void InitVal() {
        if (Objects.equals(symTab.get(t).getIdCode(), "LBRACE")) {
            addToken("LBRACE");
            if (!Objects.equals(symTab.get(t).getIdCode(), "RBRACE")) {
                InitVal();
                while (Objects.equals(symTab.get(t).getIdCode(), "COMMA")) {
                    addToken("COMMA");
                    InitVal();
                }
            }
            addToken("RBRACE");
        } else {
            Exp();
        }
        out.add("<InitVal>");
    }

    private void FuncDef() { //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        //symbolTables.add(new SymbolTable(num_st, symbolTables.indexOf(currentSymbolTable)));
        //currentSymbolTable = symbolTables.get(num_st++);
        col_func++;
        String op, result;
        if (Objects.equals(symTab.get(t).getIdCode(), "VOIDTK")) {
            op = "void";
        } else {
            op = "int";
        }
        FuncType();
        result = nowToken().getName();
        int line = nowToken().getN();
        SymbolTable tmpSymbolTable = currentSymbolTable;
        if (!currentSymbolTable.getSymboltable().containsKey(result)) {
            if (Objects.equals(op, "void")) {
                ////System.out.println("addSymbol " + result);
                currentSymbolTable.addSymbol(result, new Symbol(result, -1, line, 2));
            } else {
                currentSymbolTable.addSymbol(result, new Symbol(result, 0, line, 2));
            }

        } else {
            ////System.out.println("error b " + result + " " + line);
            error.add(line + " b");
        }
        IR.add(new Quaternion("FUNC", op, null, null, result));
        addToken("IDENFR");
        addToken("LPARENT");

        int para_n = 0;
        boolean havepara = false;
        //////System.out.println("$$$$ "+symTab.get(t).getIdCode());
        if (!Objects.equals(symTab.get(t).getIdCode(), "RPARENT")) {
            if (!Objects.equals(symTab.get(t).getIdCode(), "LBRACE")) {
                para_n = FuncFParams(result);
                havepara = true;
            }
        }
        addToken("RPARENT");
        ////System.out.println("setPara_n " + result + " " + para_n + " " + nowToken().getN());
        tmpSymbolTable.getSymboltable().get(result).setPara_n(para_n);
        boolean error_g = Block_(havepara);
        out.add("<FuncDef>");
        if(!error_g&&isIntFunc) {
            error.add(symTab.get(t-1).getN()+" g");
        }
        /*SymbolTable tmpSymbolTable = currentSymbolTable;
        //////System.out.println("???? "+result);
        //////System.out.println("????? "+currentSymbolTable.getSymboltable().containsKey(result));
        while (!tmpSymbolTable.getSymboltable().containsKey(result)) {
            if (tmpSymbolTable.getParent() != -1) {
                tmpSymbolTable = symbolTables.get(tmpSymbolTable.getParent());
            } else {

            }
        }*/

        //currentSymbolTable = symbolTables.get(currentSymbolTable.getParent());
    }

    private void FuncType() { //FuncType → 'void' | 'int'
        if (Objects.equals(symTab.get(t).getIdCode(), "VOIDTK")) {
            isVoidFunc = true;
            addToken("VOIDTK");
        } else {
            isIntFunc = true;
            //////System.out.println("FuncType()");
            addToken("INTTK");
        }
        out.add("<FuncType>");
    }

    private int FuncFParams(String func_name) {//FuncFParams → FuncFParam { ',' FuncFParam }
        symbolTables.add(new SymbolTable(num_st, symbolTables.indexOf(currentSymbolTable)));
        currentSymbolTable = symbolTables.get(num_st++);
        int para_n = 0;
        FuncFParam(func_name);
        para_n++;
        ////////System.out.println("!!!");
        while (Objects.equals(symTab.get(t).getIdCode(), "COMMA")) {
            addToken("COMMA");
            FuncFParam(func_name);
            para_n++;
        }
        out.add("<FuncFParams>");
        return para_n;
    }

    private void FuncFParam(String func_name) {//FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        //////System.out.println("3");
        BType();
        IR.add(new Quaternion("PARA", "int", null, nowToken().getName(),"var_" + nowToken().getName() + "_" + col_var));
        String arg1 = nowToken().getName();
        int line = nowToken().getN();
        int dim = 0;
        addToken("IDENFR");
        if (Objects.equals(symTab.get(t).getIdCode(), "LBRACK")) {
            addToken("LBRACK");
            addToken("RBRACK");
            dim++;
            while (Objects.equals(symTab.get(t).getIdCode(), "LBRACK")) {
                addToken("LBRACK");
                ConstExp();
                addToken("RBRACK");
                dim++;
            }
        }
        //////System.out.println("ADDDIM "+func_name + " " + dim);
        ////////System.out.println(currentSymbolTable.getSymboltable().containsKey(func_name));
        SymbolTable tmpSymbolTable = symbolTables.get(currentSymbolTable.getParent());
        tmpSymbolTable.getSymboltable().get(func_name).getPara_dim().add(dim);
        out.add("<FuncFParam>");
        if (!currentSymbolTable.getSymboltable().containsKey(arg1)) {
            currentSymbolTable.addSymbol(arg1, new Symbol(arg1, dim, line, 3));
            currentSymbolTable.getSymboltable().get(arg1).setFa_func(func_name);
        } else {

            if (currentSymbolTable.getSymboltable().get(arg1).getType() == 3 &&
                    currentSymbolTable.getSymboltable().get(arg1).getFa_func().equals(func_name)) {
                ////System.out.println("error b " + arg1 + " " + line);
                error.add(line + " b");
            }

        }
    }

    private boolean Block_(boolean havePara) { // '{' { BlockItem } '}'
        if (!havePara) {
            symbolTables.add(new SymbolTable(num_st, symbolTables.indexOf(currentSymbolTable)));
            currentSymbolTable = symbolTables.get(num_st++);
        }
        addToken("LBRACE");
        boolean isReturn = false;
        while (!Objects.equals(symTab.get(t).getIdCode(), "RBRACE")) {
            ////// ////////System.out.println("???BlockItem");
            isReturn = BlockItem();
            ////// ////////System.out.println("???BlockItem END");
        }
        //////System.out.println("symtab");
        for (int i = 0; i < symTab.size(); i++) {
            //////System.out.println(i+" "+symTab.get(i).getName());
        }
        //////System.out.println("symtab end");
        addToken("RBRACE");
        out.add("<Block>");
        currentSymbolTable = symbolTables.get(currentSymbolTable.getParent());
        return isReturn;
    }

    private boolean BlockItem() {// Decl | Stmt
        if (isDecl()) {
            Decl();
            return false;
        } else {
            ////// ////////System.out.println("???Stmt");
            return Stmt();
            ////// ////////System.out.println("???Stmt end");
        }
        //out.add("<BlockItem>");
    }

    private boolean error_a(String s) {
        s = s.substring(1, s.length() - 1);
        for (int i = 0; i < s.length(); i++) {
            ////////System.out.println("?? "+s.charAt(i));
            if (s.charAt(i) == '%') {
                if (i + 1 < s.length() && s.charAt(i + 1) == 'd') {
                    continue;
                } else {
                    return true;
                }
            } else if (s.charAt(i) == '\\') {
                if (i + 1 < s.length() && s.charAt(i + 1) == 'n') {
                    continue;
                } else {
                    return true;
                }
            } else {//32,33,40-126
                if (!(s.charAt(i) == 32 || s.charAt(i) == 33 || (40 <= s.charAt(i) && s.charAt(i) <= 126))) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
    Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    | [Exp] ';' //有无Exp两种情况
    | Block
    | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    | 'while' '(' Cond ')' Stmt
    | 'break' ';'
    | 'continue' ';'
    | 'return' [Exp] ';' // 1.有Exp 2.无Exp
    | LVal '=' 'getint''('')'';'
    | 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
     */
    private boolean Stmt() {
        boolean isReturn = false;
        if (Objects.equals(symTab.get(t).getIdCode(), "IFTK")) {
            ////// ////////System.out.println(1);
            addToken("IFTK");
            addToken("LPARENT");
            Cond();
            addToken("RPARENT");
            Stmt();
            if (Objects.equals(symTab.get(t).getIdCode(), "ELSETK")) {
                addToken("ELSETK");
                Stmt();
            }
        } else if (Objects.equals(symTab.get(t).getIdCode(), "WHILETK")) {
            ////// ////////System.out.println(2);
            addToken("WHILETK");
            addToken("LPARENT");
            Cond();
            addToken("RPARENT");
            cycleDepth++;
            Stmt();
            cycleDepth--;
        } else if (Objects.equals(symTab.get(t).getIdCode(), "BREAKTK")) {
            int line = nowToken().getN();
            if (cycleDepth == 0) {
                error.add(line + " m");
            }
            addToken("BREAKTK");
            addToken("SEMICN");
        } else if (Objects.equals(symTab.get(t).getIdCode(), "CONTINUETK")) {
            int line = nowToken().getN();
            if (cycleDepth == 0) {
                error.add(line + " m");
            }
            addToken("CONTINUETK");
            addToken("SEMICN");
        } else if (Objects.equals(symTab.get(t).getIdCode(), "RETURNTK")) {
            isReturn = true;
            int line = nowToken().getN();
            haveReturn = true;
            addToken("RETURNTK");
            //////System.out.println("!!RETURN "+ nowToken().getName());
            if (!Objects.equals(symTab.get(t).getIdCode(), "SEMICN")) {
                if (!(Objects.equals(symTab.get(t).getIdCode(), "IFTK") ||
                        Objects.equals(symTab.get(t).getIdCode(), "RETURNTK") ||
                        Objects.equals(symTab.get(t).getIdCode(), "WHILETK") ||
                        Objects.equals(symTab.get(t).getIdCode(), "INTTK") ||
                        Objects.equals(symTab.get(t).getIdCode(), "RBRACE") ||
                        Objects.equals(symTab.get(t).getIdCode(), "ELSETK") ||
                        Objects.equals(symTab.get(t).getIdCode(), "NOT"))) {
                    Exp();
                    if (isVoidFunc) {
                        error.add(line + " f");
                    }
                    IR.add(new Quaternion("RETURN", null, null, null, IR.get(IR.size() - 1).getResult()));
                }
            } else {
                IR.add(new Quaternion("RETURN", null, null, null, "-"));
            }
            //////System.out.println("!!!RETURN "+ nowToken().getName());
            addToken("SEMICN");
        } else if (Objects.equals(symTab.get(t).getIdCode(), "PRINTFTK")) {
            String ss;
            int pline = nowToken().getN();
            addToken("PRINTFTK");
            addToken("LPARENT");
            ss = nowToken().getName().substring(1, nowToken().getName().length() - 1);
            ArrayList<String> commas = new ArrayList<>();
            String string = nowToken().getName();
            int line = nowToken().getN();
            if (error_a(string)) {
                error.add(line + " a");
            }

            addToken("STRCON");
            int nExp = 0;
            while (Objects.equals(symTab.get(t).getIdCode(), "COMMA")) {
                addToken("COMMA");
                Exp();
                nExp++;
                commas.add(IR.get(IR.size() - 1).getResult());
            }
            if (nExp != (string.length() - string.replace("%d", "").length()) / 2) {
                error.add(pline + " l");
            }
            //for (int i=0;i<commas.size();i++) {
            // //System.out.print(commas.get(i)+" ");
            //}
            ////// ////////System.out.println("");
            //String []g = ss.split("%d|\\\\n");
            String[] g = ss.split("(?<=%d)|(?=%d)|(?<=\\\\n)|(?=\\\\n)");
            for (int i = 0, j = 0; i < g.length; i++) {
                if (g[i].length() > 0) {
                    ////// ////////System.out.println("!!" + g[i]);
                    if (g[i].equals("%d")) {
                        boolean isLoad = false;
                        for (int k = 0; k < data.size(); k++) {
                            if (data.get(k).getSyxtaxKind().equals("VAR")) {
                                if (data.get(k).getArg2().equals(commas.get(j))) {
                                    IR.add(new Quaternion("LOADWORD", null, null, data.get(k).getResult(), "tmp_" + col_tmp));
                                    isLoad = true;
                                }
                            }
                        }
                        if (isLoad) {
                            IR.add(new Quaternion("PRINTD", null, null, null, "tmp_" + col_tmp));
                            col_tmp++;
                        } else {
                            if (j < commas.size()) {
                                IR.add(new Quaternion("PRINTD", null, null, null, commas.get(j++)));
                            }
                        }

                    } else if (g[i].equals("\\n")) {
                        IR.add(new Quaternion("PRINTN", null, null, null, "str_0"));
                    } else {
                        data.add(new Quaternion("STR", "=", "str_" + col_str, null, g[i]));
                        IR.add(new Quaternion("PRINTS", null, "str_" + col_str, null, g[i]));
                        col_str++;
                    }
                }

            }
            addToken("RPARENT");
            addToken("SEMICN");
        } else if (Objects.equals(symTab.get(t).getIdCode(), "LBRACE")) {
            Block_(false);
        } else if (isLVal()) {
            String op, arg1, arg2, result;
            isAssign = true;
            LVal();
            isAssign = false;
            result = IR.get(IR.size() - 1).getResult();
            op = "=";
            addToken("ASSIGN");
            if (Objects.equals(symTab.get(t).getIdCode(), "GETINTTK")) {
                boolean isStore = false;
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).getSyxtaxKind().equals("VAR")) {
                        if (data.get(i).getArg2().equals(result)) {
                            IR.add(new Quaternion("GETINT", op, null, null, "tmp_" + col_tmp));
                            IR.add(new Quaternion("STOREWORD", null, null, data.get(i).getResult(), "tmp_" + col_tmp));
                            col_tmp++;
                            isStore = true;
                            break;
                        }
                    }
                }
                if (!isStore) {
                    IR.add(new Quaternion("GETINT", op, null, null, result));
                }
                addToken("GETINTTK");
                addToken("LPARENT");
                addToken("RPARENT");
                addToken("SEMICN");
            } else {
                Exp();
                arg1 = IR.get(IR.size() - 1).getResult();
                boolean isStore = false;

                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).getSyxtaxKind().equals("VAR")) {
                        if (data.get(i).getArg2().equals(result)) {
                            IR.add(new Quaternion("ASSIGN", op, arg1, null, "tmp_" + col_tmp));
                            IR.add(new Quaternion("STOREWORD", null, null, data.get(i).getResult(), "tmp_" + col_tmp));
                            col_tmp++;
                            isStore = true;
                            break;
                        }
                    }
                }
                if (!isStore) {
                    IR.add(new Quaternion("ASSIGN", op, arg1, null, result));
                }
                addToken("SEMICN");
            }
        } else {
            ////// ////////System.out.println(5);
            if (!Objects.equals(symTab.get(t).getIdCode(), "SEMICN")) {
                ////// ////////System.out.println("???Exp");
                Exp();
                ////// ////////System.out.println("???Exp end");
            }
            addToken("SEMICN");
        }
        out.add("<Stmt>");
        return isReturn;
    }

    private boolean isLVal() {
        if (Objects.equals(symTab.get(t).getIdCode(), "INTCON")) {
            return false;
        }
        if (Objects.equals(symTab.get(t).getIdCode(), "LPARENT")) {
            return false;
        }
        if (Objects.equals(symTab.get(t).getIdCode(), "SEMICN")) {
            return false;
        }
        int q = t + 1;
        while (!Objects.equals(symTab.get(q).getIdCode(), "SEMICN")) {
            if (Objects.equals(symTab.get(q).getIdCode(), "IDENFR")) {
                return false;
            }
            if (Objects.equals(symTab.get(q).getIdCode(), "RETURNTK")) {
                return false;
            }
            if (Objects.equals(symTab.get(q).getIdCode(), "LBRACK")) {
                q++;
                while (!Objects.equals(symTab.get(q).getIdCode(), "RBRACK")) {
                    if (Objects.equals(symTab.get(q).getIdCode(), "ASSIGN")) {
                        return true;
                    } else if (Objects.equals(symTab.get(q).getIdCode(), "SEMICN")) {
                        return false;
                    }
                    q++;
                }
            }
            if (Objects.equals(symTab.get(q).getIdCode(), "ASSIGN")) {
                return true;
            }
            q++;
        }
        return false;
    }

    private void LVal() {//LVal → Ident {'[' Exp ']'}
        ////// ////////System.out.println("@@@ " + nowToken().getName() + " " + findVar(nowToken().getName()));
        IR.add(new Quaternion("LVAL", null, null, null, findVar(nowToken().getName())));
        String name = nowToken().getName();
        int line = nowToken().getN();
        SymbolTable tmpSymbolTable = currentSymbolTable;
        ////////System.out.println("???? "+currentSymbolTable.getParent());
        while (!tmpSymbolTable.getSymboltable().containsKey(name)) {
            if (tmpSymbolTable.getParent() != -1) {
                tmpSymbolTable = symbolTables.get(tmpSymbolTable.getParent());
            } else {
                error.add(line + " c");
                break;
            }
        }
        tmpSymbolTable = currentSymbolTable;
        while (!tmpSymbolTable.getSymboltable().containsKey(name)) {
            if (tmpSymbolTable.getParent() != -1) {
                tmpSymbolTable = symbolTables.get(tmpSymbolTable.getParent());
            } else {
                break;
            }
        }
        if (tmpSymbolTable.getSymboltable().containsKey(name)) {
            if (tmpSymbolTable.getSymboltable().get(name).getType() == 0) {
                if (isAssign) {
                    error.add(line + " h");
                }
            }
        }
        addToken("IDENFR");
        while (Objects.equals(symTab.get(t).getIdCode(), "LBRACK")) {
            addToken("LBRACK");
            Exp();
            addToken("RBRACK");
        }
        out.add("<LVal>");
    }

    private void Exp() {// Exp → AddExp
        AddExp();
        out.add("<Exp>");
    }

    private void Cond() {//Cond → LOrExp
        LOrExp();
        out.add("<Cond>");
    }

    private void LOrExp() {//LOrExp → LAndExp | LOrExp '||' LAndExp
        LAndExp();
        out.add("<LOrExp>");
        while (Objects.equals(symTab.get(t).getIdCode(), "OR")) {
            addToken("OR");
            LAndExp();
            out.add("<LOrExp>");
        }
    }

    private void LAndExp() {//LAndExp → EqExp | LAndExp '&&' EqExp
        EqExp();
        out.add("<LAndExp>");
        while (Objects.equals(symTab.get(t).getIdCode(), "AND")) {
            addToken("AND");
            EqExp();
            out.add("<LAndExp>");
        }
    }

    private void EqExp() { //EqExp → RelExp | EqExp ('==' | '!=') RelExp
        RelExp();
        out.add("<EqExp>");
        while (Objects.equals(symTab.get(t).getIdCode(), "EQL") || Objects.equals(symTab.get(t).getIdCode(), "NEQ")) {
            if (Objects.equals(symTab.get(t).getIdCode(), "EQL")) {
                addToken("EQL");
            } else {
                addToken("NEQ");
            }
            RelExp();
            out.add("<EqExp>");
        }

    }

    private void RelExp() {//RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
        AddExp();
        out.add("<RelExp>");
        while (Objects.equals(symTab.get(t).getIdCode(), "LSS") ||
                Objects.equals(symTab.get(t).getIdCode(), "LEQ") ||
                Objects.equals(symTab.get(t).getIdCode(), "GRE") ||
                Objects.equals(symTab.get(t).getIdCode(), "GEQ")) {
            if (Objects.equals(symTab.get(t).getIdCode(), "LSS")) {
                addToken("LSS");
            } else if (Objects.equals(symTab.get(t).getIdCode(), "LEQ")) {
                addToken("LEQ");
            } else if (Objects.equals(symTab.get(t).getIdCode(), "GRE")) {
                addToken("GRE");
            } else {
                addToken("GEQ");
            }
            AddExp();
            out.add("<RelExp>");
        }

    }

    private void MainFuncDef() { //MainFuncDef → 'int' 'main' '(' ')' Block
        col_func++;
        isIntFunc = true;
        //////System.out.println("MainFuncDef()");
        addToken("INTTK");
        addToken("MAINTK");
        addToken("LPARENT");
        addToken("RPARENT");
        IR.add(new Quaternion("MAINFUNC", null, null, null, "main"));
        boolean error_g = Block_(false);
        out.add("<MainFuncDef>");
            if (!error_g&&isIntFunc) {
                error.add(symTab.get(t-1).getN() + " g");
            }

    }

}
