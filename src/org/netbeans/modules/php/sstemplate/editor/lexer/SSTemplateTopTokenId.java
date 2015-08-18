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
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LanguageEmbedding;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

public enum SSTemplateTopTokenId implements TokenId {

    T_HTML(null, "sstemplate_html"),
    T_ERROR(null, "sstemplate_error"),
    T_SSTEMPLATE(null, "sstemplate");

    private String fixedText;
    private String primaryCategory;

    SSTemplateTopTokenId(String fixedText, String primaryCategory) {
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
    private static final Language<SSTemplateTopTokenId> language
            = new LanguageHierarchy<SSTemplateTopTokenId>() {

                @Override
                protected Collection<SSTemplateTopTokenId> createTokenIds() {
                    return EnumSet.allOf(SSTemplateTopTokenId.class);
                }

                @Override
                protected Map<String, Collection<SSTemplateTopTokenId>> createTokenCategories() {
                    Map<String, Collection<SSTemplateTopTokenId>> cats = new HashMap<String, Collection<SSTemplateTopTokenId>>();
                    return cats;
                }

                @Override
                protected Lexer<SSTemplateTopTokenId> createLexer(LexerRestartInfo<SSTemplateTopTokenId> info) {
                    return SSTemplateTopLexer.create(info);
                }

                @Override
                protected String mimeType() {
                    return "text/sstemplate";
                }

                @Override
                protected LanguageEmbedding<?> embedding(Token<SSTemplateTopTokenId> token,
                        LanguagePath languagePath, InputAttributes inputAttributes) {

                    SSTemplateTopTokenId id = token.id();
                    if (id == T_HTML) {
                        return LanguageEmbedding.create(HTMLTokenId.language(), 0, 0, true);
                    } else if (id == T_SSTEMPLATE) {
                        return LanguageEmbedding.create(SSTemplateTokenId.language(), 0, 0);
                    }

                    return null;

                }
            }.language();

    public static Language<SSTemplateTopTokenId> language() {
        return language;
    }
}
