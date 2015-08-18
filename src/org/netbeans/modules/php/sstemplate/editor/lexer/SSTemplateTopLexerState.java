/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor.lexer;

public class SSTemplateTopLexerState {

    public enum Main {

        INIT,
        HTML,
        OPEN,
        SSTEMPLATE,
        CLOSE
    };

    public enum Type {

        NONE, INSTRUCTION, VARIABLE, COMMENT
    };

    Main main;
    Type type;

    public SSTemplateTopLexerState() {
        main = Main.INIT;
        type = Type.NONE;
    }

    public SSTemplateTopLexerState(SSTemplateTopLexerState copy) {
        main = copy.main;
        type = copy.type;
    }

    public SSTemplateTopLexerState(Main main, Type type) {
        this.main = main;
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.main != null ? this.main.hashCode() : 0);
        hash = 97 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        SSTemplateTopLexerState compare = (SSTemplateTopLexerState) object;
        if (main != compare.main) {
            return false;
        }
        if (type != compare.type) {
            return false;
        }
        return true;
    }

}
