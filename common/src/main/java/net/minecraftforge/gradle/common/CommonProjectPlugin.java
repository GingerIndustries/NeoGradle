package net.minecraftforge.gradle.common;

import net.minecraftforge.gradle.common.extensions.ExtensionManager;
import net.minecraftforge.gradle.common.extensions.ForcedDependencyDeobfuscationExtension;
import net.minecraftforge.gradle.common.extensions.IdeManagementExtension;
import net.minecraftforge.gradle.common.extensions.ProjectEvaluationExtension;
import net.minecraftforge.gradle.common.extensions.dependency.creation.ProjectBasedDependencyCreator;
import net.minecraftforge.gradle.common.deobfuscation.DependencyDeobfuscator;
import net.minecraftforge.gradle.common.extensions.AccessTransformersExtension;
import net.minecraftforge.gradle.common.extensions.ArtifactDownloaderExtension;
import net.minecraftforge.gradle.common.extensions.MappingsExtension;
import net.minecraftforge.gradle.common.extensions.MinecraftArtifactCacheExtension;
import net.minecraftforge.gradle.common.extensions.MinecraftExtension;
import net.minecraftforge.gradle.common.extensions.ProjectHolderExtension;
import net.minecraftforge.gradle.common.extensions.dependency.replacement.DependencyReplacementsExtension;
import net.minecraftforge.gradle.common.extensions.obfuscation.ObfuscationExtension;
import net.minecraftforge.gradle.common.extensions.repository.IvyDummyRepositoryExtension;
import net.minecraftforge.gradle.common.runs.run.RunsImpl;
import net.minecraftforge.gradle.common.runs.type.TypesImpl;
import net.minecraftforge.gradle.common.runtime.definition.CommonRuntimeDefinition;
import net.minecraftforge.gradle.common.runtime.extensions.CommonRuntimeExtension;
import net.minecraftforge.gradle.common.runtime.naming.OfficialNamingChannelConfigurator;
import net.minecraftforge.gradle.common.tasks.DisplayMappingsLicenseTask;
import net.minecraftforge.gradle.common.util.TaskDependencyUtils;
import net.minecraftforge.gradle.common.util.exceptions.MultipleDefinitionsFoundException;
import net.minecraftforge.gradle.common.util.constants.RunsConstants;
import net.minecraftforge.gradle.dsl.common.runs.type.Types;
import net.minecraftforge.gradle.dsl.common.util.NamingConstants;
import net.minecraftforge.gradle.dsl.common.extensions.AccessTransformers;
import net.minecraftforge.gradle.dsl.common.extensions.ArtifactDownloader;
import net.minecraftforge.gradle.dsl.common.extensions.Mappings;
import net.minecraftforge.gradle.dsl.common.extensions.Minecraft;
import net.minecraftforge.gradle.dsl.common.extensions.MinecraftArtifactCache;
import net.minecraftforge.gradle.dsl.common.extensions.ProjectHolder;
import net.minecraftforge.gradle.dsl.common.extensions.dependency.replacement.DependencyReplacement;
import net.minecraftforge.gradle.dsl.common.extensions.obfuscation.Obfuscation;
import net.minecraftforge.gradle.dsl.common.extensions.repository.Repository;
import net.minecraftforge.gradle.dsl.common.runs.run.Runs;
import net.minecraftforge.gradle.common.runs.run.RunImpl;
import net.minecraftforge.gradle.util.GradleInternalUtils;
import net.minecraftforge.gradle.util.UrlConstants;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.jetbrains.gradle.ext.IdeaExtPlugin;

