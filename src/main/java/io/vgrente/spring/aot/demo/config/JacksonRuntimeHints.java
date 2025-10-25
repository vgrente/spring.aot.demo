package io.vgrente.spring.aot.demo.config;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

/**
 * RuntimeHintsRegistrar for Jackson 3.x to support native image compilation.
 * Registers reflection hints for Jackson databind classes required for JSON
 * serialization.
 */
public class JacksonRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		// Register Jackson 3.x core classes
		registerJacksonClass(hints, "tools.jackson.databind.jsontype.NamedType");
		registerJacksonClass(hints, "tools.jackson.databind.ObjectMapper");
		registerJacksonClass(hints, "tools.jackson.databind.JsonSerializer");
		registerJacksonClass(hints, "tools.jackson.databind.JsonDeserializer");
		registerJacksonClass(hints, "tools.jackson.databind.Module");
		registerJacksonClass(hints, "tools.jackson.databind.module.SimpleModule");
		registerJacksonClass(hints, "tools.jackson.databind.ser.std.StdSerializer");
		registerJacksonClass(hints, "tools.jackson.databind.deser.std.StdDeserializer");

		// Register Jackson annotation classes
		registerJacksonClass(hints, "tools.jackson.annotation.JsonTypeInfo");
		registerJacksonClass(hints, "tools.jackson.annotation.JsonSubTypes");
		registerJacksonClass(hints, "tools.jackson.annotation.JsonProperty");
		registerJacksonClass(hints, "tools.jackson.annotation.JsonCreator");

		// Register Jackson core classes
		registerJacksonClass(hints, "tools.jackson.core.JsonParser");
		registerJacksonClass(hints, "tools.jackson.core.JsonGenerator");
		registerJacksonClass(hints, "tools.jackson.core.JsonFactory");
	}

	private void registerJacksonClass(RuntimeHints hints, String className) {
		try {
			hints.reflection().registerType(TypeReference.of(className), MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS,
					MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.ACCESS_DECLARED_FIELDS,
					MemberCategory.ACCESS_PUBLIC_FIELDS);
		} catch (Exception e) {
			// Class might not be present in classpath, skip
		}
	}
}
