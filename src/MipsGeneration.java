import jdk.nashorn.internal.ir.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class MipsGeneration {
    private ArrayList<Quaternion> IR;
    private ArrayList<Quaternion> data;
    private ArrayList<String> mips = new ArrayList<>();
    private HashMap<String, String> constTabs = new HashMap<>();
    //private ArrayList<GlobalVar>
    private int t;

    public ArrayList<String> getMips() {
        return mips;
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public MipsGeneration(ArrayList<Quaternion> IR, ArrayList<Quaternion> data) {
        this.IR = IR;
        this.data = data;
        t = 0;
    }

    public void generation() {
        for (int i = 0; i < IR.size(); i++) {
            ////System.out.println(IR.get(i).toString());
        }
        CompUnit();
    }

    public void CompUnit() {
        mips.add(".data");
        Data();
        mips.add(".text");
        mips.add("jal func_main");
        mips.add("ori $v0, $0, 10");
        mips.add("syscall");
        while (t < IR.size() && (IR.get(t).getSyxtaxKind().equals("FUNC") || IR.get(t).getSyxtaxKind().equals("MAINFUNC"))) {
            FuncDef();
        }
    }

    public void Data() {
        for (int i = 0; i < data.size(); i++) {
            switch (data.get(i).getSyxtaxKind()) {
                case "STR":
                    mips.add(data.get(i).getArg1() + " : .asciiz \"" + data.get(i).getResult() + "\"");
                    break;
                case "CONST":
                    mips.add(data.get(i).getResult() + "  : .word " + data.get(i).getArg2());
                    constTabs.put(data.get(i).getResult(), data.get(i).getArg2());
                    break;
                case "VAR":
                    if (data.get(i).getOp() != null) {
                        mips.add(data.get(i).getResult() + "  : .word " + data.get(i).getArg1());
                    } else {
                        mips.add(data.get(i).getResult() + "  : .word 0:1");
                    }
                    break;
            }
        }
    }

    public void FuncDef() {
        int paramNumber = 0;
        int memory = 0;
        Boolean isMain = false;
        HashMap<Integer, Integer> regInMem = new HashMap<>();
        HashMap<String, String> varTabs = new HashMap<>();
        //HashSet<Integer> regUsed = new HashSet<>();
        //HashMap<String, String> var2reg = new HashMap<>();
        HashSet<Integer> memUsed = new HashSet<>();
        HashMap<String, String> var2mem = new HashMap<>();
        mips.add("func_" + IR.get(t).getResult() + ":");
        if (IR.get(t).getResult().equals("main")) {
            isMain = true;
        }
        //////System.out.println("func start " + t);
        for (int i = t + 1; i < IR.size(); i++) {
            //////System.out.println("IR.KIND: " + IR.get(i).getSyxtaxKind());
            if (IR.get(i).getSyxtaxKind().equals("PARA")) {
                paramNumber++;
                if (!var2mem.containsKey(IR.get(t).getResult())) {
                    for (int j = 0; ; j += 4) {
                        if (!memUsed.contains(j)) {
                            memUsed.add(j);
                            var2mem.put(IR.get(i).getResult(), "sp" + j);
                            memory += 4;
                            break;
                        }
                    }
                }
            }
            if (IR.get(i).getSyxtaxKind().equals("FUNC") || IR.get(i).getSyxtaxKind().equals("MAINFUNC")) {
                break;
            }
        }
        memory += 96;
        regInMem.put(31, memory);
        memory += 4;
        memory += paramNumber > 4 ? paramNumber * 4 - 16 : 0;
        mips.add("subi $sp, $sp, " + memory);
        for (Object k : regInMem.keySet()) {
            mips.add("sw $" + k + ", " + regInMem.get(k) + "($sp)");
        }


        int num = 0;
        ////System.out.println("?? "+IR.get(t).getSyxtaxKind());
        t++;
        while (IR.get(t).getSyxtaxKind().equals("PARA")) {
            //////System.out.println("?? " + IR.get(t).getSyxtaxKind());
            if (num < 4) {
                //mips.add("move " + var2reg.get(IR.get(t).getResult())+  ", $a" + num);
                mips.add("sw $a" + num + ", " + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
            } else {
                //mips.add("lw " + var2reg.get(IR.get(t).getResult()) + ", " + String.valueOf(memory - (num - 3) * 4) + "($sp)");
                mips.add("lw $27, " + String.valueOf(memory - (num - 3) * 4) + "($sp)");
                mips.add("sw $27, " + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
            }
            num++;
            t++;
        }
        //////System.out.println("para over" + t);
        /*for (int j = 8; j<=25;j++) {
            if(regUsed.contains(j)) {
                regUsed.remove(j);
            }
        }*/
        while (!IR.get(t).getSyxtaxKind().equals("RETURN")) {
            ////System.out.println(IR.get(t).getSyxtaxKind());
            mips.add("# "+IR.get(t).getSyxtaxKind());
            switch (IR.get(t).getSyxtaxKind()) {
                case "VAR":
                    if (IR.get(t).getOp() != null) {
                        if (isInteger(IR.get(t).getArg1())) {
                            mips.add("li $27, " + IR.get(t).getArg1());
                        } else if (var2mem.get(IR.get(t).getArg1()).charAt(0) == 's') {
                            mips.add("lw $27, " + var2mem.get(IR.get(t).getArg1()).substring(2) + "($sp)");
                        }
                        if (!var2mem.containsKey(IR.get(t).getResult())) {
                            for (int j = 0; ; j += 4) {
                                if (!memUsed.contains(j)) {
                                    memUsed.add(j);
                                    //regInMem.put(j, memory);
                                    var2mem.put(IR.get(t).getResult(), "sp" + j);
                                    break;
                                }
                            }
                        }
                        mips.add("sw $27, " + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                    }
                    break;
                case "GETINT":
                    mips.add("li $v0, 5");
                    mips.add("syscall");
                    if (!var2mem.containsKey(IR.get(t).getResult())) {
                        for (int j = 0; ; j += 4) {
                            if (!memUsed.contains(j)) {
                                memUsed.add(j);
                                //regInMem.put(j, memory);
                                var2mem.put(IR.get(t).getResult(), "sp" + j);
                                break;
                            }
                        }
                    }
                    mips.add("sw $v0," + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                    //mips.add("move $"+var2reg.get(IR.get(t).getResult())+", $v0");
                    break;
                case "STOREWORD":
                    mips.add("# STOREWORD");
                    if (!var2mem.containsKey(IR.get(t).getResult())) {
                        for (int j = 0; ; j += 4) {
                            if (!memUsed.contains(j)) {
                                memUsed.add(j);
                                //regInMem.put(j, memory);
                                //////System.out.println("!!!! "+IR.get(t).getResult()+" "+"sp" + j);
                                var2mem.put(IR.get(t).getResult(), "sp" + j);
                                break;
                            }
                        }
                    }
                    if (!var2mem.containsKey(IR.get(t).getArg2())) {
                        for (int j = 0; ; j += 4) {
                            if (!memUsed.contains(j)) {
                                memUsed.add(j);
                                //regInMem.put(j, memory);
                                //////System.out.println("!!!! "+IR.get(t).getResult()+" "+"sp" + j);
                                var2mem.put(IR.get(t).getArg2(), "sp" + j);
                                break;
                            }
                        }
                    }
                    mips.add("la $27, " + IR.get(t).getArg2());
                    if (isInteger(IR.get(t).getResult())) {
                        mips.add("li $28, " + IR.get(t).getResult());
                    } else if (var2mem.get(IR.get(t).getResult()).charAt(0) == 's') {
                        mips.add("lw $28, " + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                    }
                    mips.add("sw $28, 0($27)");
                    break;
                case "LOADWORD":
                    mips.add("# LOADWORD");
                    if (!var2mem.containsKey(IR.get(t).getResult())) {
                        for (int j = 0; ; j += 4) {
                            if (!memUsed.contains(j)) {
                                memUsed.add(j);
                                //regInMem.put(j, memory);
                                //////System.out.println("!!!! "+IR.get(t).getResult()+" "+"sp" + j);
                                var2mem.put(IR.get(t).getResult(), "sp" + j);
                                break;
                            }
                        }
                    }
                    if (!var2mem.containsKey(IR.get(t).getArg2())) {
                        for (int j = 0; ; j += 4) {
                            if (!memUsed.contains(j)) {
                                memUsed.add(j);
                                //regInMem.put(j, memory);
                                //////System.out.println("!!!! "+IR.get(t).getResult()+" "+"sp" + j);
                                var2mem.put(IR.get(t).getArg2(), "sp" + j);
                                break;
                            }
                        }
                    }
                    mips.add("la $27, " + IR.get(t).getArg2());
                    mips.add("lw $27, 0($27)");
                    mips.add("sw $27, " + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                    break;
                case "PRINTS":
                    mips.add("la $a0, " + IR.get(t).getArg1());
                    mips.add("li $v0, 4");
                    mips.add("syscall");
                    break;
                case "PRINTN":
                    mips.add("la $a0, str_0");
                    mips.add("li $v0, 4");
                    mips.add("syscall");
                    break;
                case "PRINTD":
                    ////System.out.println("### "+ IR.get(t).getResult() + " " + var2mem.containsKey(IR.get(t).getResult()));
                    if (var2mem.get(IR.get(t).getResult()).charAt(0) == 's') {
                        //////System.out.println(">>> " + IR.get(t).getResult() + " " + var2mem.get(IR.get(t).getResult()));
                        mips.add("lw $a0, " + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                    } else {
                        mips.add("li $a0, " + var2mem.get("ret"));//?
                    }
                    mips.add("li $v0, 1");
                    mips.add("syscall");
                    break;
                case "ADD":
                    if (isInteger(IR.get(t).getArg1())) {
                        if (Integer.valueOf(IR.get(t).getArg1()) < 32768) {
                            mips.add("addi $27, $0, " + IR.get(t).getArg1());
                        } else {
                            mips.add("li $27, " + IR.get(t).getArg1());
                        }
                    } else if (constTabs.containsKey(IR.get(t).getArg1())) {
                        if (Integer.valueOf(constTabs.get(IR.get(t).getArg1())) < 32768) {
                            mips.add("addi $27, $0, " + constTabs.get(IR.get(t).getArg1()));
                        } else {
                            mips.add("li $27, " + constTabs.get(IR.get(t).getArg1()));
                        }
                    } else if (var2mem.get(IR.get(t).getArg1()).charAt(0) != '$') {
                        mips.add("lw $27, " + var2mem.get(IR.get(t).getArg1()).substring(2) + "($sp)");
                    }
                    ////System.out.println("!@!@ "+ IR.get(t).getArg2()+" "+var2mem.containsKey(IR.get(t).getArg2()));
                    if (isInteger(IR.get(t).getArg2())) {
                        mips.add("li $28, " + IR.get(t).getArg2());
                    } else if (constTabs.containsKey(IR.get(t).getArg2())) {
                        mips.add("li $28, " + constTabs.get(IR.get(t).getArg2()));
                    } else if (var2mem.get(IR.get(t).getArg2()).charAt(0) == 's') {
                        mips.add("lw $28, " + var2mem.get(IR.get(t).getArg2()).substring(2) + "($sp)");
                    }
                    mips.add("addu $27, $27, $28");
                    if (!var2mem.containsKey(IR.get(t).getResult())) {
                        for (int j = 0; ; j += 4) {
                            if (!memUsed.contains(j)) {
                                memUsed.add(j);
                                //regInMem.put(j, memory);
                                var2mem.put(IR.get(t).getResult(), "sp" + j);
                                break;
                            }
                        }
                    }
                    mips.add("sw $27," + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                    break;
                case "SUB":
                    if (isInteger(IR.get(t).getArg1())) {
                        mips.add("li $27, " + IR.get(t).getArg1());
                    } else if (constTabs.containsKey(IR.get(t).getArg1())) {
                        mips.add("li $27, " + constTabs.get(IR.get(t).getArg1()));
                    } else if (var2mem.get(IR.get(t).getArg1()).charAt(0) != '$') {
                        mips.add("lw $27, " + var2mem.get(IR.get(t).getArg1()).substring(2) + "($sp)");
                    }
                    if (isInteger(IR.get(t).getArg2())) {
                        mips.add("li $28, " + IR.get(t).getArg2());
                    } else if (constTabs.containsKey(IR.get(t).getArg2())) {
                        mips.add("li $28, " + constTabs.get(IR.get(t).getArg2()));
                    } else if (var2mem.get(IR.get(t).getArg2()).charAt(0) == 's') {
                        mips.add("lw $28, " + var2mem.get(IR.get(t).getArg2()).substring(2) + "($sp)");
                    }
                    mips.add("subu $27, $27, $28");
                    if (!var2mem.containsKey(IR.get(t).getResult())) {
                        for (int j = 0; ; j += 4) {
                            if (!memUsed.contains(j)) {
                                memUsed.add(j);
                                //regInMem.put(j, memory);
                                var2mem.put(IR.get(t).getResult(), "sp" + j);
                                break;
                            }
                        }
                    }
                    mips.add("sw $27," + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                    break;
                case "MUL":
                    if (isInteger(IR.get(t).getArg1())) {
                        mips.add("li $27, " + IR.get(t).getArg1());
                    } else if (constTabs.containsKey(IR.get(t).getArg1())) {
                        mips.add("li $27, " + constTabs.get(IR.get(t).getArg1()));
                    } else if (var2mem.get(IR.get(t).getArg1()).charAt(0) != '$') {
                        mips.add("lw $27, " + var2mem.get(IR.get(t).getArg1()).substring(2) + "($sp)");
                    }
                    ////System.out.println("??? " + IR.get(t).getArg2());
                    ////System.out.println("???? " + var2mem.containsKey(IR.get(t).getArg2()));
                    if (isInteger(IR.get(t).getArg2())) {
                        mips.add("li $28, " + IR.get(t).getArg2());
                    } else if (constTabs.containsKey(IR.get(t).getArg2())) {
                        mips.add("li $28, " + constTabs.get(IR.get(t).getArg2()));
                    } else if (var2mem.get(IR.get(t).getArg2()).charAt(0) == 's') {
                        mips.add("lw $28, " + var2mem.get(IR.get(t).getArg2()).substring(2) + "($sp)");
                    }
                    mips.add("mul $27, $27, $28");
                    if (!var2mem.containsKey(IR.get(t).getResult())) {
                        for (int j = 0; ; j += 4) {
                            if (!memUsed.contains(j)) {
                                memUsed.add(j);
                                //regInMem.put(j, memory);
                                var2mem.put(IR.get(t).getResult(), "sp" + j);
                                break;
                            }
                        }
                    }
                    mips.add("sw $27," + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                    break;
                case "DIV":
                    if (isInteger(IR.get(t).getArg1())) {
                        mips.add("li $27, " + IR.get(t).getArg1());
                    } else if (constTabs.containsKey(IR.get(t).getArg1())) {
                        mips.add("li $27, " + constTabs.get(IR.get(t).getArg1()));
                    } else if (var2mem.get(IR.get(t).getArg1()).charAt(0) != '$') {
                        mips.add("lw $27, " + var2mem.get(IR.get(t).getArg1()).substring(2) + "($sp)");
                    }
                    if (isInteger(IR.get(t).getArg2())) {
                        mips.add("li $28, " + IR.get(t).getArg2());
                    } else if (constTabs.containsKey(IR.get(t).getArg2())) {
                        mips.add("li $28, " + constTabs.get(IR.get(t).getArg2()));
                    } else if (var2mem.get(IR.get(t).getArg2()).charAt(0) == 's') {
                        mips.add("lw $28, " + var2mem.get(IR.get(t).getArg2()).substring(2) + "($sp)");
                    }
                    mips.add("div $27, $28");
                    mips.add("mflo $28");
                    if (!var2mem.containsKey(IR.get(t).getResult())) {
                        for (int j = 0; ; j += 4) {
                            if (!memUsed.contains(j)) {
                                memUsed.add(j);
                                //regInMem.put(j, memory);
                                var2mem.put(IR.get(t).getResult(), "sp" + j);
                                break;
                            }
                        }
                    }
                    mips.add("sw $28," + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                    break;
                case "MOD":
                    if (isInteger(IR.get(t).getArg1())) {
                        mips.add("li $27, " + IR.get(t).getArg1());
                    } else if (constTabs.containsKey(IR.get(t).getArg1())) {
                        mips.add("li $27, " + constTabs.get(IR.get(t).getArg1()));
                    } else if (var2mem.get(IR.get(t).getArg1()).charAt(0) != '$') {
                        mips.add("lw $27, " + var2mem.get(IR.get(t).getArg1()).substring(2) + "($sp)");
                    }
                    if (isInteger(IR.get(t).getArg2())) {
                        mips.add("li $28, " + IR.get(t).getArg2());
                    } else if (constTabs.containsKey(IR.get(t).getArg2())) {
                        mips.add("li $28, " + constTabs.get(IR.get(t).getArg2()));
                    } else if (var2mem.get(IR.get(t).getArg2()).charAt(0) == 's') {
                        mips.add("lw $28, " + var2mem.get(IR.get(t).getArg2()).substring(2) + "($sp)");
                    }
                    mips.add("div $27, $28");
                    mips.add("mfhi $28");
                    if (!var2mem.containsKey(IR.get(t).getResult())) {
                        for (int j = 0; ; j += 4) {
                            if (!memUsed.contains(j)) {
                                memUsed.add(j);
                                //regInMem.put(j, memory);
                                var2mem.put(IR.get(t).getResult(), "sp" + j);
                                break;
                            }
                        }
                    }
                    mips.add("sw $28," + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                    break;
                case "ASSIGN":
                    if (IR.get(t).getArg1().equals("ret")) {
                        mips.add("move $27, $v0");
                    } else if (isInteger(IR.get(t).getArg1())) {
                        mips.add("li $27, " + IR.get(t).getArg1());
                    } else if (var2mem.get(IR.get(t).getArg1()).charAt(0) == 's') {
                        mips.add("lw $27, " + var2mem.get(IR.get(t).getArg1()).substring(2) + "($sp)");
                    }
                    if (!var2mem.containsKey(IR.get(t).getResult())) {
                        for (int j = 0; ; j += 4) {
                            if (!memUsed.contains(j)) {
                                memUsed.add(j);
                                //regInMem.put(j, memory);
                                var2mem.put(IR.get(t).getResult(), "sp" + j);
                                break;
                            }
                        }
                    }
                    mips.add("sw $27, " + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                    break;
                case "PUSH":
                    num = 0;
                    while (IR.get(t).getSyxtaxKind().equals("PUSH")) {
                        if (num < 4) {
                            if (isInteger(IR.get(t).getResult())) {
                                mips.add("li $a" + num + ", " + IR.get(t).getResult());
                            } else {
                                mips.add("lw $a" + num + ", " +
                                        var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                            }
                        } else {
                            if (isInteger(IR.get(t).getResult())) {
                                mips.add("li $27, " + IR.get(t).getResult());
                            } else {
                                mips.add("lw $27, " +
                                        var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
                            }
                            mips.add("sw $27, " + String.valueOf(-(num - 3) * 4) + "($sp)");
                        }
                        num++;
                        t++;
                    }
                    t--;
                    /*if (!isPush) {
                        isPush = true;
                        for (int j = t; j < IR.size(); j++) {
                            if (IR.get(j).getSyxtaxKind().equals("PUSH")) {
                                paran++;
                            }
                            if (IR.get(j).getSyxtaxKind().equals("CALL")) {
                                break;
                            }
                        }
                    }
                    num++;
                    //System.out.println("??? "+num +" " + IR.get(t).getResult());
                    i*/
                    break;
                case "CALL":
                    mips.add("jal func_" + IR.get(t).getResult());
                    break;
            }
            t++;
        }
        if (!IR.get(t).getResult().equals("-")) {
            if (isInteger(IR.get(t).getResult())) {
                mips.add("li $v0, " + IR.get(t).getResult());
            } else {
                mips.add("lw $v0, " + var2mem.get(IR.get(t).getResult()).substring(2) + "($sp)");
            }
        }
        for (Object k : regInMem.keySet()) {
            mips.add("lw $" + k + ", " + regInMem.get(k) + "($sp)");
        }
        mips.add("addi $sp, $sp, " + memory);
        mips.add("jr $31");
        t++;
    }

    /*public void assembly() {
        int i;
        mips.add(".data");
        for (i = 0; i < IR.size(); i++) {
            int varn = 0;
            if (IR.get(i).getSyxtaxKind().equals("FUNC")) {
                break;
            }
            switch (IR.get(i).getSyxtaxKind()) {
                case "CONST":
                    if (IR.get(i).getResult().equals("int")) {
                        constTabs.add(new ConstTab("int", IR.get(i).getArg1(), Integer.parseInt(IR.get(i).getArg2())));
                    }
                    break;
                case "GPVAR":

                    break;
            }
        }
        Set ss = strs.keySet();
        for (Object o : ss) {
            mips.add(strs.get(o) + ": .asciiz " + o + "\"");
        }
        mips.add(".space " + String.valueOf(strs.size() * 4));
        mips.add(".text");
        mips.add("li $fp, 0x10040000");
        mips.add("j func_main");
        mips.add("nop");
        for (; i < IR.size(); i++) {
            if (IR.get(i).getSyxtaxKind().equals("MAINFUNC")) {
                break;
            }
            int paran = 0;
            int tt = 0;
            int[] s = new int[7];
            int st = 0;
            String ret;
            HashMap<String, String> var2reg = new HashMap<>();
            boolean[] visS = new boolean[7];
            switch (IR.get(i).getSyxtaxKind()) {
                case "FUNC":
                    paran = 0;
                    var2reg = new HashMap<>();
                    st = 0;
                    mips.add("func_" + IR.get(i).getResult() + ":");
                    if (IR.get(i).getOp().equals("int")) {
                        for (int j = i + 1; j < IR.size(); j++) {
                            if (IR.get(j).getSyxtaxKind().equals("RETURN")) {
                                var2reg.put(IR.get(j).getResult(), "v0");
                            }
                        }
                    }
                    break;
                case "PARA":
                    mips.add("lw $s" + String.valueOf(st) + ", " + paran + "($fp)");
                    paran += 4;
                    var2reg.put(IR.get(i).getResult(), "s" + st);
                    st++;
                    break;
                case "ADD":
                    if (!var2reg.get(IR.get(i).getResult()).equals("v0")) {
                        mips.add("add $t" + tt + ", $" + var2reg.get(IR.get(i).getArg1()) +
                                ", $" + var2reg.get(IR.get(i).getArg2()));
                        var2reg.put(IR.get(i).getResult(), "t" + tt);
                        tt++;
                    } else {
                        mips.add("add $v0, $" + var2reg.get(IR.get(i).getArg1()) +
                                ", $" + var2reg.get(IR.get(i).getArg2()));
                    }
                    break;
                case "SUB":
                    if (!var2reg.get(IR.get(i).getResult()).equals("v0")) {
                        mips.add("sub $t" + tt + ", $" + var2reg.get(IR.get(i).getArg1()) +
                                ", $" + var2reg.get(IR.get(i).getArg2()));
                        var2reg.put(IR.get(i).getResult(), "t" + tt);
                        tt++;
                    } else {
                        mips.add("sub $v0, $" + var2reg.get(IR.get(i).getArg1()) +
                                ", $" + var2reg.get(IR.get(i).getArg2()));
                    }
                    break;
                case "RESULT":
                    mips.add("jr $ra");
                    break;
            }
        }
        mips.add("func_main:");
        i++;
        for (; i < IR.size(); i++) {
            int tt = 0;
            int st = 0;
            String retReg=null;
            HashMap<String, String> var2reg = new HashMap<>();
            switch (IR.get(i).getSyxtaxKind()) {
                case "VAR":
                    if(IR.get(i).getOp()!=null) {
                        mips.add("li $s"+ st+", "+IR.get(i).getArg2());
                        var2reg.put(IR.get(i).getArg2(), "s" + st);
                        st++;
                    }else {
                        var2reg.put(IR.get(i).getArg2(), "s" + st);
                        st++;
                    }
                    break;
                case "PUSH":
                    int paran = 0;
                    String func=null;
                    ArrayList<Quaternion> paras=new ArrayList<>();
                    int j;
                    for(j=i;j<IR.size();j++) {
                        if(IR.get(j).getSyxtaxKind().equals("PUSH")) {
                            paras.add(IR.get(j));
                            paran++;
                        }
                        if(IR.get(j).getSyxtaxKind().equals("CALL")) {
                            func = IR.get(j).getResult();
                            i=j;
                            break;
                        }
                    }
                    for (j=0;j<paran;j++) {
                        mips.add("sw $"+var2reg.get(paras.get(j).getResult())+", "+(paran+j+1)*4+"($fp)");
                    }
                    for (j=0;j<paran;j++) {
                        mips.add("sw $"+var2reg.get(paras.get(j).getResult())+", "+(paran+j+1)*4+"($fp)");
                    }
                    mips.add("addi $sp, $sp, -"+(paran+1)*3);
                    for (j=0;j<paran;j++) {
                        mips.add("sw $s"+j+", "+(j)*4+"($sp)");
                    }
                    mips.add("sw $ra, "+(paran)*4+"($sp)");

                    38 addi $fp, $fp, 12 # 移动帧指针
                    39 jal func_foo # 调用函数foo()
                    40 addi $fp, $fp, -12 # 移动帧指针

                    mips.add("addi $fp, $fp, "+(paran+1)*4);
                    mips.add("func_"+func+":");
                    mips.add("addi $fp, $fp, -"+(paran+1)*4);
                    for (j=0;j<paran;j++) {
                        mips.add("lw $s"+j+", "+(j)*4+"($sp)");
                    }
                    mips.add("lw $ra, "+(paran)*4+"($sp)");
                    mips.add("addi $sp, $sp, "+(paran+1)*3);
                    break;
                case "ASSIGN":
                    if(IR.get(i).getArg1().equals("ret")) {
                        mips.add("move $t"+tt+", $v0");
                        var2reg.put(IR.get(i).getResult(),"t"+tt);
                        tt++;
                    }else {
                        mips.add("move $"+var2reg.get(IR.get(i).getArg1()) +", $t"+tt);
                        var2reg.put(var2reg.get(IR.get(i).getArg1()),"$t"+tt);
                        tt++;
                    }
                    break;
                case "ADD":
                    if (!var2reg.get(IR.get(i).getResult()).equals("v0")) {
                        mips.add("add $t" + tt + ", $" + var2reg.get(IR.get(i).getArg1()) +
                                ", $" + var2reg.get(IR.get(i).getArg2()));
                        var2reg.put(IR.get(i).getResult(), "t" + tt);
                        tt++;
                    } else {
                        mips.add("add $v0, $" + var2reg.get(IR.get(i).getArg1()) +
                                ", $" + var2reg.get(IR.get(i).getArg2()));
                    }
                    break;
                case "SUB":
                    if (!var2reg.get(IR.get(i).getResult()).equals("v0")) {
                        mips.add("sub $t" + tt + ", $" + var2reg.get(IR.get(i).getArg1()) +
                                ", $" + var2reg.get(IR.get(i).getArg2()));
                        var2reg.put(IR.get(i).getResult(), "t" + tt);
                        tt++;
                    } else {
                        mips.add("sub $v0, $" + var2reg.get(IR.get(i).getArg1()) +
                                ", $" + var2reg.get(IR.get(i).getArg2()));
                    }
                    break;
                case "GETINT":
                    break;
                case "PRINTS":
                    break;
                case "PRINTD":
                    break;
                case "PRINTN":
                    break;
            }

        }
        mips.add("li $v0, 10");
        mips.add("syscall");
    }*/
}