public class CommonProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        //Apply the evaluation extension to monitor immediate execution of indirect tasks when evaluation already happened.
        project.getExtensions().create(NamingConstants.Extension.EVALUATION, ProjectEvaluationExtension.class, project);

        project.getPluginManager().apply(JavaPlugin.class);

        // Apply both the idea and eclipse IDE plugins
        project.getPluginManager().apply(IdeaPlugin.class);
        project.getPluginManager().apply(IdeaExtPlugin.class);
        project.getPluginManager().apply(EclipsePlugin.class);

        project.getExtensions().create(IdeManagementExtension.class, "ideManager", IdeManagementExtension.class, project);
        project.getExtensions().create(ArtifactDownloader.class, "artifactDownloader", ArtifactDownloaderExtension.class, project);
        project.getExtensions().create(Repository.class, "ivyDummyRepository", IvyDummyRepositoryExtension.class, project);
        project.getExtensions().create(MinecraftArtifactCache.class, "minecraftArtifactCache", MinecraftArtifactCacheExtension.class, project);
        project.getExtensions().create(DependencyReplacement.class, "dependencyReplacements", DependencyReplacementsExtension.class, project, project.getObjects().newInstance(ProjectBasedDependencyCreator.class, project));
        project.getExtensions().create(AccessTransformers.class, "accessTransformers", AccessTransformersExtension.class, project);
        project.getExtensions().create(Obfuscation.class, "obfuscation", ObfuscationExtension.class, project);
        project.getExtensions().create("extensionManager", ExtensionManager.class, project);
        project.getExtensions().create("forcedDeobfuscation", ForcedDependencyDeobfuscationExtension.class);
        project.getExtensions().create("dependencyDeobfuscation", DependencyDeobfuscator.class, project);

        final ExtensionManager extensionManager = project.getExtensions().getByType(ExtensionManager.class);

        extensionManager.registerExtension("minecraft", Minecraft.class, (p) -> p.getObjects().newInstance(MinecraftExtension.class, p));
        extensionManager.registerExtension("mappings", Mappings.class, (p) -> p.getObjects().newInstance(MappingsExtension.class, p));

        OfficialNamingChannelConfigurator.getInstance().configure(project);

        project.getTasks().create("handleNamingLicense", DisplayMappingsLicenseTask.class);

        project.getRepositories().maven(e -> {
            e.setUrl(UrlConstants.MOJANG_MAVEN);
            e.metadataSources(MavenArtifactRepository.MetadataSources::artifact);
        });

        project.afterEvaluate(this::applyAfterEvaluate);

        project.getExtensions().getByType(SourceSetContainer.class)
                .configureEach(sourceSet -> sourceSet
                        .getExtensions().create(ProjectHolder.class, ProjectHolderExtension.NAME, ProjectHolderExtension.class, project));

        project.getExtensions().add(
                Types.class,
                RunsConstants.Extensions.RUN_TYPES,
                project.getObjects().newInstance(TypesImpl.class, project)
        );
        project.getExtensions().add(
                Runs.class,
                RunsConstants.Extensions.RUNS,
                project.getObjects().newInstance(RunsImpl.class, project)
        );

        project.afterEvaluate(p -> {
            final Types types = p.getExtensions().getByType(Types.class);

            p.getExtensions().getByType(Runs.class)
                    .matching(run -> run instanceof RunImpl)
                    .forEach(run -> {
                        final RunImpl impl = (RunImpl) run;
                        types.matching(type -> type.getName().equals(run.getName())).forEach(impl::configureInternally);
                    });
        });
    }

    private void applyAfterEvaluate(final Project project) {
        final Repository<?,?,?,?,?> repositoryExtension = project.getExtensions().getByType(Repository.class);
        if (repositoryExtension instanceof IvyDummyRepositoryExtension) {
            final IvyDummyRepositoryExtension ivyDummyRepositoryExtension = (IvyDummyRepositoryExtension) repositoryExtension;
            ivyDummyRepositoryExtension.onPreDefinitionBakes(project);
        }

        GradleInternalUtils.getExtensions(project.getExtensions())
                .stream()
                .filter(CommonRuntimeExtension.class::isInstance)
                .map(extension -> (CommonRuntimeExtension<?,?,?>) extension)
                .forEach(CommonRuntimeExtension::bakeDefinitions);

        project.getExtensions().getByType(Runs.class).forEach(run -> {
            if (run instanceof RunImpl) {
                if (run.getConfigureFromTypeWithName().get()) {
                    run.configure();
                }

                if (run.getConfigureFromDependencies().get()) {
                    final RunImpl runImpl = (RunImpl) run;
                    runImpl.getModSources().get().forEach(sourceSet -> {
                        final TaskProvider<JavaCompile> compileTaskProvider = project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class);
                        try {
                            final CommonRuntimeDefinition<?> definition = TaskDependencyUtils.realiseTaskAndExtractRuntimeDefinition(project, compileTaskProvider);
                            definition.configureRun(runImpl);
                        } catch (MultipleDefinitionsFoundException e) {
                            throw new RuntimeException("Failed to configure run: " + run.getName() + " there are multiple runtime definitions found for the source set: " + sourceSet.getName(), e);
                        }
                    });
                }
            }
        });

        final DependencyReplacement dependencyReplacementExtension = project.getExtensions().getByType(DependencyReplacement.class);
        if (dependencyReplacementExtension instanceof DependencyReplacementsExtension) {
            final DependencyReplacementsExtension dependencyReplacementsExtension = (DependencyReplacementsExtension) dependencyReplacementExtension;
            dependencyReplacementsExtension.onPostDefinitionBakes(project);
        }
    }
}
