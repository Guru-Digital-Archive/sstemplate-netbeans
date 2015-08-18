/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor.lexer;

public class SSTemplateLexerState {

    public enum Main {
        INIT,
        COMMENT,
        VARIABLE,
        INSTRUCTION
    };

    public enum Sub {

        NONE, INIT
    };

    Main main;
    Sub sub;

    public SSTemplateLexerState() {
        main = Main.INIT;
        sub = Sub.NONE;
    }

    public SSTemplateLexerState(SSTemplateLexerState copy) {
        main = copy.main;
        sub = copy.sub;
    }

    public SSTemplateLexerState(Main main, Sub sub) {
        this.main = main;
        this.sub = sub;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        SSTemplateLexerState compare = (SSTemplateLexerState) object;
        return !(main != compare.main ||sub != compare.sub);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.main != null ? this.main.hashCode() : 0);
        hash = 97 * hash + (this.sub != null ? this.sub.hashCode() : 0);
        return hash;
    }

}
