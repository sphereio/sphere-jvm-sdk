package io.sphere.sdk.carts;

import io.sphere.sdk.carts.commands.CartCreateCommand;
import io.sphere.sdk.carts.commands.CartUpdateCommand;
import io.sphere.sdk.carts.commands.updateactions.*;
import io.sphere.sdk.carts.queries.FetchCartById;
import io.sphere.sdk.models.Address;
import io.sphere.sdk.models.AddressBuilder;
import io.sphere.sdk.models.LocalizedString;
import io.sphere.sdk.products.Product;
import io.sphere.sdk.shippingmethods.ShippingRate;
import io.sphere.sdk.taxcategories.TaxCategory;
import io.sphere.sdk.test.IntegrationTest;
import io.sphere.sdk.utils.MoneyImpl;
import org.junit.Test;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.BiConsumer;

import static io.sphere.sdk.products.ProductFixtures.withTaxedProduct;
import static io.sphere.sdk.taxcategories.TaxCategoryFixtures.withTaxCategory;
import static io.sphere.sdk.test.OptionalAssert.assertThat;
import static org.fest.assertions.Assertions.assertThat;
import static io.sphere.sdk.test.SphereTestUtils.*;

public class CartIntegrationTest extends IntegrationTest {

    public static final int MASTER_VARIANT_ID = 1;

    @Test
    public void create() throws Exception {
        final CartDraft cartDraft = CartDraft.of(EUR).withCountry(DE);
        final Cart cart = execute(new CartCreateCommand(cartDraft));
        assertThat(cart.getTotalPrice().getCurrency().getCurrencyCode()).isEqualTo(EUR.getCurrencyCode());
        assertThat(cart.getCountry()).isEqualTo(Optional.of(DE));
        assertThat(cart.getTotalPrice().isZero()).isTrue();
    }

    @Test
    public void fetchById() throws Exception {
        final Cart cart = createCartWithCountrySomeHow();
        final Optional<Cart> fetchedCartOptional = execute(new FetchCartById(cart));
        assertThat(fetchedCartOptional).isPresentAs(cart);
    }

    @Test
    public void addLineItemUpdateAction() throws Exception {
        withEmptyCartAndProduct((cart, product) -> {
            assertThat(cart.getLineItems()).hasSize(0);
            final int quantity = 3;
            final String productId = product.getId();
            final AddLineItem action = AddLineItem.of(productId, MASTER_VARIANT_ID, quantity);

            final Cart updatedCart = execute(new CartUpdateCommand(cart, action));
            assertThat(updatedCart.getLineItems()).hasSize(1);
            final LineItem lineItem = updatedCart.getLineItems().get(0);
            assertThat(lineItem.getName()).isEqualTo(product.getMasterData().getStaged().getName());
            assertThat(lineItem.getQuantity()).isEqualTo(quantity);
        });
    }

    @Test
    public void removeLineItemUpdateAction() throws Exception {
        withEmptyCartAndProduct((cart, product) -> {
            assertThat(cart.getLineItems()).hasSize(0);
            final AddLineItem action = AddLineItem.of(product.getId(), MASTER_VARIANT_ID, 3);

            final Cart cartWith3 = execute(new CartUpdateCommand(cart, action));
            final LineItem lineItem = cartWith3.getLineItems().get(0);
            assertThat(lineItem.getQuantity()).isEqualTo(3);

            final Cart cartWith2 = execute(new CartUpdateCommand(cartWith3, RemoveLineItem.of(lineItem, 1)));
            assertThat(cartWith2.getLineItems().get(0).getQuantity()).isEqualTo(2);

            final Cart cartWith0 = execute(new CartUpdateCommand(cartWith2, RemoveLineItem.of(lineItem)));
            assertThat(cartWith0.getLineItems()).hasSize(0);
        });
    }

