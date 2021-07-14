package io.sphere.sdk.carts;

import com.neovisionaries.i18n.CountryCode;
import io.sphere.sdk.customergroups.CustomerGroup;
import io.sphere.sdk.models.Address;
import io.sphere.sdk.models.Referenceable;
import io.sphere.sdk.models.ResourceIdentifier;
import io.sphere.sdk.shippingmethods.ShippingMethod;
import io.sphere.sdk.stores.Store;
import io.sphere.sdk.taxcategories.ExternalTaxRateDraft;
import io.sphere.sdk.types.CustomFieldsDraft;

import javax.annotation.Nullable;
import javax.money.CurrencyUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class CartDraftDsl extends CartDraftDslBase<CartDraftDsl>{

    CartDraftDsl(@Nullable String anonymousId, @Nullable Address billingAddress, @Nullable CountryCode country, CurrencyUnit currency, @Nullable CustomFieldsDraft custom, @Nullable List<CustomLineItemDraft> customLineItems, @Nullable String customerEmail, @Nullable ResourceIdentifier<CustomerGroup> customerGroup, @Nullable String customerId, @Nullable Integer deleteDaysAfterLastModification, @Nullable List<String> discountCodes, @Nullable ExternalTaxRateDraft externalTaxRateForShippingMethod, @Nullable InventoryMode inventoryMode, @Nullable List<Address> itemShippingAddresses, @Nullable List<LineItemDraft> lineItems, @Nullable Locale locale, @Nullable CartOrigin origin, @Nullable Address shippingAddress, @Nullable ResourceIdentifier<ShippingMethod> shippingMethod, @Nullable ShippingRateInputDraft shippingRateInput, @Nullable final ResourceIdentifier<Store> store, @Nullable TaxCalculationMode taxCalculationMode, @Nullable TaxMode taxMode, @Nullable RoundingMode taxRoundingMode) {
        super(anonymousId, billingAddress, country, currency, custom, customLineItems, customerEmail, customerGroup, customerId, deleteDaysAfterLastModification, discountCodes, externalTaxRateForShippingMethod, inventoryMode, itemShippingAddresses, lineItems, locale, origin, shippingAddress, shippingMethod, shippingRateInput, store, taxCalculationMode, taxMode, taxRoundingMode);
    }

    public CartDraftDsl withShippingMethod(@Nullable Referenceable<ShippingMethod> shippingMethod) {
        return super.withShippingMethod(Optional.ofNullable(shippingMethod).map(Referenceable::toResourceIdentifier).orElse(null));
    }
}
