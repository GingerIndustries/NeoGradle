package net.minecraftforge.gradle.runs.tasks;

import net.minecraftforge.gradle.dsl.runs.run.Run;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Nested;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "This runs a game. It should not be cached.")
public abstract class RunExec extends JavaExec {

    public static final String GROUP = "ForgeGradle/Runs";

    public RunExec() {
        super();

        setGroup(GROUP);
    }

    @Override
    public void exec() {
        final Run run = getRun().get();

        setWorkingDir(run.getWorkingDirectory().get().getAsFile());
        getMainClass().set(run.getMainClass().get());

        JavaToolchainService service = getProject().getExtensions().getByType(JavaToolchainService.class);
        getJavaLauncher().set(service.launcherFor(getProject().getExtensions().getByType(JavaPluginExtension.class).getToolchain()));

        args(run.getProgramArguments().get());
        jvmArgs(run.getJvmArguments().get());

        environment(run.getEnvironmentVariables().get());
        systemProperties(run.getSystemProperties().get());

        super.exec();
    }

    @Nested
    public abstract Property<Run> getRun();
}