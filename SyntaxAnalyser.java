import java.io.IOException;

public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

    public SyntaxAnalyser(String filename) throws IOException {
        myGenerate = new Generate();
        lex = new LexicalAnalyser(filename);
    }

    public void _statementPart_() throws IOException, CompilationException {
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
            }

            if (nextToken.symbol == Token.eofSymbol) {
                break;
            }

            nextToken = lex.getNextToken();
        }
    }

    public void acceptTerminal(int symbol) throws IOException, CompilationException {}
}
