/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor.gsf;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.CommentHandler;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.php.sstemplate.editor.completion.SSTemplateCompletionHandler;
import org.netbeans.modules.php.sstemplate.editor.format.SSTemplateFormatter;
import org.netbeans.modules.php.sstemplate.editor.lexer.SSTemplateTopTokenId;
import org.netbeans.modules.php.sstemplate.editor.parsing.SSTemplateParser;

@LanguageRegistration(mimeType = "text/sstemplate", useCustomEditorKit = true) //NOI18N
public class SSTemplateLanguage extends DefaultLanguageConfig {

    public SSTemplateLanguage() {
    }

    @Override
    public CommentHandler getCommentHandler() {
        return null;
    }

    @Override
    public Language getLexerLanguage() {
        return SSTemplateTopTokenId.language();
    }

    @Override
    public boolean isIdentifierChar(char c) {
        return Character.isLetter(c);
    }

    @Override
    public String getDisplayName() {
        return "SSTemplate";
    }

    @Override
    public String getPreferredExtension() {
        return "ss";
    }

    // Service registrations
    @Override
    public boolean isUsingCustomEditorKit() {
        return true;
    }

    @Override
    public Parser getParser() {
        return new SSTemplateParser();
    }

    @Override
    public boolean hasStructureScanner() {
        return true;
    }

    @Override
    public StructureScanner getStructureScanner() {
        return new SSTemplateStructureScanner();
    }

    @Override
    public boolean hasHintsProvider() {
        return false;
    }

    @Override
    public CodeCompletionHandler getCompletionHandler() {
        return new SSTemplateCompletionHandler();
    }

    @Override
    public boolean hasFormatter() {
        return true;
    }

    @Override
    public Formatter getFormatter() {
        return new SSTemplateFormatter();
    }

}
