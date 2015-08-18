/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor.lexer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

public enum SSTemplateTokenId implements TokenId {

    T_SSTEMPLATE_NAME(null, "sstemplate_name"),
    T_SSTEMPLATE_STRING(null, "sstemplate_string"),
    T_SSTEMPLATE_NUMBER(null, "sstemplate_number"),
    T_SSTEMPLATE_OPERATOR(null, "sstemplate_operator"),
    T_SSTEMPLATE_PUNCTUATION(null, "sstemplate_punctuation"),
    T_SSTEMPLATE_WHITESPACE(null, "sstemplate_whitespace"),
    T_SSTEMPLATE_FUNCTION(null, "sstemplate_function"),
    T_SSTEMPLATE_INSTRUCTION(null, "sstemplate_instruction"),
    T_SSTEMPLATE_VARIABLE(null, "sstemplate_variable"),
    T_SSTEMPLATE_COMMENT(null, "sstemplate_comment"),
    T_SSTEMPLATE_OTHER(null, "sstemplate_other");

    private final String fixedText;
    private final String primaryCategory;

    SSTemplateTokenId(String fixedText, String primaryCategory) {
        this.fixedText = fixedText;
        this.primaryCategory = primaryCategory;
    }

    public String fixedText() {
        return fixedText;
    }

    @Override
    public String primaryCategory() {
        return primaryCategory;
    }

    private static final Language<SSTemplateTokenId> language
            = new LanguageHierarchy<SSTemplateTokenId>() {

                @Override
                protected Collection<SSTemplateTokenId> createTokenIds() {
                    return EnumSet.allOf(SSTemplateTokenId.class);
                }

                @Override
                protected Map<String, Collection<SSTemplateTokenId>> createTokenCategories() {
                    Map<String, Collection<SSTemplateTokenId>> cats = new HashMap<String, Collection<SSTemplateTokenId>>();
                    return cats;
                }

                @Override
                protected Lexer<SSTemplateTokenId> createLexer(LexerRestartInfo<SSTemplateTokenId> info) {
                    return SSTemplateLexer.create(info);
                }

                @Override
                protected String mimeType() {
                    return "text/sstemplate-markup";
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<SSTemplateTokenId> token,
                        LanguagePath languagePath, InputAttributes inputAttributes) {
                    return null;
                }
            }.language();

    public static Language<SSTemplateTokenId> language() {
        return language;
    }
}
