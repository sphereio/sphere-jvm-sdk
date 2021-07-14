package io.sphere.sdk.meta;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sphere.sdk.apiclient.ApiClient;
import io.sphere.sdk.apiclient.ApiClientDraft;
import io.sphere.sdk.cartdiscounts.*;
import io.sphere.sdk.carts.*;
import io.sphere.sdk.carts.commands.CartReplicationDraft;
import io.sphere.sdk.carts.commands.updateactions.*;
import io.sphere.sdk.carts.expansion.CartExpansionModel;
import io.sphere.sdk.carts.expansion.ShippingInfoExpansionModel;
import io.sphere.sdk.carts.queries.CartByIdGet;
import io.sphere.sdk.carts.queries.CartQueryModel;
import io.sphere.sdk.categories.Category;
import io.sphere.sdk.categories.CategoryDraft;
import io.sphere.sdk.categories.CategoryDraftBuilder;
import io.sphere.sdk.categories.CategoryTree;
import io.sphere.sdk.categories.commands.updateactions.SetKey;
import io.sphere.sdk.categories.queries.CategoryQueryModel;
import io.sphere.sdk.channels.Channel;
import io.sphere.sdk.channels.queries.ChannelByIdGet;
import io.sphere.sdk.channels.queries.ChannelQueryModel;
import io.sphere.sdk.client.SphereApiConfig;
import io.sphere.sdk.client.SphereAuthConfig;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.client.SphereRequest;
import io.sphere.sdk.commands.UpdateAction;
import io.sphere.sdk.commands.UpdateActionImpl;
import io.sphere.sdk.commands.UpdateCommand;
import io.sphere.sdk.customergroups.CustomerGroup;
import io.sphere.sdk.customergroups.CustomerGroupDraft;
import io.sphere.sdk.customergroups.CustomerGroupDraftBuilder;
import io.sphere.sdk.customers.Customer;
import io.sphere.sdk.customers.CustomerDraft;
import io.sphere.sdk.customers.CustomerDraftBuilder;
import io.sphere.sdk.customers.CustomerToken;
import io.sphere.sdk.customers.commands.CustomerCreateEmailTokenCommand;
import io.sphere.sdk.customers.commands.CustomerPasswordResetCommand;
import io.sphere.sdk.customers.commands.CustomerSignInCommand;
import io.sphere.sdk.customers.commands.CustomerVerifyEmailCommand;
import io.sphere.sdk.customers.commands.updateactions.AddShippingAddressId;
import io.sphere.sdk.customers.commands.updateactions.SetSalutation;
import io.sphere.sdk.customers.errors.CustomerInvalidCurrentPassword;
import io.sphere.sdk.customers.expansion.CustomerSignInResultExpansionModel;
import io.sphere.sdk.customers.queries.CustomerQueryModel;
import io.sphere.sdk.customobjects.CustomObject;
import io.sphere.sdk.customobjects.commands.CustomObjectDeleteCommand;
import io.sphere.sdk.customobjects.queries.CustomObjectByKeyGet;
import io.sphere.sdk.customobjects.queries.CustomObjectQuery;
import io.sphere.sdk.customobjects.queries.CustomObjectQueryModel;
import io.sphere.sdk.discountcodes.DiscountCode;
import io.sphere.sdk.discountcodes.DiscountCodeDraft;
import io.sphere.sdk.expansion.ExpansionPath;
import io.sphere.sdk.extensions.ExtensionResourceType;
import io.sphere.sdk.extensions.Trigger;
import io.sphere.sdk.http.*;
import io.sphere.sdk.inventory.InventoryEntry;
import io.sphere.sdk.inventory.InventoryEntryDraft;
import io.sphere.sdk.inventory.InventoryEntryDraftBuilder;
import io.sphere.sdk.inventory.commands.updateactions.SetSupplyChannel;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.messages.GenericMessageImpl;
import io.sphere.sdk.messages.Message;
import io.sphere.sdk.messages.UserProvidedIdentifiers;
import io.sphere.sdk.models.*;
import io.sphere.sdk.orders.*;
import io.sphere.sdk.orders.commands.updateactions.AddDelivery;
import io.sphere.sdk.orders.expansion.OrderExpansionModel;
import io.sphere.sdk.orders.messages.OrderPaymentStateChangedMessage;
import io.sphere.sdk.payments.*;
import io.sphere.sdk.payments.messages.PaymentStatusInterfaceCodeSetMessage;
import io.sphere.sdk.payments.messages.PaymentTransactionStateChangedMessage;
import io.sphere.sdk.productdiscounts.ProductDiscount;
import io.sphere.sdk.productdiscounts.ProductDiscountDraft;
import io.sphere.sdk.productdiscounts.ProductDiscountDraftBuilder;
import io.sphere.sdk.productdiscounts.queries.MatchingProductDiscountGet;
import io.sphere.sdk.productdiscounts.queries.ProductDiscountByIdGet;
import io.sphere.sdk.products.*;
import io.sphere.sdk.products.attributes.Attribute;
import io.sphere.sdk.products.attributes.AttributeAccess;
import io.sphere.sdk.products.attributes.AttributeDefinition;
import io.sphere.sdk.products.attributes.AttributeDefinitionDraft;
import io.sphere.sdk.products.commands.ProductImageUploadCommand;
import io.sphere.sdk.products.commands.updateactions.*;
import io.sphere.sdk.products.expansion.ProductDataExpansionModel;
import io.sphere.sdk.products.expansion.ProductProjectionExpansionModel;
import io.sphere.sdk.products.messages.ProductDeletedMessage;
import io.sphere.sdk.products.messages.ProductPublishedMessage;
import io.sphere.sdk.products.messages.ProductRevertedStagedChangesMessage;
import io.sphere.sdk.products.messages.ProductVariantDeletedMessage;
import io.sphere.sdk.products.queries.*;
import io.sphere.sdk.products.search.*;
import io.sphere.sdk.producttypes.*;
import io.sphere.sdk.producttypes.commands.updateactions.ChangeInputHint;
import io.sphere.sdk.producttypes.commands.updateactions.RemoveEnumValues;
import io.sphere.sdk.projects.Project;
import io.sphere.sdk.projects.error.LanguageUsedInStores;
import io.sphere.sdk.queries.*;
import io.sphere.sdk.reviews.ReviewDraft;
import io.sphere.sdk.reviews.ReviewDraftBuilder;
import io.sphere.sdk.search.FilteredFacetResult;
import io.sphere.sdk.search.PagedSearchResult;
import io.sphere.sdk.search.SearchKeywords;
import io.sphere.sdk.search.model.ExistsFilterSearchModelSupport;
import io.sphere.sdk.search.model.MissingFilterSearchModelSupport;
import io.sphere.sdk.shippingmethods.ShippingMethod;
import io.sphere.sdk.shippingmethods.ShippingMethodDraft;
import io.sphere.sdk.shippingmethods.ShippingMethodDraftBuilder;
import io.sphere.sdk.shippingmethods.ShippingRate;
import io.sphere.sdk.shippingmethods.commands.updateactions.RemoveZone;
import io.sphere.sdk.shoppinglists.ShoppingList;
import io.sphere.sdk.shoppinglists.ShoppingListDraft;
import io.sphere.sdk.shoppinglists.expansion.LineItemExpansionModel;
import io.sphere.sdk.states.State;
import io.sphere.sdk.states.StateDraftDsl;
import io.sphere.sdk.stores.commands.updateactions.SetLanguages;
import io.sphere.sdk.subscriptions.AzureServiceBusDestination;
import io.sphere.sdk.subscriptions.MessageSubscriptionPayload;
import io.sphere.sdk.subscriptions.Payload;
import io.sphere.sdk.subscriptions.Subscription;
import io.sphere.sdk.taxcategories.*;
import io.sphere.sdk.types.*;
import io.sphere.sdk.zones.ZoneDraftBuilder;
import io.sphere.sdk.stores.Store;
import io.sphere.sdk.stores.StoreDraft;
import io.sphere.sdk.states.queries.StateByKeyGet;
import io.sphere.sdk.inventory.messages.InventoryEntryCreatedMessage;
import io.sphere.sdk.shippingmethods.queries.ShippingMethodsByOrderEditGet;

