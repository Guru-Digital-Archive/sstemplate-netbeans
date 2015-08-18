/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor.lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.php.sstemplate.Debugger;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

public class SSTemplateLexer implements Lexer<SSTemplateTokenId> {

    protected SSTemplateLexerState state;
    protected final TokenFactory<SSTemplateTokenId> tokenFactory;
    protected final LexerInput input;

    private SSTemplateLexer(LexerRestartInfo<SSTemplateTokenId> info) {

        tokenFactory = info.tokenFactory();
        input = info.input();
        state = info.state() == null ? new SSTemplateLexerState() : new SSTemplateLexerState((SSTemplateLexerState) info.state());

        initialize();

    }

    public static synchronized SSTemplateLexer create(LexerRestartInfo<SSTemplateTokenId> info) {
        return new SSTemplateLexer(info);
    }

    @Override
    public Token<SSTemplateTokenId> nextToken() {

        SSTemplateTokenId tokenId = findNextToken();
        return tokenId == null ? null : tokenFactory.createToken(tokenId);

    }

    @Override
    public Object state() {
        return new SSTemplateLexerState(state);
    }

    @Override
    public void release() {
    }

    static protected String INSTRUCTION_START = "<%";
    static protected String COMMENT_START = "--";
    static protected String VARIABLE_START = "${";

    static protected String INSTRUCTION_END = "<%";
    static protected String COMMENT_END = "--%>";
    static protected String VARIABLE_END = "${";

    static protected String PUNCTUATION = "|()[]{}?:.,";

    static protected Pattern REGEX_ALPHANUM_END = Pattern.compile("[A-Za-z0-9]$");
    static protected Pattern REGEX_WHITESPACE_END = Pattern.compile("[\\s]+$");
    protected Pattern REGEX_OPERATOR = null;

    int OPERATOR_LENGTH = 0;

    final static List<String> OPERATORS = new ArrayList<String>();

    static {
        //https://github.com/silverstripe/silverstripe-framework/blob/3/view/SSTemplateParser.php
        OPERATORS.add("=");
        OPERATORS.add("not");
        OPERATORS.add("+");
        OPERATORS.add("-");
        OPERATORS.add("or");
        OPERATORS.add("and");
        OPERATORS.add("==");
        OPERATORS.add("!=");
        OPERATORS.add(">");
        OPERATORS.add("<");
        OPERATORS.add(">=");
        OPERATORS.add("<=");

    }

    ;

    protected class SortOperators implements Comparator<String> {

        @Override
        public int compare(String a, String b) {
            return a.length() - b.length();
        }
    }

    protected String implode(List<String> list, String delimeter) {
        String s = "";
        boolean first = true;
        for (String item : list) {
            if (!first) {
                s += delimeter;
            }
            s += item;
            first = false;
        }
        return s;
    }

