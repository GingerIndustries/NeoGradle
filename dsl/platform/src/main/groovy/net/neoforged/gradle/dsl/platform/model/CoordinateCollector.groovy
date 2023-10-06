package net.neoforged.gradle.dsl.platform.model

import net.neoforged.gradle.dsl.platform.util.ModuleIdentificationVisitor
import org.gradle.api.model.ObjectFactory
import org.jetbrains.annotations.Nullable

class CoordinateCollector extends ModuleIdentificationVisitor {

    private final Set<String> coordinates = new HashSet<>()

    CoordinateCollector(ObjectFactory objectFactory) {
        super(objectFactory)
    }

    @Override
    protected void visitModule(File file, String group, String module, String version, @Nullable String classifier, final String extension) throws Exception {
        coordinates.add(group + ":" + module + ":" + version + (classifier.isEmpty() ? "" : ":" + classifier) + "@" + extension)
    }

    Set<String> getCoordinates() {
        return coordinates
    }
}
