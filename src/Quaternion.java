public class Quaternion {
    private String syxtaxKind;
    private String op;
    private String arg1;
    private String arg2;
    private String result;

    public Quaternion(String syxtaxKind, String op, String arg1, String arg2, String result) {
        this.syxtaxKind = syxtaxKind;
        this.op = op;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    public String getSyxtaxKind() {
        return syxtaxKind;
    }

    public String getOp() {
        return op;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public String getResult() {
        return result;
    }

    /*@Override
    public String toString() {
        String ss = syxtaxKind;
        if(op!=null) {
            ss = ss+ " " + op;
        }
        if(arg1!=null) {
            ss = ss+" " + arg1;
        }
        if(arg2!=null) {
            ss = ss+" " + arg2;
        }
        if(result!=null) {
            ss = ss+" " + result;
        }

        return  ss;
    }*/

    @Override
    public String toString() {
        return "Quaternion{" +
                "syxtaxKind='" + syxtaxKind + '\'' +
                ", op='" + op + '\'' +
                ", arg1='" + arg1 + '\'' +
                ", arg2='" + arg2 + '\'' +
                ", result='" + result + '\'' +
                '}';
    }
}