    protected void initialize() {

        Collections.sort(OPERATORS, new SortOperators());
        Collections.reverse(OPERATORS);

        ArrayList<String> regex = new ArrayList<String>();

        for (String operator : OPERATORS) {

            if (REGEX_ALPHANUM_END.matcher(operator).find()) {
                regex.add(Pattern.quote(operator) + "[ ()]");
                if (operator.length() + 1 > OPERATOR_LENGTH) {
                    OPERATOR_LENGTH = operator.length() + 1;
                }
            } else {
                regex.add(Pattern.quote(operator));
                if (operator.length() > OPERATOR_LENGTH) {
                    OPERATOR_LENGTH = operator.length();
                }
            }

        }

        REGEX_OPERATOR = Pattern.compile("^" + implode(regex, "|^"));

    }

//    public boolean isInputComment() {
//        for (int i = 0; i < 2; i++) {
//            input.read();
//        }
//        String nextTwo = input.readText(input.readLength() - 2, input.readLength()).toString();
//        input.backup(2);
//        return nextTwo.equals("--");
//    }
    public SSTemplateTokenId findNextToken() {

        int c = input.read();
        int d = c;
        if (c == LexerInput.EOF) {
            return null;
        }

        Matcher matcher;

        while (c != LexerInput.EOF) {

            CharSequence text = input.readText();
            d = c;
            Debugger.oneLine("text='" + text + "', state.main='" + state.main + "'");

            switch (state.main) {

                case INIT:

                    if (CharSequenceUtilities.startsWith(text, COMMENT_START)) {
                        state.main = SSTemplateLexerState.Main.COMMENT;
                        Debugger.oneLine("state.main='" + state.main + "'");
                    } else if (CharSequenceUtilities.startsWith(text, INSTRUCTION_START)) {
                        state.main = SSTemplateLexerState.Main.INSTRUCTION;
                        state.sub = SSTemplateLexerState.Sub.INIT;
                        SSTemplateTokenId result = SSTemplateTokenId.T_SSTEMPLATE_INSTRUCTION;
                        // Since a comment(<%--) and an inscruction (<%) start the same, We need
                        // to double check the instruction is not actually a comment by peeking
                        // at the next two characters
                        String nextTwoChars = SSTemplateLexerInputHelper.peek(input, 2);
                        if (CharSequenceUtilities.startsWith(text + nextTwoChars, COMMENT_START)) {
                            result = SSTemplateTokenId.T_SSTEMPLATE_COMMENT;
                            state.main = SSTemplateLexerState.Main.COMMENT;

                        }
                        return result;
                    } else if (CharSequenceUtilities.startsWith(text, VARIABLE_START)) {
                        state.main = SSTemplateLexerState.Main.VARIABLE;
                        state.sub = SSTemplateLexerState.Sub.INIT;
                        Debugger.oneLine("state.main='" + state.main + "', state.sub='" + state.sub + "' return " + SSTemplateTokenId.T_SSTEMPLATE_VARIABLE);
                        return SSTemplateTokenId.T_SSTEMPLATE_VARIABLE;
                    }
                    break;

                case COMMENT:

                    break;

                case VARIABLE:
                case INSTRUCTION:
                    /* End markups */
                    if (state.main == SSTemplateLexerState.Main.VARIABLE) {
                        if (c == '}' || Character.isDigit(c) || Character.isAlphabetic(c)) {
                            Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_VARIABLE);
                            return SSTemplateTokenId.T_SSTEMPLATE_VARIABLE;
                        } else if (c == '.') {
                            return SSTemplateTokenId.T_SSTEMPLATE_PUNCTUATION;
                        } else {
                            return SSTemplateTokenId.T_SSTEMPLATE_OTHER;
                        }
                    }


                    /* Whitespaces */
                    if (Character.isWhitespace(c)) {

                        do {
                            c = input.read();
                        } while (c != LexerInput.EOF && Character.isWhitespace(c));

                        if (c != LexerInput.EOF) {
                            input.backup(1);
                        }
                        Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_WHITESPACE);
                        return SSTemplateTokenId.T_SSTEMPLATE_WHITESPACE;

                    }

                    /* End markups */
                    if (c == '%' || c == '>') {

                        d = input.read();

                        if (d == LexerInput.EOF) {
                            Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_OTHER);
                            return SSTemplateTokenId.T_SSTEMPLATE_OTHER;
                        }

                        int e = input.read();

                        if (d == '>' && e == LexerInput.EOF) {

                            if (state.main == SSTemplateLexerState.Main.INSTRUCTION && c == '%') {
                                Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_INSTRUCTION);
                                return SSTemplateTokenId.T_SSTEMPLATE_INSTRUCTION;
                            }

                            if (state.main == SSTemplateLexerState.Main.VARIABLE && c == '}') {
                                Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_VARIABLE);
                                return SSTemplateTokenId.T_SSTEMPLATE_VARIABLE;
                            }

                        }

                        input.backup(2);

                    }

                    /* Operators */
                    if (!(state.main == SSTemplateLexerState.Main.INSTRUCTION && state.sub == SSTemplateLexerState.Sub.INIT)) {

                        d = c;

                        int characters = 0;
                        while (c != LexerInput.EOF && input.readLength() < OPERATOR_LENGTH) {
                            c = input.read();
                            characters++;
                        }

                        matcher = REGEX_OPERATOR.matcher(input.readText());
                        if (matcher.find()) {

                            String operator = matcher.group();
                            matcher = REGEX_WHITESPACE_END.matcher(operator);

                            if (matcher.find()) {

                                input.backup(characters - matcher.start());
                                return SSTemplateTokenId.T_SSTEMPLATE_OPERATOR;

                            } else {

                                input.backup(characters - operator.length() + 1);
                                Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_OPERATOR);
                                return SSTemplateTokenId.T_SSTEMPLATE_OPERATOR;

                            }

                        }

                        input.backup(characters);
                        c = d;

                    } else if (c == '-') { /* Trim operator */

                        Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_OPERATOR);

                        return SSTemplateTokenId.T_SSTEMPLATE_OPERATOR;
                    }

                    /* Names */
                    if (Character.isLetter(c) || c == '_') {

                        do {
                            c = input.read();
                        } while (c != LexerInput.EOF && (Character.isLetter(c) || Character.isDigit(c) || c == '_'));

                        if (c != LexerInput.EOF) {
                            input.backup(1);
                        }

                        if (state.main == SSTemplateLexerState.Main.INSTRUCTION && state.sub == SSTemplateLexerState.Sub.INIT) {
                            state.sub = SSTemplateLexerState.Sub.NONE;
                            Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_FUNCTION);
                            return SSTemplateTokenId.T_SSTEMPLATE_FUNCTION;
                        } else {
                            Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_NAME);
                            return SSTemplateTokenId.T_SSTEMPLATE_NAME;
                        }

                    }

                    /* Numbers */
                    if (Character.isDigit(c)) {

                        boolean dotFound = false;

                        do {
                            if (c == '.') {
                                dotFound = true;
                            }
                            c = input.read();
                        } while (c != LexerInput.EOF && (Character.isDigit(c) || (!dotFound && c == '.')));

                        if (c != LexerInput.EOF) {
                            input.backup(1);
                        }
                        Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_NUMBER);
                        return SSTemplateTokenId.T_SSTEMPLATE_NUMBER;

                    }

                    /* Double quoted strings */
                    if (c == '"') {

                        boolean escaped = false;

                        do {
                            if (c == '\\' && !escaped) {
                                escaped = true;
                            } else {
                                escaped = false;
                            }
                            c = input.read();
                        } while (c != LexerInput.EOF && (escaped || c != '"'));
                        Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_STRING);

                        return SSTemplateTokenId.T_SSTEMPLATE_STRING;

                    }

                    /* Single quoted strings */
                    if (c == '\'') {

                        boolean escaped = false;

                        do {
                            if (c == '\\' && !escaped) {
                                escaped = true;
                            } else {
                                escaped = false;
                            }
                            c = input.read();
                        } while (c != LexerInput.EOF && (escaped || c != '\''));
                        Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_STRING);

                        return SSTemplateTokenId.T_SSTEMPLATE_STRING;

                    }

                    /* PUNCTUATION */
                    if (PUNCTUATION.indexOf(c) >= 0) {
                        Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_PUNCTUATION);
                        return SSTemplateTokenId.T_SSTEMPLATE_PUNCTUATION;
                    }
                    Debugger.oneLine("text='" + text + "', state.main='" + state.main + "' return " + SSTemplateTokenId.T_SSTEMPLATE_OTHER);

                    return SSTemplateTokenId.T_SSTEMPLATE_OTHER;

            }

            c = input.read();

        }

        if (state.main == SSTemplateLexerState.Main.COMMENT) {
            return SSTemplateTokenId.T_SSTEMPLATE_COMMENT;
        }
        return SSTemplateTokenId.T_SSTEMPLATE_OTHER;

    }

}
