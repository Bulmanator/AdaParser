import java.io.IOException;

public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

    private String filename;

    public SyntaxAnalyser(String filename) throws IOException {
        this.filename = filename;
        lex = new LexicalAnalyser(filename);
    }

    public void _statementPart_() throws IOException, CompilationException {
        myGenerate.filename = filename;
        if (nextToken.symbol != Token.beginSymbol) {
            myGenerate.reportError(nextToken, "\"begin\" symbol not found");
        }
        while (true) {
            switch (nextToken.symbol) {
                case Token.beginSymbol: {
                    myGenerate.insertToken(nextToken);
                    myGenerate.commenceNonterminal("StatementPart");
                    myGenerate.commenceNonterminal("StatementList");
                }
                break;
                case Token.endSymbol: {
                    myGenerate.finishNonterminal("StatementList");
                    myGenerate.finishNonterminal("StatementPart");
                    myGenerate.insertToken(nextToken);
                }
                break;
                case Token.ifSymbol: {
                    myGenerate.insertToken(nextToken);
                    myGenerate.commenceNonterminal("IfStatement");
                    IfStatement();
                }
                break;
            }

            if (nextToken.symbol == Token.endSymbol) {
                break;
            }

            nextToken = lex.getNextToken();
        }
    }

    public void IfStatement() throws IOException, CompilationException {
        Condition();
        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.thenSymbol) {
            System.err.println(nextToken.toString());
            myGenerate.reportError(nextToken, "\"then\" was expected after \"if\"");
        }

        myGenerate.insertToken(nextToken);
        myGenerate.finishNonterminal("IfStatement");
    }

    public void Condition() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Condition");
        Token a = lex.getNextToken();
        Token op = lex.getNextToken();
        Token b = lex.getNextToken();

        // The first identifier for the condition statement
        if (a.symbol != Token.identifier) {
            myGenerate.reportError(a, "identifier expected as leading part of conditional");
        }
        myGenerate.insertToken(a);

        // @Todo(James): Do identifier checking!!!!

        // The conditional operator
        switch (op.symbol) {
            case Token.greaterEqualSymbol:
            case Token.greaterThanSymbol:
            case Token.equalSymbol:
            case Token.notEqualSymbol:
            case Token.lessEqualSymbol:
            case Token.lessThanSymbol: {
                // Don't do anything as this is valid
            }
            break;
            default: {
                myGenerate.reportError(op, "\"" + op.text + "\" was not a valid conditional operator");
            }
            break;
        }
        myGenerate.insertToken(op);


        // The second identifier for the condition statement
        switch (b.symbol) {
            case Token.identifier:
            case Token.numberConstant:
            case Token.stringConstant: {
                // Once again don't do anything as this is valid!
            }
            break;
            default: {
                myGenerate.reportError(b, "expected identifier, number literal or string literal in conditional");
            }
            break;
        }

        myGenerate.finishNonterminal("Condition");
    }

    public void acceptTerminal(int symbol) throws IOException, CompilationException {}
}
