package net.neoforged.gradle.platform.tasks;

import com.google.gson.Gson;
import net.neoforged.gradle.common.runtime.tasks.DefaultRuntime;
import net.neoforged.gradle.dsl.common.tasks.WithOutput;
import net.neoforged.gradle.dsl.common.tasks.WithWorkspace;
import net.neoforged.gradle.dsl.platform.model.LauncherProfile;
import net.neoforged.gradle.platform.util.ProfileFiller;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

@CacheableTask
public abstract class CreateLauncherJson extends DefaultRuntime implements WithOutput, WithWorkspace {

    public CreateLauncherJson() {
        getOutputFileName().set("version.json");
    }
    
    @TaskAction
    public void run() {
        final Gson gson = LauncherProfile.createGson(getObjectFactory());
        final File output = ensureFileWorkspaceReady(getOutput());
        
        final LauncherProfile profile = getProfile().get();
        final LauncherProfile clone = gson.fromJson(gson.toJson(profile), LauncherProfile.class);
        
        clone.getLibraries().addAll(
                getProviderFactory().provider(() -> {
                    final ProfileFiller profileFiller = new ProfileFiller(getObjectFactory());
                    getLibraries().getAsFileTree().visit(profileFiller);
                    return profileFiller.getLibraries();
                })
        );
        
        try {
            Files.write(output.toPath(), gson.toJson(clone).getBytes());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    @Nested
    public abstract Property<LauncherProfile> getProfile();
    
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getLibraries();
}
