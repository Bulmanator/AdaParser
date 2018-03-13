import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

    private String filename;
    /**
      Creates a new SyntaxAnalyser which will check the syntax of the file given
      @param filename The name of the file to analyse
    */
    public SyntaxAnalyser(String filename) throws IOException {
        lex = new LexicalAnalyser(filename);
        this.filename = filename;
    }

    /**
      Attepmts to parse the entire file catching and reporting any errors it encounters
    */
    public void _statementPart_() throws IOException, CompilationException {
        myGenerate.filename = filename;
        myGenerate.commenceNonterminal("StatementPart");
        // Accept the 'begin' symbol terminal
        acceptTerminal(Token.beginSymbol);
        // Start a StatementPart non-terminal
        // Try to read a StatementList as it always follows a StatementPart
        StatementList();

        // Finally, accept 'end' symbol to finish the parse
        acceptTerminal(Token.endSymbol);
        myGenerate.finishNonterminal("StatementPart");
    }

    /**
      Attempts to parse a StatementList, reporting any errors it encounters
        StatementList ::= Statement | StatementList; Statement
    */
    public void StatementList() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("StatementList");
        // There is always at least one statement
        Statement();
        while (nextToken.symbol == Token.semicolonSymbol) {
            // While a semi-colon is found after parsing a statement
            // there must be another statement, so keep parsing
            acceptTerminal(Token.semicolonSymbol);
            Statement();
        }
        myGenerate.finishNonterminal("StatementList");
    }

    /**
      This will parse a single statement, one of six: if, while, do until, call, assignment and for
     */
    private void Statement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("Statement");
        switch (nextToken.symbol) {
            case Token.identifier: {
                // Should try to parse an assignment statement
                AssignmentStatement();
            }
            break;
            case Token.ifSymbol: {
                // Should try to parse an if statement
                IfStatement();
            }
            break;
            case Token.whileSymbol: {
                // Should try to parse a while loop statement
                WhileStatement();
            }
            break;
            case Token.doSymbol: {
                // Should try to parse a do until loop statement
                UntilStatement();
            }
            break;
            case Token.callSymbol: {
                // Should try to parse a procedure statement
                ProcedureStatement();
            }
            break;
            case Token.forSymbol: {
                // Should try to parse a for loop statement
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


    /**
      This will attempt to parse an assigment statement, which can be a string constant or expression
     */
    public void AssignmentStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("AssignmentStatement");
        // An identifier must come first
        acceptTerminal(Token.identifier);
        // Followed by a ':=' becomes symbol
        acceptTerminal(Token.becomesSymbol);

        // If the available symbol is a string, then accept it
        if (nextToken.symbol == Token.stringConstant) {
            acceptTerminal(Token.stringConstant);
        }
        else {
            // Otherwise try to parse an expression
            Expression();
        }
        myGenerate.finishNonterminal("AssignmentStatement");
    }

    /**
      This will attempt to parse an factor, which can be an identifier, number constant or bracketed expression
     */
    public void Factor() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("Factor");
        // If the available symbol is an identifier or number constant, accept it and be done
        switch (nextToken.symbol) {
            case Token.identifier:
            case Token.numberConstant: {
                acceptTerminal(nextToken.symbol);
            }
            break;
            case Token.leftParenthesis: {
                // If a left parenthesis is found then a bracketed expression must come next
                acceptTerminal(Token.leftParenthesis);
                Expression();
                // After the expession has been parsed a closing right parenthesis must be accepted
                acceptTerminal(Token.rightParenthesis);
            }
            break;
            default: {
                // If neither one of the three, identifier, numberConstant or leftParenthesis
                // then an error should be reported
                myGenerate.reportError(nextToken, "unexpected symbol '"
                        + Token.getName(nextToken.symbol) +
                        "', expected identifier, numberConstant or (expression)");
            }
            break;
        }
        myGenerate.finishNonterminal("Factor");
    }

    /**
      This will parse a term, which can be a factor, term * factor or term / factor
     */
    public void Term() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("Term");
        // There will always be a factor to start with
        Factor();
        // While there is a multiply or divide symbol available
        while (nextToken.symbol == Token.timesSymbol || nextToken.symbol == Token.divideSymbol) {
            // Accept the multiply or divide
            acceptTerminal(nextToken.symbol);
            // Try to parse another term
            Term();
        }
        myGenerate.finishNonterminal("Term");
    }

    /**
      This will parse an expression, which can be a term, expression + term or expression - term
     */
    public void Expression() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("Expression");
        // There should always be a single term to start with
        Term();
        // While there is a plus or minus symbol available
        while (nextToken.symbol == Token.plusSymbol || nextToken.symbol == Token.minusSymbol) {
            // Accept the plus or minus symbol
            acceptTerminal(nextToken.symbol);
            // Try to parse another term
            Term();
        }
        myGenerate.finishNonterminal("Expression");
    }

    /**
      This will try to parse an if statement and report any errors it encounters
    */
    public void IfStatement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("IfStatement");
        // Accept the 'if' symbol
        acceptTerminal(Token.ifSymbol);
        // Then a condition must follow so try to parse
        Condition();
        // If the condition successfully parses, then a 'then' symbol should follow
        acceptTerminal(Token.thenSymbol);
        // The body of the if statment should contain a StatementList
        StatementList();

        // Once the StatementList has been parsed, if the available symbol is else
        if (nextToken.symbol == Token.elseSymbol) {
            // Accept the 'else' symbol
            acceptTerminal(Token.elseSymbol);
            // Another StatementList should follow the else
            StatementList();
        }

        // Accept the 'end' symbol
        acceptTerminal(Token.endSymbol);
        // Accept the 'if' symbol
        acceptTerminal(Token.ifSymbol);
        myGenerate.finishNonterminal("IfStatement");
    }

    /**
      This will try to parse a for loop statement and report any errors it encounters
    */
    public void ForStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("ForStatement");
        // Accept the 'for' symbol
        acceptTerminal(Token.forSymbol);
        // A left parenthesis should follow, so accept it
        acceptTerminal(Token.leftParenthesis);

        // The first section of the for loop declaration should be an assigmnet
        // so try to parse one
        AssignmentStatement();
        // After the assignment, a semi-colon should follow. Accept it
        acceptTerminal(Token.semicolonSymbol);

        // The secdon section is a condition, so try to parse
        Condition();
        // Once again, a semi-colon should follow. Accept it again
        acceptTerminal(Token.semicolonSymbol);

        // The final section of the declaration is another assignment
        // so try to parse
        AssignmentStatement();

        // After this assignment, the closing right parenthesis should follow. Accept it
        acceptTerminal(Token.rightParenthesis);
        // Then a 'do' symbol should follow. Accept this as well
        acceptTerminal(Token.doSymbol);

        // The body of the for loop is a statement list, try to pase one
        StatementList();

        // Finally to close an 'end' symbol should be found
        acceptTerminal(Token.endSymbol);
        // followed by a 'loop' symbol
        acceptTerminal(Token.loopSymbol);
        myGenerate.finishNonterminal("ForStatement");
    }

    /**
      This will try to parse a while loop statement and report any errors it encounters
    */
    public void WhileStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("WhileStatement");
        // Accept the 'while' symbol
        acceptTerminal(Token.whileSymbol);
        // Then a condition should follow, so try to parse
        Condition();
        // After the condition a 'loop' symbol should follow
        acceptTerminal(Token.loopSymbol);

        // The body of the while loop is a StatementList, try to parse
        StatementList();

        // Finally, an 'end' symbol should be present
        acceptTerminal(Token.endSymbol);
        // Followed by a 'loop' symbol
        acceptTerminal(Token.loopSymbol);
        myGenerate.finishNonterminal("WhileStatement");
    }

    /**
      This will try to parse a do until statement and report and errors it encounters
    */
    public void UntilStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("UntilStatement");
        // Accept the 'do' symbol
        acceptTerminal(Token.doSymbol);
        // The body of the do until loop is a StatementList, try to parse
        StatementList();
        // After an 'until' symbol should be found, accept it
        acceptTerminal(Token.untilSymbol);
        // The do until loop ends with a condition so try to parse
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
        // Accept the 'identifier' symbol
        acceptTerminal(Token.identifier);

        // Should then be followed by a conditional operator
        ConditionalOperator();

        // The available token could be one of three choices
        switch (nextToken.symbol) {
            case Token.identifier:
            case Token.numberConstant:
            case Token.stringConstant: {
                // Was an identifier, numberConstant or stringConstant so accept
                acceptTerminal(nextToken.symbol);
            }
            break;
            default: {
                // Not one of the three report an error
                myGenerate.reportError(nextToken, "unexpected symbol '"
                            + Token.getName(nextToken.symbol)
                            + "', expected identifier, numberConstant or stringConstant");
            }
            break;
        }
        myGenerate.finishNonterminal("Condition");
    }

    /**
      Will attempt to parse a conditional operator, which can be one of: greaterThan, greaterEqual, equal,
                lessThan, lessEqual
     */
    private void ConditionalOperator() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("ConditionalOperator");
        // Check the available symbol for a conditional operator
        switch (nextToken.symbol) {
            case Token.greaterThanSymbol:
            case Token.greaterEqualSymbol:
            case Token.equalSymbol:
            case Token.notEqualSymbol:
            case Token.lessThanSymbol:
            case Token.lessEqualSymbol: {
                // Is a conditional operator so accept it
                acceptTerminal(nextToken.symbol);
            }
            break;
            default: {
                // The available symbol was not a conditional operator so report an error
                myGenerate.reportError(nextToken, "unexpected symbol '" + Token.getName(nextToken.symbol)
                        + "', expected >, >=, =, /=, < or <=");
            }
            break;
        }

        myGenerate.finishNonterminal("ConditionalOperator");

    }

    /**
      This will attempt to parse an procedure call
     */
    public void ProcedureStatement() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("ProcedureStatement");
        // Expects a 'call' symbol to start with, accept it
        acceptTerminal(Token.callSymbol);
        // Then an 'identifier' should follow the call so accept this as well
        acceptTerminal(Token.identifier);
        // Next a opening left parenthesis should follow
        acceptTerminal(Token.leftParenthesis);

        // The argument list comes between the opening and closing parentheses
        // Try to parse the list
        ArgumentList();
        // Finally, the closing right parenthesis should be the last thing to accept
        acceptTerminal(Token.rightParenthesis);
        myGenerate.finishNonterminal("ProcedureStatement");
    }

    /**
      This will attempt to parse an argument list
     */
    public void ArgumentList() throws CompilationException, IOException {
        myGenerate.commenceNonterminal("ArgumentList");
        // There must be an identifier in the argument list, so accept symbol
        acceptTerminal(Token.identifier);

        // Keep accepting another identifier as long as there is a comma separating them
        while (nextToken.symbol == Token.commaSymbol) {
            acceptTerminal(Token.commaSymbol);
            acceptTerminal(Token.identifier);
        }

        myGenerate.finishNonterminal("ArgumentList");
    }

    /**
      This will try to accept the symbol given, if the nextToken does not contain this symbol
      an error will be reported instead
      @param symbol The symbol that the parser needs to accept
     */
    public void acceptTerminal(int symbol) throws IOException, CompilationException {
        // If nextToken is null then, the end of file has been reached and
        // getNextToken() has been called again. Should never happen
        if (nextToken == null) {
            myGenerate.reportError(null, "unexpected end of file");
        }
        else if (nextToken.symbol == symbol) {
            // If the symbol matches that of nextToken then insert it and move nextToken to the next available
            myGenerate.insertToken(nextToken);
            nextToken = lex.getNextToken();
        }
        else {
            // If an error is reported and symbol is EOF, error with 'unexpected end of file' (looks nicer)
            if (nextToken.symbol == Token.eofSymbol) {
                myGenerate.reportError(nextToken,
                        "unexpected end of file, expected " + Token.getName(symbol));
            }
            else if (nextToken.symbol == Token.identifier) {
                // If it is an identifier, error with the name of the identifier rather than IDENTIFIER
                myGenerate.reportError(nextToken, "unexpected identifier '"
                        + nextToken.text + "', expected " + Token.getName(symbol));
            }

            // Otherwise just report an error with the name of the symbol found
            // and the name of the symbol expected
            myGenerate.reportError(nextToken, "unexpected symbol '"
                    + Token.getName(nextToken.symbol) + "', expected " + Token.getName(symbol));
        }
    }
}
