/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor;

import javax.swing.text.Document;
import org.netbeans.modules.editor.NbEditorKit;

public class SSTemplateEditorKit extends NbEditorKit {

    @Override
    public Document createDefaultDocument() {

        return super.createDefaultDocument();

    }

    @Override
    public String getContentType() {
        return "text/sstemplate";
    }

}
