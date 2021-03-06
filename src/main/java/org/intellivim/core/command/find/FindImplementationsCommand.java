package org.intellivim.core.command.find;

import com.intellij.codeInsight.navigation.ImplementationSearcher;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.intellivim.Command;
import org.intellivim.ProjectCommand;
import org.intellivim.Required;
import org.intellivim.Result;
import org.intellivim.SimpleResult;
import org.intellivim.core.model.VimEditor;
import org.intellivim.inject.Inject;

import java.util.List;

/**
 * Find implementations for the element at the given offset
 * @author dhleong
 */
@Command("find_implementations")
public class FindImplementationsCommand extends ProjectCommand {

    @Required @Inject PsiFile file;
    @Required int offset;

    public FindImplementationsCommand(final Project project,
            final PsiFile file, int offset) {
        super(project);

        this.file = file;
        this.offset = offset;
    }

    @Override
    public Result execute() {

        final Editor editor = createEditor(file, offset);
        final PsiElement element = VimEditor.findTargetElement(editor);

        final PsiElement[] implementations = new ImplementationSearcher()
                .searchImplementations(editor, element, offset);
        final List<LocationResult> results = ContainerUtil.map(implementations,
                new Function<PsiElement, LocationResult>() {
                    @Override
                    public LocationResult fun(PsiElement psiElement) {
                        return new LocationResult(psiElement);
                    }
                });

        return SimpleResult.success(results);
    }
}
