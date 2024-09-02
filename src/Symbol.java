import java.util.ArrayList;

public class Symbol {
    //记录符号的名字，地址，维度，声明行号和使用行号
    private String name;
    private int address;
    private int dim;
    private int def_n;
    private int type;//0:常量1:变量2:函数3:参数
    private int para_n;
    private ArrayList<Integer> para_dim=new ArrayList<>();
    private String fa_func;
    private ArrayList<Integer> use_n=new ArrayList<>();
    private int value;

    public int getType() {
        return type;
    }

    public Symbol(String name, int dim, int def_n, int type) {
        this.name = name;
        this.dim = dim;
        this.def_n = def_n;
        this.type = type;
    }

    public String getFa_func() {
        return fa_func;
    }

    public void setFa_func(String fa_func) {
        this.fa_func = fa_func;
    }

    public ArrayList<Integer> getUse_n() {
        return use_n;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getPara_n() {
        return para_n;
    }

    public void setPara_n(int para_n) {
        this.para_n = para_n;
    }

    public String getName() {
        return name;
    }

    public int getAddress() {
        return address;
    }

    public int getDim() {
        return dim;
    }

    public int getDef_n() {
        return def_n;
    }

    public ArrayList<Integer> getPara_dim() {
        return para_dim;
    }

    public int getValue() {
        return value;
    }
}

