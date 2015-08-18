/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor.lexer;

import org.netbeans.spi.lexer.LexerInput;

/**
 *
 * @author corey
 */
public class SSTemplateLexerInputHelper {

    public static String peek(LexerInput input, int count) {
        String result;
        int readCount = 0;
        for (int i = 0; i < count; i++) {
            readCount++;
            if (input.read() == LexerInput.EOF) {
                break;
            }
        }
        result = input.readText(input.readLength() - readCount, input.readLength()).toString();
        input.backup(readCount);
        return result;
    }
}
