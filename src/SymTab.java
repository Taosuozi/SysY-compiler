public class SymTab {
    String name;
    String idCode;
    int n;
    public SymTab(String name,String idCode,int n){
        this.name = name;
        this.idCode = idCode;
        this.n=n;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }

    public void setN(int n) {
        this.n = n;
    }

    public String getName() {
        return name;
    }

    public String getIdCode() {
        return idCode;
    }

    public int getN() {
        return n;
    }
}
