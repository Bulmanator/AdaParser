import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

    public String filename;
    // List of reserved words to prevent an identifier using them
    private String[] reserved_words = {
        "begin", "call", "do", "else", "end",
        "float", "if", "integer", "is", "loop",
        "procedure", "string", "then", "until",
        "while", "for"
    };

    /**
      Creates a new SyntaxAnalyser which will check the syntax of the file given
      @param filename The name of the file to analyse
    */
    public SyntaxAnalyser(String filename) throws IOException {
        this.filename = filename;
        lex = new LexicalAnalyser(filename);
    }

    /**
      Attepmts to parse the entire file catching and reporting any errors it encounters
    */
    public void _statementPart_() throws IOException, CompilationException {
        // @Debug!!
        {
            String actual = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.length());
            myGenerate.filename = actual;
        }

        // Accept the 'begin' symbol terminal
        acceptTerminal(Token.beginSymbol);
        // Start a StatementPart non-terminal
        myGenerate.commenceNonterminal("StatementPart");
        // Try to read a StatementList as it always follows a StatementPart
        StatementList();

        myGenerate.finishNonterminal("StatementPart");
        acceptTerminal(Token.endSymbol);
    }

    /**
      Attempts to parse a StatementList, reporting any errors it encounters
        StatementList ::= Statement | StatementList; Statement
    */
    public void StatementList() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("StatementList");
        Statement();
        while (nextToken.symbol == Token.semicolonSymbol) {
            acceptTerminal(Token.semicolonSymbol);
            Statement();
        }
        myGenerate.finishNonterminal("StatementList");
    }

    private void Statement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("Statement");
        switch (nextToken.symbol) {
            case Token.identifier: {
                // Must be an assignment statement
                AssignmentStatement();
            }
            break;
            case Token.ifSymbol: {
                // If statement!
                IfStatement();
            }
            break;
            case Token.whileSymbol: {
                // While loop!
                WhileStatement();
            }
            break;
            case Token.doSymbol: {
                // Do until loop!
                UntilStatement();
            }
            break;
            case Token.callSymbol: {
                // Procedure call!
                CallStatement();
            }
            break;
            case Token.forSymbol: {
                // For loop!
                ForStatement();
            }
            break;
            default: {
                // Any other symbol is unexpected, thus should cause an error
                myGenerate.reportError(nextToken, "unexpected symbol '" + Token.getName(nextToken.symbol)
                        + "', expected identifier, if, while, do, call or for");
            }
            break;
        }
        myGenerate.finishNonterminal("Statement");
    }


    public void AssignmentStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("AssignmentStatement");
        acceptTerminal(Token.identifier);
        acceptTerminal(Token.becomesSymbol);
        if (nextToken.symbol == Token.stringConstant) {
            acceptTerminal(Token.stringConstant);
        }
        else {
            Expression();
        }
        myGenerate.finishNonterminal("AssignmentStatement");
    }

    // @Todo
    public void Factor() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("Factor");
        switch (nextToken.symbol) {
            case Token.identifier:
            case Token.numberConstant: {
                acceptTerminal(nextToken.symbol);
            }
            break;
            case Token.leftParenthesis: {
                acceptTerminal(Token.leftParenthesis);
                Expression();
                acceptTerminal(Token.rightParenthesis);
            }
            break;
            default: {
                myGenerate.reportError(nextToken, "unexpected symbol '"
                        + Token.getName(nextToken.symbol) +
                        "', expected identifier, numberConstant or (expression)");
            }
            break;
        }
        myGenerate.finishNonterminal("Factor");
    }

    public void Term() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("Term");
        Factor();
        if (nextToken.symbol == Token.timesSymbol || nextToken.symbol == Token.divideSymbol) {
            acceptTerminal(nextToken.symbol);
            Factor();
        }
        myGenerate.finishNonterminal("Term");
    }

    public void Expression() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("Expression");
        Term();
        if (nextToken.symbol == Token.plusSymbol || nextToken.symbol == Token.minusSymbol) {
            acceptTerminal(nextToken.symbol);
            Term();
        }
        myGenerate.finishNonterminal("Expression");
    }

    /**
      This will try to parse an if statement and report any errors it encounters
    */
    public void IfStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("IfStatement");
        acceptTerminal(Token.ifSymbol);
        Condition();
        acceptTerminal(Token.thenSymbol);
        StatementList();

        if (nextToken.symbol == Token.elseSymbol) {
            acceptTerminal(Token.elseSymbol);
            StatementList();
        }

        acceptTerminal(Token.endSymbol);
        acceptTerminal(Token.ifSymbol);
        myGenerate.finishNonterminal("IfStatement");
    }

    /**
      This will try to parse a for loop statement and report any errors it encounters
    */
    public void ForStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("ForStatement");
        acceptTerminal(Token.forSymbol);
        acceptTerminal(Token.leftParenthesis);
        AssignmentStatement();
        acceptTerminal(Token.semicolonSymbol);
        Condition();
        acceptTerminal(Token.semicolonSymbol);
        AssignmentStatement();
        acceptTerminal(Token.rightParenthesis);
        acceptTerminal(Token.doSymbol);
        StatementList();
        acceptTerminal(Token.endSymbol);
        acceptTerminal(Token.loopSymbol);
        myGenerate.finishNonterminal("ForStatement");
    }

    /**
      This will try to parse a while loop statement and report any errors it encounters
    */
    public void WhileStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("WhileStatement");
        acceptTerminal(Token.whileSymbol);
        Condition();
        acceptTerminal(Token.loopSymbol);
        StatementList();
        acceptTerminal(Token.endSymbol);
        acceptTerminal(Token.loopSymbol);
        myGenerate.finishNonterminal("WhileStatement");
    }

    /**
      This will try to parse a do until statement and report and errors it encounters
    */
    public void UntilStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("UntilStatement");
        acceptTerminal(Token.doSymbol);
        StatementList();
        acceptTerminal(Token.untilSymbol);
        Condition();
        myGenerate.finishNonterminal("UntilStatement");
    }

    /**
      Attempts to parse a condition statement
            Condition ::= Identifier ConditionalOperator Identifier |
                            Identifier ConditionalOperator NumberConstant |
                            Identifier ConditionalOperator StringConstant
     */
    public void Condition() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Condition");
        acceptTerminal(Token.identifier);
        ConditionalOperator();
        switch (nextToken.symbol) {
            case Token.identifier: {
                acceptTerminal(Token.identifier);
            }
            break;
            case Token.numberConstant: {
                acceptTerminal(Token.numberConstant);
            }
            break;
            case Token.stringConstant: {
                acceptTerminal(Token.stringConstant);
            }
            break;
            default: {
                myGenerate.reportError(nextToken, "unexpected symbol '"
                            + Token.getName(nextToken.symbol)
                            + "', expected identifier, numberConstant or stringConstant");
            }
            break;
        }
        myGenerate.finishNonterminal("Condition");
    }

    private void ConditionalOperator() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("ConditionalOperator");
        switch (nextToken.symbol) {
            case Token.greaterThanSymbol:
            case Token.greaterEqualSymbol:
            case Token.equalSymbol:
            case Token.notEqualSymbol:
            case Token.lessThanSymbol:
            case Token.lessEqualSymbol: {
                acceptTerminal(nextToken.symbol);
            }
            break;
            default: {
                myGenerate.reportError(nextToken, "unexpected symbol '" + Token.getName(nextToken.symbol)
                        + "', expected greaterThan, greaterEqual, equal, notEqual, lessThan or lessEqual");
            }
            break;
        }
        myGenerate.finishNonterminal("ConditionalOperator");
    }

    public void CallStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("CallStatement");
        acceptTerminal(Token.callSymbol);
        acceptTerminal(Token.identifier);
        acceptTerminal(Token.leftParenthesis);
        ArgumentList();
        acceptTerminal(Token.rightParenthesis);
        myGenerate.finishNonterminal("CallStatement");
    }

    public void ArgumentList() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("ArgumentList");
        acceptTerminal(Token.identifier);
        while (nextToken.symbol == Token.commaSymbol) {
            acceptTerminal(Token.commaSymbol);
            acceptTerminal(Token.identifier);
        }
        myGenerate.finishNonterminal("ArgumentList");
    }

    public void acceptTerminal(int symbol) throws IOException, CompilationException {
        if (nextToken == null) {
            myGenerate.reportError(null, "unexpected end of file");
        }
        else if (nextToken.symbol == symbol) {
            myGenerate.insertToken(nextToken);
            nextToken = lex.getNextToken();
        }
        else {
            myGenerate.reportError(nextToken, "unexpected symbol '"
                    + Token.getName(nextToken.symbol) + "', expected " + Token.getName(symbol));
        }
    }

    private boolean IsReservedWord(String word) {
        for (int i = 0; i < reserved_words.length; i++) {
            if (reserved_words[i].equals(word)) {
                return true;
            }
        }
        return false;
    }
}
