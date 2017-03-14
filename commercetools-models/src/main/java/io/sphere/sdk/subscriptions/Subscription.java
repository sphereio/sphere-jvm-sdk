package io.sphere.sdk.subscriptions;

import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.sdk.annotations.*;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.models.Resource;
import io.sphere.sdk.types.Custom;
import io.sphere.sdk.types.CustomFields;
import io.sphere.sdk.types.TypeDraft;

import javax.annotation.Nullable;
import java.util.List;

@ResourceValue
@ResourceInfo(pluralName = "subscriptions", pathElement = "subscriptions")
@HasQueryEndpoint
@HasQueryModel
@HasByIdGetEndpoint(javadocSummary = "Fetches a subscription by id.")
// includeExamples = "io.sphere.sdk.shoppinglists.queries.ShoppingListByIdGetIntegrationTest#byIdGet()")
@HasByKeyGetEndpoint(javadocSummary = "Fetches a subscription by key.")
public interface Subscription extends Resource<Subscription>, Custom {

    @Nullable
    String getKey();

    @IgnoreInQueryModel
    Destination getDestination();

    @IgnoreInQueryModel
    List<MessageSubscription> getMessages();

    @IgnoreInQueryModel
    List<ChangeSubscription> getChanges();

    /**
     * Creates a container which contains the full Java type information to deserialize this class from JSON.
     *
     * @see io.sphere.sdk.json.SphereJsonUtils#readObject(byte[], TypeReference)
     * @see io.sphere.sdk.json.SphereJsonUtils#readObject(String, TypeReference)
     * @see io.sphere.sdk.json.SphereJsonUtils#readObject(com.fasterxml.jackson.databind.JsonNode, TypeReference)
     * @see io.sphere.sdk.json.SphereJsonUtils#readObjectFromResource(String, TypeReference)
     *
     * @return type reference
     */
    static TypeReference<Subscription> typeReference() {
        return new TypeReference<Subscription>() {
            @Override
            public String toString() {
                return "TypeReference<Subscription>";
            }
        };
    }

    /**
     * Creates a reference for one item of this class by a known ID.
     *
     * <p>An example for categories but this applies for other resources, too:</p>
     * {@include.example io.sphere.sdk.categories.CategoryTest#referenceOfId()}
     *
     * <p>If you already have a resource object, then use {@link #toReference()} instead:</p>
     *
     * {@include.example io.sphere.sdk.categories.CategoryTest#toReference()}
     *
     * @param id the ID of the resource which should be referenced.
     * @return reference
     */
    static Reference<Subscription> referenceOfId(final String id) {
        return Reference.of(referenceTypeId(), id);
    }

    /**
     * A type hint for references which resource type is linked in a reference.
     * @see Reference#getTypeId()
     * @return type hint
     */
    static String referenceTypeId() {
        return "subscription";
    }

    /**
     * An identifier for this resource which supports {@link CustomFields}.
     * @see TypeDraft#getResourceTypeIds()
     * @see io.sphere.sdk.types.Custom
     * @return ID of this resource type
     */
    static String resourceTypeId() {
        return "subscription";
    }

    @Override
    default Reference<Subscription> toReference() {
        return Reference.of(referenceTypeId(), getId(), this);
    }

}
