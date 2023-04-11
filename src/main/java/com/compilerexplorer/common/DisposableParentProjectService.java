package com.compilerexplorer.common;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
public final class DisposableParentProjectService implements Disposable {
    public static DisposableParentProjectService getInstance(@NotNull Project project) {
        return project.getService(DisposableParentProjectService.class);
    }

    public DisposableParentProjectService(@SuppressWarnings("unused") Project project) {
        // empty
    }

    public void dispose() {
        // empty
    }
}
