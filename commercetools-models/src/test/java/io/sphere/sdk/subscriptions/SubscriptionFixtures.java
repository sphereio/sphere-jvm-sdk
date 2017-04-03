package io.sphere.sdk.subscriptions;

import io.sphere.sdk.categories.Category;
import io.sphere.sdk.categories.messages.CategoryCreatedMessage;
import io.sphere.sdk.client.BlockingSphereClient;
import io.sphere.sdk.subscriptions.commands.SubscriptionCreateCommand;
import io.sphere.sdk.subscriptions.commands.SubscriptionDeleteCommand;

import java.net.URI;
import java.util.Collections;
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

    private final static String CTP_AWS_REGION = "CTP_AWS_REGION";

    private final static String CTP_AWS_SQS_QUEUE_URL = "CTP_AWS_SQS_QUEUE_URL";

    private final static String CTP_AWS_SNS_TOPIC_ARN = "CTP_AWS_SNS_TOPIC_ARN";

    public static SubscriptionDraftBuilder ironMqSubscriptionDraftBuilder() {
        final String ironMqUriEnv = System.getenv(CTP_IRON_MQ_URI_ENV);
        assumeNotNull(ironMqUriEnv);

        final URI ironMqUri = URI.create(ironMqUriEnv);
        return SubscriptionDraftBuilder.of(IronMqDestination.of(ironMqUri))
                .key(IRON_MQ_SUBSCRIPTION_KEY);
    }

    public static SubscriptionDraftBuilder sqsSubscriptionDraftBuilder() {
        assumeTrue(AwsCredentials.hasAwsCliEnv());
        final AwsCredentials awsCredentials = AwsCredentials.ofAwsCliEnv();
        final String awsRegion = System.getenv(CTP_AWS_REGION);
        final String sqsQueueUrl = System.getenv(CTP_AWS_SQS_QUEUE_URL);
        assumeNotNull(awsRegion);

        return SubscriptionDraftBuilder.of(SqsDestination.of(awsCredentials, awsRegion, URI.create(sqsQueueUrl)))
                .key(AWS_SQS_SUBSCRIPTION_KEY);
    }

    public static SubscriptionDraftBuilder snsSubscriptionDraftBuilder() {
        assumeTrue(AwsCredentials.hasAwsCliEnv());
        final AwsCredentials awsCredentials = AwsCredentials.ofAwsCliEnv();
        final String snsTopicArn = System.getenv(CTP_AWS_SNS_TOPIC_ARN);
        assumeNotNull(snsTopicArn);

        return SubscriptionDraftBuilder.of(SnsDestination.of(awsCredentials, snsTopicArn))
                .key(AWS_SQS_SUBSCRIPTION_KEY);
    }

    public static SubscriptionDraftBuilder withCategoryChanges(final SubscriptionDraftBuilder subscriptionDraftBuilder) {
        return subscriptionDraftBuilder.changes(Collections.singletonList(ChangeSubscription.of(Category.class)));
    }

    public static SubscriptionDraftBuilder withCategoryCreatedMessage(final SubscriptionDraftBuilder subscriptionDraftBuilder) {
        return subscriptionDraftBuilder.messages(Collections.singletonList(MessageSubscription.of(Category.class, CategoryCreatedMessage.class)));
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
}
