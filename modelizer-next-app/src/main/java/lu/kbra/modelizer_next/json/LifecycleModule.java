package lu.kbra.modelizer_next.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import lu.kbra.modelizer_next.ui.impl.PostConstructOwner;
import lu.kbra.modelizer_next.ui.impl.PreDeconstructOwner;

public class LifecycleModule extends SimpleModule {

	private static final long serialVersionUID = -6875862500633129534L;

	@Override
	public void setupModule(final SetupContext context) {
		context.addBeanSerializerModifier(new BeanSerializerModifier() {

			@Override
			public JsonSerializer<?>
					modifySerializer(final SerializationConfig config, final BeanDescription beanDesc, final JsonSerializer<?> serializer) {

				return new JsonSerializer<>() {

					@Override
					public void serialize(final Object value, final JsonGenerator gen, final SerializerProvider serializers)
							throws IOException {

						if (value instanceof final PreDeconstructOwner owner) {
							owner.preDeconstruct();
						}

						((JsonSerializer<Object>) serializer).serialize(value, gen, serializers);
					}
				};
			}
		});

		context.addBeanDeserializerModifier(new BeanDeserializerModifier() {

			@Override
			public JsonDeserializer<?> modifyDeserializer(
					final DeserializationConfig config,
					final BeanDescription beanDesc,
					final JsonDeserializer<?> deserializer) {

				return new DelegatingDeserializer(deserializer) {

					@Override
					public Object deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
						final Object obj = super.deserialize(p, ctxt);

						if (obj instanceof final PostConstructOwner owner) {
							owner.postConstruct();
						}

						return obj;
					}

					@Override
					protected JsonDeserializer<?> newDelegatingInstance(final JsonDeserializer<?> d) {
						return this;
					}
				};
			}
		});
	}
}
