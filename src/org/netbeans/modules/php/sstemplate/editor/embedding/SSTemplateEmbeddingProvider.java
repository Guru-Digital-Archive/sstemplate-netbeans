/**
 * Silverstripe Template for Netbeans
 *
 * Copyright (c) 2015 Corey Sewell
 *
 * For warranty and licensing information, view the LICENSE file.
 */
package org.netbeans.modules.php.sstemplate.editor.embedding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.php.sstemplate.editor.lexer.SSTemplateTopTokenId;

public class SSTemplateEmbeddingProvider extends EmbeddingProvider {

    public static final String GENERATED_CODE = "@@@"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {

        TokenHierarchy<CharSequence> th = TokenHierarchy.create(snapshot.getText(), SSTemplateTopTokenId.language());
        TokenSequence<SSTemplateTopTokenId> sequence = th.tokenSequence(SSTemplateTopTokenId.language());
        if (sequence == null) {
            return Collections.emptyList();
        }

        sequence.moveStart();
        List<Embedding> embeddings = new ArrayList<Embedding>();

        int offset = -1;
        int length = 0;
        while (sequence.moveNext()) {
            Token t = sequence.token();

            if (t.id() == SSTemplateTopTokenId.T_HTML) {
                if (offset < 0) {
                    offset = sequence.offset();
                }
                length += t.length();
            } else if (offset >= 0) {
                embeddings.add(snapshot.create(offset, length, "text/html"));
                offset = -1;
                length = 0;
            }
        }

        if (offset >= 0) {
            embeddings.add(snapshot.create(offset, length, "text/html"));
        }

        if (embeddings.isEmpty()) {
            return Collections.singletonList(snapshot.create("", "text/html"));
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void cancel() {
    }

    public static final class Factory extends TaskFactory {

        @Override
        public Collection<SchedulerTask> create(final Snapshot snapshot) {
            return Collections.<SchedulerTask>singletonList(new SSTemplateEmbeddingProvider());
        }
    }

}
