package io.sphere.sdk.stores.commands.updateactions;

import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.channels.Channel;
import io.sphere.sdk.commands.UpdateActionImpl;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.models.Referenceable;
import io.sphere.sdk.models.ResourceIdentifier;
import io.sphere.sdk.stores.Store;
import io.sphere.sdk.types.Type;
import io.sphere.sdk.types.customupdateactions.SetCustomTypeBase;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * Sets, overwrites or removes a custom type.
 *
 * {@doc.gen intro}
 *
 * {@include.example io.sphere.sdk.stores.commands.StoreUpdateCommandIntegrationTest}
 *
 * @see io.sphere.sdk.types.Custom
 */
public final class SetCustomType extends SetCustomTypeBase<Store> {

    private SetCustomType(@Nullable final String typeId, @Nullable final String typeKey, @Nullable final Map<String, JsonNode> fields) {
        super(typeId, typeKey, fields);
    }

    public static SetCustomType ofTypeKeyAndObjects(final String typeKey, final Map<String, Object> fields) {
        final Map<String, JsonNode> fieldsJson = mapObjectToJsonMap(fields);
        return ofTypeKeyAndJson(typeKey, fieldsJson);
    }

    public static SetCustomType ofTypeIdAndObjects(final String typeId, final Map<String, Object> fields) {
        final Map<String, JsonNode> fieldsJson = mapObjectToJsonMap(fields);
        return ofTypeIdAndJson(typeId, fieldsJson);
    }

    public static SetCustomType ofTypeIdAndObjects(final String typeId, final String fieldName, final Object value) {
        final Map<String, Object> fields = Collections.singletonMap(fieldName, value);
        return ofTypeIdAndObjects(typeId, fields);
    }

    public static SetCustomType ofTypeIdAndJson(final String typeId, final Map<String, JsonNode> fields) {
        return new SetCustomType(typeId, null, fields);
    }

    public static SetCustomType ofTypeKeyAndJson(final String typeKey, final Map<String, JsonNode> fields) {
        return new SetCustomType(null, typeKey, fields);
    }

    public static SetCustomType ofRemoveType() {
        return new SetCustomType(null, null, null);
    }
}
