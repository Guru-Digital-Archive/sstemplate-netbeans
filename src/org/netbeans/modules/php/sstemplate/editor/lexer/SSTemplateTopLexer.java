/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor.lexer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.lexer.Token;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.php.sstemplate.Debugger;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

public class SSTemplateTopLexer implements Lexer<SSTemplateTopTokenId> {

    protected SSTemplateTopLexerState state;
    protected final TokenFactory<SSTemplateTopTokenId> tokenFactory;
    protected final LexerInput input;

    static String OPEN_INSTRUCTION = "<%";
    static String OPEN_VARIABLE = "${";
    static String OPEN_COMMENT = "<%--";

    static String CLOSE_INSTRUCTION = "%>";
    static String CLOSE_VARIABLE = "}";
    static String CLOSE_COMMENT = "--%>";

    private SSTemplateTopLexer(LexerRestartInfo<SSTemplateTopTokenId> info) {

        tokenFactory = info.tokenFactory();
        input = info.input();
        state = info.state() == null ? new SSTemplateTopLexerState() : new SSTemplateTopLexerState((SSTemplateTopLexerState) info.state());

    }

    public static synchronized SSTemplateTopLexer create(LexerRestartInfo<SSTemplateTopTokenId> info) {
        return new SSTemplateTopLexer(info);
    }

    @Override
    public Token<SSTemplateTopTokenId> nextToken() {
        SSTemplateTopTokenId tokenId = findNextToken();
        return tokenId == null ? null : tokenFactory.createToken(tokenId);

    }

    @Override
    public Object state() {
        return new SSTemplateTopLexerState(state);
    }

    @Override
    public void release() {
    }

    SSTemplateTopLexerState.Type findTag(CharSequence text, boolean open) {
        SSTemplateTopLexerState.Type result = SSTemplateTopLexerState.Type.NONE;
        //Comments
        if (open && CharSequenceUtilities.endsWith(text, OPEN_COMMENT)) {
            result = SSTemplateTopLexerState.Type.COMMENT;
        } else if (!open && CharSequenceUtilities.endsWith(text, CLOSE_COMMENT)) {
            result = SSTemplateTopLexerState.Type.COMMENT;
            //Instructions
        } else if (open && CharSequenceUtilities.endsWith(text, OPEN_INSTRUCTION)) {
            result = SSTemplateTopLexerState.Type.INSTRUCTION;
        } else if (!open && CharSequenceUtilities.endsWith(text, CLOSE_INSTRUCTION)) {
            result = SSTemplateTopLexerState.Type.INSTRUCTION;
            //Variables
        } else if (open && CharSequenceUtilities.endsWith(text, OPEN_VARIABLE)) {
            result = SSTemplateTopLexerState.Type.VARIABLE;
        } else if (!open && CharSequenceUtilities.endsWith(text, CLOSE_VARIABLE)) {
            result = SSTemplateTopLexerState.Type.VARIABLE;
        }
        // Since a comment(<%--) and an inscruction (<%) start the same, We need
        // to double check the instruction is not actually a comment by peeking
        // at the next two characters
        if (result == SSTemplateTopLexerState.Type.INSTRUCTION) {
            String nextTwoChars = SSTemplateLexerInputHelper.peek(input, 2);
            if (findTag(text + nextTwoChars, open) == SSTemplateTopLexerState.Type.COMMENT) {
                result = SSTemplateTopLexerState.Type.COMMENT;
            }

        }
        Debugger.oneLine("findTag('" + text + "', " + open + "){ return " + result + "}");

        return result;
    }

    public SSTemplateTopTokenId findNextToken() {
        int c = input.read();
        SSTemplateTopLexerState.Type result;

        if (c == LexerInput.EOF) {
            return null;
        }

        while (c != LexerInput.EOF) {

            CharSequence text = input.readText();

            switch (state.main) {

                case INIT:
                case HTML:
                    result = findTag(text, true);
                    Debugger.oneLine("text='" + text + "', state.main='" + state.main + "', result='" + result + "'");
                    if (result != SSTemplateTopLexerState.Type.NONE) {
                        state.main = SSTemplateTopLexerState.Main.OPEN;
                        state.type = result;
                        if (input.readLength() > 2) {
                            input.backup(2);
                            return SSTemplateTopTokenId.T_HTML;
                        }
                    } else {
                        break;
                    }
                case OPEN:
                    if (input.readLength() == 2) {
                        state.main = SSTemplateTopLexerState.Main.SSTEMPLATE;
                    }
                    break;
                case SSTEMPLATE:
                    result = findTag(text, false);
                    Debugger.oneLine("text='" + text + "', state.main='" + state.main + "', result='" + result + "'");
                    if (result != SSTemplateTopLexerState.Type.NONE) {
                        if (result == SSTemplateTopLexerState.Type.COMMENT && state.type == SSTemplateTopLexerState.Type.INSTRUCTION) {
                            state.main = SSTemplateTopLexerState.Main.CLOSE;
                            if (input.readLength() > 2) {
                                input.backup(2);
                            }

                        } else if (result == state.type) {

                            boolean escape = false;
                            boolean doubleQuotes = false;
                            boolean singleQuotes = false;

                            if (result != SSTemplateTopLexerState.Type.COMMENT) {

                                for (int i = 0; i < text.length() - 2; i++) {
                                    char q = text.charAt(i);
                                    if (q == '\\') {
                                        escape = true;
                                    } else if (!escape) {
                                        if (q == '"' && !singleQuotes) {
                                            doubleQuotes = !doubleQuotes;
                                        } else if (q == '\'' && !doubleQuotes) {
                                            singleQuotes = !singleQuotes;
                                        }
                                    } else {
                                        escape = false;
                                    }
                                }

                            }

                            if (singleQuotes || doubleQuotes) {
                                break;
                            }

                            if (result == SSTemplateTopLexerState.Type.COMMENT) {
                                state.main = SSTemplateTopLexerState.Main.CLOSE;
                            } else {
                                state.main = SSTemplateTopLexerState.Main.CLOSE;
                            }

                            if (input.readLength() > 2) {
                                input.backup(2);
                            }
                        }
                        break;
                    }
                    break;
                case CLOSE:
                    if ((state.type == SSTemplateTopLexerState.Type.INSTRUCTION && CharSequenceUtilities.endsWith(text, CLOSE_INSTRUCTION))
                            || (state.type == SSTemplateTopLexerState.Type.VARIABLE && CharSequenceUtilities.endsWith(text, CLOSE_VARIABLE))
                            || ((state.type == SSTemplateTopLexerState.Type.COMMENT || state.type == SSTemplateTopLexerState.Type.INSTRUCTION) && CharSequenceUtilities.endsWith(text, CLOSE_COMMENT))) {
                        state.main = SSTemplateTopLexerState.Main.HTML;
                        return SSTemplateTopTokenId.T_SSTEMPLATE;
                    }
                    break;

            }

            c = input.read();

        }

        switch (state.main) {
            case SSTEMPLATE:
                return SSTemplateTopTokenId.T_SSTEMPLATE;
            case HTML:
                return SSTemplateTopTokenId.T_HTML;
        }

        return SSTemplateTopTokenId.T_HTML;

    }

}
