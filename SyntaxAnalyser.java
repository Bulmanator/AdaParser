import java.io.IOException;
import java.util.ArrayList;

public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

    private ArrayList<String> identifiers;
    private String[] reserved_words = {
        "begin", "call", "do", "else", "end",
        "float", "if", "integer", "is", "loop",
        "procedure", "string", "then", "until",
        "while", "for"
    };

    public SyntaxAnalyser(String filename) throws IOException {
        lex = new LexicalAnalyser(filename);
        identifiers = new ArrayList<>();
    }

    public void _statementPart_() throws IOException, CompilationException {
        if (nextToken.symbol != Token.beginSymbol) {
            myGenerate.reportError(nextToken, "expected 'begin' symbol");
        }
        myGenerate.commenceNonterminal("StatementPart");
        StatementList();
        myGenerate.finishNonterminal("StatementPart");
        nextToken = lex.getNextToken();
        if (nextToken == null) {
            myGenerate.reportError(null, "unexpected End of File");
        }
        if (nextToken.symbol != Token.endSymbol) {
            myGenerate.reportError(nextToken, "expected 'end' symbol");
        }
    }

    public void StatementList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("StatementList");
        do {
            nextToken = lex.getNextToken();
            switch (nextToken.symbol) {
                case Token.identifier: {
                    // Must be an assignment statement
                    // @Todo: AssignmentStatement
                }
                break;
                case Token.ifSymbol: {
                    // If statement!
                    IfStatement();
                }
                break;
                case Token.whileSymbol: {
                    // While loop!
                    // @Todo: WhileStatement
                }
                break;
                case Token.doSymbol: {} break;
                case Token.callSymbol: {
                    // Procedure call!
                    // @Todo: ProcedureStatement
                }
                break;
                case Token.forSymbol: {

                }
                break;
                default: {
                    myGenerate.reportError(nextToken, "unexpected symbol \"" + nextToken.text + "\"");
                }
                break;
            }
            nextToken = lex.getNextToken();
            System.err.println(nextToken.toString());
        } while (nextToken.symbol != Token.semicolonSymbol);

        // Statement list done!
        myGenerate.finishNonterminal("StatementList");
    }

    public void AssignmentStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("AssignmentStatement");
        if (identifiers.contains(nextToken.text)) {
            myGenerate.reportError(nextToken, "redeclaration of '" + nextToken.text + "'");
        }
        else if(IsReservedWord(nextToken.text)) {
            myGenerate.reportError(nextToken, "'" + nextToken.text + "' is reserved and cannot be used as an identifier");
        }

        Token op = lex.getNextToken();
        Token b = lex.getNextToken();

        if (op.symbol != Token.becomesSymbol) {
            if (op.symbol == Token.equalSymbol) {
                myGenerate.reportError(op, "expected ':=' for assignment, did you use '=' instead?");
            }
            myGenerate.reportError(op, "unexpected symbol '" + op.text + "' during assignment");
        }
        myGenerate.insertToken(op);

        switch (b.symbol) {
            case Token.identifier:
            case Token.numberConstant:
            case Token.leftParenthesis: {
                Expression();
            }
            break;
            case Token.stringConstant: {
                myGenerate.insertToken(b);
            }
            break;
        }

    }

    public void Factor(Token token) {
        myGenerate.commenceNonterminal("Factor");
        switch (token.symbol) {
            case Token.identifier: {
                // Check if the identifier exists, if not error
                if (!identifiers.contains(token.text)) {
                    myGenerate.reportError(token, "undeclared identifier '" + token.text + "'");
                }

                // Identifier exists, insert the token
                myGenerate.insertToken(token);
            }
            break;
            case Token.numberConstant: {
                // Just a number insert the token and be done
                myGenerate.insertToken(token);
            }
            break;
            case Token.leftParenthesis: {
                myGenerate.insertToken(token);
                Expression();
                nextToken = lex.getNextToken();
                if (nextToken.symbol != Token.rightParenthesis) {
                    myGenerate.reportError(nextToken, "missing ')' in expression");
                }
                myGenerate.insertToken(nextToken);
            } break;
            default: {
                myGenerate.reportError(token, "unexpected symbol '" + token.text + "'");
            }
            break;
        }
        myGenerate.finishNonterminal("Factor");
    }

    public void Term() {
        myGenerate.commenceNonterminal("Term");
        Token a = lex.getNextToken();
        nextToken = lex.getNextToken();

        // @Todo: Implement Term handling!

        myGenerate.finishNonterminal("Term");
    }

    public void Expression() {
        myGenerate.commenceNonterminal("Expression");
        myGenerate.finishNonterminal("Expression");
    }

    public void IfStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("IfStatement");
        // If
        myGenerate.insertToken(nextToken);
        // Followed By Condition
        Condition();

        // Followed by Then
        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.thenSymbol) {
            System.err.println(nextToken.toString());
            myGenerate.reportError(nextToken, "\"then\" was expected after \"if\"");
        }

        myGenerate.insertToken(nextToken);

        // Followed by StatementList
        StatementList();

        // @Todo: Else
        nextToken = lex.getNextToken();
        // This could be the case if we hit EOF while doing the statement list
        if (nextToken == null) {
            myGenerate.reportError(null, "unexpected end of file");
        }
        else if (nextToken.symbol != Token.endSymbol) {
            myGenerate.reportError(nextToken, "\"end if\" was expected after \"then\"");
        }

        myGenerate.insertToken(nextToken);

        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.ifSymbol) {
            myGenerate.reportError(nextToken, "\"end if\" was expected after \"then\"");
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
        else if (!identifiers.contains(a.text)) {
            myGenerate.reportError(a, "undeclared identifier '" + a.text + "'");
        }
        myGenerate.insertToken(a);

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
                myGenerate.reportError(op, "'" + op.text + "' was not a valid conditional operator");
            }
            break;
        }
        myGenerate.insertToken(op);


        // The second identifier for the condition statement
        switch (b.symbol) {
            case Token.identifier: {
                String name = b.text;
                if (!identifiers.contains(name)) {
                    myGenerate.reportError(b, "undeclared identifier '" + name + "'");
                }
            }
            break;
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
