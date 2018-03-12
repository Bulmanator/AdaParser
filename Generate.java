import java.io.IOException;
import java.util.Stack;

public class Generate extends AbstractGenerate {
    public int indent;
    private Stack<String> nonterminals;

    public Generate() {
        indent = 0;
        nonterminals = new Stack<>();
    }

    public void insertToken(Token token) {
        for (int i = 0; i < indent; i++) { System.out.print(" "); }
        if (token.symbol == Token.identifier) {
            System.out.println("rggTOKEN IDENTIFIER '" + token.text + "' on line " + token.lineNumber);
        }
        else if (token.symbol == Token.numberConstant) {
            System.out.println("rggTOKEN NUMBER '" + token.text + "' on line " + token.lineNumber);
        }
        else if(token.symbol == Token.stringConstant) {
            System.out.println("rggTOKEN STRING '" + token.text + "' on line " + token.lineNumber);
        }
        else {
            System.out.println("rggTOKEN " + token.text + " on line " + token.lineNumber);
        }
    }

    public void commenceNonterminal(String nonterminal) {
        for (int i = 0; i < indent; i++) { System.out.print(" "); }
        System.out.println("rggBEGIN " + nonterminal);
        nonterminals.push(nonterminal);
        indent += 4;
    }

    public void finishNonterminal(String nonterminal) {
        indent -= 4;
        for (int i = 0; i < indent; i++) { System.out.print(" "); }
        System.out.println("rggEND " + nonterminal);
        String last = nonterminals.pop();
        if (!last.equals(nonterminal)) {
            System.out.println("Error: Failed to end non-terminal (" + nonterminal + " != " + last + ")");
        }
    }

    public void reportSuccess() {
        System.out.println("rggSUCCESS");
        if (indent != 0) System.out.println("Incorrect indent!");
    }

    public void reportError(Token token, String message) throws CompilationException {
        String final_message = "";
        if (token != null)
            final_message = "\"Error: " + message + " at line " + token.lineNumber + "\"";
        else
            final_message = "\"Error: " + message + "\"";

        throw new CompilationException(final_message);
    }

    public void Reset() {
        nonterminals.clear();
        indent = 0;
    }
}
