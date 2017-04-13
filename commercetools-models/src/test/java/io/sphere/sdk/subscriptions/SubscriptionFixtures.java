package io.sphere.sdk.subscriptions;

import io.sphere.sdk.categories.Category;
import io.sphere.sdk.categories.messages.CategoryCreatedMessage;
import io.sphere.sdk.client.BlockingSphereClient;
import io.sphere.sdk.products.Product;
import io.sphere.sdk.subscriptions.commands.SubscriptionCreateCommand;
import io.sphere.sdk.subscriptions.commands.SubscriptionDeleteCommand;
import io.sphere.sdk.subscriptions.queries.SubscriptionQuery;
import org.junit.Assume;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

/**
 * Test fixtures for {@link Subscription} tests.
 */
public class SubscriptionFixtures {
    public final static String IRON_MQ_SUBSCRIPTION_KEY = "iron-mq-subscription-integration-test";
    public final static String AWS_SQS_SUBSCRIPTION_KEY = "aws-sqs-subscription-integration-test";

    /**
     * We will run an IronMQ test if this environment variable is set to an IronMQ URI.
     */
    private final static String CTP_IRON_MQ_URI_ENV = "CTP_IRON_MQ_URI";

    private final static String AWS_REGION = "AWS_REGION";

    public static SubscriptionDraftBuilder ironMqSubscriptionDraftBuilder() {
        final String ironMqUriFromEnv = ironMqUriFromEnv();

        final URI ironMqUri = URI.create(ironMqUriFromEnv);
        return SubscriptionDraftBuilder.of(IronMqDestination.of(ironMqUri))
                .key(IRON_MQ_SUBSCRIPTION_KEY);
    }

    /**
     * Checks if the environment variables required for IronMQ tests are set.
     *
     * @see Assume#assumeTrue(boolean)
     */
    public static void assumeHasIronMqEnv() {
        final String ironMqUri = ironMqUriFromEnv();

        assumeNotNull(ironMqUri);
    }

    /**
     * Returns the iron mq uri to run tests against.
     *
     * @see #CTP_IRON_MQ_URI_ENV
     *
     * @return the IronMQ uri or null
     */
    public static String ironMqUriFromEnv() {
        final String ironMqUriEnv = System.getenv(CTP_IRON_MQ_URI_ENV);
        return ironMqUriEnv;
    }

    public static SubscriptionDraftBuilder sqsSubscriptionDraftBuilder(final String queueUrl) {
        final AwsCredentials awsCredentials = AwsCredentials.ofAwsCliEnv();
        final String awsRegion = System.getenv(AWS_REGION);
        assumeNotNull(awsRegion);

        return SubscriptionDraftBuilder.of(SqsDestination.of(awsCredentials, awsRegion, URI.create(queueUrl)))
                .key(AWS_SQS_SUBSCRIPTION_KEY);
    }

    /**
     * Checks if the environment variable required for AWS tests are set.
     *
     * @see Assume#assumeTrue(boolean)
     * @see AwsCredentials#hasAwsCliEnv()
     */
    public static void assumeHasAwsCliEnv() {
        assumeTrue(AwsCredentials.hasAwsCliEnv());
    }

    public static SubscriptionDraftBuilder snsSubscriptionDraftBuilder(final String topicArn) {
        final AwsCredentials awsCredentials = AwsCredentials.ofAwsCliEnv();

        return SubscriptionDraftBuilder.of(SnsDestination.of(awsCredentials, topicArn))
                .key(AWS_SQS_SUBSCRIPTION_KEY);
    }

    public static SubscriptionDraftBuilder withCategoryChanges(final SubscriptionDraftBuilder subscriptionDraftBuilder) {
        return subscriptionDraftBuilder.changes(Collections.singletonList(ChangeSubscription.of(Category.resourceTypeId())));
    }

    public static SubscriptionDraftBuilder withProductChanges(final SubscriptionDraftBuilder subscriptionDraftBuilder) {
        return subscriptionDraftBuilder.changes(Collections.singletonList(ChangeSubscription.of(Product.resourceTypeId())));
    }

    public static SubscriptionDraftBuilder withCategoryCreatedMessage(final SubscriptionDraftBuilder subscriptionDraftBuilder) {
        return subscriptionDraftBuilder.messages(Collections.singletonList(MessageSubscription.of(Category.resourceTypeId(), CategoryCreatedMessage.MESSAGE_TYPE)));
    }

    public static void withSubscription(final BlockingSphereClient client, final SubscriptionDraftBuilder builder, final Function<Subscription, Subscription> f) {
        final SubscriptionDraftDsl subscriptionDraft = builder.build();
        final Subscription subscription = client.executeBlocking(SubscriptionCreateCommand.of(subscriptionDraft));
        final Subscription possiblyUpdatedSubscription = f.apply(subscription);
        client.executeBlocking(SubscriptionDeleteCommand.of(possiblyUpdatedSubscription));
    }

    public static Subscription createSubscription(final BlockingSphereClient client, final SubscriptionDraftBuilder builder) {
        final SubscriptionDraftDsl subscriptionDraftDsl = builder.build();
        return client.executeBlocking(SubscriptionCreateCommand.of(subscriptionDraftDsl));
    }

    public static void deleteSubscription(final BlockingSphereClient client, final String subscriptionKey) {
        List<Subscription> results = client.executeBlocking(SubscriptionQuery.of()
                .withPredicates(l -> l.key().is(subscriptionKey)))
                .getResults();
        results.forEach(subscription -> client.executeBlocking(SubscriptionDeleteCommand.of(subscription)));
    }
}
