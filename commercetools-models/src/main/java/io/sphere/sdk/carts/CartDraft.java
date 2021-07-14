package io.sphere.sdk.carts;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.neovisionaries.i18n.CountryCode;
import io.sphere.sdk.annotations.FactoryMethod;
import io.sphere.sdk.annotations.QueryModelHint;
import io.sphere.sdk.annotations.ResourceDraftValue;
import io.sphere.sdk.customergroups.CustomerGroup;
import io.sphere.sdk.discountcodes.DiscountCodeInfo;
import io.sphere.sdk.models.Address;
import io.sphere.sdk.models.ResourceIdentifier;
import io.sphere.sdk.shippingmethods.ShippingMethod;
import io.sphere.sdk.stores.Store;
import io.sphere.sdk.taxcategories.ExternalTaxRateDraft;
import io.sphere.sdk.types.CustomDraft;
import io.sphere.sdk.types.CustomFieldsDraft;

import javax.annotation.Nullable;
import javax.money.CurrencyUnit;
import java.util.List;
import java.util.Locale;

/**
 * <p>Draft for {@link Cart} creation.</p>
 *
 * @see io.sphere.sdk.carts.commands.CartCreateCommand
 * @see CartDraftBuilder
 */
@JsonDeserialize(as = CartDraftDsl.class)
@ResourceDraftValue(
        factoryMethods = @FactoryMethod(parameterNames = "currency"),
        abstractResourceDraftValueClass = true,
        abstractBuilderClass = true
)
public interface CartDraft extends CustomDraft {
    CurrencyUnit getCurrency();

    @Nullable
    String getCustomerId();

    @Nullable
    CountryCode getCountry();

    @Nullable
    InventoryMode getInventoryMode();

    @Nullable
    String getCustomerEmail();

    @Nullable
    ResourceIdentifier<CustomerGroup> getCustomerGroup();

    @Nullable
    Address getBillingAddress();

    @Nullable
    List<CustomLineItemDraft> getCustomLineItems();

    @Nullable
    List<LineItemDraft> getLineItems();

    @Nullable
    Address getShippingAddress();

    @Nullable
    ResourceIdentifier<ShippingMethod> getShippingMethod();

    @Nullable
    CustomFieldsDraft getCustom();

    @Nullable
    TaxMode getTaxMode();

    @Nullable
    String getAnonymousId();

    @Nullable
    Locale getLocale();

    @Nullable
    Integer getDeleteDaysAfterLastModification();

    @Nullable
    RoundingMode getTaxRoundingMode();

    @Nullable
    ShippingRateInputDraft getShippingRateInput();

    @Nullable
    TaxCalculationMode getTaxCalculationMode();

    @Nullable
    CartOrigin getOrigin();

    /**
     *  Contains addresses for carts with multiple shipping addresses.
     *  Each address must contain a key which is unique in this cart.
     *  Line items will use these keys to reference the addresses under
     *  their `shippingDetails`
     * @return
     */
    @Nullable
    List<Address> getItemShippingAddresses();

    @Nullable
    ResourceIdentifier<Store> getStore();

    @Nullable
    List<String> getDiscountCodes();

    @Nullable
    ExternalTaxRateDraft getExternalTaxRateForShippingMethod();


    static CartDraftDsl of(final CurrencyUnit currency) {
        return CartDraftDsl.of(currency);
    }
}
