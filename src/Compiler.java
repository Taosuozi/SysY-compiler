import java.io.*;
import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        String code = "";
        int num = 0;
        Lexer lexer;
        try {
            BufferedReader in = new BufferedReader(new FileReader("testfile.txt"));
            String str;
            while ((str = in.readLine()) != null) {
                num++;
                code = code + str + '\n';
            }
            lexer = new Lexer(code);
            lexer.analyse();
            /*PrintWriter out = new PrintWriter("output.txt");
            for (int i = 0; i < lexer.getSymTabs().size();i++) {
                out.println(lexer.getSymTabs().get(i).getIdCode()+" " + lexer.getSymTabs().get(i).getName());
            }
            out.close();*/
            //System.out.println(code);
            Parser parser =new Parser(lexer.getSymTabs());
            parser.analyse();
            /*PrintWriter out = new PrintWriter("output.txt");
            for (int i = 0; i < parser.getOut().size();i++) {
                out.println(parser.getOut().get(i));
            }
            out.close();*/
            PrintWriter error = new PrintWriter("error.txt");
            for (int i = 0; i < parser.getError().size();i++) {
                error.println(parser.getError().get(i));
            }
            error.close();
            /*MipsGeneration mipsGeneration = new MipsGeneration(parser.getIR(),parser.getData());
            mipsGeneration.generation();
            PrintWriter out = new PrintWriter("mips.txt");
            for (int i = 0; i < mipsGeneration.getMips().size();i++) {
                out.println(mipsGeneration.getMips().get(i));
            }
            out.close();*/
        } catch (IOException e) {

        }
    }


}
