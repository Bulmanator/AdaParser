import java.io.IOException;
import java.util.Stack;

public class Generate extends AbstractGenerate {
    public int indent;
    public String filename;
    private Stack<String> nonterminals;

    public Generate() {
        indent = 0;
        nonterminals = new Stack<>();
    }

    public void insertToken(Token token) {
        for (int i = 0; i < indent; i++) { System.out.print(" "); }
        switch (token.symbol) {
            case Token.identifier:
            case Token.numberConstant:
            case Token.stringConstant: {
                System.out.println("rggTOKEN " + Token.getName(token.symbol) + " '"
                        + token.text + "' on line " + token.lineNumber);
            }
            break;
            default: {
                System.out.println("rggTOKEN " +
                        Token.getName(token.symbol) + " on line " + token.lineNumber);
            }
            break;
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
        /*if (token != null)
            final_message = "\"Error: " + message + " at line " + token.lineNumber + " (" + filename + ")\"";
        else
            final_message = "\"Error: " + message + " (" + filename + ")\"";
*/
        if (token != null)
            final_message = filename + ":" + token.lineNumber + ": \"Error: " + message + "\"";
        else
            final_message = filename + ": \"Error: " + message + "\"";

        CompilationException trace = new CompilationException(final_message);
        while (!nonterminals.isEmpty()) {
            trace = new CompilationException(nonterminals.pop(), trace);
        }

        throw trace;
    }

    public void Reset() {
        nonterminals.clear();
        indent = 0;
    }
}
