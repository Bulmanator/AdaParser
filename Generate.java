import java.io.IOException;
import java.util.Stack;

public class Generate extends AbstractGenerate {

    // The amount to indent printing by
    private int indent;
    // The non-terminals currently in processing
    private Stack<String> nonterminals;

    public String filename;
    /**
      Creates a new Generate instance
     */
    public Generate() {
        indent = 0;
        nonterminals = new Stack<>();
    }

    /**
      Inserts the given token into the parse tree
      @param token The token to insert into the parse tree
     */
    public void insertToken(Token token) {
        // Indents by the current indent amount
        for (int i = 0; i < indent; i++) { System.out.print(" "); }

        switch (token.symbol) {
            // If the token is an identifier, number or string print out its value as well as its name
            // and line number
            case Token.identifier:
            case Token.numberConstant:
            case Token.stringConstant: {
                System.out.println("rggTOKEN " + Token.getName(token.symbol) + " '"
                        + token.text + "' on line " + token.lineNumber);
            }
            break;
            // Otherwise, just print the name of the token and the line number
            default: {
                System.out.println("rggTOKEN " +
                        Token.getName(token.symbol) + " on line " + token.lineNumber);
            }
            break;
        }
    }

    /**
      Starts the given non-terminal and pushes it onto the process stack
      @param nonterminal The non-terminal to begin
     */
    public void commenceNonterminal(String nonterminal) {
        // Indents by the current indent amount
        for (int i = 0; i < indent; i++) { System.out.print(" "); }

        // Begins the non-terminal and pushes it to the processing stack
        System.out.println("rggBEGIN " + nonterminal);
        nonterminals.push(nonterminal);

        // Increments the indent by 4 spaces
        indent += 4;
    }

    /**
      Finishes the given non-terminal and pops the top non-terminal from the processing stack<br/>
      The non-terminal popped off the processing stack should be equal to the non-terminal given
      @param nonterminal The non-terminal to finish
     */
    public void finishNonterminal(String nonterminal) {
        indent -= 4;
        for (int i = 0; i < indent; i++) { System.out.print(" "); }
        System.out.println("rggEND " + nonterminal);
        nonterminals.pop();
    }

    /**
      Reports success to the parser
     */
    public void reportSuccess() {
        System.out.println("rggSUCCESS");
    }

    /**
      Reports an error to the parser, with the given message and the parse trace will be shown
      @param token The token that caused the error
      @param message An explanatory message about why the error occurred
     */
    public void reportError(Token token, String message) throws CompilationException {
        String final_message = "";
        // If the token isn't null add the line number the error occurred at to the error message
        if (token != null)
            final_message = filename + ":  \"Error: on line " + token.lineNumber + ", " + message + "\"";
        else
            final_message = "  \"Error: " + message + "\"";

        // Pop all of the non-terminals currently in the processing stack and
        // add them to the exception trace
        CompilationException trace = new CompilationException(final_message);
        while (!nonterminals.isEmpty()) {
            trace = new CompilationException("parsing <" + nonterminals.pop() + ">", trace);
        }

        // Throw the error
        throw trace;
    }
}
