import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private int address;
    private Map<String,Symbol> symboltable;
    private ArrayList<SymbolTable> son;
    private int parent;
    public SymbolTable(int address,int parent) {
        this.address = address;
        this.parent = parent;
        symboltable = new HashMap<>();
        son = new ArrayList<>();
    }

    public int getAddress() {
        return address;
    }

    public int getParent() {
        return parent;
    }

    public Map<String, Symbol> getSymboltable() {
        return symboltable;
    }

    public void addSymbol(String string,Symbol symbol) {
        symboltable.put(string,symbol);
    }

    public ArrayList<SymbolTable> getSon() {
        return son;
    }
}
