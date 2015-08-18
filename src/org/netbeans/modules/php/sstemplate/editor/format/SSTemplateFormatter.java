/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor.format;

import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;

public class SSTemplateFormatter implements Formatter {

    @Override
    public void reformat(Context context, ParserResult info) {
    }

    @Override
    public void reindent(Context context) {
    }

    @Override
    public boolean needsParserResult() {
        return false;
    }

    @Override
    public int indentSize() {
        return 0;
    }

    @Override
    public int hangingIndentSize() {
        return 0;
    }

}
