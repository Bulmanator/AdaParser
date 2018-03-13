import java.io.IOException;
import java.util.ArrayList;

public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

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
        lex = new LexicalAnalyser(filename);
    }

    /**
      Attepmts to parse the entire file catching and reporting any errors it encounters
    */
    public void _statementPart_() throws IOException, CompilationException {
        if (nextToken.symbol != Token.beginSymbol) {
            myGenerate.reportError(nextToken, "expected 'begin' symbol");
        }
        myGenerate.insertToken(nextToken);
        myGenerate.commenceNonterminal("StatementPart");
        StatementList();
        myGenerate.finishNonterminal("StatementPart");
        if (nextToken == null) {
            myGenerate.reportError(null, "unexpected end of file");
        }
        if (nextToken.symbol != Token.endSymbol) {
            myGenerate.reportError(nextToken, "expected 'end' symbol");
        }
        myGenerate.insertToken(nextToken);
    }

    /**
      Attempts to parse a StatementList, reporting any errors it encounters
        StatementList ::= Statement | StatementList; Statement
    */
    public void StatementList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("StatementList");
        do {
            nextToken = lex.getNextToken();
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
                    // Do until loop
                    UntilStatement();
                }
                break;
                case Token.callSymbol: {
                    // Procedure call!
                    CallStatement();
                }
                break;
                case Token.forSymbol: {
                    ForStatement();
                }
                break;
                default: {
                    myGenerate.reportError(nextToken, "unexpected symbol \"" + nextToken.text + "\"");
                }
                break;
            }
            nextToken = lex.getNextToken();
            myGenerate.finishNonterminal("Statement");
            if (nextToken.symbol == Token.semicolonSymbol) {
                myGenerate.insertToken(nextToken);
            }
        } while (nextToken.symbol == Token.semicolonSymbol);

        // Statement list done!
        myGenerate.finishNonterminal("StatementList");
    }

    // @Incomplete!
    public void AssignmentStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("AssignmentStatement");

        if(IsReservedWord(nextToken.text)) {
            myGenerate.reportError(nextToken, "'" + nextToken.text + "' is reserved and cannot be used as an identifier");
        }
        myGenerate.insertToken(nextToken);

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
        myGenerate.finishNonterminal("AssignmentStatement");
    }

    // @Incomplete
    public void Factor(Token token) throws CompilationException, IOException {
        myGenerate.commenceNonterminal("Factor");
        switch (token.symbol) {
            case Token.identifier:
            case Token.numberConstant: {
                // Just an identifier or number, so insert and be done
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
            }
            break;
            default: {
                myGenerate.reportError(token, "unexpected symbol '" + token.text + "'");
            }
            break;
        }
        myGenerate.finishNonterminal("Factor");
    }

    public void Term() throws CompilationException, IOException {
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

    /**
      This will try to parse an if statement and report any errors it encounters
    */
    public void IfStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("IfStatement");
        // If
        myGenerate.insertToken(nextToken);
        // Followed By Condition
        Condition();

        // Followed by Then
        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.thenSymbol) {
            myGenerate.reportError(nextToken, "'then' was expected after 'if'");
        }

        myGenerate.insertToken(nextToken);

        // Followed by StatementList
        StatementList();

        // @Todo: Else
        //nextToken = lex.getNextToken();
        // This could be the case if we hit EOF while doing the statement list
        if (nextToken == null) {
            myGenerate.reportError(null, "unexpected end of file");
        }
        else if(nextToken.symbol == Token.elseSymbol) {
            myGenerate.insertToken(nextToken);
            StatementList();
        }

        if (nextToken.symbol != Token.endSymbol) {
            myGenerate.reportError(nextToken, "'end if' was expected after 'then'");
        }

        myGenerate.insertToken(nextToken);

        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.ifSymbol) {
            myGenerate.reportError(nextToken, "'end if' was expected after 'then'");
        }
        myGenerate.insertToken(nextToken);

        myGenerate.finishNonterminal("IfStatement");
    }

    /**
      This will try to parse a for loop statement and report any errors it encounters
    */
    public void ForStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("ForStatement");
        // Already know a for token is present
        myGenerate.insertToken(nextToken);

        // Look for the opening parenthesis, error if not found
        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.leftParenthesis) {
            myGenerate.reportError(nextToken, "missing '(' in for loop declaration");
        }
        myGenerate.insertToken(nextToken);

        // An assignment statement must come next
        nextToken = lex.getNextToken();
        AssignmentStatement();

        // Check for the closing semi-colon
        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.semicolonSymbol) {
            if (nextToken.symbol == Token.colonSymbol) {
                myGenerate.reportError(nextToken, "expected ';' after assignment in for loop declaration, did you use a colon instead?");
            }
            else if(nextToken.symbol == Token.identifier) {
                myGenerate.reportError(nextToken, "unexpected identifier '" + nextToken.text + "' in for loop declaration, are you missing a semi-colon?");
            }
            else {
                myGenerate.reportError(nextToken, "unexpected symbol '" + nextToken.text + "' in for loop declaration");
            }
        }
        myGenerate.insertToken(nextToken);

        // Then after the first semi-colon, a condition must follow
        Condition();

        // Once again check for the closing semi-colon
        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.semicolonSymbol) {
            if (nextToken.symbol == Token.colonSymbol) {
                myGenerate.reportError(nextToken, "expected ';' after assignment in for loop declaration, did you use a colon instead?");
            }
            else if(nextToken.symbol == Token.identifier) {
                myGenerate.reportError(nextToken, "unexpected identifier '" + nextToken.text + "' in for loop declaration, are you missing a semi-colon?");
            }
            else {
                myGenerate.reportError(nextToken, "unexpected symbol '" + nextToken.text + "' in for loop declaration");
            }
        }
        myGenerate.insertToken(nextToken);

        // Finally, another assignment statement should be present to finish the declaration
        lex.getNextToken();
        AssignmentStatement();

        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.rightParenthesis) {
            myGenerate.reportError(nextToken, "missing ')' at the end of for loop declaration");
        }
        myGenerate.insertToken(nextToken);

        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.doSymbol) {
            myGenerate.reportError(nextToken, "expected 'do' symbol after for loop declaration");
        }
        myGenerate.insertToken(nextToken);

        StatementList();

        // The 'end' symbol should already be stored in nextToken from the StatementList
        if (nextToken.symbol != Token.endSymbol) {
            myGenerate.reportError(nextToken, "expected 'end loop' at the end of for loop");
        }
        myGenerate.insertToken(nextToken);

        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.loopSymbol) {
            myGenerate.reportError(nextToken, "expected 'end loop' at the end of for loop");
        }
        myGenerate.insertToken(nextToken);

        myGenerate.finishNonterminal("ForStatement");
    }

    /**
      This will try to parse a while loop statement and report any errors it encounters
    */
    public void WhileStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("WhileStatement");
        // Insert the while token
        myGenerate.insertToken(nextToken);

        // Condition
        Condition();

        // The while condition must be followed by a loop keyword
        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.loopSymbol) {
            myGenerate.reportError(nextToken, "expected 'loop' symbol after 'while'");
        }

        // Insert the loop token
        myGenerate.insertToken(nextToken);

        // A StatementList then proceeds the loop token
        StatementList();

        // An 'end loop' statement is expected at the end
        // The 'end' token will already be in nextToken as it was used to leave the StatementList
        if (nextToken.symbol != Token.endSymbol) {
            System.err.println("end loop after while error: " + nextToken);
            myGenerate.reportError(nextToken, "expected 'end loop' symbol after 'while'");
        }
        myGenerate.insertToken(nextToken);

        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.loopSymbol) {
            myGenerate.reportError(nextToken, "expected 'end loop' symbol after 'while'");
        }
        myGenerate.insertToken(nextToken);
        myGenerate.finishNonterminal("WhileStatement");
    }

    /**
      This will try to parse a do until statement and report and errors it encounters
    */
    public void UntilStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("UntilStatement");
        // Insert the 'do' token
        myGenerate.insertToken(nextToken);

        // It is then followed by a StatementList
        StatementList();

        // The until symbol will already be stored in nextToken as it was used to
        // leave the StatementList
        if (nextToken.symbol != Token.untilSymbol) {
            myGenerate.reportError(nextToken, "expected 'until' symbol after 'do'");
        }

        // The condition then follows the 'until' keyword
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
        Token a = lex.getNextToken();
        Token op = lex.getNextToken();
        Token b = lex.getNextToken();

        // The first identifier for the condition statement
        if (a.symbol != Token.identifier) {
            myGenerate.reportError(a, "identifier expected as leading part of conditional");
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
        myGenerate.insertToken(b);

        myGenerate.finishNonterminal("Condition");
    }

    public void CallStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("CallStatement");
        // Insert the 'call' token
        myGenerate.insertToken(nextToken);

        // Make sure the next token is an identifer and insert it
        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.identifier) {
            myGenerate.reportError(nextToken, "identifer was expected for procedure call");
        }
        myGenerate.insertToken(nextToken);

        // Check the left parenthesis is present
        nextToken = lex.getNextToken();
        if (nextToken.symbol != Token.leftParenthesis) {
            myGenerate.reportError(nextToken, "'(' missing from procedure call");
        }
        myGenerate.insertToken(nextToken);

        // An argument list should then follow
        ArgumentList();

        // Finally, check the right parenthesis is present
        // nextToken should already contain it as it was used to get out of the ArgumentList
        if (nextToken.symbol != Token.rightParenthesis) {
            myGenerate.reportError(nextToken, "')' missing from procedure call");
        }
        myGenerate.insertToken(nextToken);

        myGenerate.finishNonterminal("CallStatement");
    }

    public void ArgumentList() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("ArgumentList");
        do {
            nextToken = lex.getNextToken();
            if (nextToken.symbol != Token.identifier) {
                myGenerate.reportError(nextToken, "expected identifer in argument list");
            }
            myGenerate.insertToken(nextToken);

            nextToken = lex.getNextToken();
            if (nextToken.symbol == Token.commaSymbol) {
                myGenerate.insertToken(nextToken);
            }
        }
        while (nextToken.symbol == Token.commaSymbol);

        if (nextToken.symbol == Token.identifier) {
            myGenerate.reportError(nextToken, "unexpected identifer '" + nextToken.text + "' during procedure call, did you miss a comma?");
        }
        myGenerate.finishNonterminal("ArgumentList");
    }

    public void acceptTerminal(int symbol) throws IOException, CompilationException {
        while (nextToken.symbol != symbol) {
            nextToken = lex.getNextToken();
            if (nextToken == null) return;
        }

        Token token = new Token(symbol, Token.getName(symbol), nextToken.lineNumber);

        myGenerate.insertToken(token);
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