import javax.money.CurrencyUnit;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 <div class=release-notes>
 <h3>Legend</h3>
 <ul>
 <li class=removed-in-release>removed functionality</li>
 <li class=new-in-release>added functionality</li>
 <li class=change-in-release>breaking change</li>
 <li class=fixed-in-release>bugfix, can include a breaking change</li>
 </ul>

 <!--
 <ul>
 <li class=removed-in-release></li>
 <li class=new-in-release></li>
 <li class=change-in-release></li>
 <li class=fixed-in-release></li>
 </ul>
 -->
 -->
 <h3 class=released-version id="v1_65_0">1.65.0 (02.08.2021)</h3>
 <ul>
 <li class=new-in-release>Support external Tax Rate For Shipping Method in {@link CartDraft#getExternalTaxRateForShippingMethod()}</li>
 </ul>
 <h3 class=released-version id="v2_0_0">2.0.0 beta 1 (03.06.2021)</h3>
 <ul>
 <li class=removed-in-release>Removed the OSGi support</li>
 <li class=change-in-release>Updated Apache HTTP client to 5.1</li>
 </ul>
 <h3 class=released-version id="v1_64_0">1.64.0 (07.06.2021)</h3>
 <ul>
 <li class=fixed-in-release>Added in the Product Projection Model the possibility to filter by key from the predicate {@link ProductProjectionQueryModelImpl#key()} and {@link ProductProjectionQueryModel#key()}  }
 <li class=fixed-in-release>Added as an update action for Cart: SetLineItemDistributionChannel
 </ul>
 <h3 class=released-version id="v1_63_0">1.63.0 (03.05.2021)</h3>
 <ul>
 <li class=fixed-in-release>Fixed method that did not include the field tiers {@link PriceDraftBuilder#of(Price)} and {@link PriceDraftBuilder#of(PriceDraft)}. In introduced the method to set tiers directly by {@link PriceDraftDsl#withTiers(List)}
 <li class=fixed-in-release>Fixed deadlock bug in the {@link io.sphere.sdk.retry.RetryAction#ofScheduledRetry(long, Function)} in case of exception
 </ul>
 <h3 class=released-version id="v1_62_0">1.62.0 (06.04.2021)</h3>
 <ul>
 <li class=fixed-in-release>Fixed type for asset in AddVariant update action</li>
 <li class=fixed-in-release>Address PO box gets correctly deserialized</li>
 <li class=new-in-release>Resources implement WithKey and Custom interfaces</li>
 <li class=new-in-release>Address supports custom fields</li>
 <li class=new-in-release>Support for {@link io.sphere.sdk.customers.messages.CustomerPasswordUpdatedMessage}</li>
 <li class=new-in-release>ShoppingList addLineItem supports variant selection by sku</li>
 <li class=new-in-release>Support addedAt for shopping list line items</li>
 <li class=new-in-release>Support oldSlug in product & category slug changed messages</li>
 <li class=new-in-release>Support fixed amount cart discounts</li>
 <li class=new-in-release>Support custom fields for shipping methods</li>
 <li class=new-in-release>Support SearchIndexingConfiguration for project</li>
 <li class=new-in-release>Support replicate cart in store endpoint</li>
 </ul>
 <h3 class=released-version id="v1_61_0">1.61.0 (06.04.2021)</h3>
 <ul>
 <li class=new-in-release>Deprecated {@link CustomerDraft#getAnonymousCartId()} and replaced with {@link CustomerDraft#getAnonymousCart()} support anonymousCart into the {@link CustomerDraftBuilder}, into the {@link CustomerSignInCommand} and in the {@link io.sphere.sdk.customers.commands.CustomerInStoreSignInCommand}</li>
 <li class=new-in-release>Support discounted field in the {@link PriceDraft#getDiscounted()}
 </ul>
 <h3 class=released-version id="v1_60_0">1.60.0 (01.03.2021)</h3>
 <ul>
 <li class=new-in-release>Support custom fields and itemShippingDetails in OrderImport for CustomLineItems {@link CustomLineItemImportDraft#getShippingDetails()}</li>
 <li class=new-in-release>Support Error codes adding the related classes that has to be found in this page of the documentation {@link //docs.commercetools.com/api/errors#top} </li>
 <li class=change-in-release> {@link PriceDraft#getCustomerGroup()} is of type {@link ResourceIdentifier<CustomerGroup>} instead of {@link Reference<CustomerGroup>}</li>
 </ul>
 <h3 class=released-version id="v1_58_0">1.58.0 (01.02.2021)</h3>
 <ul>
 <li class=new-in-release>Support supplyChannels field to Store and add update action {@link io.sphere.sdk.stores.commands.updateactions.SetSupplyChannels}, {@link io.sphere.sdk.stores.commands.updateactions.AddSupplyChannel} and {@link io.sphere.sdk.stores.commands.updateactions.RemoveSupplyChannel}</li>
 <li class=new-in-release>Support addedAt to LineItem in the Cart and support the relative action with the new field {@link AddLineItem#getAddedAt()}</li>
 <li class=new-in-release>Support new field in the Store {@link Store#getCustom()} adding related updated actions {@link io.sphere.sdk.stores.commands.updateactions.SetCustomField} and {@link io.sphere.sdk.stores.commands.updateactions.SetCustomType}</li>
 <li class=new-in-release>Support {@link io.sphere.sdk.products.messages.ProductVariantAddedMessage}</li>
 <li class=new-in-release>Support discount codes in {@link CartDraft#getDiscountCodes()}</li>
 <li class=new-in-release>Support key to Cart {@link Cart#getKey()}, support update action {@link SetKey}, support {@link io.sphere.sdk.carts.queries.CartByKeyGet}, support {@link CartReplicationDraft#getKey()}</li>
 <li class=new-in-release>Deprecated {@link OrderFromCartDraft#getId()} and related methods, it has been replaced with support {@link OrderFromCartDraft#getCart()}; support of method for the Cart {@link OrderFromCartDraft#of(Cart)}</li>
 <li class=new-in-release>Support resource identifier to set customer groups in {@link io.sphere.sdk.customers.commands.updateactions.SetCustomerGroup}, {@link io.sphere.sdk.orderedits.commands.stagedactions.SetCustomerGroup}, {@link SetCustomerGroup}</li>
 <li class=new-in-release>Support resource identifier to set customer in {@link io.sphere.sdk.reviews.commands.updateactions.SetCustomer}, {@link io.sphere.sdk.shoppinglists.commands.updateactions.SetCustomer}</li>
 <li class=new-in-release>Add lastModifiedAt to {@link LineItem#getLastModifiedAt()}</li>
 </ul>
 <h3 class=released-version id="v1_56_0">1.56.0 (04.12.2020)</h3>
 <ul>
 <li class=fixed-in-release>Fixed requests for resources by key with special characters by url encoding the key</li>
 <li class=fixed-in-release>Support customer address by key selection for {@link AddShippingAddressId}, {@link io.sphere.sdk.customers.commands.updateactions.AddBillingAddressId}, {@link io.sphere.sdk.customers.commands.updateactions.RemoveBillingAddressId} and {@link io.sphere.sdk.customers.commands.updateactions.RemoveShippingAddressId}</li>
 </ul>
 <h3 class=released-version id="v1_55_0">1.55.0 (23.11.2020)</h3>
 <ul>
 <li class=fixed-in-release>Support customer address by key selection for updates</li>
 <li class=new-in-release>Support errors for store {@link io.sphere.sdk.stores.error.ProjectNotConfiguredForLanguagesError} and {@link io.sphere.sdk.stores.error.MissingRoleOnChannelError} </li>
 </ul>
 <h3 class=released-version id="v1_54_0">1.54.0 (01.10.2020)</h3>
 <ul>
 <li class=fixed-in-release>Fixed possible thread safe issue in HighPrecisionMoney usage</li>
 <li class=fixed-in-release>Fixed type for {@link PriceCollectionQueryModel#channel()}</li>
 </ul>
 <ul>
 <li class=new-in-release>Added new field in the CartsConfiguration {@link CartsConfiguration#getCountryTaxRateFallbackEnabled()} and the related update action changeCountryTaxRateFallbackEnabled in the Project {@link Project#} </li>
 <li class=new-in-release>Support setStore update action {@link Order#getStore()} and OrderStoreSet message {@link io.sphere.sdk.orders.messages.OrderStoreSetMessage} </li>
 <li class=new-in-release>Support distributionChannels field to Store and add update action {@link io.sphere.sdk.stores.commands.updateactions.SetDistributionChannels}, {@link io.sphere.sdk.stores.commands.updateactions.AddDistributionChannel} and {@link io.sphere.sdk.stores.commands.updateactions.RemoveDistributionChannel}</li>
 <li class=new-in-release>Support LanguageUsedInStore error {@link io.sphere.sdk.projects.error.LanguageUsedInStores}</li>
 <li class=new-in-release>Support AnonymousId for ShoppingLists {@link ShoppingList#getAnonymousId()}</li>
 <li class=new-in-release>Updated <a href="https://javamoney.github.io/ri.html#welcome-to-moneta---the-jsr-354-reference-implementation">moneta</a> to version 1.2</li>
 <li class=new-in-release>Changed state to ResourceIdentifier for product draft  {@link ProductDraft#getState()}</li>
 <li class=new-in-release>Changed customer to ResourceIdentifier for shopping list draft  {@link ShoppingListDraft#getCustomer()}</li>
 </ul>

 <h3 class=released-version id="v1_53_0">1.53.0 (18.08.2020)</h3>
 <ul>
 <li class=new-in-release>Added new {@link ShippingMethodsByOrderEditGet}.</li>
 <li class=new-in-release>Added new {@link InventoryEntryCreatedMessage}.</li>
 <li class=new-in-release>Added new {@link StateByKeyGet} and now update and delete can be by Key as well.</li>
 </ul>

 <h3 class=released-version id="v1_52_1">1.52.1 (18.08.2020)</h3>
 <ul>
 <li class=fixed-in-release>Fixed storeProjection and localeProjection as query parameter in Product Projection and not anymore in the priceSelection </li>
 </ul>

 <h3 class=released-version id="v1_52_0">1.52.0 (07.07.2020)</h3>
 <ul>
 <li class=new-in-release>Added missing {@link ExternalTaxRateDraft#isIncludedInPrice()} and added it to builder {@link ExternalTaxRateDraftBuilder#includedInPrice()}</li>
 <li class=new-in-release>Added support for additional query parameters for updates {@link io.sphere.sdk.commands.UpdateCommandDsl#withAdditionalHttpQueryParameters(NameValuePair)} and search endpoints {@link io.sphere.sdk.search.MetaModelSearchDsl#withAdditionalQueryParameter(NameValuePair)}.</li>
 <li class=new-in-release>Added missing {@link ShippingMethodDraft#getLocalizedDescription()} and added it to builder {@link ShippingMethodDraftBuilder#localizedDescription()}</li>
 <li class=new-in-release>Added support for additional action {@link io.sphere.sdk.shippingmethods.commands.updateactions.SetLocalizedDescription} and deprecated the attribute "description" and the action "setDescription" </li>
 <li class=new-in-release>Added {@link Store#getLanguages()} and {@link StoreDraft#getLanguages()} and added update action {@link io.sphere.sdk.stores.commands.updateactions.SetLanguages} </li>
 <li class=new-in-release>Added query parameters in ProductProjection {@link PriceSelection#getLocaleProjection()} and {@link PriceSelection#getStoreProjection()} and also in the {@link PriceSelectionBuilder }  </li>
 </ul>

 </ul>
 <h3 class=released-version id="v1_51_0">1.51.0 (31.03.2020)</h3>
 <ul>
 <li class=fixed-in-release>Fix formatting of {@link GeoJSONQueryModel#withinCircle(Point, Double)} with default locale not using '.' as decimal delimiter.</li>
 </ul>

 <h3 class=released-version id="v1_50_0">1.50.0 (05.03.2020)</h3>
 <ul>
 <li class=fixed-in-release>Replace old default ct urls (https://api.sphere.io, https://auth.sphere.io) with new default urls (https://api.europe-west1.gcp.commercetools.com, https://auth.europe-west1.gcp.commercetools.com)</li>
 </ul>

 <h3 class=released-version id="v1_49_0">1.49.0 (16.01.2020)</h3>
 <ul>
    <li class=fixed-in-release>{@link TransactionDraftDsl} is now public</li>
    <li class=new-in-release>Added new method {@link ParcelDraft#of(TrackingData, List)}</li>
    <li class=new-in-release>Added support for carts and shopping lists configuration to {@link Project} </li>
    <li class=new-in-release>{@link io.sphere.sdk.client.correlationid.CorrelationIdRequestDecorator} to attach a user-defined correlation id as a value for a header with key "X-Correlation-ID" on requests going to the commercetools platform.</li>
    <li class=fixed-in-release>fixed {@link io.sphere.sdk.messages.GenericMessageImpl} json deserialization where {@link GenericMessageImpl#getResourceUserProvidedIdentifiers()} was missing </li>
 </ul>

 <h3 class=released-version id="v1_48_0">1.48.0 (16.12.2019)</h3>
 <ul>
    <li class=new-in-release>Added new scopes to {@link io.sphere.sdk.client.SphereProjectScope}</li>
    <li class=new-in-release>Added new customer update actions: {@link io.sphere.sdk.customers.commands.updateactions.AddStore}, {@link io.sphere.sdk.customers.commands.updateactions.SetStores}, {@link io.sphere.sdk.customers.commands.updateactions.RemoveStore}</li>
    <li class=fixed-in-release>Fixed {@link io.sphere.sdk.orders.messages.OrderMessage} json deserialization bug where <code>type</code> field wouldn't get deserialized </li>
 </ul>

 <h3 class=released-version id="v1_47_0">1.47.0 (11.10.2019)</h3>
 <ul>
     <li class=fixed-in-release>Fixed {@link ProductImageUploadCommand#withContentType(String)} ignoring provided content type</li>
     <li class=new-in-release>Added store key reference to {@link OrderImportDraft}</li>
     <li class=fixed-in-release>{@link HttpHeaders#getHeader(String) now treats headers as case insensitive}</li></li>
     <li class=new-in-release>Added {@link Customer#getStores()} field</li>
 </ul>

 <h3 class=released-version id="v1_46_0">1.46.0 (21.08.2019)</h3>
 <ul>
     <li class=new-in-release>Added support for {@link io.sphere.sdk.products.messages.ProductAddedToCategoryMessage} and {@link io.sphere.sdk.products.messages.ProductRemovedFromCategoryMessage}</li>
     <li class=fixed-in-release>Update actions for setting custom fields now accept empty array values</li>
 </ul>

 <h3 class=released-version id="v1_45_0">1.45.0 (02.08.2019)</h3>
 <ul>
     <li class=fixed-in-release>{@link Category}, {@link CategoryDraft}, {@link ProductLike} and {@link ProductDraft} now extend the {@link WithKey} interface.</li>
 </ul>

 <h3 class=released-version id="v1_44_0">1.44.0 (07.06.2019)</h3>
 <ul>
    <li class=fixed-in-release>{@link PagedSearchResult#empty()} now creates an instance with the {@link PagedSearchResult#getLimit()} field set to 20, instead of 0</li>
 </ul>

 <h3 class=released-version id="v1_43_0">1.43.0 (03.06.2019)</h3>
 <ul>
    <li class=new-in-release>Added {@link ProductDiscount#getKey()} and {@link ProductDiscountDraft#getKey()} ()} fields.</li>
    <li class=new-in-release>Added {@link ProductByKeyGet}, {@link io.sphere.sdk.products.commands.ProductUpdateCommand#ofKey(String, Long, UpdateAction)}, {@link io.sphere.sdk.productdiscounts.commands.ProductDiscountDeleteCommand#ofKey(String, Long)} commands</li>
    <li class=new-in-release>Added {@link io.sphere.sdk.productdiscounts.commands.updateactions.SetKey} update action</li>
    <li class=new-in-release>Added {@link CustomerToken#getExpiresAt()} field</li>
    <li class=new-in-release>Added key property to {@link CartDiscount} and {@link CartDiscountDraft}</li>
 </ul>

 <h3 class=released-version id="v1_42_0">1.42.0 (24.05.2019)</h3>
 <ul>
    <li class=fixed-in-release>Added support for {@link io.sphere.sdk.extensions.AuthorizationHeaderAuthentication} for api extensions.</li>
    <li class=fixed-in-release>Fixed {@link io.sphere.sdk.orderedits.commands.stagedactions.AddLineItem} staged update action structure</li>
    <li class=new-in-release>Added support for {@link io.sphere.sdk.stores.Store}</li>
    <li class=new-in-release>Added support commands that allow you to access carts and orders belonging to a specific store: {@link io.sphere.sdk.carts.commands.CartInStoreCreateCommand}, {@link io.sphere.sdk.carts.commands.CartInStoreDeleteCommand}, {@link io.sphere.sdk.carts.commands.CartInStoreUpdateCommand}, {@link io.sphere.sdk.carts.queries.CartInStoreByCustomerIdGet}, {@link io.sphere.sdk.carts.queries.CartInStoreByIdGet}, {@link io.sphere.sdk.carts.queries.CartInStoreQuery}, {@link io.sphere.sdk.orders.commands.OrderFromCartInStoreCreateCommand}, {@link io.sphere.sdk.orders.commands.OrderInStoreDeleteByIdCommand}, {@link io.sphere.sdk.orders.commands.OrderInStoreDeleteByOrderNumberCommand}, {@link io.sphere.sdk.orders.commands.OrderInStoreUpdateByIdCommand}, {@link io.sphere.sdk.orders.commands.OrderInStoreUpdateByOrderNumberCommand}, {@link io.sphere.sdk.orders.queries.OrderInStoreByIdGet}, {@link io.sphere.sdk.orders.queries.OrderInStoreByOrderNumberGet}, {@link io.sphere.sdk.orders.queries.OrderInStoreQuery}</li>
    <li class=new-in-release>Added new {@link SetShippingMethod#of(ResourceIdentifier)} method</li>
    <li class=new-in-release>Added new {@link SetSupplyChannel#of(ResourceIdentifier)} method</li>
    <li class=new-in-release>Added new {@link SetTaxCategory#of(ResourceIdentifier)} method</li>
    <li class=new-in-release>Added new {@link RemoveZone#of(ResourceIdentifier)} method</li>
    <li class=change-in-release>Static 'of' methods that accept {@link Referenceable} got their names changed to 'ofReferencable' and are now deprecated. This was done for the following update actions: {@link SetShippingMethod}, {@link SetSupplyChannel}, {@link SetTaxCategory}, {@link RemoveZone}</li>
    <li class=new-in-release>Added new {@link Type} update actions : {@link io.sphere.sdk.types.commands.updateactions.ChangeEnumValueLabel}, {@link io.sphere.sdk.types.commands.updateactions.ChangeInputHint}, {@link io.sphere.sdk.types.commands.updateactions.ChangeLocalizedEnumValueLabel}</li>
 </ul>

 <h3 class=released-version id="v1_41_0">1.41.0 (10.04.2019)</h3>
 <ul>
    <li class=fixed-in-release>Generated update commands no longer produce "Unchecked generics array creation for varargs" warning</li>
    <li class=new-in-release>Added timeout property to {@link io.sphere.sdk.extensions.Extension} and {@link io.sphere.sdk.extensions.ExtensionDraft}</li>
    <li class=new-in-release>Added ttlMinutes to {@link io.sphere.sdk.customers.commands.CustomerCreatePasswordTokenCommand} command</li>
    <li class=new-in-release>added the {@link Project#getExternalOAuth()} to specify external auth providers in the project level</li>
 </ul>

 <h3 class=released-version id="v1_40_0">1.40.0 (25.02.2019)</h3>
 <ul>
    <li class=new-in-release>{@link java.time.ZonedDateTime} formatter now always include milliseconds even if they are equal to 0</li>
    <li class=new-in-release>{@link io.sphere.sdk.customobjects.queries.CustomObjectByIdGet} and {@link CustomObjectByKeyGet} now support expansion parameter</li>
    <li class=fixed-in-release>{@link Attribute#of(String, AttributeAccess, Object)} now works properly with Set of {@link Reference}</li>
    <li class=change-in-release>{@link io.sphere.sdk.producttypes.commands.updateactions.ChangeAttributeOrder} is now deprecated</li>
    <li class=new-in-release>{@link io.sphere.sdk.producttypes.commands.updateactions.ChangeAttributeOrderByName} added</li>
    <li class=new-in-release>Update commands 'of' methods now accept varargs</li>
    <li class=new-in-release>Added {@link io.sphere.sdk.orderedits.OrderEdit} with create, query, update and delete commands</li>
    <li class=new-in-release>Added {@link io.sphere.sdk.orderedits.OrderEdit} update and staged actions</li>
    <li class=new-in-release>Added {@link io.sphere.sdk.orderedits.commands.OrderEditApplyCommand}</li>
    <li class=new-in-release>Added new {@link io.sphere.sdk.orderedits.OrderEdit} message types</li>
    <li class=new-in-release>Added {@link ProductCatalogData#getCurrentUnsafe()} method</li>
 </ul>

 <h3 class=released-version id="v1_39_0">1.39.0 (14.01.2019)</h3>
 <ul>
    <li class=change-in-release> changed {@link CartDraft#getShippingMethod()}, {@link CartShippingInfo#getTaxCategory()}, {{@link CustomLineItemDraft#getTaxCategory()}, {@link AddCustomLineItem#getTaxCategory()}, {@link SetCustomShippingMethod#getTaxCategory()}, {@link SetShippingMethod#getShippingMethod()}, {@link InventoryEntryDraft#getSupplyChannel()}, {@link SetSupplyChannel#getSupplyChannel()}, {@link OrderShippingInfo#getTaxCategory()}, {@link ProductDraft#getTaxCategory()}, {@link SetTaxCategory#getTaxCategory()} return type from {@link Reference} to {@link ResourceIdentifier} </li>
    <li class=change-in-release> {@link OrderImportDraft#getShippingInfo()} is of type {@link ShippingInfoImportDraft} instead of {@link OrderShippingInfo}</li>
    <li class=change-in-release> {@link io.sphere.sdk.apiclient.ApiClient} has a new attribute {@link ApiClient#getDeleteAt()} which marks when the project key would be deleted</li>
    <li class=change-in-release> {@link io.sphere.sdk.apiclient.ApiClientDraft} has a new attribute {@link ApiClientDraft#getDeleteDaysAfterCreation()} which allows to specifiy the key life length </li>
    <li class=change-in-release> {@link LineItemDraft#getSupplyChannel()} and {@link LineItemDraft#getDistributionChannel()} are now of type {@link ResourceIdentifier<Channel>} instead of {@link Reference<Channel>}</li>
    <li class=change-in-release> {@link PriceDraft#getChannel()} is of type {@link ResourceIdentifier<Channel>} instead of {@link Reference<Channel>}</li>
    <li class=change-in-release> removed @Deprecated annotation from {@link io.sphere.sdk.carts.commands.updateactions.AddLineItem#of(String, int, long)}</li>
    <li class=change-in-release> removed @Deprecated annotation from {@link io.sphere.sdk.carts.commands.updateactions.AddLineItem#of(ProductIdentifiable, int, long)}</li>
    <li class=change-in-release> {@link ProductVariantDraft#getPrices()}, {@link ProductVariantDraft#getAttributes} and {@link ProductVariantDraft#getImages()} are now optional</li>
    <li class=change-in-release> {@link AddVariant} action now includes assets field </li>
 </ul>

 <h3 class=released-version id="v1_38_0">1.38.0 (07.12.2018)</h3>
 <ul>
    <li class=change-in-release> {@link ShippingMethodDraft}  accepts {@link List<io.sphere.sdk.shippingmethods.ZoneRateDraft>} instead of {@link List<io.sphere.sdk.shippingmethods.ZoneRate>}</li>
    <li class=change-in-release> {@link io.sphere.sdk.zones.Zone} and {@link io.sphere.sdk.zones.ZoneDraft} contains a key property</li>
    <li class=change-in-release> {@link io.sphere.sdk.zones.Zone} is updatable, deletable by key </li>
    <li class=new-in-release> {@link io.sphere.sdk.zones.commands.updateactions.SetKey} to set key for a zone </li>
    <li class=change-in-release> {@link RemoveZone#getZone()} return a {@link ResourceIdentifier<io.sphere.sdk.zones.Zone>} instead of {@link Reference<io.sphere.sdk.zones.Zone>}</li>
    <li class=change-in-release> {@link GiftLineItemCartDiscountValue#getProduct()}, {@link GiftLineItemCartDiscountValue#getDistributionChannel()}, {@link GiftLineItemCartDiscountValue#getSupplyChannel()} changed return type from {@link Reference} to {@link ResourceIdentifier}</li>
 </ul>

 <h3 class=released-version id="v1_37_0">1.37.0 (06.11.2018)</h3>
 <ul>
    <li class=change-in-release> {@link io.sphere.sdk.shippingmethods.commands.updateactions.ChangeTaxCategory} accept {@link ResourceIdentifier<TaxCategory>} as a parameter now </li
    <li class=change-in-release> {@link ShippingMethodDraftBuilder} accept a {@link ResourceIdentifier<TaxCategory>} which is a {@link Reference<TaxCategory>} generalization </li>
    <li class=change-in-release> Added support for asynchttpclient 2.5.4 </li>
    <li class=change-in-release> Now {@link ReturnItem} has two specializations {@link LineItemReturnItem} and {@link CustomLineItemReturnItem} </li>
    <li class=change-in-release> Added new field {@link UserProvidedIdentifiers#getCustomerNumber()} to reflect customer number in change messages</li>
    <li class=new-in-release> Now you can manage {@link io.sphere.sdk.apiclient.ApiClient}s programmatically, that was possible only via user interface before</li>
 </ul>

 <h3 class=released-version id="v1_36_0">1.36.0 (16.10.2018)</h3>
 <ul>
    <li class=change-in-release>added new fields {@link Payload#getResourceUserProvidedIdentifiers()} and {@link Message#getResourceUserProvidedIdentifiers()} to reference user defined identifiers.</li>
    <li class=change-in-release>added new subscriptions for the following types {@link CartDiscount}, {@link Channel}, {@link DiscountCode}, {@link io.sphere.sdk.extensions.Extension}, {@link ProductDiscount}, {@link ShoppingList}, {@link Subscription}, {@link State}, {@link TaxCategory}, {@link Type}, </li>
    <li class=change-in-release>added new field {@link Order#getRefusedGifts()} in {@link Order} </li>
    <li class=new-in-release>added new message {@link io.sphere.sdk.orders.messages.OrderCustomerGroupSetMessage} </li>
    <li class=fixed-in-release>Now the use of invalid project key leads to client shutdown, instead of token fetching retries </li>
    <li class=new-in-release>adding new authentication scopes {@link io.sphere.sdk.client.SphereProjectScope#MANAGE_EXTENSIONS} and  {@link io.sphere.sdk.client.SphereProjectScope#MANAGE_PROJECT_SETTINGS} </li>
    <li class=change-in-release>explicit sort by {@link ProductProjectionSortSearchModel#score()} in {@link ProductProjectionSearch} </li>
    <li class=new-in-release> Now you can specify the days till deletion of messages using {@link io.sphere.sdk.projects.commands.updateactions.ChangeMessagesConfiguration} update action</li>
    <li class=new-in-release>two new messages {@link io.sphere.sdk.products.messages.ProductPriceDiscountsSetMessage} and {@link io.sphere.sdk.products.messages.ProductPriceExternalDiscountSetMessage}</li>
 </ul>

 <h3 class=released-version id="v1_35_0">1.35.0 (23.08.2018)</h3>
 <ul>
    <li class=fixed-in-release>Fixed field name in cart update action {@link io.sphere.sdk.carts.commands.updateactions.SetShippingRateInput}`, which was not properly working before</li>
    <li class=change-in-release>Add field modified a to Payload {@link io.sphere.sdk.subscriptions.ResourceCreatedPayload}, {@link io.sphere.sdk.subscriptions.ResourceUpdatedPayload} and {@link io.sphere.sdk.subscriptions.ResourceDeletedPayload} </li>
    <li class=new-in-release>It's now possible to query the subscriptions endpoint health via the new {@link Subscription#getStatus()}</li>
    <li class=change-in-release>Added new attributes  {@link OrderFromCartDraft#getOrderState()} and {@link OrderFromCartDraft#getState()}</li>
    <li class=new-in-release>Added new update action {@link io.sphere.sdk.productdiscounts.commands.updateactions.SetValidFromAndUntil} to {@link ProductDiscount}</li>
    <li class=change-in-release>{@link io.sphere.sdk.producttypes.commands.updateactions.AddAttributeDefinition#of(AttributeDefinition)} changed parameter to {@link io.sphere.sdk.producttypes.commands.updateactions.AddAttributeDefinition#of(AttributeDefinitionDraft)} instead</li>
    <li class=new-in-release>It's now possible to change customer in order via {@link io.sphere.sdk.orders.commands.updateactions.SetCustomerId} which would result in a {@link io.sphere.sdk.orders.messages.OrderCustomerSetMessage}</li>
    <li  class=new-in-release>Added new update action {@link io.sphere.sdk.cartdiscounts.commands.updateactions.SetValidFromAndUntil} to {@link CartDiscount}</li>
    <li  class=new-in-release>Added new update action {@link io.sphere.sdk.discountcodes.commands.updateactions.SetValidFromAndUntil} to {@link DiscountCode}</li>
    <li class=new-in-release>Added new {@link io.sphere.sdk.orders.messages.OrderDeletedMessage}.</li>
    <li class=new-in-release>Added reference expansion to {@link io.sphere.sdk.shippingmethods.queries.ShippingMethodsByCartGet}.</li>
    <li class=new-in-release>Added reference expansion to {@link io.sphere.sdk.shippingmethods.queries.ShippingMethodsByLocationGet}</li>
 </ul>

 <h3 class=released-version id="v1_34_0">1.34.0 (10.07.2018)</h3>
 <ul>
    <li class=change-in-release>Changed {@link ProductVariantImportDraftBuilder#prices(List)} argument from {@link List<Price>} to {@link List<PriceDraft>}</li>
    <li class=added-in-release>Now {@link io.sphere.sdk.categories.commands.updateactions.ChangeParent} has an additional method {@link io.sphere.sdk.categories.commands.updateactions.ChangeParent#of(ResourceIdentifier)} to point out the changed parent via its resource identifier.</li>
    <li class=added-in-release>Now supporting google PubSub destination {@link io.sphere.sdk.subscriptions.PubSubDestination} </li>
    <li class=change-in-release>Added field {@link MessageSubscriptionPayload#getPaylaodNotIncluded()} to {@link io.sphere.sdk.subscriptions.PayloadNotIncluded} to give more details when the payload is not included in a message</li>
 </ul>

 <h3 class=released-version id="v1_33_0">1.33.0 (08.06.2018)</h3>
 <ul>
 <li class=change-in-release>Added field {@link OrderFromCartDraft#getShipmentState()} to {@link OrderFromCartDraft}</li>
 <li class=change-in-release>Added {@link CustomerQueryModel#title()} and {@link CustomerQueryModel#middleName()} to {@link CustomerQueryModel}</li>
 <li class=change-in-release>{@link LineItemImportDraftBuilder#of(ProductVariantImportDraft, long, PriceDraft, LocalizedString)} accepts price draft instead of price</li>
 <li class=change-in-release>{@link LineItemImportDraftBuilder#of(ProductVariantImportDraft, long, Price, LocalizedString)} deprecated</li>
 <li class=change-in-release>{@link ProductVariantImportDraft#getPrices()} returns a {@link PriceDraft} instead {@link Price}</li>
 <li class=change-in-release>Fixing bug {@link CustomFields} addition to {@link PriceDraft}</li>
 <li class=fixed-in-release>while making an order with prices containing custom fields, the request used to fail, this has now been fixed.</li>
 </ul>

 <h3 class=released-version id="v1_32_0">1.32.0 (23.05.2018)</h3>
 <ul>
 <li class=change-in-release>Added field {@link RemoveLineItem#getShippingDetailsToRemove()} to {@link RemoveLineItem}</li>
 <li class=change-in-release>update jackson to 2.9.5</li>
 <li class=change-in-release>Update org.asynchttpclient:async-http-client from version 2.0.38 to 2.4.5</li>
 <li class=change-in-release>Changed return type of {@link Cart#getCustomerGroup()} and {@link Customer#getCustomerGroup()} from {@link Reference<CustomerGroup>} to {@link ResourceIdentifier<CustomerGroup>}</li>
 <li class=change-in-release>Add {@link io.sphere.sdk.utils.HighPrecisionMoneyImpl} to enable high precision money in commercetools plateform when needed.</li>
 <li class=change-in-release>to enable permanent erasure of users data, a boolean parameter can be set now to specify this in
    {@link io.sphere.sdk.customers.commands.CustomerDeleteCommand},{@link io.sphere.sdk.orders.commands.OrderDeleteCommand}, {@link io.sphere.sdk.carts.commands.CartDeleteCommand}, {@link io.sphere.sdk.payments.commands.PaymentDeleteCommand},
    {@link io.sphere.sdk.shoppinglists.commands.ShoppingListDeleteCommand}, {@link io.sphere.sdk.reviews.commands.ReviewDeleteCommand}, {@link io.sphere.sdk.discountcodes.commands.DiscountCodeDeleteCommand}, {@link CustomObjectDeleteCommand}.
 </li>
 </ul>

 <h3 class=released-version id="v1_31_0">1.31.0 (12.04.2018)</h3>
 <ul>
 <li class=new-in-release>add {@link SphereJsonUtils#configureObjectMapper(ObjectMapper)} to configure
 an existing jackson object mapper for usage with our JVM SDK
 </li>
 <li class=new-in-release>added {@link Payment#getAnonymousId()}, {@link PaymentDraft#getAnonymousId()}
 and corresponding update action {@link io.sphere.sdk.payments.commands.updateactions.SetAnonymousId}</li>
 <li class=new-in-release>
 new ShippingDetails actions for order :
     {@link io.sphere.sdk.orders.commands.updateactions.SetLineItemShippingDetails},
     {@link io.sphere.sdk.orders.commands.updateactions.SetCustomLineItemShippingDetails},
     {@link io.sphere.sdk.orders.commands.updateactions.AddItemShippingAddress}
     {@link io.sphere.sdk.orders.commands.updateactions.RemoveItemShippingAddress}
     {@link io.sphere.sdk.orders.commands.updateactions.UpdateItemShippingAddress},
 supported now in our SDK.
 </li>
 <li class=change-in-release>{@link ItemShippingDetails#getTargets()} returns {@link Map<String, Long>} instead of {@link Map<String, Integer>}</li>
 <li class=new-in-release>add support for Aws lambda based extension via {@link io.sphere.sdk.extensions.AWSLambdaDestination}</li>
 <li class=removed-in-release> {@link MatchingProductDiscountGet} Query to lockup matching product discount.</li>
 <li class=fixed-in-release>Our javadoc now contains documentation for our generated classes too.</li>
 <li class=change-in-release>Correct typo on {@link SetAssetCustomType#ofSkuAndAssetKey(String, String, CustomFieldsDraft)}, previously named ofSkuAndAssetKeyAndAssetKey</li>
 <li class=change-in-release>Added support for Order extensions {@link ExtensionResourceType#ORDER}</li>
 </ul>

 <h3 class=released-version id="v1_30_0">1.30.0 (08.03.2018)</h3>
 <ul>
 <li class=new-in-release>
 Added customer change messages:
 {@link io.sphere.sdk.customers.messages.CustomerAddressAddedMessage},
 {@link io.sphere.sdk.customers.messages.CustomerAddressChangedMessage},
 {@link io.sphere.sdk.customers.messages.CustomerAddressRemovedMessage},
 {@link io.sphere.sdk.customers.messages.CustomerCompanyNameSetMessage},
 {@link io.sphere.sdk.customers.messages.CustomerDateOfBirthSetMessage},
 {@link io.sphere.sdk.customers.messages.CustomerEmailChangedMessage},
 {@link io.sphere.sdk.customers.messages.CustomerEmailVerifiedMessage},
 {@link io.sphere.sdk.customers.messages.CustomerGroupSetMessage}
 </li>
 <li class=new-in-release>Cart replication feature with {@link io.sphere.sdk.carts.commands.CartReplicationCommand} and {@link io.sphere.sdk.carts.commands.CartReplicationDraft}</li>
 <li class=change-in-release>Changed visibility of {@link ProductLike} interface to public.</li>
 <li class=change-in-release>Added {@link ExtensionResourceType} in order to add type check while creating {@link Trigger#getResourceTypeId()} field, instead of using string laterals.</li>
 <li class=change-in-release>Update jackson from 2.9.3 to 2.9.4 </li>
 <li class=change-in-release>Changed return type of {@link ProductTypeDraft#getAttributes()} from {@link List<AttributeDefinition>} for {@link List<io.sphere.sdk.products.attributes.AttributeDefinitionDraft>}</li>
 <li class=change-in-release>Changed return type of {@link ProductTypeDraft#getAttributes()} from {@link List<AttributeDefinition>} for {@link List<io.sphere.sdk.products.attributes.AttributeDefinitionDraft>}</li>
 <li class=change-in-release>Changed return type of {@link ProductTypeDraftDsl#getAttributes()} from {@link List<AttributeDefinition>} for {@link List<io.sphere.sdk.products.attributes.AttributeDefinitionDraft>}</li>
 <li class=change-in-release>Added {@link ItemShippingDetails#getTargetsMap()} convenience method.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.producttypes.commands.updateactions.ChangeEnumKey} and  {@link io.sphere.sdk.producttypes.commands.updateactions.ChangeAttributeName} update actions to {@link ProductType}</li>
 <li class=fixed-in-release>Added missing accessor method for {@code assetKey} to {@link ChangeAssetName#getAssetKey()} and {@link SetAssetCustomField#getAssetKey()}.</li>
 </ul>

 <h3 class=released-version id="v1_29_1">1.29.1 (22.02.2018)</h3>
 <ul>
 <li class=fixed-in-release>Javamoney issue with class loader.</li>
 </ul>

 <h3 class=released-version id="v1_29_0">1.29.0 (08.02.2018)</h3>
 <ul>
 <li class=change-in-release>added {@link CustomFields} to {@link CustomerGroup}</li>
 <li class=change-in-release>added  {@link io.sphere.sdk.states.StateRole#RETURN}  to {@link io.sphere.sdk.states.StateRole} enumeration</li>
 <li class=change-in-release>added {@link DiscountCode#getGroups()} to {@link DiscountCode}</li>
 <li class=change-in-release>added field deliveryId (referring delivery) to {@link io.sphere.sdk.orders.messages.ParcelItemsUpdatedMessage}</li>
 <li class=change-in-release>updated jackson version to 2.9.3</li>
 <li class=change-in-release>added {@link io.sphere.sdk.discountcodes.DiscountCodeState#APPLICATION_STOPPED_BY_PREVIOUS_DISCOUNT} and {@link io.sphere.sdk.discountcodes.DiscountCodeState#NOT_VALID} to {@link io.sphere.sdk.discountcodes.DiscountCodeState} </li>
 <li class=change-in-release>added field origin of type {@link CartOrigin} to {@link Cart} and {@link Order} with the associated import actions and drafts</li>
 <li class=change-in-release>added {@link Cart#getItemShippingAddresses()} field to {@link Cart} and {@link Order} and associated drafts </li>
 <li class=change-in-release>added {@link ItemShippingDetails} to {@link CustomLineItem} and {@link LineItem} and associated drafts </li>
 <li class=change-in-release>added {@link ProductVariantQueryModel#key()} to {@link ProductVariantQueryModel} </li>
 <li class=new-in-release>added {@link ItemShippingDetails} and {@link ItemShippingTarget} as support for new multiple shipping addresses feature from backend.</li>
 </ul>

 <h3 class=released-version id="v1_28_0">1.28.0 (18.01.2018)</h3>
 <ul>
 <li class=change-in-release>{@link CategoryDraft#getParent()} changed return type from {@link Reference<Category>} to {@link ResourceIdentifier<Category>}</li>
 <li class=change-in-release>{@link CategoryDraftBuilder#parent(Referenceable)} is now deprecated and should be replaced by the new method {@link CategoryDraftBuilder#parent(ResourceIdentifier)}</li>
 <li class=change-in-release>Updated Asynchronous Http Client to 2.0.38</li>
 <li class=change-in-release>Added member {@link ParcelDraft#getItems()}</li>
 <li class=new-in-release>Added {@link DiscountCode#getValidFrom()} and {@link DiscountCode#getValidUntil()}</li>
 <li class=new-in-release>Added {@link ProductDiscount#getValidFrom()} and {@link ProductDiscount#getValidUntil()}</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.orders.messages.OrderReturnShipmentStateChangedMessage}</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.orders.messages.OrderShipmentStateChangedMessage}</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.taxcategories.queries.TaxCategoryQuery#byKey(String)} to {@link io.sphere.sdk.taxcategories.queries.TaxCategoryQuery}</li>
 <li class=new-in-release>Added {@link PagedSearchResult#empty()} for API consistency reasons</li>
 <li class=new-in-release>Added {@link OrderExpansionModel#state()} for API consistency reasons</li>
 <li class=new-in-release>Added a new endpoint {@link io.sphere.sdk.extensions.Extension} and query, delete, update actions all listed in the new in {@link io.sphere.sdk.extensions} package.</li>
 <li class=new-in-release>Added {@link ParcelDraftBuilder}</li>
 </ul>

 <h3 class=released-version id="v1_27_0">1.27.0 (14.12.2017)</h3>
 <ul>
 <li class=new-in-release>Added {@link OrderImportDraft#getTaxCalculationMode()}.</li>
 <li class=new-in-release>Added new {@link io.sphere.sdk.orders.messages.OrderReturnShipmentStateChangedMessage}
 and {@link io.sphere.sdk.orders.messages.OrderShipmentStateChangedMessage}.</li>
 <li class=new-in-release>Added {@link OrderImportDraft#getTaxCalculationMode()}.</li>
 <li class=new-in-release>Added {@link AddDelivery#getAddress()}.</li>
 <li class=fixed-in-release>Fixed bug in serialization of {@link LocalizedEnumValue}.</li>
 <li class=new-in-release>Added new update action to change the attribute constraint of a product type
 {@link io.sphere.sdk.producttypes.commands.updateactions.ChangeAttributeConstraint}.</li>
 <li class=new-in-release>The following product variant update actions can now use the {@link Asset#getKey()} property to identify an asset:
 {@link RemoveAsset}, {@link SetAssetTags}, {@link ChangeAssetName}, {@link SetAssetDescription}, {@link SetAssetSources}, {@link SetAssetCustomType},
 {@link SetAssetCustomField}.
 </li>
 <li class=new-in-release>The following category update actions can now use the {@link Asset#getKey()} property to identify an asset:
 {@link io.sphere.sdk.categories.commands.updateactions.RemoveAsset}, {@link io.sphere.sdk.categories.commands.updateactions.SetAssetTags},
 {@link io.sphere.sdk.categories.commands.updateactions.ChangeAssetName}, {@link io.sphere.sdk.categories.commands.updateactions.SetAssetDescription},
 {@link io.sphere.sdk.categories.commands.updateactions.SetAssetSources}, {@link io.sphere.sdk.categories.commands.updateactions.SetAssetCustomType},
 {@link io.sphere.sdk.categories.commands.updateactions.SetAssetCustomField}.
 </li>

 <li class=new-in-release>
 Added optional property position on update actions {@link AddAsset#getPosition()} and {@link io.sphere.sdk.categories.commands.updateactions.AddAsset#getPosition()}
 </li>
 <li class=new-in-release>Added new product update action {@link RevertStagedVariantChanges}.</li>
 <li class=new-in-release>Added custom fields for cart discounts {@link CartDiscount#getCustom()}, {@link CartDiscountDraft#getCustom()} and the
 update action {@link io.sphere.sdk.cartdiscounts.commands.updateactions.SetCustomField} and
 {@link io.sphere.sdk.cartdiscounts.commands.updateactions.SetCustomType}.</li>
 </ul>


 <h3 class=released-version id="v1_26_0">1.26.0 (20.11.2017)</h3>
 <ul>
 <li class=new-in-release>Added update action for setting the predicate of a shipping method {@link io.sphere.sdk.shippingmethods.commands.updateactions.SetPredicate}.
 </li>
 <li class=new-in-release>Added shopping list item sku {@link io.sphere.sdk.shoppinglists.LineItemDraft#getSku()}.
 </li>
 <li class=new-in-release>Added support for new cart tax calculation mode {@link Cart#getTaxCalculationMode()}, {@link CartDraft#getTaxCalculationMode()}
 and the corresponding update action {@link io.sphere.sdk.carts.commands.updateactions.ChangeTaxCalculationMode}.
 </li>

 <li class=new-in-release>Added support for creating multi buy discounts for custom line items{@link io.sphere.sdk.cartdiscounts.MultiBuyCustomLineItemsTarget},
 {@link io.sphere.sdk.cartdiscounts.SelectionMode}.
 </li>
 <li class=new-in-release>Added support for getting {@link io.sphere.sdk.orders.queries.OrderByOrderNumberGet},
 updating {@link io.sphere.sdk.orders.commands.OrderUpdateCommand#ofOrderNumber(String, Long, UpdateAction)} and
 deleting {@link io.sphere.sdk.orders.commands.OrderDeleteCommand#ofOrderNumber(String, Long)} of an order by order number.
 </li>
 <li class=new-in-release>
 Added new {@link CustomerSignInCommand#updateProductData} property to {@link CustomerSignInCommand}.
 </li>
 </ul>

 <h3 class=released-version id="v1_25_0">1.25.0 (19.10.2017)</h3>
 <ul>
 <li class=new-in-release>Added support for OSGi.</li>
 <li class=new-in-release>Added support for keys in {@link CategoryTree} with {@link CategoryTree#findByKey(String)}.</li>
 <li class=new-in-release>
    Added new shipping rate input property in {@link Order#getShippingRateInput()} and {@link Project#getShippingRateInputType()}.
    Added new tiered shipping rates property in {@link ShippingRate#getTiers()}.
 </li>
 <li class=new-in-release>
    Added support for new parcel items property in {@link Parcel#getItems()} and {@link ParcelMeasurements#getItems()}.
 </li>
 <li class=new-in-release>Added new product type update action {@link RemoveEnumValues}.</li>
 <li class=new-in-release>Added new product update action {@link SetImageLabel}.</li>
 <li class=new-in-release>Added getter methods for our generated draft builder classes.</li>
 <li class=new-in-release>Added new property {@link CartDiscount#getStackingMode()}, {@link CartDiscountDraft#getStackingMode()} and enum {@link StackingMode}.</li>
 <li class=new-in-release>
    Improved performance of {@link QueryExecutionUtils} helper class by providing the query method
    {@link QueryExecutionUtils#queryAll(SphereClient, QueryDsl, Consumer)} .
 </li>
 <li class=change-in-release>Marked {@link ProductDraft#getMasterVariant()} as nullable.</li>
 </ul>

 <h3 class=released-version id="v1_24_0">1.24.0 (29.09.2017)</h3>
 <ul>
 <li class=change-in-release>Changed type of {@link io.sphere.sdk.shoppinglists.LineItemDraft#getCustom()} to {@link CustomFieldsDraft}.
 This breaking change might require changes in your source code.
 </li>
 <li class=change-in-release>Update com.fasterxml.jackson.core plugins group from 2.6.5 to 2.8.9</li>
 <li class=fixed-in-release>Added missing {@link io.sphere.sdk.shoppinglists.LineItemDraftBuilder#custom(CustomFieldsDraft)} builder method
 to replace now deprecated {@link io.sphere.sdk.shoppinglists.LineItemDraftBuilder#custom(CustomFields)} builder method.
 </li>
 <li class=change-in-release>Deprecated several payment properties and update actions:
    <ul>
        <li>{@link Payment#getAmountAuthorized()}, {@link Payment#getAmountPaid()}, {@link Payment#getAmountRefunded()},
            {@link Payment#getAuthorizedUntil()} and {@link Payment#getExternalId()}
        </li>
        <li>{@link PaymentDraft#getAmountAuthorized()}, {@link PaymentDraft#getAmountPaid()}, {@link PaymentDraft#getAmountRefunded()},
        {@link PaymentDraft#getAuthorizedUntil()} and {@link PaymentDraft#getExternalId()}
        </li>
        <li>{@link io.sphere.sdk.payments.commands.updateactions.SetAmountPaid}, {@link io.sphere.sdk.payments.commands.updateactions.SetAmountRefunded},
        {@link io.sphere.sdk.payments.commands.updateactions.SetAuthorization} and {@link io.sphere.sdk.payments.commands.updateactions.SetExternalId}
        </li>
    </ul>
 </li>
 <li class=fixed-in-release>Fixed {@link NullPointerException} in {@link TypeDraftBuilder#plusFieldDefinitions(FieldDefinition)}.</li>
 <li class=new-in-release>Added new custom fields on discount code {@link DiscountCode#getCustom()}, {@link DiscountCodeDraft#getCustom()} and
    corresponding update actions {@link io.sphere.sdk.discountcodes.commands.updateactions.SetCustomType}, {@link io.sphere.sdk.discountcodes.commands.updateactions.SetCustomField}
 </li>
 <li class=new-in-release>Added new project update actions {@link io.sphere.sdk.projects.commands.updateactions.ChangeCountries},
 {@link io.sphere.sdk.projects.commands.updateactions.ChangeCurrencies}, {@link io.sphere.sdk.projects.commands.updateactions.ChangeLanguages},
 {@link io.sphere.sdk.projects.commands.updateactions.ChangeMessages}, {@link io.sphere.sdk.projects.commands.updateactions.ChangeMessagesEnabled}
 and {@link io.sphere.sdk.products.commands.updateactions.ChangeName}.
 </li>
 <li class=new-in-release>Added {@code plus*} methods to generated draft builder to ease working with list properties
 (e.g. {@link io.sphere.sdk.shoppinglists.ShoppingListDraftBuilder#plusLineItems(io.sphere.sdk.shoppinglists.LineItemDraft)},
 {@link io.sphere.sdk.shoppinglists.ShoppingListDraftBuilder#plusLineItems(List)}).
 </li>
 <li class=new-in-release>Added missing {@link io.sphere.sdk.payments.queries.PaymentByKeyGet} query,
 {@link io.sphere.sdk.payments.commands.PaymentDeleteCommand#ofKey(String, Long)} and<
 {@link io.sphere.sdk.payments.commands.PaymentUpdateCommand#ofKey(String, Long, UpdateAction)} commands.
 </li>
 <li class=new-in-release>Added support for creating multi buy discounts {@link io.sphere.sdk.cartdiscounts.MultiBuyLineItemsTarget},
 {@link io.sphere.sdk.cartdiscounts.SelectionMode}.
 </li>
 </ul>
 <h3 class=released-version id="v1_23_1">1.23.1 (26.09.2017)</h3>
 <ul>
 <li class=fixed-in-release>Fixed NullPointerException in {@link PriceDraftBuilder#of(Price)}.</li>
 </ul>
 <h3 class=released-version id="v1_23_0">1.23.0 (11.09.2017)</h3>
 <ul>
 <li class=new-in-releas>Added new shipping method predicate {@link ShippingMethod#getPredicate()}, {@link ShippingMethodDraft#getPredicate()}.</li>
 <li class=new-in-release>Added new shipping info method state {@link CartShippingInfo#getShippingMethodState()}.</li>
 <li class=new-in-release>Introduced new {@link io.sphere.sdk.cartdiscounts.CartPredicate} which generalizes and deprecates the
 {@link io.sphere.sdk.cartdiscounts.CartDiscountPredicate}.</li>
 <li class=new-in-release>Added new tax mode {@link TaxMode#EXTERNAL_AMOUNT}. Added new {@link ExternalTaxAmountDraft} type, which can be used to set the external tax amount
 with the new update actions {@link io.sphere.sdk.carts.commands.updateactions.SetLineItemTaxAmount}, {@link io.sphere.sdk.carts.commands.updateactions.SetCustomLineItemTaxAmount},
 {@link io.sphere.sdk.carts.commands.updateactions.SetShippingMethodTaxAmount} and {@link io.sphere.sdk.carts.commands.updateactions.SetCartTotalTax}.</li>
 <li class=new-in-release>Add support for getting {@link io.sphere.sdk.shippingmethods.queries.ShippingMethodByKeyGet} and updating {@link io.sphere.sdk.shippingmethods.commands.ShippingMethodUpdateCommand#ofKey(String, Long, UpdateAction)}
 a shipping method by key.</li>
 <li class=new-in-release>Added correlation id to oauth requests.</li>
 <li class=new-in-release>Added new {@link io.sphere.sdk.carts.commands.updateactions.SetAnonymousId} cart update action.</li>
 <li class=new-in-release>Added new key property to customer {@link Customer#getKey()}, {@link CustomerDraft#getKey()} and corresponding update action {@link io.sphere.sdk.customers.commands.updateactions.SetKey}.
 Added {@link io.sphere.sdk.customers.queries.CustomerByKeyGet}, {@link io.sphere.sdk.customers.commands.CustomerDeleteCommand#ofKey(String, Long)} and {@link io.sphere.sdk.customers.commands.CustomerUpdateCommand#ofKey(String, Long, UpdateAction)}.</li>
 <li class=new-in-release><Added support for publish prices only {@link PublishScope}, {@link io.sphere.sdk.products.commands.updateactions.Publish#ofScope(PublishScope)} and {@link ProductPublishedMessage#getScope()}.</li>
 </ul>
 <h3 class=released-version id="v1_22_0">1.22.0 (09.08.2017)</h3>
 <ul>
 <li class=new-in-release>A correlation id is now generated for each {@link SphereRequest}.</li>
 <li class=new-in-release>Added {@link TaxCategory#getKey()}, {@link TaxCategoryDraft#getKey()} and
 corresponding update action {@link io.sphere.sdk.taxcategories.commands.updateactions.SetKey}.
 </li>
 <li class=new-in-release>
 Added {@link io.sphere.sdk.taxcategories.queries.TaxCategoryByKeyGet} and
 {@link io.sphere.sdk.taxcategories.commands.TaxCategoryDeleteCommand#ofKey(String, Long)}.
 </li>
 <li class=new-in-release>Add {@link LineItemDraft#getSku()}.</li>
 <li class=new-in-release>Add {@link io.sphere.sdk.shippingmethods.ShippingMethod#getKey()},
 {@link io.sphere.sdk.shippingmethods.ShippingMethodDraft#getKey()} and update action
 {@link io.sphere.sdk.shippingmethods.commands.updateactions.SetKey}.
 </li>
 <li class=new-in-release>
 Added {@link io.sphere.sdk.shippingmethods.commands.ShippingMethodDeleteCommand#ofKey(String, Long)}.
 </li>
 <li class=new-in-release>Add {@link CartDraft#getCustomerGroup()} and update action
 {@link io.sphere.sdk.carts.commands.updateactions.SetCustomerGroup}.</li>
 </ul>


 <h3 class=released-version id="v1_21_0">1.21.0 (18.07.2017)</h3>
 <ul>
 <li class=new-in-release>Added {@link CustomerGroup#getKey()} and {@link CustomerGroupDraft#getKey()}.</li>
 <li class=new-in-release>Added {@link ProductPublishedMessage#getRemovedImageUrls()}.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.customers.queries.CustomerByEmailTokenGet} to retrieve a customer by email token.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.producttypes.commands.updateactions.SetInputTip} update action.</li>
 <li class=fixed-in-release>{@link io.sphere.sdk.customers.queries.CustomerByPasswordTokenGet} now uses the new endpoint.</li>
 <li class=change-in-release>Changed type of {@link ProductDraft#getCategories()} from Set&lt;Reference&lt;Category&gt;&gt; to Set&lt;ResourceIdentifier&lt;Category&gt;&gt;.
 This breaking change may require an update of your source code, depending on how you use the {@link ProductDraft} type.
 The previously returned {@link Reference} instances provided a {@link Reference#getObj()} method
 that is not available from {@link ResourceIdentifier}. If you relied on the {@link Reference#getObj()} method to retrieve the id or key of the referenced object, you now have
 to use the {@link ResourceIdentifier#getId()} and {@link ResourceIdentifier#getKey()} as exposed by the {@link ResourceIdentifier} interface.
 <li class=change-in-release>Updated <a href="https://javamoney.github.io/ri.html#welcome-to-moneta---the-jsr-354-reference-implementation">moneta</a> to version 1.1</li>
 <li class=change-in-release>Changed {@link io.sphere.sdk.client.SphereProjectScope} from an enum to a class and added missing scopes. This change doesn't require changes to your source code,
 but requires a recompilation of all projects that depend on this class.</li>
 </ul>

 <h3 class=released-version id="v1_20_0">1.20.0 (23.06.2017)</h3>
 <ul>
 <li class=new-in-release>Added support for Azure ServiceBus subscription destinations {@link AzureServiceBusDestination}.</li>
 <li class=new-in-release>Added support for for external line item prices {@link LineItemDraft#getExternalPrice()}, {@link LineItemPriceMode#EXTERNAL_PRICE}.</li>
 <li class=new-in-release>Added {@link ChangeInputHint} update action for product types/attribute definitions.</li>
 <li class=new-in-release>Added {@link CustomerInvalidCurrentPassword} error.</li>
 <li class=fixed-in-release>Fixed bug in {@link CustomFieldsDraftBuilder#of(CustomFields)}, where the id was confused with the typeId.</li>
 </ul>

 <h3 class=released-version id="v1_19_0">1.19.0 (06.06.2017)</h3>
 <ul>
 <li class=new-in-release>Added getter method {@link SphereClient#getConfig()} for accessing the {@link SphereApiConfig}.</li>
 <li class=new-in-release>Added new messages {@link OrderPaymentStateChangedMessage}, {@link PaymentStatusInterfaceCodeSetMessage},
 {@link ProductRevertedStagedChangesMessage}, {@link ProductVariantDeletedMessage} and {@link ProductDeletedMessage}.</li>
 <li class=new-in-release>Added new cart discount value {@link GiftLineItemCartDiscountValue} for gift line items {@link LineItem#getLineItemMode()},
 {@link LineItemMode#GIFT_LINE_ITEM}.</li>
 <li class=new-in-release>Added new key property for category {@link Category#getKey()}, {@link CategoryDraft#getKey()} and corresponding update action {@link SetKey}.</li>
 <li class=new-in-release>Added new salutation for customer {@link Customer#getSalutation()}, {@link CustomerDraft#getSalutation()} and
 corresponding update action {@link SetSalutation ()}.</li>
 <li class=new-in-release>Added {@link ProductImageUploadCommand}, which previously was only available via a separate module.</li>
 <li class=change-in-release>Added missing {@link javax.annotation.Nullable} annotations to {@link PagedSearchResult}.</li>
 </ul>

 <h3 class=released-version id="v1_18_0">1.18.0 (18.05.2017)</h3>
 <ul>
 <li class=change-in-release>The {@link ProductDraftBuilder} class is now using a generated base class. This change doesn't require changes to your source code,
 but requires a recompilation of all projects that depend on this class.</li>
 <li class=new-in-release>{@link io.sphere.sdk.categories.queries.CategoryQuery} now
 supports sorting by {@link Category#getOrderHint()} via {@link CategoryQueryModel#orderHint()}.</li>
 </li>
 <li class=new-in-release>Added {@link io.sphere.sdk.products.commands.updateactions.SetAssetSources} update action for products.</li>
 <li class=new-in-release>Added new message {@link io.sphere.sdk.products.messages.ProductImageAddedMessage}.</li>
 <li class=fixed-in-release>The shopping lists {@link io.sphere.sdk.shoppinglists.LineItem#getVariant()} {@link ProductVariant#getIdentifier()}
 is now correctly initialized.</li>
 <li class=fixed-in-release>Removed the incorrect nullable annotation from {@link InventoryEntryDraft#getQuantityOnStock()}.</li>
 </ul>

 <h3 class=released-version id="v1_17_0">1.17.0 (28.04.2017)</h3>
 <ul>
 <li class=new-in-release>Added {@link Payment#getKey()}, {@link PaymentDraft#getKey()} and
 corresponding update action {@link io.sphere.sdk.payments.commands.updateactions.SetKey}.</li>
 <li class=new-in-release>Added copy factor methods to draft builder classes ({@link CategoryDraftBuilder#of(Category)},
 {@link InventoryEntryDraftBuilder#of(InventoryEntry)}, {@link AssetDraftBuilder#of(Asset)}, {@link PriceDraftBuilder#of(Price)},
 {@link ProductVariantDraftBuilder#of(ProductVariant)}, {@link io.sphere.sdk.products.attributes.AttributeDraftBuilder#of(Attribute)} and
 {@link ProductTypeDraftBuilder#of(ProductType)}) to convert from a resource to the corresponding resource draft.
 These changes don't require changes to your source code, but they require a recompilation of all projects that depend on these classes.</li>
 <li class=new-in-release>Added support for change subscriptions {@link io.sphere.sdk.subscriptions.ChangeSubscription} and
 message subscriptions {@link io.sphere.sdk.subscriptions.MessageSubscription}.
 </li>
 </ul>

 <h3 class=released-version id="v1_16_0">1.16.0 (12.04.2017)</h3>
 <ul>
 <li class=new-in-release>Added {@link ShippingRate#isMatching()}.</li>
 <li class=change-in-release>To enable code generation, we changed the type of {@link ShippingRate} from class to interface.
 This change doesn't require you to change your source code, but requires a recompilation of all projects that depend on this class.
 </li>
 </ul>

 <h3 class=released-version id="v1_15_0">1.15.0 (04.04.2017)</h3>
 <ul>
 <li class=new-in-release>Added query model {@link PriceTierQueryModel} for tiered prices {@link PriceQueryModel#tiers()}.</li>
 </ul

 <h3 class=released-version id="v1_14_1">1.14.1 (30.03.2017)</h3>
 <ul>
 <li class=fixed-in-release>Fix for {@link LineItemExpansionModel#variant()} so that it works correctly.
 This is a breaking change, but the previous version was incorrect and didn't work.</li>
 </ul>


 <h3 class=released-version id="v1_14_0">1.14.0 (28.03.2017)</h3>
 <ul>
 <li class=new-in-release>Added {@link PriceTier} for tired prices, which are accessible via {@link Price#getTiers()} and {@link PriceDraft#getTiers()}</li>
 </ul>

 <h3 class=released-version id="v1_13_0">1.13.0 (20.03.2017)</h3>
 <ul>
 <li class=new-in-release>{@link OrderImportDraft} now provides custom fields {@link OrderImportDraft#getCustom()}</li>
 <li class=new-in-release>{@link LineItemImportDraft} now provides custom fields {@link LineItemImportDraft#getCustom()}</li>
 <li class=new-in-release>{@link CustomerSignInResultExpansionModel} now provides expansion of the cart {@link CustomerSignInResultExpansionModel#cart}</li>
 <li class=new-in-release>{@link ProductVariantAvailabilityFilterSearchModel} now provides filtering by {@code isOnStockInChannels} via {@link ProductVariantAvailabilityFilterSearchModel#onStockInChannels()}</li>
 <li class=new-in-release>{@link ShoppingList} and {@link ShoppingListDraft} now provide time to live attribute {@link ShoppingList#getDeleteDaysAfterLastModification()} and {@link ShoppingListDraft#getDeleteDaysAfterLastModification()}</li>
 <li class=new-in-release>{@link Cart} and {@link CartDraft} now provide time to live attribute {@link Cart#getDeleteDaysAfterLastModification()} and {@link CartDraft#getDeleteDaysAfterLastModification()}</li>
 <li class=new-in-release>{@link ProductVariantSortSearchModel} now provides sorting by {@code sku} {@link ProductVariantSortSearchModel#sku()}</li>
 <li class=new-in-release>Product update actions now support {@code staged} parameter</li>
 <li class=change-in-release>Some of our draft builder now return the more specific {@code <Draft>Dsl} types.
 This change doesn't require you to change your source code, but requires a recompilation of all projects that depend on these classes.
 The following classes changed:
 <ul>
 <li>{@link CartDiscountDraftBuilder}</li>
 <li>{@link CategoryDraftBuilder}</li>
 <li>{@link CustomerGroupDraftBuilder}</li>
 <li>{@link InventoryEntryDraftBuilder}</li>
 <li>{@link PaymentDraftBuilder}</li>
 <li>{@link ProductDiscountDraftBuilder}</li>
 <li>{@link ProductVariantDraftBuilder}</li>
 <li>{@link ProductTypeDraftBuilder}</li>
 <li>{@link ReviewDraftBuilder}</li>
 <li>{@link ShippingMethodDraftBuilder}</li>
 <li>{@link TaxCategoryDraftBuilder}</li>
 <li>{@link TypeDraftBuilder}</li>
 <li>{@link ZoneDraftBuilder}</li>
 <li>{@link ZoneDraftBuilder}</li>
 </ul>
 <li class=change-in-release>The missing and exists filter support is now using separate interfaces.
 This change doesn't require you to change your source code, but requires a recompilation of all projects that depend on these classes.
 The following classes changed:
 <ul>
 <li>{@link ProductAttributeFilterSearchModel}</li>
 <li>{@link ProductVariantFilterSearchModel}</li>
 </ul>
 </li>
 <li class=fixed-in-release>{@link ProductVariantDraftBuilder#of(ProductVariantDraft)} now correctly copies all attributes of the given {@code template}</li>
 </ul>

 <h3 class=released-version id="v1_12_0">1.12.0 (27.02.2017)</h3>
 <ul>
 <li class=new-in-release>Added {@link Channel#getGeoLocation()}, added support for it in {@link ChannelQueryModel#geoLocation()} and support for {@code withinCircle}
 predicate {@link GeoJSONQueryModel#withinCircle(Point, Double)}.</li>
 <li class=new-in-release>Added shopping list resource {@link io.sphere.sdk.shoppinglists.ShoppingList} with new cart update action {@link io.sphere.sdk.carts.commands.updateactions.AddShoppingList}.</li>
 <li class=change-in-release>Improved recovery strategy for {@link io.sphere.sdk.sequencegenerators.CustomObjectBigIntegerNumberGenerator} so that it retries when the id already exists.</li>
 <li class=change-in-release>Updated {@code org.asynchttpclient:async-http-client} version of the {@code sdk-http-ahc-2_0} module to version {@code 2.0.28}.</li>
 </ul>

 <h3 class=released-version id="v1_11_0">1.11.0 (07.02.2017)</h3>
 <ul>
 <li class=new-in-release>{@link Asset}s on categories</li>
 <li class=new-in-release>{@link OrderImportDraft} can now provide the {@code inventoryMode} to track inventory</li>
 <li class=fixed-in-release>{@code Set} types preserve input order, see <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/1294">#1294</a></li>
 <li class=fixed-in-release>{@link ProductDraftBuilder} was not preserving the {@code key} when using a {@link ProductDraft}</li>
 </ul>

 <h3 class=released-version id="v1_10_0">1.10.0 (17.01.2017)</h3>
 <ul>
 <li class=new-in-release>Carts and orders have a new field taxRoundingMode. This rounding mode is used when calculating taxes. On Cart it is possible to set this field either at creation time or via the new update action {@link io.sphere.sdk.carts.commands.updateactions.ChangeTaxRoundingMode}. When creating an Order from a Cart, the Order has the tax rounding mode of the Cart it was created from.</li>
 <li class=new-in-release>{@link LineItem#getProductType()}</li>
 </ul>


 <h3 class=released-version id="v1_9_0">1.9.0 (06.01.2017)</h3>
 <ul>
 <li class=new-in-release>{@link LineItem#getPriceMode()}</li>
 <li class=new-in-release>Provided {@link PriceUtils} class to assist on some typical price calculations, such as gross and net conversions</li>
 <li class=new-in-release>Added {@link CartLike#calculateSubTotalPrice()} and {@link CartLike#calculateTotalAppliedTaxes()}</li>
 </ul>


 <h3 class=released-version id="v1_8_0">1.8.0 (09.12.2016)</h3>
 <ul>
 <li class="change-in-release">{@link CustomerQueryModel#customerGroup()}, {@link io.sphere.sdk.inventory.queries.InventoryEntryQueryModel#supplyChannel()} and {@link io.sphere.sdk.orders.queries.OrderQueryModel#cart()} returns a {@link io.sphere.sdk.queries.ReferenceOptionalQueryModel} instead of {@link io.sphere.sdk.queries.ReferenceQueryModel} to enable additional query predicates.</li>
 <li class=new-in-release>{@link CustomerQueryModel#customerNumber()} </li>
 <li class=new-in-release>links to the Java SE classes are provided in the SDK so for example for {@link String} and {@link CompletionStage}</li>
 <li class=new-in-release>{@link Customer#getShippingAddressIds()} and {@link Customer#getBillingAddressIds()} as well as the update actions {@link io.sphere.sdk.customers.commands.updateactions.AddBillingAddressId}, {@link io.sphere.sdk.customers.commands.updateactions.RemoveBillingAddressId}, {@link AddShippingAddressId}, {@link io.sphere.sdk.customers.commands.updateactions.RemoveShippingAddressId}</li>
 <li class=new-in-release>create commands got withers for the draft objects like {@code io.sphere.sdk.products.commands.ProductCreateCommand#withDraft(Object)} </li>
 <li class=new-in-release>{@link FilteredFacetResult#getProductCount()} </li>
 </ul>


 <h3 class=released-version id="v1_7_0">1.7.0 (22.11.2016)</h3>
 <ul>
 <li class="change-in-release">In {@code DraftBuilder} classes the {@code build()} method returns a {@code DraftDsl} class instead of its corresponding {@code Draft} interface. Similarly some methods relying on these builders have changed their return type to {@code DraftDsl} as well. The following methods are affected by this change: {@link CartDiscountDraftBuilder#build()}, {@link CategoryDraftBuilder#build()}, {@link CustomerDraftBuilder#build()}, {@link InventoryEntryDraftBuilder#build()}, {@link PaymentDraftBuilder#build()}, {@link ProductDiscountDraftBuilder#build()}, {@link ReviewDraftBuilder#build()}, {@link TypeDraftBuilder#build()}, {@link StateDraftDsl#withRoles(io.sphere.sdk.states.StateRole)}, {@link StateDraftDsl#withRoles(Set)} and {@link StateDraftDsl#withTransitions(Set)}.</li>
 <li class=new-in-release>For each resource draft builders and implementation classes are generated</li>
 <li class=new-in-release>{@link io.sphere.sdk.products.commands.updateactions.SetDiscountedPrice}</li>
 <li class=new-in-release>{@link Address#getExternalId()} </li>
 <li class=new-in-release>{@link ProductProjectionSearch#withMarkingMatchingVariants(Boolean)}</li>
 </ul>


 <h3 class=released-version id="v1_6_0">1.6.0 (21.10.2016)</h3>
 <ul>
 <li class=new-in-release>expansion paths for discounts in line items and custom line items</li>
 <li class=new-in-release>change enum labels for product types: {@link io.sphere.sdk.producttypes.commands.updateactions.ChangePlainEnumValueLabel} and {@link io.sphere.sdk.producttypes.commands.updateactions.ChangeLocalizedEnumValueLabel}</li>
 <li class=new-in-release>add convenience factory methods for {@link SetAttribute}: {@link SetAttribute#ofVariantId(java.lang.Integer, java.lang.String, java.lang.Object)}, {@link SetAttribute#ofSku(java.lang.String, java.lang.String, java.lang.Object)}</li>
 <li class=new-in-release>{@link io.sphere.sdk.orders.errors.PriceChangedError}</li>
 <li class=new-in-release>{@link ProductDraftBuilder#of(ResourceIdentifiable, LocalizedString, LocalizedString, List)} to pass all variants as one list</li>
 <li class=change-in-release>{@link io.sphere.sdk.search.model.Range} constructor no longer throws {@code InvertedBoundsException}. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/1247">#1247</a></li>
 <li class=fixed-in-release>A JavaMoney initialization issue with sbt has been fixed.</li>
 <li class=fixed-in-release>{@link io.sphere.sdk.sequencegenerators.CustomObjectBigIntegerNumberGenerator} does retries only on {@link io.sphere.sdk.client.ConcurrentModificationException}s.</li>
 </ul>

 <h3 class=released-version id="v1_5_0">1.5.0 (14.10.2016)</h3>
 <ul>
 <li class=new-in-release>{@link Asset}s on products</li>
 <li class=new-in-release>{@link io.sphere.sdk.carts.commands.updateactions.SetLineItemTotalPrice}</li>
 <li class=new-in-release>{@link io.sphere.sdk.carts.commands.updateactions.ChangeCustomLineItemQuantity} and {@link io.sphere.sdk.carts.commands.updateactions.ChangeCustomLineItemMoney}</li>
 <li class=change-in-release>The format of the User-Agent header changed to sth. like
 "{@code commercetools-jvm-sdk/1.5.0 (AHC/2.0) Java/1.8.0_101-b13 (Linux; amd64)}" from originally "{@code commercetools JVM SDK 1.4.0}". It is also possible to add a solution info, see {@link io.sphere.sdk.client.SolutionInfo}.</li>
 <li class=change-in-release>Using the default {@link SphereClient} will attempt to fetch a new token if an {@link io.sphere.sdk.client.InvalidTokenException} occurs.</li>
 <li class=change-in-release>{@link LocalizedString#slugified()} and {@link LocalizedString#slugifiedUnique()} generate a String with a max length of 256 to be valid for the commercetools platform. In addition, only allowed characters like {@code [-_a-zA-Z0-9]} will be in the output. Before that it was possible to keep for example a "+".</li>
 </ul>

 <h3 class=released-version id="v1_4_0">1.4.0 (29.09.2016)</h3>

 <ul>
 <li class=new-in-release>{@link State#getRoles()}, {@link io.sphere.sdk.states.commands.updateactions.AddRoles}, {@link io.sphere.sdk.states.commands.updateactions.RemoveRoles} and {@link io.sphere.sdk.states.commands.updateactions.SetRoles} </li>
 <li class=new-in-release>{@link Reference#ofResourceTypeIdAndId(String, String)} and others</li>
 <li class=new-in-release>key on products and product variants: {@link Product#getKey()}, {@link ProductProjection#getKey()}, {@link ProductVariant#getKey()}, {@link io.sphere.sdk.products.commands.ProductDeleteCommand#ofKey(String, Long)}, {@link io.sphere.sdk.products.commands.ProductUpdateCommand#ofKey(String, Long, List)}</li>
 <li class=new-in-release>{@link io.sphere.sdk.products.commands.updateactions.AddVariant#withImages(List)}, {@link io.sphere.sdk.products.commands.updateactions.AddVariant#withKey(String)}, {@link io.sphere.sdk.products.commands.updateactions.AddVariant#withSku(String)}, {@link io.sphere.sdk.products.commands.updateactions.SetKey}, {@link io.sphere.sdk.products.commands.updateactions.SetProductVariantKey}, {@link ProductByKeyGet}, {@link ProductProjectionByKeyGet}</li>
 <li class=new-in-release>{@link io.sphere.sdk.orders.errors.OutOfStockError}</li>
 <li class=new-in-release>{@link SetShippingMethod#ofId(String)} which is easier to use in a form than {@link SetShippingMethod#of(Referenceable)} </li>
 <li class=new-in-release>product update actions like {@link io.sphere.sdk.products.commands.updateactions.RemoveImage} now support to address a {@link ProductVariant} by using the SKU with {@link io.sphere.sdk.products.commands.updateactions.RemoveImage#ofSku(String, String)} </li>
 <li class=new-in-release>type update actions: {@link io.sphere.sdk.types.commands.updateactions.ChangeEnumValueOrder}, {@link io.sphere.sdk.types.commands.updateactions.ChangeFieldDefinitionOrder} and {@link io.sphere.sdk.types.commands.updateactions.ChangeLocalizedEnumValueOrder}</li>
 <li class=new-in-release>{@link io.sphere.sdk.customers.Customer#findAddressById(String)} </li>
 <li class=new-in-release>a simple generator for customer and order numbers by using {@link CustomObject}s as storage: {@link io.sphere.sdk.sequencegenerators.CustomObjectBigIntegerNumberGenerator}</li>
 <li class=new-in-release>{@link io.sphere.sdk.inventory.messages.InventoryEntryDeletedMessage}</li>
 <li class=new-in-release>custom fields for {@link io.sphere.sdk.inventory.InventoryEntry}s</li>
 <li class=new-in-release>{@link io.sphere.sdk.products.commands.updateactions.MoveImageToPosition}</li>
 <li class=new-in-release>{@link PaymentTransactionStateChangedMessage#getTransactionId()} </li>
 <li class=new-in-release>{@link CartShippingInfo#getDiscountedPrice()} and the {@link ShippingInfoExpansionModel#discountedPrice()} to receive the value of discounts for shipping costs and maybe expand in the {@link Cart} the {@link io.sphere.sdk.cartdiscounts.CartDiscount} objects.</li>
 <li class=fixed-in-release>{@link Category}s {@link Category#toString()} is now using reflection so former missing fields like "metaTitle" will be included in the output.</li>
 <li class=fixed-in-release>{@link io.sphere.sdk.jsonnodes.queries.JsonNodeQuery#of(String)} accepts now also query parameters</li>
 <li class=fixed-in-release>{@link ProductDraftBuilder#of(ProductDraft)} is not leaky anymore, in previous versions not all fields were copied</li>
 </ul>

 <h3 class=released-version id="v1_3_0">1.3.0 (22.07.2016)</h3>

 <ul>
 <li class=new-in-release>added {@link Channel#getAddress()} and {@link io.sphere.sdk.channels.commands.updateactions.SetAddress}</li>
 <li class=new-in-release>added field "locale" to {@link Order}, {@link Cart} and {@link io.sphere.sdk.customers.Customer}</li>
 <li class=new-in-release>added {@link Address#getFax()} </li>
 <li class=new-in-release>added {@link PaymentTransactionStateChangedMessage#getTransactionId()} </li>
 </ul>


 <h3 class=released-version id="v1_2_0">1.2.0 (18.07.2016)</h3>
 <p>Thanks to Cristian for his contributions!</p>
 <ul>
 <li class=new-in-release>{@link CategoryDraft} and {@link io.sphere.sdk.categories.CategoryDraftBuilder}
 contain the fields "metaTitle", "metaDescription" and "metaKeywords".</li>
 <li class=new-in-release>added {@link Project#getMessages()}</li>
 <li class=new-in-release>added new update actions for {@link Order}:
 {@link io.sphere.sdk.orders.commands.updateactions.SetCustomerEmail},
 {@link io.sphere.sdk.orders.commands.updateactions.SetShippingAddress} and
 {@link io.sphere.sdk.orders.commands.updateactions.SetBillingAddress}</li>
 <li class=new-in-release>added new messages for {@link Order}: {@link io.sphere.sdk.orders.messages.OrderCustomerEmailSetMessage},
 {@link io.sphere.sdk.orders.messages.OrderShippingAddressSetMessage} and
 {@link io.sphere.sdk.orders.messages.OrderBillingAddressSetMessage}</li>
 </ul>

 <h3 class=released-version id="v1_1_0">1.1.0 (11.07.2016)</h3>
 <p>Thanks to Sarah and Martin for their contributions!</p>

 <ul>
 <li class=new-in-release>support {@link io.sphere.sdk.carts.AnonymousCartSignInMode}</li>
 <li class=new-in-release>added {@link PriceSelection} for
 {@link ProductProjectionQuery#withPriceSelection(PriceSelection)},
 {@link ProductByIdGet#withPriceSelection(PriceSelection)},
 {@link ProductQuery#withPriceSelection(PriceSelection)},
 {@link ProductProjectionByIdGet#withPriceSelection(PriceSelection)},
 {@link io.sphere.sdk.products.commands.ProductUpdateCommand#withPriceSelection(PriceSelection)}  </li>
 <li class=new-in-release>added anonymousId to {@link Order#getAnonymousId()},
 {@link Cart#getAnonymousId()}, {@link CartDraft#getAnonymousId()},
 {@link io.sphere.sdk.customers.commands.CustomerSignInCommand#withAnonymousId(String)}  </li>
 <li class=new-in-release>added {@link ProductProjectionSearch#bySlug(Locale, String)},
 {@link ProductProjectionSearch#bySku(String)}, {@link ProductProjectionSearch#bySku(List)} </li>
 <li class=new-in-release>added {@link io.sphere.sdk.client.JavaAndJsonSphereRequest} which returns the Java object and the JSON from a {@link SphereRequest}</li>
 <li class=new-in-release>added {@link io.sphere.sdk.client.JavaAndHttpResponseSphereRequest} which returns the Java object and the HTTP response from a {@link SphereRequest} which can be useful to retrieve HTTP headers</li>
 <li class=new-in-release>added {@link io.sphere.sdk.carts.commands.updateactions.Recalculate#withUpdateProductData(Boolean)} </li>
 <li class=new-in-release>added {@link ProductProjectionSortSearchModel#id()} to sort search results by the product ID</li>
 <li class=new-in-release>added {@link io.sphere.sdk.productdiscounts.ProductDiscountDraftBuilder}</li>
 <li class=new-in-release>added {@link LocalizedString#get(String)} </li>
 <li class=change-in-release>{@link SphereJsonUtils#newObjectMapper()} does not use anymore "SerializationFeature.FAIL_ON_EMPTY_BEANS"</li>
 </ul>


 <h3 class=released-version id="v1_0_0">1.0.0 (03.06.2016)</h3>
 <ul>
 <li class=new-in-release>a new commercetools landing page for the JVM SDK is available at <a href="https://docs.commercetools.com/sdk/jvm-sdk">https://docs.commercetools.com/sdk/jvm-sdk</a></li>
 <li class=new-in-release>the GitHub repository has been relocated to <a href="https://github.com/commercetools/commercetools-jvm-sdk">https://github.com/commercetools/commercetools-jvm-sdk</a></li>
 <li class=new-in-release>{@link io.sphere.sdk.client.RetrySphereClientDecorator} to deal with server errors and retry requests</li>
 <li class=new-in-release>{@link ProductProjectionSearch#withFuzzyLevel(java.lang.Integer)} to configure the fuzzy level in the product search</li>
 <li class=change-in-release>a lot of classes without public available constructors have been converted to interfaces
 <ul>
 <li>{@link io.sphere.sdk.client.Tokens}</li>
 <li>{@link io.sphere.sdk.cartdiscounts.DiscountedLineItemPortion}</li>
 <li>{@link io.sphere.sdk.cartdiscounts.DiscountedLineItemPrice}</li>
 <li>{@link io.sphere.sdk.cartdiscounts.DiscountedLineItemPriceForQuantity}</li>
 <li>{@link io.sphere.sdk.carts.ItemState}</li>
 <li>{@link io.sphere.sdk.carts.PaymentInfo}</li>
 <li>{@link io.sphere.sdk.carts.TaxPortion}</li>
 <li>{@link io.sphere.sdk.discountcodes.DiscountCodeInfo}</li>
 <li>{@link io.sphere.sdk.orders.Parcel}</li>
 <li>{@link io.sphere.sdk.orders.ReturnInfo}</li>
 <li>{@link io.sphere.sdk.orders.ReturnItem}</li>
 <li>{@link io.sphere.sdk.orders.SyncInfo}</li>
 <li>{@link io.sphere.sdk.orders.TrackingData}</li>
 <li>{@link io.sphere.sdk.payments.PaymentMethodInfo}</li>
 <li>{@link io.sphere.sdk.payments.PaymentStatus}</li>
 <li>{@link Transaction}</li>
 <li>{@link io.sphere.sdk.productdiscounts.DiscountedPrice}</li>
 <li>{@link Project}</li>
 <li>{@link io.sphere.sdk.shippingmethods.ZoneRate}</li>
 <li>{@link io.sphere.sdk.types.FieldDefinition}</li>
 <li>{@link io.sphere.sdk.zones.Location}</li>
 <li>{@link EnumValue}</li>
 <li>{@link LocalizedEnumValue}</li>
 <li>{@link PagedQueryResult}</li>
 <li>{@link PagedResult}</li>
 <li>{@link io.sphere.sdk.search.PagedSearchResult}</li>
 <li>{@link io.sphere.sdk.search.SearchKeyword}</li>
 <li>{@link SearchKeywords}</li>
 <li>{@link io.sphere.sdk.search.TermStats}</li>
 </ul>

 </li>
 <li class=removed-in-release>A lot of deprecated methods and classes has been removed, the previous release contains information what to use instead.</li>
 <li class=removed-in-release>{@code io.sphere.sdk.customers.commands.CustomerCreateTokenCommand}, use {@link io.sphere.sdk.customers.commands.CustomerCreatePasswordTokenCommand} instead</li>
 <li class=removed-in-release>{@code io.sphere.sdk.client.SphereAccessTokenSupplierFactory}, use static methods of {@link io.sphere.sdk.client.SphereAccessTokenSupplier} </li>
 <li class=removed-in-release>test double methods in {@link io.sphere.sdk.client.SphereClientFactory}</li>
 <li class=removed-in-release>the static method {@code typeId} in resources</li>
 <li class=removed-in-release>{@code CartDraftDsl.witCustom}</li>
 <li class=removed-in-release>{@code LineItem#getDiscountedPrice()} and {@code CustomLineItem#getDiscountedPrice()}</li>
 <li class=removed-in-release>{@code getVersion()} in {@link CustomerCreateEmailTokenCommand}</li>
 <li class=removed-in-release>{@code CustomerByTokenGet}</li>
 <li class=removed-in-release>{@code CustomObjectDeleteCommand#of(CustomObject<?>)}</li>
 <li class=removed-in-release>{@code CustomObjectDeleteCommand#of(String, String)}</li>
 <li class=removed-in-release>{@code CustomObjectByKeyGet#of(String, String)}</li>
 <li class=removed-in-release>{@code CustomObjectQuery#of()}</li>
 <li class=removed-in-release>{@code ProductProjection#findVariantBySky}</li>
 <li class=removed-in-release>{@code AttributeDefinition#getIsRequired}</li>
 <li class=removed-in-release>{@code AttributeDefinition#getIsSearchable}</li>
 <li class=removed-in-release>{@code ProductAttributeFacetSearchModel#ofLocalizableEnum(String)}</li>
 <li class=removed-in-release>{@code ProductAttributeFacetSearchModel#ofLocalizableEnumSet(String)}</li>
 <li class=removed-in-release>{@code ProductAttributeFacetedSearchSearchModel#ofLocalizableEnum(String)}</li>
 <li class=removed-in-release>{@code ProductAttributeFacetedSearchSearchModel#ofLocalizableEnumSet(String)}</li>
 <li class=removed-in-release>{@code ProductAttributeFilterSearchModel#ofLocalizableEnum(String)}</li>
 <li class=removed-in-release>{@code ProductAttributeFilterSearchModel#ofLocalizableEnumSet(String)}</li>
 <li class=removed-in-release>{@code ProductAttributeSortSearchModel#ofLocalizableEnum(String)}</li>
 <li class=removed-in-release>{@code ProductAttributeSortSearchModel#ofLocalizableEnumSet(String)}</li>
 <li class=removed-in-release>{@code ProductTypeDraft#of(String, String, List)}</li>
 <li class=removed-in-release>{@code TaxRate#equalsIgnoreId(TaxRate)}</li>
 <li class=removed-in-release>{@code SetCustomTypeBase#getTypeId()}</li>
 <li class=removed-in-release>{@code SetCustomTypeBase#getTypeKey()}</li>
 <li class=removed-in-release>{@code LocalizedString#ofEnglishLocale(String)}</li>
 <li class=removed-in-release>{@link io.sphere.sdk.search.FilterDsl} <abbr title="copy methods starting with 'with'">withers</abbr> taking {@link io.sphere.sdk.search.FilterExpression} as argument</li>
 <li class=removed-in-release>methods {@code getTermFacetResult(final TermFacetExpression<T> facetExpression)}, {@code getRangeFacetResult(final RangeFacetExpression<T> facetExpression)}, and {@code getFilteredFacetResult(final FilteredFacetExpression<T> facetExpression)} in {@link io.sphere.sdk.search.PagedSearchResult}</li>
 <li class=removed-in-release>in search models the methods starting with {@code by}</li>
 <li class=removed-in-release>internal utility classes {@code IterableUtils}, {@code MapUtils}, {@code SetUtils}, {@code StringUtils}, {@code PatternMatcher} and {@code UrlUtils}</li>
 <li class=removed-in-release>{@code FormUrlEncodedHttpRequestBody#getData()}</li>
 </ul>


 <h3 class=released-version id="v1_0_0_RC8">1.0.0-RC8 (13.05.2016)</h3>

 <ul>
 <li class=new-in-release>added {@link ProductProjectionFilterSearchModel#slug()} </li>
 <li class=new-in-release>added {@link io.sphere.sdk.customers.queries.CustomerQueryModel#lowercaseEmail()}</li>
 <li class=new-in-release>added {@link io.sphere.sdk.utils.CompletableFutureUtils#listOfFuturesToFutureOfList(List)},
 {@link io.sphere.sdk.utils.CompletableFutureUtils#exceptionallyCompletedFuture(Throwable)} and
 {@link io.sphere.sdk.utils.CompletableFutureUtils#recoverWith(CompletionStage, Function, Executor)} </li>
 <li class=new-in-release>added {@link LineItem#getTaxedPrice()}, {@link CustomLineItem#getTaxedPrice()} and {@link io.sphere.sdk.carts.CartShippingInfo#getTaxedPrice()} </li>
 <li class=new-in-release>added {@link io.sphere.sdk.products.search.ProductCategoriesIdTermFilterSearchModel}
 and {@link io.sphere.sdk.products.search.ProductCategoriesIdTermFacetSearchModel} to filter and facet for products in category trees</li>
 <li class=new-in-release>add {@link io.sphere.sdk.carts.TaxMode} to enable external tax calculation</li>
 <li class=change-in-release>{@link TaxRate} has been split into {@link TaxRate} (response object) and {@link io.sphere.sdk.taxcategories.TaxRateDraft} (input object)</li>
 <li class=fixed-in-release>{@link io.sphere.sdk.utils.CompletableFutureUtils#recoverWith(CompletionStage, Function)} could hang under some circumstances</li>
 </ul>


 <h3 class=released-version id="v1_0_0_RC7">1.0.0-RC7 (06.05.2016)</h3>

 <ul>
 <li class=removed-in-release>{@code QuerySortingModel#sort(io.sphere.sdk.queries.QuerySortDirection)}, use {@link QuerySortingModel#sort()} and then {@link DirectionlessQuerySort#asc()}/{@link DirectionlessQuerySort#desc()} instead</li>
 <li class=new-in-release>improved {@link io.sphere.sdk.types.queries.FieldsQueryModel} to support the more field types</li>
 <li class=new-in-release>added custom fields for {@link io.sphere.sdk.channels.Channel}</li>
 <li class=new-in-release>added missing fields for {@link io.sphere.sdk.channels.queries.ChannelQueryModel} so channels can be queried by roles</li>
 <li class=new-in-release>{@link PagedResult#getCount()} </li>
 <li class=change-in-release>deprecated {@code PagedResult#size()} in favor of {@link PagedResult#getCount()}</li>
 <li class=fixed-in-release>made factory methods of {@link ProductProjectionQueryBuilder} public (was not possible to instantiate)</li>
 </ul>


 <h3 class=released-version id="v1_0_0_RC6">1.0.0-RC6 (22.04.2016)</h3>

 <ul>
 <li class=new-in-release>expand all variants in products with {@link ProductDataExpansionModel#allVariants()}</li>
 <li class=new-in-release>add facets for product availability, {@link io.sphere.sdk.products.search.ProductVariantAvailabilityFacetSearchModel}</li>
 <li class=new-in-release>fuzzy suggest query {@link SuggestQuery#withFuzzy(boolean)} </li>
 <li class=new-in-release>{@link io.sphere.sdk.products.commands.updateactions.ChangeMasterVariant}</li>
 <li class=new-in-release>{@link ProductDraftBuilder#publish(Boolean)} to create published products</li>
 </ul>

 <h3 class=released-version id="v1_0_0_RC5">1.0.0-RC5 (15.04.2016)</h3>
 <ul>
 <li class=new-in-release>Add getter in Facet, Filter and FacetedSearch models to access the internal {@link io.sphere.sdk.search.model.SearchModel}.</li>
 <li class=new-in-release>{@link io.sphere.sdk.client.TokensFacade#fetchCustomerPasswordFlowTokens(SphereAuthConfig, String, String)} to get the access token for a customer for example to give the token to a mobile app</li>
 <li class=new-in-release>{@link SphereAuthConfig#getScopes()} to configure the scope of the access token</li>
 <li class=new-in-release>product search filters for product availability, see {@link io.sphere.sdk.products.search.ProductVariantAvailabilityFilterSearchModel#isOnStock()} and  {@link io.sphere.sdk.products.search.ProductVariantAvailabilityFilterSearchModel#availableQuantity()}</li>
 <li class=new-in-release>product search sort expressions for product availability, see {@link ProductVariantAvailabilitySortSearchModel#restockableInDays()} and {@link ProductVariantAvailabilitySortSearchModel#availableQuantity()}</li>
 <li class=change-in-release>The class {@link io.sphere.sdk.products.commands.updateactions.SetSku} executes the action {@code setSku} which is staged,
 previous to this version it was {@code setSKU} (upper case 'K' and 'U')
 which updates in staged and current and has been moved to the deprecated class {@code LegacySetSku}.
 See also <a href="https://docs.commercetools.com/http-api-projects-products.html#set-sku" target="_blank">the HTTP API doc of SetSku.</a></li>
 <li class=change-in-release>{@link io.sphere.sdk.customers.commands.CustomerCreateEmailTokenCommand}, {@link io.sphere.sdk.customers.commands.CustomerVerifyEmailCommand} and {@link io.sphere.sdk.customers.commands.CustomerPasswordResetCommand} do not use anymore optimistic concurrency control,
 so it won't fail with {@link io.sphere.sdk.client.ConcurrentModificationException} and it is not necessary to load the customer version.
 <br>Deprecated methods:
 <ul>
 <li>{@code CustomerCreateEmailTokenCommand#getVersion()}</li>
 <li>{@link io.sphere.sdk.customers.commands.CustomerPasswordResetCommand#of(io.sphere.sdk.models.Versioned, java.lang.String, java.lang.String)}</li>
 <li>{@link CustomerPasswordResetCommand#of(io.sphere.sdk.models.Versioned, io.sphere.sdk.customers.CustomerToken, java.lang.String)}</li>
 <li>{@link CustomerPasswordResetCommand#getId()}</li>
 <li>{@link CustomerPasswordResetCommand#getVersion()}</li>
 <li>{@link CustomerVerifyEmailCommand#of(io.sphere.sdk.models.Versioned, java.lang.String)}</li>
 <li>{@link CustomerVerifyEmailCommand#of(io.sphere.sdk.models.Versioned, io.sphere.sdk.customers.CustomerToken)}</li>
 <li>{@link CustomerVerifyEmailCommand#getId()}</li>
 <li>{@link CustomerVerifyEmailCommand#getVersion()}</li>
 </ul>
 <br>We don't intend to delete these methods soon (later after 1.0.0).</li>
 <li class=fixed-in-release>{@link io.sphere.sdk.producttypes.queries.ProductTypeQueryBuilder} was a builder for {@link io.sphere.sdk.categories.queries.CategoryQuery}, it builds now {@link io.sphere.sdk.producttypes.queries.ProductTypeQuery}.</li>
 </ul>

 <h3 class=released-version id="v1_0_0_RC4">1.0.0-RC4 (18.03.2016)</h3>

 <ul>
 <li class=new-in-release>{@link io.sphere.sdk.products.commands.updateactions.SetPrices} to update all prices in a {@link ProductVariant}</li>
 <li class=new-in-release>{@link MissingFilterSearchModelSupport#missing() missing filters for product search} </li>
 <li class=new-in-release>{@link ExistsFilterSearchModelSupport#exists()  exists filters for product search} </li>
 <li class=new-in-release>{@link ProductVariantFilterSearchModel#sku()}</li>
 <li class=new-in-release>{@link ProductProjection#getState()} </li>
 <li class=new-in-release>force parameter to products, reviews and orders transition state action ({@link io.sphere.sdk.products.commands.updateactions.TransitionState}, {@link io.sphere.sdk.orders.commands.updateactions.TransitionState}, {@link io.sphere.sdk.orders.commands.updateactions.TransitionState})</li>
 <li class=new-in-release>query {@link io.sphere.sdk.carts.Cart}s by {@link CartQueryModel#cartState()} </li>
 <li class=new-in-release>{@link io.sphere.sdk.models.errors.DuplicateFieldError}</li>
 <li class=new-in-release>{@link io.sphere.sdk.producttypes.commands.updateactions.SetKey} to update the key of a {@link ProductType}</li>
 <li class=new-in-release>{@link io.sphere.sdk.types.commands.updateactions.ChangeKey} to update the key of a {@link io.sphere.sdk.types.Type}</li>
 <li class=new-in-release>{@link AttributeDefinition#getInputHint()} and {@link io.sphere.sdk.products.attributes.AttributeDefinitionBuilder#inputTip(LocalizedString)} </li>
 <li class=new-in-release>{@link io.sphere.sdk.products.messages.ProductSlugChangedMessage}</li>
 <li class=fixed-in-release>The documentation of {@link SphereResources} was empty after the migration from SBT to Maven.</li>
 </ul>

 <h3 class=released-version id="v1_0_0_RC3">1.0.0-RC3 (11.03.2016)</h3>

 <ul>
 <li class=new-in-release>filter by scoped prices, see {@link ProductVariantFilterSearchModel#scopedPrice()} </li>
 <li class=new-in-release>{@link io.sphere.sdk.producttypes.commands.updateactions.ChangeIsSearchable}</li>
 <li class=new-in-release>improved Exception classes documentation</li>
 <li class=new-in-release>
 {@link CategoryTree#findSiblings(Collection)},
 {@link CategoryTree#getSubtree(Collection)},
 {@link CategoryTree#getSubtreeRoots()} and
 {@link CategoryTree#getRootAncestor(Identifiable)}</li>
 <li class=new-in-release>set default timeout for HTTP client in {@code commercetools-java-client} to 121 seconds so {@link io.sphere.sdk.client.GatewayTimeoutException} can occur</li>
 <li class=new-in-release>{@link io.sphere.sdk.payments.commands.updateactions.ChangeAmountPlanned}</li>
 <li class=new-in-release>{@link io.sphere.sdk.types.commands.TypeDeleteCommand#ofKey(String, Long)} and {@link io.sphere.sdk.types.commands.TypeUpdateCommand#ofKey(String, Long, List)} </li>
 <li class=new-in-release>{@link io.sphere.sdk.orders.OrderState#CONFIRMED} and {@link io.sphere.sdk.orders.ShipmentState#DELAYED}</li>
 <li class=new-in-release>improve timeout stacktraces of {@link io.sphere.sdk.client.BlockingSphereClient} by including the corresponding {@link SphereRequest} into the message</li>
 <li class=change-in-release>should be source code compatible but still a binary incompatibility: {@link Price} is now an interface instead of a final class</li>
 </ul>

 <h3 class=released-version id="v1_0_0_RC2">1.0.0-RC2 (02.03.2016)</h3>

 <ul>
 <li class=new-in-release>create DraftBuilders from Draft objects:
 <ul>
 <li>{@link io.sphere.sdk.cartdiscounts.CartDiscountDraftBuilder#of(CartDiscountDraft)}</li>
 <li>{@link io.sphere.sdk.carts.CartDraftBuilder#of(CartDraft)}</li>
 <li>{@link io.sphere.sdk.categories.CategoryDraftBuilder#of(CategoryDraft)}</li>
 <li>{@link io.sphere.sdk.payments.PaymentDraftBuilder#of(PaymentDraft)}</li>
 <li>{@link io.sphere.sdk.payments.TransactionDraftBuilder#of(TransactionDraft)}</li>
 <li>{@link ProductDraftBuilder#of(ProductDraft)}</li>
 <li>{@link ProductVariantDraftBuilder#of(ProductVariantDraft)}</li>
 <li>{@link io.sphere.sdk.reviews.ReviewDraftBuilder#of(ReviewDraft)}</li>
 <li>{@link CustomFieldsDraftBuilder}</li>
 <li>{@link io.sphere.sdk.types.TypeDraftBuilder#of(TypeDraft)}</li>
 </ul>
 </li>
 <li class=new-in-release>{@link CustomObject}s can be queried by value, see {@link CustomObjectQueryModel#value()}</li>
 <li class=new-in-release>{@link io.sphere.sdk.client.metrics.ObservedTotalDuration#getErrorResult()},
 {@link io.sphere.sdk.client.metrics.ObservedTotalDuration#getSuccessResult()},
 {@link io.sphere.sdk.client.metrics.ObservedDeserializationDuration#getHttpResponse()}
 and {@link io.sphere.sdk.client.metrics.ObservedDeserializationDuration#getResult()}
 for better analysis of metrics with the
 {@link io.sphere.sdk.client.metrics.SimpleMetricsSphereClient}</li>


 <li class=new-in-release>add alias
 {@link io.sphere.sdk.products.attributes.DefaultProductAttributeFormatter#format(Attribute, Referenceable)}
 for
 {@link io.sphere.sdk.products.attributes.DefaultProductAttributeFormatter#convert(Attribute, Referenceable)}</li>

 <li class=new-in-release>{@link PriceDraftBuilder#countryCode(String)}, {@link PriceDraftBuilder#customerGroupId(String)}</li>
 <li class=new-in-release>{@link io.sphere.sdk.producttypes.ProductTypeLocalRepository#findByKey(String)} and {@link ProductTypeLocalRepository#getAll()} </li>
 <li class=new-in-release>{@link io.sphere.sdk.client.SphereClientUtils} supports methods to collect async results, for example {@link io.sphere.sdk.client.SphereClientUtils#blockingWaitForEach(List, Duration)} </li>
 <li class=new-in-release>{@link LocalizedStringEntry#of(String, String)} </li>
 <li class=new-in-release>support for price selection, see {@link ProductVariant#getPrice()} and
 {@link io.sphere.sdk.products.search.ProductProjectionSearch#withPriceSelection(PriceSelection)}</li>
 <li class=new-in-release>{@link io.sphere.sdk.client.ConcurrentModificationException#getCurrentVersion()}
 and {@link io.sphere.sdk.models.errors.ConcurrentModificationError}
 to get the current version number when a version conflict occurs</li>
 <li class=change-in-release>rename {@code LocalizedToStringProductAttributeConverter}
 to {@link io.sphere.sdk.products.attributes.DefaultProductAttributeFormatter}</li>
 <li class=change-in-release>make constructors of
 {@link io.sphere.sdk.products.attributes.DefaultProductAttributeFormatter} public</li>
 <li class=change-in-release>{@link io.sphere.sdk.utils.MoneyImpl}s toString method has been altered</li>
 <li class=change-in-release>it should not affect production code, we use now Maven as build tool instead of sbt</li>
 <li class=change-in-release>all http client implementations use the logger io.sphere.sdk.http.HttpClient for logging requests/responses</li>
 <li class=fixed-in-release>the {@link Duration} using methods did not use the correct timeout,
 for example {@link io.sphere.sdk.client.SphereClientUtils#blockingWait(CompletionStage, Duration)}
 and {@link io.sphere.sdk.client.BlockingSphereClient#of(SphereClient, Duration)} were affected</li>
 </ul>


 <h3 class=released-version id="v1_0_0_RC1">1.0.0-RC1 (12.02.2016)</h3>
 <a class="theme-btn expand-all">Expand all</a>

 <br>
 <br>
 <ul>
 <li class=new-in-release>use a new groupId as well as artifactId for the JVM SDK, have a look at the <a href="https://github.com/commercetools/commercetools-jvm-sdk#commercetools-jvm-sdk">GitHub readme</a></li>
 <li class=new-in-release>in commercetools-convenience module: {@link io.sphere.sdk.client.metrics.SimpleMetricsSphereClient} makes durations for requests observable</li>
 <li class=new-in-release>add {@link io.sphere.sdk.jsonnodes.queries.JsonNodeQuery} to use the query endpoints and get directly {@link com.fasterxml.jackson.databind.JsonNode} instances</li>
 <li class=new-in-release>add draft getters to create commands</li>
 <li class=new-in-release>a lot of new documentation in the classes</li>
 <li class=new-in-release>cross reference expansion for a lot of endpoints, so for example expand of a review its current state and of this state the possible transition states</li>
 <li class=new-in-release>add {@link java.time.Duration} API for {@link io.sphere.sdk.client.SphereClientUtils#blockingWait(CompletionStage, Duration)} and {@link io.sphere.sdk.client.BlockingSphereClient#of(SphereClient, Duration)}  </li>

 <li class=new-in-release>add {@link LocalizedString#ofStringToStringMap(Map)} </li>
 <li class=new-in-release>add {@link io.sphere.sdk.client.TokensFacade} to conveniently just get an access token
 <li class=new-in-release>add {@link io.sphere.sdk.messages.queries.MessageQuery#forMessageTypes(List)}</li>
 <li class=new-in-release>use {@code io.sphere.sdk.http.HttpClient} as logger name with debug level to log the http method and the path (currently works only for the default HTTP client)</li>

 <li class=change-in-release>deprecate {@code LocalizedString#ofEnglishLocale(String)}, use  {@link LocalizedString#ofEnglish(String)} instead</li>
 <li class=change-in-release>{@code VariantIdentifier} became {@link ByIdVariantIdentifier} and use {@link VariantIdentifier} as super interface for {@link ByIdVariantIdentifier} and {@link BySkuVariantIdentifier}</li>
 <li class=change-in-release>drafts are interfaces</li>
 <li class=change-in-release>{@link io.sphere.sdk.products.search.ProductProjectionSearch} uses POST instead of GET</li>
 <li class=change-in-release>set the default HTTP client to Async Http Client to 2.0.0-RC9</li>
 <li class=change-in-release>deprecate {@code io.sphere.sdk.client.SphereAccessTokenSupplierFactory}, use {@link io.sphere.sdk.client.SphereAccessTokenSupplier} methods instead</li>

 <li class=removed-in-release>class {@code SphereRequestBase}, use {@link io.sphere.sdk.client.SphereRequestUtils} instead</li>
 <li class=removed-in-release>{@code ExperimentalReactiveStreamUtils} is not experimental anymore and has been moved out of commercetools-convenience to a separate repository: <a href="https://github.com/commercetools/commercetools-jvm-sdk-reactive-streams-add-ons" target="_blank">https://github.com/commercetools/commercetools-jvm-sdk-reactive-streams-add-ons</a></li>
 <li class=removed-in-release>some internal util classes (StringUtils, IterableUtils, MapUtils, SetUtils, TriFunction, PatternMatcher, UrlUtils)</li>
 <li class=removed-in-release>{@code CustomObjectDraft#withVersion(String, String, JsonNode)}</li>
 <li class=removed-in-release>{@code HttpQueryParameter} class</li>
 <li class=removed-in-release>{@code InvalidQueryOffsetException}</li>

 <li class=change-in-release>align {@link io.sphere.sdk.products.search.ProductProjectionSearchModel} methods with the Query DSL, e.g. {@code is} instead of {@code by}, {@code isIn} instead of {@code byAny} and {@code isBetween} instead of {@code byRange}</li>
 <li class=change-in-release>unify methods to extract {@code TermFacetResult}, {@code RangeFacetResult} and {@code FilteredFacetResult} into the same method name {@code getFacetResult}</li>
 <li class=change-in-release>{@code FacetAndFilter} Expression and its Search Model have been renamed to {@code FacetedSearch} to better reflect the use case</li>
 </ul>




 <h3 class=released-version id="v1_0_0_M26">1.0.0-M26 (20.01.2016)</h3>
 <a class="theme-btn expand-all">Expand all</a>

 <br>
 <br>
 <ul>
 <li class=new-in-release>Added {@link io.sphere.sdk.reviews.Review}s.</li>

 <li class=new-in-release>Builders for query and search requests. So for example
 {@link io.sphere.sdk.products.queries.ProductProjectionQueryBuilder}
 to build {@link io.sphere.sdk.products.queries.ProductProjectionQuery} and
 {@link io.sphere.sdk.products.search.ProductProjectionSearchBuilder}
 to build {@link io.sphere.sdk.products.search.ProductProjectionSearch}.


 <div class="rn-hidden">
 <p>The new way with builders (code with side effects)</p>
 {@include.example io.sphere.sdk.meta.M26Demo#createSearchQueryWithBuilder}
 <p>The classic way (immutable objects, reassigning)</p>
 {@include.example io.sphere.sdk.meta.M26Demo#createSearchQueryClassicWay()}
 </div>

 </li>

 <li class=new-in-release>Added customer update actions
 {@link io.sphere.sdk.customers.commands.updateactions.SetTitle},
 {@link io.sphere.sdk.customers.commands.updateactions.SetFirstName},
 {@link io.sphere.sdk.customers.commands.updateactions.SetMiddleName} and
 {@link io.sphere.sdk.customers.commands.updateactions.SetLastName}.</li>
 <li class=new-in-release>{@link CustomObjectQuery} supports now predicates containing key.
 <div class="rn-hidden">
 {@include.example io.sphere.sdk.meta.M26Demo#queryCustomObjectsByKeyExample()}
 </div>
 </li>
 <li class=new-in-release>Added {@link Project#getCurrencies()} and {@link Project#getCurrencyUnits()} so you can get your enabled currencies for the commercetools project.</li>

 <li class=new-in-release>Added some short hand methods for the work with product attributes like {@link Attribute#getValueAsLong()} and {@link Attribute#getValueAsString()}. For all see {@link Attribute}.</li>
 <li class=new-in-release>Added {@code LocalizedToStringProductAttributeConverter} which provides some defaults to present product attribute (including monetary amounts and date) values as String. The behaviour can be changed through subclasses.</li>
 </ul>


 <h3 class=released-version id="v1_0_0_M25">1.0.0-M25 (08.01.2016)</h3>
 <ul>
 <li class=fixed-in-release>Support full locales for search endpoint. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/922" target="_blank">#922</a></li>
 </ul>

 <h3 class=released-version id="v1_0_0_M24">1.0.0-M24 (05.01.2016)</h3>

 <ul>
 <li class=new-in-release>Commercetools responses are gzipped which should result in less traffic and faster responses. {@link HttpResponse#getResponseBody()} returns the unpacked body.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.client.BlockingSphereClient} which does not execute asynchronous: {@include.example io.sphere.sdk.meta.BlockingClientValueGetDemo}</li>
 <li class=new-in-release>Added {@link ProductProjectionExpansionModel#allVariants()} for expanding objects in masterVariant and the other variants with one code expression. {@include.example io.sphere.sdk.products.expansion.ProductProjectionExpansionModelTest#allVariants()}</li>
 <li class=new-in-release>{@link io.sphere.sdk.meta.GraphQLDocumentation GraphQL Example}</li>
 <li class=new-in-release>InputHint for types is optional, so added {@link io.sphere.sdk.types.FieldDefinition#of(FieldType, String, LocalizedString, Boolean)}.</li>
 <li class=new-in-release>Ease initialising clients which need a bearer token instead of the client secret:
 {@link io.sphere.sdk.client.SphereClientFactory#createClientOfApiConfigAndAccessToken(SphereApiConfig, String)} and
 {@link io.sphere.sdk.client.SphereClientFactory#createClientOfApiConfigAndAccessToken(SphereApiConfig, String, HttpClient)}
 </li>
 <li class=new-in-release>{@link ProductDraftBuilder#plusVariants(List)} and {@link ProductDraftBuilder#plusVariants(ProductVariantDraft)}</li>
 <li class=new-in-release>Improve error reporting for failed attempts fetching OAuth tokens.</li>
 <li class=new-in-release>Added missing transaction query fields.</li>
 <li class=new-in-release>{@link io.sphere.sdk.client.SphereClientUtils#blockingWait(CompletionStage, long, TimeUnit)} for waiting for sphere requests</li>
 <li class=new-in-release>{@link CartLike#getCurrency()} which gets the currency for a cart or an order</li>
 <li class=new-in-release>Added category order hints to search model.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.types.ResourceTypeIdsSetBuilder} which helps to find the resource type ids for creation {@link io.sphere.sdk.types.Type}s.</li>
 <li class=new-in-release>{@link ResourceIdentifier}, for example for creating products with the product type key: {@link ProductDraftBuilder#of(ResourceIdentifiable, LocalizedString, LocalizedString, ProductVariantDraft)} </li>
 <li class=new-in-release>Added {@link ProductVariant#isMatchingVariant()}.</li>
 <li class=new-in-release>Several documentation improvements.</li>
 <li class=change-in-release>Renamed methods like "ofLocalizable*" to "ofLocalized*". (the old methods are deprecated)</li>
 <li class=change-in-release>Renamed duplicated classes like "*Type" to "*AttributeType" and "*FieldType". (direct change, no deprecation possible)</li>
 <li class=change-in-release>Use {@link io.sphere.sdk.client.TestDoubleSphereClientFactory#createHttpTestDouble(Function)}
 and {@link io.sphere.sdk.client.TestDoubleSphereClientFactory#createObjectTestDouble(Function)} instead of methods from {@link io.sphere.sdk.client.SphereClientFactory} for test doubles.</li>
 <li class=fixed-in-release>In the properties config the default auth url is wrong. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/889">889</a>.</li>
 <li class=removed-in-release>The review classes are removed.</li>
 </ul>

 <h3 class=released-version id="v1_0_0_M23">1.0.0-M23 (09.12.2015)</h3>

 <ul>
 <li class=new-in-release>Create a cart including shipping and billing address as well as with items. See {@link io.sphere.sdk.carts.CartDraftBuilder}</li>
 <li class=new-in-release>Improved Order Query, query by (custom) line items, billing and shipping address, discount codes and custom fields.</li>
 <li class=new-in-release>Query {@link io.sphere.sdk.types.Type}s by resourceTypeIds and fieldDefinitions.</li>
 <li class=new-in-release>Query {@link Order} by empty or not empty syncInfo.</li>
 <li class=new-in-release>Added {@link QueryDsl#plusSort(List)}.</li>
 <li class=new-in-release>Added query predicates for createdAt and lastModifiedAt.</li>
 <li class=new-in-release>Added isIn() query for enum constants like {@link io.sphere.sdk.states.StateType}.</li>
 <li class=new-in-release>Added {@link ProductProjectionQuery#bySku(List)}.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.taxcategories.queries.TaxCategoryByIdGet}.</li>
 <li class=new-in-release>{@link io.sphere.sdk.client.SphereClientFactory} also auto discovers the Apache HTTP client SDK module if this is in the classpath and not the Async HTTP Client module.</li>
 <li class=new-in-release>Added {@link CustomFields#getFieldsJsonMap()}.</li>
 <li class=change-in-release>Primitive scalar values are now as wrapper classes defined (int to Long, long to Long)</li>
 <li class=change-in-release>{@link io.sphere.sdk.cartdiscounts.DiscountedLineItemPriceForQuantity} and {@link io.sphere.sdk.carts.commands.updateactions.RemoveLineItem} uses long for quantity. </li>
 <li class=change-in-release>{@link Versioned#getVersion()} is of type {@link Long} instead of long.</li>
 <li class=change-in-release>Http core: {@link io.sphere.sdk.http.FormUrlEncodedHttpRequestBody} did not respect the order of elements and possible duplicates.
 Use  {@link io.sphere.sdk.http.FormUrlEncodedHttpRequestBody#of(List)} for construction and {@link FormUrlEncodedHttpRequestBody#getParameters()} for getting the data.
 {@code FormUrlEncodedHttpRequestBody#getData()} is deprecated.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.client.SphereClientConfig#ofProperties(Properties, String)} to get the commercetools credentials form a properties file.</li>
 <li class=new-in-release>For SDK contributors: <a href="https://github.com/commercetools/commercetools-jvm-sdk#executing-integration-tests" target="_blank">integration test credentials can be set via a properties file</a></li>
 </ul>

 <h3 class=released-version id="v1_0_0_M22">1.0.0-M22 (01.12.2015)</h3>
 <ul>
 <li class=change-in-release>{@code AttributeDefinition#getIsRequired()} and {@code AttributeDefinition#getIsSearchable()} ()} have been
 deprecated in favor of {@link AttributeDefinition#isRequired()} and {@link AttributeDefinition#isSearchable}.
 </li>
 <li class=new-in-release>Added new Payment actions:
 {@link io.sphere.sdk.payments.commands.updateactions.ChangeTransactionTimestamp},
 {@link io.sphere.sdk.payments.commands.updateactions.ChangeTransactionInteractionId} and
 {@link io.sphere.sdk.payments.commands.updateactions.ChangeTransactionState}
 </li>
 <li class=new-in-release>Added {@link Transaction#getId()} and {@link Transaction#getState()}</li>
 <li class=change-in-release>{@link Transaction} has been split to {@link io.sphere.sdk.payments.TransactionDraft} as input object
 and {@link Transaction} as output object of the API.</li>
 <li class=change-in-release>Fixed method typo {@code ProductProjection#findVariantBySky(String)}, use {@link ProductProjection#findVariantBySku(String)}</li>
 </ul>

 <h3 class=released-version id="v1_0_0_M21">1.0.0-M21 (25.11.2015)</h3>
 <ul>
 <li class=new-in-release>Introduced the possibility to perform faceted search on products, for more information see the {@link io.sphere.sdk.meta.SearchDocumentation search documentation}, section Faceted Search.</li>
 <li class=new-in-release>Added getters {@link io.sphere.sdk.search.SearchExpression#value()} and {@link io.sphere.sdk.search.FacetExpression#alias()} for the Search API.</li>
 <li class=new-in-release>Added helper method {@link io.sphere.sdk.search.model.RangeTermFacetSearchModel#allRanges()} to request a range facet for all values.</li>
 <li class=fixed-in-release>Validates the provided parameters in filter and facet expressions in order to avoid generating wrong requests.</li>
 <li class=change-in-release>Deprecated {@code io.sphere.sdk.search.model.RangeFacetBaseSearchModel#onlyRangeAsString(java.lang.String, java.lang.String)} in favour of a more generic {@code io.sphere.sdk.search.model.RangeFacetBaseSearchModel#onlyRangeAsString(FacetRange)}.</li>
 <li class=new-in-release>Added static constructors to all facet/filter/sort search model classes, to easily build customized search expressions.</li>
 <li class=change-in-release>Since {@link io.sphere.sdk.search.model.SortSearchModel} implementation classes have now static factory constructors, these class constructors have been removed from the public API.</li>
 <li class=change-in-release>Renamed {@code Range} filter/facet search models to {@code RangeTerm} to emphasize that you can build both range and term oriented search expressions with them.</li>
 <li class=new-in-release>ExpansionPath for shippingInfo fields: {@link CartExpansionModel#shippingInfo()} and {@link OrderExpansionModel#shippingInfo()}</li>
 <li class=new-in-release>{@link ProductType#getKey()}</li>
 <li class=change-in-release>{@code ProductTypeDraft#of(String, String, List)} has been deprecated in favor of {@link ProductTypeDraft#of(String, String, String, List)} since it is a very good practice to create a {@link ProductType} with a key.</li>
 <li class=new-in-release>{@link SetShippingMethod#ofRemove()} to be able to remove a {@link io.sphere.sdk.shippingmethods.ShippingMethod} from a {@link io.sphere.sdk.carts.Cart}</li>
 <li class=new-in-release>{@link io.sphere.sdk.orders.commands.OrderFromCartCreateCommand} contains an example</li>
 <li class=new-in-release>Support for currencies like Yen.</li>
 <li class=new-in-release>create {@link javax.money.MonetaryAmount} with centAmount: {@link io.sphere.sdk.utils.MoneyImpl#ofCents(long, CurrencyUnit)}</li>
 <li class=new-in-release>Add {@code state} field to QueryModel for Products, Reviews and Orders.</li>
 <li class=new-in-release>Custom fields for prices: {@link Price#getCustom()}</li>
 <li class=change-in-release>For custom fields in prices the HTTP API and hence the SDK differentiate now between {@link Price} and {@link io.sphere.sdk.products.PriceDraft}.
 <p>Places where {@link io.sphere.sdk.products.PriceDraft} has to be used:</p>
 <ul>
 <li>{@link io.sphere.sdk.products.commands.updateactions.AddPrice}</li>
 <li>{@link io.sphere.sdk.products.commands.updateactions.ChangePrice}</li>
 <li>{@link io.sphere.sdk.products.ProductVariantDraftBuilder#price(PriceDraft)}</li>
 <li>{@link io.sphere.sdk.products.ProductVariantDraftBuilder#prices(List)}</li>
 <li>{@link io.sphere.sdk.products.commands.updateactions.AddVariant}</li>
 </ul>
 <p>To roughly compare a {@link Price} with a {@link io.sphere.sdk.products.PriceDraft}, use {@link PriceDraft#of(Price)} to create a draft out of the price and then compare the drafts.</p>
 </li>
 </ul>


 <h3 class=released-version id="v1_0_0_M20">1.0.0-M20 (02.11.2015)</h3>
 <ul>
 <li class=change-in-release>{@code query.withSort(m -> m.createdAt().sort(DESC));} is deprecated, use it with {@code query.withSort(m -> m.createdAt().sort().desc());}</li>
 <li class=fixed-in-release>Duplicates with {@code io.sphere.sdk.queries.ExperimentalReactiveStreamUtils#publisherOf(QueryDsl, SphereClient)} on multiple parallel request(n) calls. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/809">809</a>.</li>
 <li class=fixed-in-release>Logger name shows query string. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/802">802</a>.</li>
 <li class=fixed-in-release>Missing Content-Length header in Async HTTP Client. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/799">799</a>.</li>
 <li class=fixed-in-release>variantIdentifier in {@link LineItem} is null. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/771">771</a>.</li>
 </ul>


 <h3 class=released-version id="v1_0_0_M19">1.0.0-M19 (16.10.2015)</h3>

 <a class="theme-btn expand-all">Expand all</a>
 <br>
 <br>
 <ul>
 <li class=new-in-release>The Search API now allows multi-sort by accepting a list of {@link io.sphere.sdk.search.SortExpression} instead of a single value.</li>
 <li class=new-in-release>Introduced explicit Set product custom attribute models to build search expressions (i.e. facets, filters and sort expressions).</li>
 <li class=new-in-release>Introduced methods {@link io.sphere.sdk.search.PagedSearchResult#getTermFacetResult}, {@link io.sphere.sdk.search.PagedSearchResult#getRangeFacetResult} and {@link io.sphere.sdk.search.PagedSearchResult#getFilteredFacetResult} that accept the facet path.</li>
 <li class=change-in-release>Introduced search filter methods {@code io.sphere.sdk.search.model.TermFilterSearchModel#byAny} and {@code io.sphere.sdk.search.model.TermFilterSearchModel#byAll} (OR and AND semantics, respectively), along with the counterpart for range filters {@code io.sphere.sdk.search.model.RangeTermFilterSearchModel#byAnyRange} and {@code io.sphere.sdk.search.model.RangeTermFilterSearchModel#byAllRanges}), change that affected the signature of all filter methods which now return a list of {@link io.sphere.sdk.search.FilterExpression} instead of a single value.</li>
 <li class=change-in-release>The Search Sort Model offers now the methods {@code io.sphere.sdk.search.model.SortSearchModel#byAsc} and {@code io.sphere.sdk.search.model.SortSearchModel#byDesc}, as well as {@code io.sphere.sdk.search.model.MultiValueSortSearchModel#byAscWithMax} and {@code io.sphere.sdk.search.model.MultiValueSortSearchModel#byDescWithMin} for multi-valued attributes.</li>
 <li class=change-in-release>Removed {@code io.sphere.sdk.search.UntypedSearchModel} class and instead provide methods to give filter and facet parameters as simple strings.
 <div class="rn-hidden">
 {@include.example io.sphere.sdk.products.search.ProductProjectionSearchFiltersIntegrationTest#filterByValueAsString}
 </div>
 </li>
 <li class=change-in-release>While before the Search Model was built using the structure {@code attribute + action + parameters}, now it changed to {@code action + attribute + parameters}, in order to split the model into 3 independent models: Search Filter Model, Search Facet Model and Search Sort Model.
 <div class="rn-hidden">
 Before it looked like:
 <pre>{@code
ProductProjectionSearch search = ProductProjectionSearch.ofStaged()
.plusQueryFilters(product -> product.name().locale(ENGLISH).filtered().by("shoes"));
PagedSearchResult<ProductProjection> result = client.execute(search);
}</pre>

 Now it is built as follows:
 <pre>{@code
ProductProjectionSearch search = ProductProjectionSearch.ofStaged()
.plusQueryFilters(filter -> filter.name().locale(ENGLISH).by("shoes"));
PagedSearchResult<ProductProjection> result = client.execute(search);
}</pre>
 </div>
 </li>
 <li class=change-in-release>Renamed {@code SearchSort} to {@link io.sphere.sdk.search.SortExpression}, which now shares the same properties as {@link io.sphere.sdk.search.FacetExpression} and {@link io.sphere.sdk.search.FilterExpression} under the {@link io.sphere.sdk.search.SearchExpression} interface.</li>
 <li class=change-in-release>The static factory method to directly build unsafe {@link io.sphere.sdk.search.FacetExpression} is now located in {@link io.sphere.sdk.search.TermFacetExpression}, {@link io.sphere.sdk.search.RangeFacetExpression} and {@link io.sphere.sdk.search.FilteredFacetExpression}, so that a facet expression for the corresponding type is obtained.</li>
 <li class=change-in-release>Moved related Search Model classes from {@code io.sphere.sdk.search} to {@code io.sphere.sdk.search.model} package, to clearly separate the Search Model from the Search API classes.</li>
 <li class=removed-in-release>Removed type parameters from {@link io.sphere.sdk.search.TermFacetResult}, {@link io.sphere.sdk.search.RangeFacetResult} and {@link io.sphere.sdk.search.FilteredFacetResult}, which now return simple strings as they are received from the platform.</li>
 <li class=new-in-release>Added {@link ProductProjection#getCategoryOrderHints()} and {@link ProductProjectionQueryModel#categoryOrderHints()}. It can be used for search as shown in the following example, but the meta model comes in a later release.
 <div class="rn-hidden">{@include.example io.sphere.sdk.products.ProductCategoryOrderHintIntegrationTest#searchForCategoryAndSort()}</div>
 </li>
 <li class=new-in-release>Added states and update actions for Orders, Reviews and Products: {@link Order#getState()}, {@link io.sphere.sdk.products.Product#getState()}, {@code io.sphere.sdk.reviews.Review#getState()}</li>
 <li class=new-in-release>Added {@link LineItem#getTotalPrice()}, {@link LineItem#getDiscountedPricePerQuantity()}, {@link CustomLineItem#getTotalPrice()} and {@link CustomLineItem#getDiscountedPricePerQuantity()}</li>
 <li class=change-in-release>Deprecated {@code LineItem#getDiscountedPrice()} and {@code CustomLineItem#getDiscountedPrice()} since they are deprecated in the HTTP API</li>
 <li class=new-in-release>Improved documentation of {@link io.sphere.sdk.models.Versioned}.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.payments.Payment}.</li>
 <li class=change-in-release>Deprecated typo method {@code io.sphere.sdk.carts.CartDraft#witCustom(CustomFieldsDraft)}, use {@code io.sphere.sdk.carts.CartDraft#withCustom(CustomFieldsDraft)} instead.</li>
 <li class=change-in-release>Deprecated {@code io.sphere.sdk.customers.queries.CustomerByTokenGet}, use {@link io.sphere.sdk.customers.queries.CustomerByPasswordTokenGet} instead.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.customobjects.queries.CustomObjectByIdGet} and {@link CustomObjectDeleteCommand#of(String, Long, Class)} which can delete a {@link CustomObject} by ID.</li>
 <li class=change-in-release>Simplified CustomObject endpoints. Instead of a {@link com.fasterxml.jackson.core.type.TypeReference} for the endpoint result only the class of the value of the custom object needs to be provided.
 <div class="rn-hidden">
 <p>Before:</p>
 <pre>{@code final CustomObjectQuery<Pojo> query = CustomObjectQuery.of(new TypeReference<PagedQueryResult<CustomObject<Pojo>>>(){});}</pre>
 <p>After:</p>
 <pre>{@code final CustomObjectQuery<Pojo> query = CustomObjectQuery.of(Pojo.class);}</pre>

 <p>This change applies for other endpoints, too.</p>
 </div>
 </li>
 <li class=change-in-release>
 Deprecations in custom object endpoints:
 <dl>
 <dt>{@code CustomObjectDeleteCommand#of(io.sphere.sdk.customobjects.CustomObject)}</dt>
 <dd>use instead {@link CustomObjectDeleteCommand#ofJsonNode(CustomObject)}</dd>

 <dt>{@code CustomObjectDeleteCommand#of(java.lang.String, java.lang.String)}</dt>
 <dd>use instead  {@link CustomObjectDeleteCommand#ofJsonNode(java.lang.String, java.lang.String)}</dd>
 <dt>{@code CustomObjectByKeyGet#of(java.lang.String, java.lang.String)}</dt>
 <dd>use instead {@link CustomObjectByKeyGet#ofJsonNode(java.lang.String, java.lang.String)}</dd>
 <dt>{@code CustomObjectQuery#of()}</dt>
 <dd>use instead {@link CustomObjectQuery#ofJsonNode()}</dd>

 </dl>
 </li>
 <li class=fixed-in-release>Don't include customer password in logs. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/767">767</a>.</li>
 </ul>



 <h3 class=released-version id="v1_0_0_M18">1.0.0-M18 (01.10.2015)</h3>

 <ul>
 <li class=new-in-release>{@link io.sphere.sdk.products.search.ProductProjectionSearch#withFuzzy(Boolean)}</li>
 <li class=new-in-release>documentation of the customer flow in {@link io.sphere.sdk.customers.Customer}</li>
 <li class=new-in-release>{@link io.sphere.sdk.types.Custom custom fields}</li>
 <li class=new-in-release>Improved error reporting for status code 413 and 414.</li>

 <li class=change-in-release>{@link io.sphere.sdk.search.TermStats} contains not anymore "T" and "F" but "true" and "false".</li>
 <li class=change-in-release>deprecate {@code io.sphere.sdk.customers.commands.CustomerCreateTokenCommand}, use {@link io.sphere.sdk.customers.commands.CustomerCreatePasswordTokenCommand} instead</li>
 <li class=fixed-in-release>DiscountedLineItemPrice money field is called value not money. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/711">711</a>.</li>
 </ul>


 <h3 class=released-version id="v1_0_0_M17">1.0.0-M17 (28.08.2015)</h3>

 <ul>
 <li class=new-in-release>Support of full locales like "de_DE"</li>
 <li class=new-in-release>{@link io.sphere.sdk.products.ProductProjection} is also a {@link io.sphere.sdk.models.Versioned} for {@link io.sphere.sdk.products.Product}</li>
 <li class=new-in-release>{@link AttributeAccess#ofInteger()}, {@link AttributeAccess#ofIntegerSet()}, {@link AttributeAccess#ofLong()}, {@link AttributeAccess#ofLongSet()}</li>
 <li class=new-in-release>{@link io.sphere.sdk.taxcategories.commands.TaxCategoryUpdateCommand}</li>
 <li class=new-in-release>{@link io.sphere.sdk.products.ProductDraftBuilder#taxCategory(Referenceable)}, {@link io.sphere.sdk.products.ProductDraftBuilder#searchKeywords(SearchKeywords)}</li>
 <li class=new-in-release>{@code Reference<Void>} and {@code Reference<Object>} is now {@code Reference<JsonNode>}</li>
 <li class=new-in-release>{@link UpdateCommand#getUpdateActions()}</li>
 <li class=new-in-release>{@link io.sphere.sdk.products.messages.ProductPublishedMessage}, {@link io.sphere.sdk.products.messages.ProductUnpublishedMessage}</li>
 <li class=new-in-release>{@link io.sphere.sdk.models.Address#equalsIgnoreId(Address)}, {@code io.sphere.sdk.products.Price#equalsIgnoreId(Price)}, {@code io.sphere.sdk.taxcategories.TaxRate#equalsIgnoreId(TaxRate)}</li>
 </ul>

 <h3 class=released-version id="v1_0_0_M16">1.0.0-M16 (06.08.2015)</h3>
 <p>This release is intended to be the last release before 1.0.0-RC1.</p>

 <ul>
 <li class=new-in-release>{@link io.sphere.sdk.productdiscounts.commands.ProductDiscountCreateCommand},
 {@link io.sphere.sdk.productdiscounts.commands.ProductDiscountUpdateCommand},
 {@link io.sphere.sdk.productdiscounts.commands.ProductDiscountDeleteCommand},
 {@link ProductDiscountByIdGet},
 {@link io.sphere.sdk.productdiscounts.queries.ProductDiscountQuery}
 </li>
 <li class=new-in-release>More reference expansion for product fields.</li>
 <li class=new-in-release>Added a {@link ProductAttributeDocumentation product attribute tutorial}.</li>
 <li class=new-in-release>Added a  {@link CategoryDocumentation category tutorial}.</li>
 <li class=new-in-release>Added the {@link io.sphere.sdk.messages.queries.MessageQuery message endpoint}.</li>
 <li class=new-in-release>Added query and expansion meta models for carts.</li>
 <li class=new-in-release>{@link io.sphere.sdk.utils.MoneyImpl#ofCents(long, CurrencyUnit)} can be used to create monetary values</li>
 <li class=new-in-release>Customer can be created with attributes dateOfBirth, companyName, vatId, isEmailVerified, customerGroup and addresses.</li>
 <li class=new-in-release>External images can be included in the product creation.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.inventory.commands.updateactions.ChangeQuantity} to update {@link io.sphere.sdk.inventory.InventoryEntry}s.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.channels.ChannelRole#PRODUCT_DISTRIBUTION}.</li>
 <li class=new-in-release>Added {@link LineItem#getDistributionChannel()}.</li>
 <li class=new-in-release>Meta models can be used in the product search, too. See {@link io.sphere.sdk.products.search.ProductProjectionSearch#plusFacetFilters(Function)}.</li>
 <li class=new-in-release>Added documentation how to query for large offsets in {@link QueryDocumentation}.</li>
 <li class=new-in-release>Added hints how to format date and monetary data in {@link FormattingDocumentation}.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.producttypes.commands.ProductTypeUpdateCommand}.</li>
 <li class=new-in-release>To imrove the query speed the calculation of the total amount of items in commercetools platform can be deactivated with {@link io.sphere.sdk.products.queries.ProductQuery#withFetchTotal(boolean)}.</li>
 <li class=new-in-release>{@link QueryPredicate}s can be negated with {@link QueryPredicate#negate()}</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.carts.CartState#ORDERED} and {@link Order#getCart()}.</li>
 <li class=new-in-release>Added {@code io.sphere.sdk.products.ProductProjection#findVariantBySky(String)}.</li>
 <li class=new-in-release>Reference expansion for the product projection search.</li>
 <li class=change-in-release>The default behaviour of queries changed, things are not sorted by ID by default for performance reasons. The impact is that performing the exact query again may yields different results.</li>
 <li class=change-in-release>{@link java.util.Optional} as return type for optional values has been removed, now the annotation {@link javax.annotation.Nullable} is used to mark objects as not mandatory. This change was necessary to provide a stable API, to provide later serializable Objects and sparse representations for objects.</li>
 <li class=change-in-release>For API read objects primitives have been replaced by wrapper classed to implement later sparse representations. This may affect type conversions which won't work anymore.</li>
 <li class=change-in-release>The instantiation of the {@link io.sphere.sdk.client.SphereClient} has been changed, see {@link GettingStarted} and {@link io.sphere.sdk.client.SphereClientFactory}.</li>
 <li class=change-in-release>Product type creation has been refactored. Have a look at {@link ProductAttributeDocumentation product attribute tutorial}.</li>
 <li class=change-in-release>Product prices have an ID and price updates need to be performed via the price ID: {@link io.sphere.sdk.products.commands.updateactions.ChangePrice}, {@link io.sphere.sdk.products.commands.updateactions.RemovePrice}. Keep in mind that {@code io.sphere.sdk.products.Price#equals(Object)} includes the ID.</li>
 <li class=change-in-release>The naming conventions in the commercetools platform have been changed:
 <ul>
 <li>PlainEnumValue is now EnumValue</li>
 <li>LocalizedStrings is now LocalizedString</li>
 <li>LocalizedStringsEntry is now LocalizedStringEntry</li>
 <li>Fetch is now Get (e.g. {@code ProductProjectionByIdFetch} is now {@link ProductProjectionByIdGet}</li>
 </ul>
 </li>

 <li class=change-in-release>{@code JsonUtils} have been renamed to {@link io.sphere.sdk.json.SphereJsonUtils}, this is an internal utility class working for the commercetools platform context, it is not intended to build apps or libs on it.</li>
 <li class=change-in-release>{@code ChannelRoles} have been renamed to {@link io.sphere.sdk.channels.ChannelRole}.</li>
 <li class=change-in-release>{@code SearchText} is now {@link io.sphere.sdk.models.LocalizedStringEntry}</li>
 <li class=removed-in-release>Removed {@code ProductUpdateScope}, so all product update actions update only staged.</li>
 <li class=removed-in-release>Builders for resources (read objects) have been removed. See {@link TestingDocumentation} to build the objects anyway.</li>
 <li class=removed-in-release>QueryToFetch adapter has been removed.</li>

 <li class=fixed-in-release>{@link io.sphere.sdk.queries.StringQueryModel#isGreaterThan(Object)} and other methods do not quote strings correctly. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/558">558</a>.</li>
 <li class=fixed-in-release>Product variant expansion does not work. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/631">631</a>.</li>

 </ul>


 <h3 class=released-version id="v1_0_0_M15">1.0.0-M15 (23.06.2015)</h3>

 <a class="theme-btn expand-all">Expand all</a>

 <br>
 <br>

 <ul>
 <li class=new-in-release>{@code io.sphere.sdk.products.search.ExperimentalProductProjectionSearchModel#productType()} enables you to build search expressions with the Product Type reference of products</li>
 <li class=new-in-release>{@link ProductVariant#getIdentifier()} enables to get the product id and the variant id from the variant. This is nice, since this data is often needed and on the product variant level the product ID is not available.</li>
 <li class=new-in-release>Error reporting has been improved. Especially if JSON mappings do not fit.</li>
 <li class=new-in-release>{@link LineItem#getProductSlug()}</li>
 <li class=new-in-release>Send User-Agent head for oauth requests.</li>
 <li class=new-in-release>A lot of more reference expansions.</li>
 <li class=new-in-release>Reference expansion for single elements of a list, example: {@link io.sphere.sdk.categories.expansion.CategoryExpansionModel#ancestors(int)}.</li>
 <li class=new-in-release>Reference expansion for fetch endpoints, e.g., {@link ProductProjectionByIdGet#withExpansionPaths(ExpansionPath)}.</li>
 <li class=new-in-release>Set the reference expansion, query predicate and sort expression more convenient (less imports, less class search) with lambdas.
 <div class="rn-hidden">
 Migration pains: {@code QueryDsl<Category>} will be {@code CategoryQuery}

 <p>Benefits of the new API:</p>

 {@include.example example.QueryMetaDslDemo#demo1()}

 instead of

 {@include.example example.QueryMetaDslDemo#demo2()}

 In Scala (with add-ons) it will look like <pre><code>val query = ProductProjectionQuery.ofCurrent()
 .withPredicateScala(_.productType.id.is("product-type-id-1"))
 .withSortScala(_.name.lang(ENGLISH).sort.asc)
 .withExpansionPathsScala(_.productType)</code></pre>
 </div>
 </li>
 <li class=new-in-release>
 DSL for expansion of references in a product attribute.
 <div class="rn-hidden">{@include.example io.sphere.sdk.products.expansion.ProductVariantExpansionModelTest#productAttributeReferenceExpansion()}</div>
 </li>
 <li class=new-in-release>{@code io.sphere.sdk.reviews.commands.ReviewDeleteCommand}</li>
 <li class=new-in-release>{@link io.sphere.sdk.client.SphereClientConfig#ofEnvironmentVariables(String)} to get the </li>
 <li class=change-in-release>The product attributes have been refactored, look at the {@link ProductAttributeDocumentation} how it works now.</li>
 <li class=change-in-release>{@link io.sphere.sdk.client.SphereClient} implements {@link AutoCloseable} instead of {@link java.io.Closeable}.</li>
 <li class=change-in-release>For timestamps we moved from {@link java.time.Instant} to {@link java.time.ZonedDateTime} since the latter also contains a timezone which better reflects the platforms date time data.</li>
 <li class=change-in-release>Getting the child categories of a category is not in category anymore but in {@link CategoryTree#findChildren(Identifiable)}.</li>
 <li class=fixed-in-release>Sphere client does not shutdown actors properly.  See <a target="_blank" href="https://github.com/commercetools/commercetools-jvm-sdk/issues/491">#491</a>.</li>
 <li class=removed-in-release>{@code Category#getPathInTree()}</li>
 <li class=removed-in-release>{@code ExperimentalProductImageUploadCommand}, but you can find a similar command here: <a href="https://github.com/commercetools/commercetools-jvm-sdk-experimental-java-add-ons">https://github.com/commercetools/commercetools-jvm-sdk-experimental-java-add-ons</a></li>
 </ul>




 <h3 class=released-version id=v1_0_0_M14>1.0.0-M14 (27.05.2015)</h3>
 <ul>
 <li class=new-in-release>New fields in {@link io.sphere.sdk.products.Price}: {@link Price#getValidFrom()} and {@link Price#getValidUntil()}.</li>
 <li class=new-in-release>Use {@link io.sphere.sdk.products.queries.ProductProjectionQueryModel#allVariants()} to formulate a predicate for all variants. In the platform the json fields masterVariant (object) and variants (array of objects) together contain all variants.</li>
 <li class=new-in-release>Using {@link ProductProjectionQuery#ofCurrent()} and {@link ProductProjectionQuery#ofStaged()} saves you the import of {@link ProductProjectionType}.</li>
 <li class=new-in-release>{@link CompletionStage} does not support by default timeouts which are quite important in a reactive application so you can decorate the {@link io.sphere.sdk.client.SphereClient} with {@link io.sphere.sdk.client.TimeoutSphereClientDecorator} to get a {@link java.util.concurrent.TimeoutException} after a certain amount of time. But this does NOT cancel the request to the platform.</li>
 <li class=new-in-release>The {@code io.sphere.sdk.reviews.Review} endpoints and models are implemented, but we suggest to not use it, since {@code io.sphere.sdk.reviews.Review}s cannot be deleted or marked as hidden.</li>
 <li class=new-in-release>New endpoint: Use {@link io.sphere.sdk.projects.queries.ProjectGet} to get the currencies, countries and languages of the commercetools project.</li>
 <li class=new-in-release>Categories with SEO meta attributes {@link Category#getMetaTitle()}, {@link Category#getMetaDescription()} and {@link Category#getMetaKeywords()} and
 update actions {@link io.sphere.sdk.categories.commands.updateactions.SetMetaTitle}, {@link io.sphere.sdk.categories.commands.updateactions.SetMetaDescription} and {@link io.sphere.sdk.categories.commands.updateactions.SetMetaKeywords}.</li>
 <li class=new-in-release>Cart discounts: {@link io.sphere.sdk.cartdiscounts.commands.CartDiscountCreateCommand}.</li>
 <li class=new-in-release>Discount codes: {@link io.sphere.sdk.discountcodes.commands.DiscountCodeCreateCommand}.</li>
 <li class=change-in-release>{@link io.sphere.sdk.client.SphereApiConfig}, {@link io.sphere.sdk.client.SphereAuthConfig}, {@link io.sphere.sdk.client.SphereClientConfig} validates the input, so for example you cannot enter null or whitespace for the project key.</li>
 <li class=change-in-release>Date time attributes in {@code ProductProjectionSearchModel} are using <a href="https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html">ZonedDateTime</a> instead of <a href="https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html">LocalDateTime</a>.</li>
 <li class=change-in-release>The {@code ProductProjectionSearchModel} has been improved with better naming and better documentation.</li>
 <li class=change-in-release>Sort related classes for the Query API have been renamed with a "Query" prefix, to distinguish them from the Search API sort classes.</li>
 <li class=change-in-release>{@code io.sphere.sdk.queries.Predicate} has been renamed to {@link io.sphere.sdk.queries.QueryPredicate}.</li>
 <li class=change-in-release>The JVM SDK itself uses for tests the <a href="https://joel-costigliola.github.io/assertj/">assertj</a> assertion methods instead of fest assertions.</li>
 <li class=change-in-release>{@code io.sphere.sdk.products.commands.updateactions.SetMetaAttributes} has been removed since it is deprecated in the commercetools platform.
 Use {@link SetMetaTitle},
 {@link SetMetaDescription},
 {@link SetMetaKeywords} or {@link io.sphere.sdk.products.commands.updateactions.MetaAttributesUpdateActions} for all together.
 </li>
 <li class=change-in-release>Update of the Java Money library to version 1.0 which has some breaking changes in comparison to the release candidates.</li>
 <li class=change-in-release>Some predicate methods have been renamed: "isOneOf" to "isIn", "isAnyOf" to "isIn", "isLessThanOrEquals" to "isLessThanOrEqualTo" (analog for greaterThan).</li>
 <li class=fixed-in-release>The exception for a failing {@link io.sphere.sdk.customers.commands.CustomerSignInCommand} shows more problem details. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/397">#397</a>.</li>
 <li class=fixed-in-release>There has been a thread leak if {@code io.sphere.sdk.client.SphereClientFactory#createClient(io.sphere.sdk.client.SphereClientConfig)} was used.
 It created twice a http client instance and closed just one.</li>
 </ul>

 <h3 class=released-version id=v1_0_0_M13>1.0.0-M13 (21.04.2015)</h3>
 <ul>
 <li class=new-in-release>{@link LocalizedString#mapValue(BiFunction)} and {@link LocalizedString#stream()}
 can be used transform {@link LocalizedString}, for example for creating slugs or formatting.</li>
 <li class=new-in-release>Experimental {@link io.sphere.sdk.utils.CompletableFutureUtils} to work with Java 8 Futures.</li>
 <li class=new-in-release>{@link AsyncDocumentation} documents how to work with {@link java.util.concurrent.CompletableFuture} and {@link java.util.concurrent.CompletionStage}.</li>

 <li class=new-in-release>{@link io.sphere.sdk.models.SphereException}s may give hint to developers how to recover from errors. For example on typical elasticsearch related problems it suggests to reindex the product index.</li>
 <li class=new-in-release>The JVM SDK is available for Ning Async HTTP Client 1.8 and 1.9 (incompatible to 1.8).</li>
 <li class=new-in-release>State update actions and {@code io.sphere.sdk.states.StateBuilder} contributed by <a href="https://github.com/abrauner">Ansgar Brauner</a></li>
 <li class=new-in-release>Embedded predicates (as used in 'where'-clauses) now support lambda syntax: {@code ProductVariantQueryModel#where(java.util.function.Function)}.</li>
 <li class=new-in-release>{@link io.sphere.sdk.orders.queries.OrderQuery#byCustomerId(java.lang.String)} and {@link io.sphere.sdk.orders.queries.OrderQuery#byCustomerEmail(java.lang.String)}</li>
 <li class=new-in-release>{@link io.sphere.sdk.channels.commands.ChannelUpdateCommand}</li>
 <li class=new-in-release>{@link ChannelByIdGet}</li>
 <li class=new-in-release>{@link io.sphere.sdk.customers.queries.CustomerQuery#byEmail(java.lang.String)}</li>
 <li class=change-in-release>{@link io.sphere.sdk.client.SphereRequest} has been refactored:<ul>
 <li>{@code Function<HttpResponse, T> resultMapper()} is now {@link io.sphere.sdk.client.SphereRequest#deserialize(HttpResponse)}</li>
 <li>{@code boolean canHandleResponse(final HttpResponse response)} is now {@link io.sphere.sdk.client.SphereRequest#canDeserialize(HttpResponse)}</li>
 </ul>
 </li>
 <li class=change-in-release>Artifact IDs start now with "sphere-".</li>
 <li class=change-in-release> {@link java.util.concurrent.CompletableFuture} has been replaced with {@link java.util.concurrent.CompletionStage}.

 You can convert from {@link java.util.concurrent.CompletionStage} to {@link java.util.concurrent.CompletableFuture} with {@link CompletionStage#toCompletableFuture()}.

 The opposite direction can be achieved with assigning {@link java.util.concurrent.CompletableFuture} (implementation) to {@link java.util.concurrent.CompletionStage} (interface).</li>
 <li class=change-in-release>Refactoring of {@link io.sphere.sdk.client.SphereClient}, {@code <T> CompletionStage<T> execute(final SphereRequest<T> sphereRequest)}
 is now {@link io.sphere.sdk.client.SphereClient#execute(SphereRequest)} which returns {@link java.util.concurrent.CompletionStage} instead of {@link java.util.concurrent.CompletableFuture}.
 </li>
 <li class=change-in-release>{@link  io.sphere.sdk.carts.LineItem#getTaxRate()} is optional.</li>
 <li class=change-in-release>{@link io.sphere.sdk.carts.LineItem#getState()} now returns a set instead of a list.</li>
 <li class=change-in-release>{@link io.sphere.sdk.products.ProductProjection#getCategories()} now returns a set instead of a list.</li>

 <li class=fixed-in-release>URL encoding of parameters. See <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/240">#240</a>.</li>
 </ul>

 <h3 class=released-version id=v1_0_0_M12>1.0.0-M12 (19.03.2015)</h3>
 <ul>
 <li class=new-in-release>Added the {@link io.sphere.sdk.orders.commands.OrderImportCommand}.</li>
 <li class=new-in-release>Added the nested attributes: {@code io.sphere.sdk.attributes.AttributeAccess#ofNested()} + {@code io.sphere.sdk.attributes.AttributeAccess#ofNestedSet()}.</li>
 <li class=new-in-release>The error JSON body from the platform responses can be directly extracted as JSON with {@link io.sphere.sdk.client.SphereServiceException#getJsonBody()}.</li>
 <li class=new-in-release>{@link io.sphere.sdk.http.HttpResponse} also contains {@link io.sphere.sdk.http.HttpHeaders}.</li>
 <li class=new-in-release>Experimental search filter/facet/sort expression model {@code ProductProjectionSearchModel}. See also {@link io.sphere.sdk.meta.SearchDocumentation}.</li>
 <li class=change-in-release>The {@link io.sphere.sdk.producttypes.ProductType} creation has been simplified (TextAttributeDefinition, LocalizedStringsAttributeDefinition, ... are just AttributeDefinition), see {@link io.sphere.sdk.producttypes.commands.ProductTypeCreateCommand} how to create them.</li>
 <li class=change-in-release>{@link io.sphere.sdk.search.TermFacetResult} and
 {@link io.sphere.sdk.search.RangeFacetResult} are using generics.
 {@link io.sphere.sdk.search.TermFacetResult} uses long instead of int for some methods like {@link io.sphere.sdk.search.TermFacetResult#getMissing()}.</li>
 <li class=change-in-release>Methods in {@link io.sphere.sdk.search.SearchDsl} have been renamed.</li>
 <li class=change-in-release>{@code io.sphere.sdk.search.RangeStats#getMean()} now returns a double.</li>
 <li class=change-in-release>{@link io.sphere.sdk.http.HttpHeaders} allows reoccurring headers.</li>
 <li class=change-in-release>Use of a new toString style, from <pre>io.sphere.sdk.client.HttpRequestIntent@7308a939[httpMethod=POST,path=/categories,headers={},body=Optional[io.sphere.sdk.http.StringHttpRequestBody@216ec9be[body={invalidJson :)]]]</pre> to <pre>HttpRequestIntent[httpMethod=POST,path=/categories,headers={},body=Optional[StringHttpRequestBody[body={invalidJson :)]]]</pre></li>
 <li class=fixed-in-release>{@link io.sphere.sdk.client.ErrorResponseException#getMessage()} now returns also the project debug info.</li>
 <li class=fixed-in-release><a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/312">Incompatibility with Jackson 2.5.1</a> has been fixed. A cause message was "No suitable constructor found for type [simple type, class io.sphere.sdk.models.ImageImpl]: can not instantiate from JSON object (missing default constructor or creator, or perhaps need to add/enable type information?)"</li>
 </ul>

 <h3 class=released-version id=v1_0_0_M11>1.0.0-M11 (03.03.2015)</h3>
 <h4>Overall</h4>
 <ul>
 <li class=new-in-release>Code examples contain the links to the GitHub source code.</li>
 <li class=new-in-release>The {@link io.sphere.sdk.client.SphereClient} architecture has been refactored, so it is now possible to inject access token suppliers and custom HTTP clients.
 <ul>
 <li>{@link AsyncHttpClientAdapter} enables to use a custom underlying Ning HTTP client for settings like proxies or max connections per host.</li>
 <li>The new module {@code java-client-apache-async} contains an {@link ApacheHttpClientAdapter adapter} to use the Apache HTTP client instead of the current default client Ning.</li>
 <li>The {@link io.sphere.sdk.client.QueueSphereClientDecorator} enables to limit the amount of concurrent requests to the platform with a task queue.</li>
 <li>{@code io.sphere.sdk.client.SphereAccessTokenSupplierFactory} is a starting point to create custom access token suppliers for one token (either fetched from commercetools or as String) or auto refreshing for online shops.</li>
 </ul>
 </li>
 <li class=new-in-release>Added {@link io.sphere.sdk.client.SphereRequestDecorator} to decorate {@link io.sphere.sdk.client.SphereRequest}s.</li>
 <li class=change-in-release>{@link io.sphere.sdk.meta.ExceptionDocumentation Exception hierarchy}, relocated some exceptions and deleted some.
 <ul>
 <li>Removed SphereBackendException, SphereClientException, JavaConcurrentUtils, Requestable</li>
 <li>Removed ReferenceExistsException, usage as {@code SphereError} from a {@link io.sphere.sdk.client.ErrorResponseException}</li>
 <li>JsonParseException is now {@link io.sphere.sdk.json.JsonException}.</li>
 <li>InvalidQueryOffsetException is replaced with {@link java.lang.IllegalArgumentException}.</li>
 </ul>
 </li>
 <li class=change-in-release>For SDK devs: {@link io.sphere.sdk.http.HttpRequest} has changed tasks and structure, now it contains the full information for a HTTP request whereas now {@link io.sphere.sdk.client.HttpRequestIntent} is an element to describe an endpoint of sphere project independent.</li>
 <li class=change-in-release>For SDK devs: {@link io.sphere.sdk.client.JsonEndpoint} moved to the client package</li>
 <li class=change-in-release>{@link Get} class names end now with Fetch for consistency, so for example {@code CartFetchById} is now {@link CartByIdGet}.</li>
 <li class=change-in-release>{@link io.sphere.sdk.commands.DeleteCommand} implementations don't have {@code ById} or {@code ByKey} in the class name and the {@code of} factory method returns the interface, not the implementation, example {@link io.sphere.sdk.categories.commands.CategoryDeleteCommand#of(io.sphere.sdk.models.Versioned)}.</li>
 <li class=change-in-release>LocalizedText* classes have been renamed to LocalizedString.</li>
 <li class=fixed-in-release>Fixed: UnknownCurrencyException <a href="https://github.com/commercetools/commercetools-jvm-sdk/issues/264">#264</a>.</li>
 </ul>

 <h4>States</h4>
 <ul>
 <li class=new-in-release>{@link io.sphere.sdk.states.State} models, creation and deletion contributed by <a href="https://github.com/abrauner">Ansgar Brauner</a></li>
 </ul>

 <h4>Products</h4>
 <ul>
 <li class=new-in-release>Added update actions: {@link io.sphere.sdk.products.commands.updateactions.AddVariant}, {@link io.sphere.sdk.products.commands.updateactions.RemoveFromCategory}, {@link io.sphere.sdk.products.commands.updateactions.RemoveVariant}, {@link io.sphere.sdk.products.commands.updateactions.RevertStagedChanges}, {@link io.sphere.sdk.products.commands.updateactions.SetAttribute}, {@link io.sphere.sdk.products.commands.updateactions.SetAttributeInAllVariants}, {@link io.sphere.sdk.products.commands.updateactions.SetSearchKeywords}</li>
 </ul>

 <h3 class=released-version id=v1_0_0_M10>1.0.0-M10 (26.01.2015)</h3>
 <ul>
 <li class=new-in-release>Added {@link io.sphere.sdk.customobjects.CustomObject} models and endpoints. There is also a {@link io.sphere.sdk.meta.CustomObjectDocumentation tutorial for custom objects}.</li>
 <li class=new-in-release>Added the {@link io.sphere.sdk.zones.Zone} models and endpoints.</li>
 <li class=new-in-release>Added the {@link io.sphere.sdk.shippingmethods.ShippingMethod} models and endpoints.</li>
 <li class=new-in-release>Added Typesafe Activator files, so you can edit the SDK on UNIX with {@code ./activator ui} or on Windows with {@code activator ui}.</li>
 <li class=new-in-release>Added io.sphere.sdk.client.ConcurrentModificationException which is thrown when an {@link io.sphere.sdk.commands.UpdateCommand} fails because of concurrent usage.</li>
 <li class=new-in-release>Added io.sphere.sdk.client.ReferenceExistsException which is thrown when executing a {@link io.sphere.sdk.commands.DeleteCommand} and the resource is referenced by another resource and cannot be deleted before deleting the other resource.</li>
 <li class=new-in-release>Added io.sphere.sdk.queries.InvalidQueryOffsetException which is thrown if offset is
 not between {@value io.sphere.sdk.queries.Query#MIN_OFFSET} and {@value io.sphere.sdk.queries.Query#MAX_OFFSET}..</li>
 <li class=new-in-release>Improved on different location the structure of interfaces so important methods are highlighted bold in the IDE.</li>
 <li class=new-in-release>JSON mapping errors are better logged.</li>
 <li class=new-in-release>Added update actions for cart: {@link io.sphere.sdk.carts.commands.updateactions.SetShippingMethod} and {@link io.sphere.sdk.carts.commands.updateactions.SetCustomerId}.</li>
 <li class=new-in-release>Added update actions for customer: {@link io.sphere.sdk.carts.commands.updateactions.SetCustomerId}.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.models.Referenceable#hasSameIdAs(io.sphere.sdk.models.Referenceable)} to check if a similar object has the same ID.</li>
 <li class=new-in-release>Added {@code AttributeAccess#ofName(String)} as alias to {@code io.sphere.sdk.attributes.AttributeAccess#getterSetter(String)}.</li>

 <li class=new-in-release>Update action list in update commands do not have the type {@literal List<UpdateAction<T>>} {@literal  List<? extends UpdateAction<T>>}, so you can pass a list of a subclass of {@link UpdateActionImpl}.
 Example: {@literal List<ChangeName>} can be assigned where {@literal ChangeName} extends {@link UpdateActionImpl}.</li>

 <li class=new-in-release>Added {@link io.sphere.sdk.models.Address#of(com.neovisionaries.i18n.CountryCode)}.</li>
 <li class=new-in-release>Added {@code io.sphere.sdk.carts.Cart#getLineItem(String)} and {@code io.sphere.sdk.carts.Cart#getCustomLineItem(String)} to find items in a cart.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.products.ProductProjection#getAllVariants()} to receive master variant and all other variants in one call. {@link io.sphere.sdk.products.ProductProjection#getVariants()} just returns all variants except the master variant.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.products.ProductProjection#getVariant(int)} and {@link io.sphere.sdk.products.ProductProjection#getVariantOrMaster(int)} to find a product variant by id.</li>
 <li class=new-in-release>Added {@code VariantIdentifier} to have a container to address product variants which needs a product ID and a variant ID.</li>
 <li class=new-in-release>added {@link io.sphere.sdk.customers.commands.CustomerDeleteCommand} to delete customers.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.products.commands.updateactions.AddExternalImage} to connect products with images not hosted by the commercetools platform.</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.products.commands.updateactions.RemoveImage} to disconnect images from a product (external images and commercetools platform hosted).</li>
 <li class=new-in-release>Added {@link io.sphere.sdk.client.SphereAccessTokenSupplier} as authentication method in the {@link io.sphere.sdk.client.SphereClient}.
 It is possible to automatically refresh a token or just pass a token to the client, see {@link io.sphere.sdk.client.SphereClientFactory#createClient(io.sphere.sdk.client.SphereApiConfig, io.sphere.sdk.client.SphereAccessTokenSupplier)} and {@link io.sphere.sdk.client.SphereAccessTokenSupplier#ofConstantToken(String)}.</li>


 <li class=change-in-release>Product variants are all of type int, was int and long before.</li>
 <li class=change-in-release>{@link io.sphere.sdk.models.Reference} is not instantiated with new.</li>
 <li class=change-in-release>{@link io.sphere.sdk.http.UrlQueryBuilder} is not instantiated with new.</li>
 <li class=change-in-release>io.sphere.sdk.client.SphereErrorResponse is not instantiated with new.</li>
 <li class=change-in-release>{@code ClientRequest} has been renamed to {@link io.sphere.sdk.client.SphereRequest} and therefore {@code ClientRequestBase} to {@code io.sphere.sdk.client.SphereRequestBase}. </li>
 <li class=change-in-release>{@code ClientRequest} has been renamed to {@link io.sphere.sdk.client.SphereRequest} and therefore {@code ClientRequestBase} to {@code io.sphere.sdk.client.SphereRequestBase}. </li>
 <li class=change-in-release>{@code JavaClient} has been renamed to {@link io.sphere.sdk.client.SphereClient} and uses the {@link io.sphere.sdk.client.SphereClientFactory} to initialized a client, {@code JavaClientIml} has been removed, see {@link io.sphere.sdk.meta.GettingStarted}.
 The typesafe config library is not used anymore. The class {@code HttpClientTestDouble} has been removed, use {@code io.sphere.sdk.client.SphereClientFactory#createHttpTestDouble(java.util.function.Function)} instead.
 {@code SphereRequestExecutor} and {@code SphereRequestExecutorTestDouble} have been removed, use {@code io.sphere.sdk.client.SphereClientFactory#createObjectTestDouble(java.util.function.Function)} instead.
 </li>


 <li class=fixed-in-release>Money portions in the taxed price is not null. The method name is now {@link io.sphere.sdk.carts.TaxPortion#getAmount()} instead of {@code getMoney()}.</li>
 <li class=fixed-in-release>Fixed JSON serialization and deserialization of {@code ImageDimensions} which caused adding external images to a product to fail.</li>
 </ul>

 <h3>1.0.0-M9</h3>
 <ul>
 <li>Added Known Issues page.</li>
 <li>Added experimental support for uploading product images in variants. See {@code io.sphere.sdk.products.commands.ExperimentalProductImageUploadCommand}.</li>
 <li>Added factory methods for {@code Image}.</li>
 <li>{@code Image} contains directly getters for width {@code Image#getWidth()}
 and height {@code Image#getHeight()}.</li>
 <li>{@link io.sphere.sdk.queries.PagedQueryResult} is constructable for empty results. Before this, the SDK throwed an Exception.</li>
 <li>Fields called {@code quantity} are now of type long instead of int.</li>
 <li>Added a documentation page {@link io.sphere.sdk.meta.ConstructionDocumentation how to construct objects}.</li>
 </ul>

 <h3>1.0.0-M8</h3>

 <ul>
 <li>Query models contain id, createdAt and lastModifiedAt for predicates and sorting.</li>
 <li>Introduced endpoints and models for {@link io.sphere.sdk.carts.Cart}s, {@link io.sphere.sdk.customers.Customer}s, {@link io.sphere.sdk.customergroups.CustomerGroup}s and {@link io.sphere.sdk.orders.Order}s.</li>
 <li>Quantity fields are now of type long.</li>
 <li>Classes like {@link ProductByIdGet} take now a string parameter for the ID and not an {@link io.sphere.sdk.models.Identifiable}.</li>
 <li>Queries, Fetches, Commands and Searches are only instantiable with an static of method like {@link io.sphere.sdk.categories.commands.CategoryCreateCommand#of(io.sphere.sdk.categories.CategoryDraft)}. The instantiation by constructor is not supported anymore.</li>
 <li>Enum constant names are only in upper case.</li>
 </ul>

 <h3>1.0.0-M7</h3>

 <ul>
 <li>Incompatible change: Classes to create templates for new entries in the platform like {@code NewCategory} have been renamed to {@link io.sphere.sdk.categories.CategoryDraft}. </li>
 <li>Incompatible change: {@link ProductTypeDraft} has now only
 factory methods with an explicit parameter for the attribute declarations to prevent to use
 the getter {@link ProductTypeDraft#getAttributes()} and list add operations. </li>
 <li>Incompatible change: {@code LocalizedString} has been renamed to {@link LocalizedString}, since it is not a container for one string and a locale, but for multiple strings of different locals. It is like a map.</li>
 <li>Incompatible change: The {@link Get} classes have been renamed. From FetchRESOURCEByWhatever to RESOURCEFetchByWhatever</li>
 <li>Moved Scala and Play clients out of the Git repository to <a href="https://github.com/commercetools/commercetools-jvm-sdk-scala-add-ons">https://github.com/commercetools/commercetools-jvm-sdk-scala-add-ons</a>. The artifact ID changed.</li>
 <li>{@link io.sphere.sdk.meta.SphereResources} contains now also a listing of queries and commands for the resources.</li>
 <li>Added {@link io.sphere.sdk.products.search.ProductProjectionSearch} for full-text, filtered and faceted search.</li>
 <li>Incompatible change: {@code io.sphere.sdk.products.ProductUpdateScope} makes it more visible that product update operations can be for only staged or for current and staged. The product update actions will be affected by that.</li>
 <li>Implemented anonymous carts.</li>
 </ul>

 <h3>1.0.0-M6</h3>

 <ul>
 <li>Usage of <a href="https://javamoney.java.net/">Java money</a> instead of custom implementation.</li>
 <li>Introduce {@link io.sphere.sdk.products.queries.ProductProjectionQuery}.</li>
 <li>Introduce {@link io.sphere.sdk.meta.QueryDocumentation} to document the query API.</li>
 </ul>

 <h3>1.0.0-M5</h3>

 <ul>
 <li>Fixed client shutdown problem.</li>
 <li>Put {@link io.sphere.sdk.models.MetaAttributes MetaAttributes} in common module and make it an interface.</li>
 <li>Add {@link io.sphere.sdk.products.ProductProjection#isPublished()}.</li>
 <li>Add {@link io.sphere.sdk.productdiscounts.ProductDiscount ProductDiscount} models.</li>
 <li>Add {@link io.sphere.sdk.categories.Category#getExternalId() external id fields and methods} for categories.</li>
 <li>Make {@link io.sphere.sdk.products.ProductCatalogData#getCurrent()} return an optional {@link io.sphere.sdk.products.ProductData}, since current should not be accessible if {@link io.sphere.sdk.products.ProductCatalogData#isPublished()} returns false.</li>
 <li>Make masterVariant in {@link io.sphere.sdk.products.ProductDraftBuilder} mandatory.</li>
 </ul>


 <h3>1.0.0-M4</h3>
 <ul>
 <li>Replacing joda time library with Java 8 DateTime API.</li>
 <li>Removing dependency to Google Guava.</li>
 <li>Rename artifact organization to {@code io.sphere.sdk.jvm}.</li>
 <li>Rename {@code JsonUtils.readObjectFromJsonFileInClasspath} to {@code JsonUtils.readObjectFromResource}.</li>
 <li>Reduced the number of SBT modules to speed up travis builds since the resolving of artifacts for every module is slow. In addition fewer JARs needs to be downloaded.</li>
 <li>Introduced {@link io.sphere.sdk.products.ProductProjection}s.</li>
 <li>Javadoc does contain a table of content box for h3 headings.</li>
 </ul>

 <h3>1.0.0-M3</h3>
 <ul>
 <li>The query model can now be accessed by it's Query class, e.g., {@code io.sphere.sdk.categories.queries.CategoryQuery#model()}.</li>
 <li>Added a {@link io.sphere.sdk.meta.GettingStarted Getting Started} page.</li>
 <li>Added a {@link io.sphere.sdk.meta.JvmSdkFeatures Features of the SDK} page.</li>
 <li>Addad a legacy Play Java client for Play Framework 2.2.x.</li>
 <li>Added {@link io.sphere.sdk.products.PriceBuilder}.</li>
 <li>Further null checks.</li>
 <li>Add a lot of a Javadoc, in general for the packages.</li>
 <li>{@link CategoryTree#of(java.util.List)} instead of CategoryTreeFactory is to be used for creating a category tree.</li>
 <li>Move {@link io.sphere.sdk.models.AddressBuilder} out of the {@link io.sphere.sdk.models.Address} class.</li>
 <li>Performed a lot of renamings like the {@code requests} package to {@code http}</li>
 <li>Moved commands and queries to own packages for easier discovery.</li>
 <li>Introduced new predicates for inequality like {@link io.sphere.sdk.queries.StringQueryModel#isGreaterThanOrEqualTo(String)},
 {@link io.sphere.sdk.queries.StringQueryModel#isNot(String)},
 {@code io.sphere.sdk.queries.StringQueryModel#isNotIn(String, String...)} or {@link io.sphere.sdk.queries.StringQueryModel#isNotPresent()}.</li>
 <li>Introduced an unsafe way to create predicates from strings with {@link QueryPredicate#of(String)}.</li>
 </ul>

 <h3>1.0.0-M2</h3>
 <ul>
 <li>With the new reference expansion dsl it is possible to discover and create reference expansion paths for a query.</li>
 <li>All artifacts have the ivy organization {@code io.sphere.jvmsdk}.</li>
 <li>Migration from Google Guavas com.google.common.util.concurrent.ListenableFuture to Java 8 java.util.concurrent.CompletableFuture.</li>
 <li>Removed all Google Guava classes from the public API (internally still used).</li>
 <li>The logger is more fine granular controllable, for example the logger {@code sphere.products.responses.queries} logs only the responses of the queries for products. The trace level logs the JSON of responses and requests as pretty printed JSON.</li>
 <li>Introduced the class {@code io.sphere.sdk.models.Referenceable} which enables to use a model or a reference to a model as parameter, so no direct call of {@code io.sphere.sdk.models.DefaultModel#toReference()} is needed anymore for model classes.</li>
 <li>It is possible to overwrite the error messages of {@code io.sphere.sdk.test.DefaultModelAssert}, {@code io.sphere.sdk.test.LocalizedStringAssert} and {@code io.sphere.sdk.test.ReferenceAssert}.</li>
 <li>{@link io.sphere.sdk.models.Versioned} contains a type parameter to prevent copy and paste errors.</li>
 <li>Sorting query model methods have better support in the IDE, important methods are bold.</li>
 <li>Queries and commands for models are in there own package now and less coupled to the model.</li>
 <li>The query classes have been refactored.</li>
 </ul>




 </div>
 */
public final class ReleaseNotes {
    private ReleaseNotes() {
    }
}
