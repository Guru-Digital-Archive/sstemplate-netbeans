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
import java.util.Map;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.sstemplate.editor.parsing.SSTemplateParserResult;

public class SSTemplateStructureScanner implements StructureScanner {

    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        SSTemplateParserResult result = (SSTemplateParserResult) info;
        List<SSTemplateParserResult.Block> blocks = new ArrayList<SSTemplateParserResult.Block>();
        List<SSTemplateStructureItem> items = new ArrayList<SSTemplateStructureItem>();

        for (SSTemplateParserResult.Block item : result.getBlocks()) {
            if (CharSequenceUtilities.equals(item.getDescription(), "block") || CharSequenceUtilities.equals(item.getDescription(), "*inline-block")) {
                blocks.add(item);
            }
        }

        boolean isTopLevel = false;

        for (SSTemplateParserResult.Block item : blocks) {

            isTopLevel = true;

            for (SSTemplateParserResult.Block check : blocks) {

                if (item.getOffset() > check.getOffset()
                        && item.getOffset() + item.getLength() < check.getOffset() + check.getLength()) {
                    isTopLevel = false;
                    break;
                }

            }

            if (isTopLevel) {
                items.add(new SSTemplateStructureItem(result.getSnapshot(), item, blocks));
            }

        }

        return items;

    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {

        SSTemplateParserResult result = (SSTemplateParserResult) info;
        List<OffsetRange> ranges = new ArrayList<OffsetRange>();

        for (SSTemplateParserResult.Block block : result.getBlocks()) {

            ranges.add(new OffsetRange(
                    block.getOffset(), block.getOffset() + block.getLength()
            ));

        }

        return Collections.singletonMap("tags", ranges);

    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

}
