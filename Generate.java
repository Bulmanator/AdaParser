import java.io.IOException;
import java.util.Stack;

/**
  Prints out the information about the prase tree
  @author James Bulman
 */
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
      Inserts a token into the parse tree with the correct indentation
      @param token The token to insert
     */
    public void insertTerminal(Token token) {
        // Print spaces for indentation
        for (int i = 0; i < indent; i++) { System.out.print(" "); }
        super.insertTerminal(token);
    }

    /**
      Commences a non-terminal in the parse tree with correct indentation
      @param name The name of the non-terminal to commence
     */
    public void commenceNonterminal(String name) {
        // Move the indentation 4 spaces in
        indent += 4;
        // Print the spaces for indentation
        for (int i = 0; i < indent; i++) { System.out.print(" "); }
        super.commenceNonterminal(name);
    }

    /**
      Finishes the non-terminal given with correct indentation
      @param name The name of the non-terminal to finish
     */
    public void finishNonterminal(String name) {
        // Print the spaces for indentation
        for (int i = 0; i < indent; i++) { System.out.print(" "); }
        super.finishNonterminal(name);
        // Move the indentation 4 spaces out
        indent -= 4;
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
