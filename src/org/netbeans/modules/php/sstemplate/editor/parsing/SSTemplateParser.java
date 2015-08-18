/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import javax.swing.event.ChangeListener;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.php.sstemplate.editor.lexer.SSTemplateTokenId;

public class SSTemplateParser extends Parser {

    Snapshot snapshot;
    SSTemplateParserResult result;

    final static List<String> parseElements = new ArrayList<String>();

    static {
        // https://docs.silverstripe.org/en/3.1/developer_guides/templates/syntax/
        // https://github.com/silverstripe/silverstripe-framework/blob/3/view/SSTemplateParser.php
        parseElements.add("loop");
        parseElements.add("end_loop");

        parseElements.add("if");
        parseElements.add("else");
        parseElements.add("else_if");
        parseElements.add("end_if");

        parseElements.add("with");
        parseElements.add("end_with");

        parseElements.add("require");

        parseElements.add("include");

        parseElements.add("cached");
        parseElements.add("cacheblock");

    }

    @Override
    public void parse(Snapshot snapshot, Task task, SourceModificationEvent sme) throws ParseException {
        this.snapshot = snapshot;
        result = new SSTemplateParserResult(snapshot);

        TokenHierarchy<?> tokenHierarchy = snapshot.getTokenHierarchy();

        LanguagePath sstemplatePath = null;

        for (LanguagePath path : tokenHierarchy.languagePaths()) {

            if (path.mimePath().endsWith("sstemplate-markup")) {
                sstemplatePath = path;
                break;
            }

        }

        if (sstemplatePath != null) {

            List<TokenSequence<?>> tokenSequenceList = tokenHierarchy.tokenSequenceList(sstemplatePath, 0, Integer.MAX_VALUE);
            List<Instruction> instructionList = new ArrayList<Instruction>();

            for (TokenSequence<?> sequence : tokenSequenceList) {

                while (sequence.moveNext()) {

                    Token<SSTemplateTokenId> token = (Token<SSTemplateTokenId>) sequence.token();

                    /* Parse instruction */
                    if (token.id() == SSTemplateTokenId.T_SSTEMPLATE_INSTRUCTION) {

                        Instruction instruction = new Instruction();
                        instruction.function = "";
                        instruction.startTokenIndex = sequence.index();
                        instruction.endTokenIndex = sequence.index();
                        instruction.from = token.offset(tokenHierarchy);

                        while (sequence.moveNext()) {

                            token = (Token<SSTemplateTokenId>) sequence.token();
                            if (token.id() == SSTemplateTokenId.T_SSTEMPLATE_NAME) {
                                instruction.extra = token.text();
                            }
                            if (token.id() == SSTemplateTokenId.T_SSTEMPLATE_INSTRUCTION) {
                                instruction.endTokenIndex = sequence.index();
                                instruction.length = token.offset(tokenHierarchy) - instruction.from + token.length();
                                break;
                            }

                        }

                        if (instruction.startTokenIndex != instruction.endTokenIndex) { // Closed instruction found

                            sequence.moveIndex(instruction.startTokenIndex);

                            while (sequence.moveNext()) {

                                token = (Token<SSTemplateTokenId>) sequence.token();
                                if (token.id() == SSTemplateTokenId.T_SSTEMPLATE_FUNCTION) {

                                    instruction.function = token.text();
                                    instruction.functionTokenIndex = sequence.index();
                                    instruction.functionFrom = token.offset(tokenHierarchy);
                                    instruction.functionLength = token.length();
                                    break;

                                }

                            }

                            if (parseElements.contains(instruction.function.toString())) {
                                /* Have we captured a standalone instruction? */
                                if (CharSequenceUtilities.equals(instruction.function, "block")) {

                                    boolean standalone = false;
                                    int names = 0;

                                    do {

                                        sequence.moveNext();
                                        token = (Token<SSTemplateTokenId>) sequence.token();

                                        if (token.id() == SSTemplateTokenId.T_SSTEMPLATE_NAME || token.id() == SSTemplateTokenId.T_SSTEMPLATE_STRING) {
                                            names++;
                                        }

                                        if (names > 1) {
                                            standalone = true;
                                            break;
                                        }

                                    } while (sequence.index() < instruction.endTokenIndex);

                                    if (!standalone) {
                                        instructionList.add(instruction);
                                    } else { // add a inline "block" immediately to the result set
                                        result.addBlock("*inline-block", instruction.from, instruction.length, instruction.extra);
                                    }

                                } else if (CharSequenceUtilities.equals(instruction.function, "set")) {

                                    boolean standalone = false;

                                    do {

                                        sequence.moveNext();
                                        token = (Token<SSTemplateTokenId>) sequence.token();

                                        if (token.id() == SSTemplateTokenId.T_SSTEMPLATE_OPERATOR) {
                                            standalone = true;
                                            break;
                                        }

                                    } while (sequence.index() < instruction.endTokenIndex);

                                    if (!standalone) {
                                        instructionList.add(instruction);
                                    }

                                } else {
                                    instructionList.add(instruction);
                                }

                            }

                            sequence.moveIndex(instruction.endTokenIndex);

                        }

                    }

                }

            } // endfor: All instructions are now saved in instructionList

            /* Analyse instruction structure */
            Stack<Instruction> instructionStack = new Stack<Instruction>();

            for (Instruction instruction : instructionList) {

                if (CharSequenceUtilities.startsWith(instruction.function, "end")) {

                    if (instructionStack.empty()) { // End tag, but no more tokens on stack!

                        result.addError(
                                "Unopened '" + instruction.function + "' block",
                                instruction.functionFrom,
                                instruction.functionLength
                        );

                    } else if (CharSequenceUtilities.endsWith(instruction.function, instructionStack.peek().function)) {
                        // end[sth] found a [sth] on the stack!

                        Instruction start = instructionStack.pop();
                        result.addBlock(start.function, start.from, instruction.from - start.from + instruction.length, start.extra);

                    } else {
                        // something wrong lies on the stack!
                        // assume that current token is invalid and let it stay on the stack

                        result.addError(
                                "Unexpected '" + instruction.function + "', expected 'end" + instructionStack.peek().function + "'",
                                instruction.functionFrom,
                                instruction.functionLength
                        );

                    }

                } else {
                    instructionStack.push(instruction);
                }

            }

            // All instructions were parsed. Are there any left on the stack?
            if (!instructionStack.empty()) {
                // Yep, they were never closed!

                while (!instructionStack.empty()) {

                    Instruction instruction = instructionStack.pop();

                    result.addError(
                            "Unclosed '" + instruction.function + "'",
                            instruction.functionFrom,
                            instruction.functionLength
                    );

                }

            }

            // Parsing done!
        }

    }

    @Override
    public Result getResult(Task task) throws ParseException {
        return result;
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
    }

    static public class Factory extends ParserFactory {

        @Override
        public Parser createParser(Collection<Snapshot> clctn) {
            return new SSTemplateParser();
        }

    }

    class Instruction {

        CharSequence function = null;
        CharSequence extra = null;
        int startTokenIndex = 0;
        int endTokenIndex = 0;
        int functionTokenIndex = 0;

        int from = 0;
        int length = 0;

        int functionFrom = 0;
        int functionLength = 0;

    }

}
