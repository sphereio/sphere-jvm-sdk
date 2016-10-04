package io.sphere.sdk.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sphere.sdk.types.Custom;
import io.sphere.sdk.types.CustomFields;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@JsonDeserialize(as = AssetImpl.class)
public interface Asset extends Custom {
    String getId();

    List<AssetSource> getSources();

    LocalizedString getName();

    @Nullable
    LocalizedString getDescription();

    /**
     * Gets the tags belonging to this asset or an empty set.
     *
     * @return tags
     */
    @Nonnull
    Set<String> getTags();

    @Override
    @Nullable
    CustomFields getCustom();
}