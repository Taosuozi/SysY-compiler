import java.util.ArrayList;

public class Lexer {
    private String code;
    private ArrayList<SymTab> symTabs;
    private int line = 1;
    private int lastline = 1;
    public Lexer(String code) {
        this.code = code;
        symTabs = new ArrayList<>();

    }

    public ArrayList<SymTab> getSymTabs() {
        return symTabs;
    }

    //state:0:space 1:letter or _ 2:digit 3:string 4:// 5:/**/ 6:op
    public void analyse() {
        String word = null;
        int state = 0;
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            switch (state) {
                case 0:
                    if (isSpace(c)) {
                        state = 0;
                    } else if (c == '\n') {
                        state = 0;
                    } else {
                        if (word == null) {
                            word = String.valueOf(c);
                        } else {
                            word = word + c;
                        }
                        if (Character.isLetter(c) || c == '_') {
                            state = 1;
                        } else if (Character.isDigit(c)) {
                            state = 2;
                        } else if (c == '"') {
                            state = 3;
                        } else if (c == '/') {
                            char c_next = code.charAt(i + 1);
                            if (c_next == '/') {
                                i++;
                                state = 4;
                            } else if (c_next == '*') {
                                i++;
                                state = 5;
                            } else {
                                symTabs.add(new SymTab("/", "DIV",line));
                                state = 0;
                                word = null;
                            }
                        } else {
                            char c_next;
                            switch (c) {
                                case '!':
                                    c_next = code.charAt(i + 1);
                                    if (c_next == '=') {
                                        i++;
                                        symTabs.add(new SymTab("!=", "NEQ",line));
                                    } else {
                                        symTabs.add(new SymTab("!", "NOT",line));
                                    }
                                    break;
                                case '&':
                                    i++;
                                    symTabs.add(new SymTab("&&", "AND",line));
                                    break;
                                case '|':
                                    i++;
                                    symTabs.add(new SymTab("||", "OR",line));
                                    break;
                                case '+':
                                    symTabs.add(new SymTab("+", "PLUS",line));
                                    break;
                                case '-':
                                    symTabs.add(new SymTab("-", "MINU",line));
                                    break;
                                case '*':
                                    symTabs.add(new SymTab("*", "MULT",line));
                                    break;
                                case '%':
                                    symTabs.add(new SymTab("%", "MOD",line));
                                    break;
                                case '<':
                                    c_next = code.charAt(i + 1);
                                    if (c_next == '=') {
                                        i++;
                                        symTabs.add(new SymTab("<=", "LEQ",line));
                                    } else {
                                        symTabs.add(new SymTab("<", "LSS",line));
                                    }
                                    break;
                                case '>':
                                    c_next = code.charAt(i + 1);
                                    if (c_next == '=') {
                                        i++;
                                        symTabs.add(new SymTab(">=", "GEQ",line));
                                    } else {
                                        symTabs.add(new SymTab(">", "GRE",line));
                                    }
                                    break;
                                case '=':
                                    c_next = code.charAt(i + 1);
                                    if (c_next == '=') {
                                        i++;
                                        symTabs.add(new SymTab("==", "EQL",line));
                                    } else {
                                        symTabs.add(new SymTab("=", "ASSIGN",line));
                                    }
                                    break;
                                case ';':
                                    symTabs.add(new SymTab(";", "SEMICN",line));
                                    break;
                                case ',':
                                    symTabs.add(new SymTab(",", "COMMA",line));
                                    break;
                                case '(':
                                    symTabs.add(new SymTab("(", "LPARENT",line));
                                    break;
                                case ')':
                                    symTabs.add(new SymTab(")", "RPARENT",line));
                                    break;
                                case '[':
                                    symTabs.add(new SymTab("[", "LBRACK",line));
                                    break;
                                case ']':
                                    symTabs.add(new SymTab("]", "RBRACK",line));
                                    break;
                                case '{':
                                    symTabs.add(new SymTab("{", "LBRACE",line));
                                    break;
                                case '}':
                                    symTabs.add(new SymTab("}", "RBRACE",line));
                                    break;
                            }
                            state = 0;
                            word=null;
                        }
                    }
                    break;
                case 1:
                    if (Character.isDigit(c) || Character.isLetter(c) || c == '_') {
                        if (word == null) {
                            word = String.valueOf(c);
                        } else {
                            word = word + c;
                        }
                        state = 1;
                    } else {
                        switch (word) {
                            case "main":
                                symTabs.add(new SymTab(word, "MAINTK",line));
                                break;
                            case "const":
                                symTabs.add(new SymTab(word, "CONSTTK",line));
                                break;
                            case "int":
                                symTabs.add(new SymTab(word, "INTTK",line));
                                break;
                            case "break":
                                symTabs.add(new SymTab(word, "BREAKTK",line));
                                break;
                            case "continue":
                                symTabs.add(new SymTab(word, "CONTINUETK",line));
                                break;
                            case "if":
                                symTabs.add(new SymTab(word, "IFTK",line));
                                break;
                            case "else":
                                symTabs.add(new SymTab(word, "ELSETK",line));
                                break;
                            case "while":
                                symTabs.add(new SymTab(word, "WHILETK",line));
                                break;
                            case "getint":
                                symTabs.add(new SymTab(word, "GETINTTK",line));
                                break;
                            case "printf":
                                symTabs.add(new SymTab(word, "PRINTFTK",line));
                                break;
                            case "return":
                                symTabs.add(new SymTab(word, "RETURNTK",line));
                                break;
                            case "void":
                                symTabs.add(new SymTab(word, "VOIDTK",line));
                                break;
                            default:
                                symTabs.add(new SymTab(word, "IDENFR",line));
                                break;
                        }
                        state = 0;
                        word = null;
                        i--;
                        continue;
                    }
                    break;
                case 2:
                    if (Character.isDigit(c)) {
                        if (word == null) {
                            word = String.valueOf(c);
                        } else {
                            word = word + c;
                        }
                        state = 2;
                    } else {
                        symTabs.add(new SymTab(word, "INTCON",line));
                        state = 0;
                        word = null;
                        i--;
                        continue;
                    }
                    break;
                case 3:
                    if (c == '"') {
                        if (word == null) {
                            word = String.valueOf(c);
                        } else {
                            word = word + c;
                        }
                        symTabs.add(new SymTab(word, "STRCON",line));
                        state = 0;
                        word = null;
                        continue;
                    } else {
                        if (word == null) {
                            word = String.valueOf(c);
                        } else {
                            word = word + c;
                        }
                        state = 3;
                    }
                    break;
                case 4:
                    if (c == '\n') {
                        word = null;
                        state = 0;
                    } else {
                        state = 4;
                    }
                    break;
                case 5:
                    char c_next = code.charAt(i + 1);
                    if (c == '*' && c_next == '/') {
                        i++;
                        word = null;
                        state = 0;
                    } else {
                        state = 5;
                    }
            }
            if(c=='\n') {
                line++;
            }
        }
    }

    public boolean isSpace(char c) {
        if (c == ' ' || c == '\t' || c == '\r') {
            return true;
        }
        return false;
    }
}
