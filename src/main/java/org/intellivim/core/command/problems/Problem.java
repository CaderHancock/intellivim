package org.intellivim.core.command.problems;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dhleong
 */
public class Problem {

    public static final char FIX_ID_SEPARATOR = '.';

    private final int id;
    private final int line;
    private final int col;
    private final int startOffset;
    private final int endOffset;
    private final HighlightSeverity severity;
    private final String description;

    /** it is too slow if we include all these */
    private transient final List<QuickFixDescriptor> fixes;

    private Problem(int id, int line, int col,
            int startOffset, int endOffset,
            HighlightSeverity severity,
            String description,
            List<QuickFixDescriptor> fixes) {
        this.id = id;
        this.line = line;
        this.col = col;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.severity = severity;
        this.description = description;
        this.fixes = fixes;
    }

    public boolean containsOffset(int offset) {
        return offset >= startOffset && offset < endOffset;
    }

    /**
     * @param line 1-based line number
     * @return True if this problem exists on the given line
     */
    public boolean isOnLine(int line) {
        return this.line == line;
    }

    public String getDescription() {
        return description;
    }

    public List<QuickFixDescriptor> getFixes() {
        return fixes;
    }

    public boolean isError() {
        return severity == HighlightSeverity.ERROR;
    }

    @Override
    public String toString() {
        return String.format("[%d@%d:%d][%s]%s",
                id, line, col,
                severity,
                description );
    }

    public static Problem from(int id, EditorEx editor, PsiFile file, HighlightInfo info) {
        if (info.getDescription() == null)
            return null;

        final Project project = editor.getProject();
        if (project == null) {
            throw new IllegalArgumentException(); // shouldn't happen
        }

        final String description = info.getDescription();
        final Document doc = editor.getDocument();
        final int line = doc.getLineNumber(info.getStartOffset());
        final int col = info.getStartOffset() - doc.getLineStartOffset(line);

        List<QuickFixDescriptor> quickFixes = new ArrayList<QuickFixDescriptor>();
        int quickFixNumber = 0;
        if (info.quickFixActionRanges != null) {
            for (Pair<HighlightInfo.IntentionActionDescriptor, TextRange> pair
                    : info.quickFixActionRanges) {

                final String quickFixId = "" + id + FIX_ID_SEPARATOR + quickFixNumber++;
                final HighlightInfo.IntentionActionDescriptor desc = pair.getFirst();
                final TextRange range = pair.getSecond();

                final IntentionAction action = desc.getAction();
                if (action.isAvailable(project, editor, file)) {
                    quickFixes.add(QuickFixDescriptor.from(description, quickFixId, desc, range));
                }
            }
        }

        // the lines returned are 0-indexed, and we want 1-indexed
        // the offsets also start at 0, so our cols will be 0-indexed also
        return new Problem(id,
                line + 1, col + 1,
                info.getActualStartOffset(), info.getActualEndOffset(),
                info.getSeverity(),
                description,
                quickFixes);
    }

    public QuickFixDescriptor locateQuickFix(String fixId) {
        final String id = fixId.substring(fixId.indexOf(FIX_ID_SEPARATOR) + 1);
        final int index = Integer.parseInt(id);
        return fixes.get(index);
    }

}
