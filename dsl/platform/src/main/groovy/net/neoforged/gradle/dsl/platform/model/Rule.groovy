package net.neoforged.gradle.dsl.platform.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.minecraftforge.gdi.ConfigurableDSLElement
import net.minecraftforge.gdi.annotations.DSLProperty
import net.neoforged.gradle.dsl.common.util.PropertyUtils
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional

import java.lang.reflect.Type

abstract class Rule implements ConfigurableDSLElement<Rule> {

    @Input
    @DSLProperty
    @Optional
    abstract Property<RuleAction> getAction();

    @Nested
    @DSLProperty
    @Optional
    abstract Property<OsCondition> getOs();

    @Input
    @Optional
    @DSLProperty
    abstract MapProperty<String, Boolean> getFeatures();

    static class Serializer implements JsonSerializer<Rule>, JsonDeserializer<Rule> {

        private final ObjectFactory factory;

        Serializer(ObjectFactory factory) {
            this.factory = factory
        }

        @Override
        Rule deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (!jsonElement.isJsonObject())
                throw new JsonParseException("Rule must be a json object")

            final JsonObject payload = jsonElement.getAsJsonObject();
            final Rule instance = factory.newInstance(Rule.class);

            PropertyUtils.deserialize(instance.getAction(), payload, "action", RuleAction.class, jsonDeserializationContext)
            PropertyUtils.deserialize(instance.getOs(), payload, "os", OsCondition.class, jsonDeserializationContext)
            PropertyUtils.deserializeMap(instance.getFeatures(), payload, "features", (name, element) -> element.getAsBoolean())

            return instance;
        }

        @Override
        JsonElement serialize(Rule rule, Type type, JsonSerializationContext jsonSerializationContext) {
            final JsonObject result = new JsonObject();

            PropertyUtils.serializeObject(rule.getAction(), result, "action", jsonSerializationContext)
            PropertyUtils.serializeObject(rule.getOs(), result, "os", jsonSerializationContext)
            PropertyUtils.serializeMap(rule.getFeatures(), result, "features", key -> key, value -> new JsonPrimitive(value))

            return result;
        }
    }
}