    @Test
    public void changeLineItemQuantityUpdateAction() throws Exception {
        withEmptyCartAndProduct((cart, product) -> {
            assertThat(cart.getLineItems()).hasSize(0);
            final AddLineItem action = AddLineItem.of(product.getId(), MASTER_VARIANT_ID, 3);

            final Cart cartWith3 = execute(new CartUpdateCommand(cart, action));
            final LineItem lineItem = cartWith3.getLineItems().get(0);
            assertThat(lineItem.getQuantity()).isEqualTo(3);

            final Cart cartWith2 = execute(new CartUpdateCommand(cartWith3, ChangeLineItemQuantity.of(lineItem, 2)));
            assertThat(cartWith2.getLineItems().get(0).getQuantity()).isEqualTo(2);

            final Cart cartWith0 = execute(new CartUpdateCommand(cartWith2, ChangeLineItemQuantity.of(lineItem, 0)));
            assertThat(cartWith0.getLineItems()).hasSize(0);
        });
    }

    @Test
    public void  addCustomLineItemUpdateAction() throws Exception {
        withTaxCategory(client(), taxCategory -> {
            final Cart cart = createCartWithCountrySomeHow();
            assertThat(cart.getCustomLineItems()).hasSize(0);
            final MonetaryAmount money = MoneyImpl.of(new BigDecimal("23.50"), EUR);
            final String slug = "thing-slug";
            final LocalizedString name = en("thing");
            final int quantity = 5;
            final CustomLineItemDraft item = CustomLineItemDraft.of(name, slug, money, taxCategory, quantity);

            final Cart cartWith5 = execute(new CartUpdateCommand(cart, AddCustomLineItem.of(item)));
            assertThat(cartWith5.getCustomLineItems()).hasSize(1);
            final CustomLineItem customLineItem = cartWith5.getCustomLineItems().get(0);
            assertThat(customLineItem.getMoney()).isEqualTo(money);
            assertThat(customLineItem.getName()).isEqualTo(name);
            assertThat(customLineItem.getQuantity()).isEqualTo(quantity);
            assertThat(customLineItem.getSlug()).isEqualTo(slug);
            assertThat(customLineItem.getState()).isEqualTo(asList(ItemState.of(quantity)));
            assertThat(customLineItem.getTaxCategory()).isEqualTo(taxCategory.toReference());
        });
    }

    @Test
    public void  removeCustomLineItemUpdateAction() throws Exception {
        withTaxCategory(client(), taxCategory -> {
            final Cart cart = createCartWithCountrySomeHow();
            assertThat(cart.getCustomLineItems()).hasSize(0);
            final CustomLineItemDraft draftItem = createCustomLineItemDraft(taxCategory);

            final Cart cartWithCustomLineItem = execute(new CartUpdateCommand(cart, AddCustomLineItem.of(draftItem)));
            assertThat(cartWithCustomLineItem.getCustomLineItems()).hasSize(1);
            final CustomLineItem customLineItem = cartWithCustomLineItem.getCustomLineItems().get(0);

            final Cart emptyCart = execute(new CartUpdateCommand(cartWithCustomLineItem, RemoveCustomLineItem.of(customLineItem)));
            assertThat(emptyCart.getCustomLineItems()).hasSize(0);
        });
    }

    @Test
    public void setCustomerEmailUpdateAction() throws Exception {
        final Cart cart = createCartWithCountrySomeHow();
        assertThat(cart.getCustomerEmail()).isAbsent();
        final String email = "info@commercetools.de";
        final Cart cartWithEmail = execute(new CartUpdateCommand(cart, SetCustomerEmail.of(email)));
        assertThat(cartWithEmail.getCustomerEmail()).isPresentAs(email);
    }

    @Test
    public void setShippingAddressUpdateAction() throws Exception {
        final Cart cart = createCartWithCountrySomeHow();
        assertThat(cart.getShippingAddress()).isAbsent();
        final Address address = AddressBuilder.of(DE).build();
        final Cart cartWithAddress = execute(new CartUpdateCommand(cart, SetShippingAddress.of(address)));
        assertThat(cartWithAddress.getShippingAddress()).isPresentAs(address);
        final Cart cartWithoutAddress = execute(new CartUpdateCommand(cartWithAddress, SetShippingAddress.of(Optional.empty())));
        assertThat(cartWithoutAddress.getShippingAddress()).isAbsent();
    }

