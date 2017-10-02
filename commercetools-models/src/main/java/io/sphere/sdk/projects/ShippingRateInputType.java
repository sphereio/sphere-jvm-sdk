package io.sphere.sdk.projects;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.sphere.sdk.models.WithType;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CartValueImpl.class, name = "CartValue"),
        @JsonSubTypes.Type(value = CartClassificationImpl.class, name = "CartClassification"),
        @JsonSubTypes.Type(value = CartScoreImpl.class, name = "CartScore")
})
public interface ShippingRateInputType extends WithType{
}
