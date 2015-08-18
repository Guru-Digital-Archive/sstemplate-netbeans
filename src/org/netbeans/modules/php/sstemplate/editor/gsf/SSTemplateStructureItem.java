/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor.gsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.php.sstemplate.editor.parsing.SSTemplateParserResult;
import org.openide.filesystems.FileObject;

public class SSTemplateStructureItem implements StructureItem {

    List<SSTemplateStructureItem> blocks;
    SSTemplateParserResult.Block item;
    Snapshot snapshot;

    public SSTemplateStructureItem(Snapshot snapshot, SSTemplateParserResult.Block item, List<SSTemplateParserResult.Block> blocks) {

        this.item = item;
        this.blocks = new ArrayList<SSTemplateStructureItem>();
        this.snapshot = snapshot;

        for (SSTemplateParserResult.Block current : blocks) {

            if (item.getOffset() < current.getOffset()
                    && current.getOffset() + current.getLength() < item.getOffset() + item.getLength()) {

                this.blocks.add(new SSTemplateStructureItem(snapshot, current, blocks));

            }

        }

    }

    @Override
    public String getName() {
        return "Block " + item.getExtra();
    }

    @Override
    public String getSortText() {
        return "Block " + item.getDescription();
    }

    @Override
    public String getHtml(HtmlFormatter hf) {
        return "Block " + item.getExtra();
    }

    @Override
    public ElementHandle getElementHandle() {
        return new SSTemplateElementHandle(item, snapshot);
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.ATTRIBUTE;
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (CharSequenceUtilities.startsWith(item.getDescription(), "*")) {
            return Collections.singleton(Modifier.STATIC);
        }
        return Collections.emptySet();
    }

    @Override
    public boolean isLeaf() {
        return blocks.isEmpty();
    }

    @Override
    public List<? extends StructureItem> getNestedItems() {
        return blocks;
    }

    @Override
    public long getPosition() {
        return item.getOffset();
    }

    @Override
    public long getEndPosition() {
        return item.getOffset() + item.getLength();
    }

    @Override
    public ImageIcon getCustomIcon() {
        return null;
    }

    class SSTemplateElementHandle implements ElementHandle {

        SSTemplateParserResult.Block item;
        Snapshot snapshot;

        public SSTemplateElementHandle(SSTemplateParserResult.Block item, Snapshot snapshot) {
            this.item = item;
            this.snapshot = snapshot;
        }

        @Override
        public FileObject getFileObject() {
            return snapshot.getSource().getFileObject();
        }

        @Override
        public String getMimeType() {
            return "text/sstemplate";
        }

        @Override
        public String getName() {
            return "Block " + item.getExtra();
        }

        @Override
        public String getIn() {
            return "Block " + item.getExtra();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.ATTRIBUTE;
        }

        @Override
        public Set<Modifier> getModifiers() {
            if (CharSequenceUtilities.startsWith(item.getDescription(), "*")) {
                return Collections.singleton(Modifier.STATIC);
            }
            return Collections.emptySet();
        }

        @Override
        public boolean signatureEquals(ElementHandle eh) {
            if (!(eh instanceof SSTemplateElementHandle)) {
                return false;
            }
            if (eh.getName().equals(this.getName())) {
                return true;
            }
            return false;
        }

        @Override
        public OffsetRange getOffsetRange(ParserResult pr) {
            return new OffsetRange(item.getOffset(), item.getOffset() + item.getLength());
        }

    }

}