    @Test
    public void setBillingAddressUpdateAction() throws Exception {
        final Cart cart = createCartWithCountrySomeHow();
        assertThat(cart.getBillingAddress()).isAbsent();
        final Address address = AddressBuilder.of(DE).build();
        final Cart cartWithAddress = execute(new CartUpdateCommand(cart, SetBillingAddress.of(address)));
        assertThat(cartWithAddress.getBillingAddress()).isPresentAs(address);
        final Cart cartWithoutAddress = execute(new CartUpdateCommand(cartWithAddress, SetBillingAddress.of(Optional.empty())));
        assertThat(cartWithoutAddress.getBillingAddress()).isAbsent();
    }

    @Test
    public void setCountryUpdateAction() throws Exception {
        final Cart cart = createCartWithoutCountrySomeHow();
        assertThat(cart.getCountry()).isAbsent();
        final Cart cartWithCountry = execute(new CartUpdateCommand(cart, SetCountry.of(DE)));
        assertThat(cartWithCountry.getCountry()).isPresentAs(DE);
        final Cart cartWithoutCountry = execute(new CartUpdateCommand(cartWithCountry, SetCountry.of(Optional.empty())));
        assertThat(cartWithoutCountry.getCountry()).isAbsent();
    }

    @Test
    public void setCustomShippingMethodUpdateAction() throws Exception {
        withTaxCategory(client(), taxCategory -> {
            final Cart cart = createCartWithShippingAddressSomeHow();
            assertThat(cart.getShippingInfo()).isAbsent();
            final MonetaryAmount price = MoneyImpl.of(new BigDecimal("23.50"), EUR);
            final ShippingRate shippingRate = ShippingRate.of(price);
            final String shippingMethodName = "custom-shipping";
            final SetCustomShippingMethod action = SetCustomShippingMethod.of(shippingMethodName, shippingRate, taxCategory);
            final Cart cartWithShippingMethod = execute(new CartUpdateCommand(cart, action));
            final CartShippingInfo shippingInfo = cartWithShippingMethod.getShippingInfo().get();
            assertThat(shippingInfo.getPrice()).isEqualTo(price);
            assertThat(shippingInfo.getShippingMethod()).isAbsent();
            assertThat(shippingInfo.getShippingMethodName()).isEqualTo(shippingMethodName);
            assertThat(shippingInfo.getShippingRate()).isEqualTo(shippingRate);
            assertThat(shippingInfo.getTaxCategory()).isEqualTo(taxCategory.toReference());
            assertThat(shippingInfo.getTaxRate()).isNotNull();
        });
    }

    private Cart createCartWithShippingAddressSomeHow() {
        final Cart cart = createCart(CartDraft.of(EUR).withCountry(DE));
        return execute(new CartUpdateCommand(cart, SetShippingAddress.of(AddressBuilder.of(DE).build())));
    }

    private CustomLineItemDraft createCustomLineItemDraft(final TaxCategory taxCategory) {
        final MonetaryAmount money = MoneyImpl.of(new BigDecimal("23.50"), EUR);
        return CustomLineItemDraft.of(en("thing"), "thing-slug", money, taxCategory, 5);
    }

    private void withEmptyCartAndProduct(final BiConsumer<Cart, Product> f) {
        withTaxedProduct(client(), product -> {
            final Cart cart = createCartWithCountrySomeHow();
            f.accept(cart, product);
        });
    }

    private Cart createCartWithoutCountrySomeHow() {
        return createCart(CartDraft.of(EUR));
    }

    private Cart createCartWithCountrySomeHow() {
        return createCart(CartDraft.of(EUR).withCountry(DE));
    }

    private Cart createCart(final CartDraft cartDraft) {
        return execute(new CartCreateCommand(cartDraft));
    }
}
