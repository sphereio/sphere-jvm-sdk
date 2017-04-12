package io.sphere.sdk.subscriptions;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.type.TypeReference;
import io.sphere.sdk.categories.Category;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.subscriptions.commands.SubscriptionCreateCommand;
import io.sphere.sdk.subscriptions.commands.SubscriptionDeleteCommand;
import io.sphere.sdk.subscriptions.queries.SubscriptionQuery;
import io.sphere.sdk.test.IntegrationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static io.sphere.sdk.categories.CategoryFixtures.withCategory;
import static io.sphere.sdk.subscriptions.SubscriptionFixtures.sqsSubscriptionDraftBuilder;
import static io.sphere.sdk.subscriptions.SubscriptionFixtures.withCategoryCreatedMessage;
import static io.sphere.sdk.test.SphereTestUtils.assertEventually;
import static io.sphere.sdk.test.SphereTestUtils.randomInt;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link MessageSubscription} and {@link SqsDestination}.
 */
public class MessageSubscriptionSqsIntegrationTest extends IntegrationTest {
    private static Subscription subscription;
    private static AmazonSQS sqsClient;
    private static String queueUrl;

    @Test
    public void categoryCreated() throws Exception {
        withCategory(client(), category -> {
            assertEventually(() -> {
                final ReceiveMessageResult result = sqsClient.receiveMessage(queueUrl);

                assertThat(result).isNotNull();
                final List<Message> sqsMessages = result.getMessages();
                assertThat(sqsMessages).hasSize(1);

                final Message sqsMessage = sqsMessages.get(0);
                sqsClient.deleteMessage(queueUrl, sqsMessage.getReceiptHandle());

                final MessageSubscriptionPayload<Category> messageSubscriptionPayload =
                        SphereJsonUtils.readObject(sqsMessage.getBody(), new TypeReference<MessageSubscriptionPayload<Category>>() {});
                assertThat(messageSubscriptionPayload).isNotNull();
                final Reference resource = messageSubscriptionPayload.getResource();
                assertThat(resource).isNotNull();
                assertThat(resource.getTypeId()).isEqualTo(Category.referenceTypeId());
            });
        });
    }

    @BeforeClass
    public static void setup() throws Exception {
        initSqs();

        List<Subscription> results = client().executeBlocking(SubscriptionQuery.of()
                .withPredicates(l -> l.key().is(SubscriptionFixtures.AWS_SQS_SUBSCRIPTION_KEY)))
                .getResults();
        results.forEach(subscription -> client().executeBlocking(SubscriptionDeleteCommand.of(subscription)));

        final SubscriptionDraftDsl subscriptionDraft = withCategoryCreatedMessage(sqsSubscriptionDraftBuilder(queueUrl)).build();

        final SubscriptionCreateCommand createCommand = SubscriptionCreateCommand.of(subscriptionDraft);
        subscription = client().executeBlocking(createCommand);

        assertThat(subscription).isNotNull();

        waitForSubscriptionTestMessage();
    }

    private static void initSqs() {
        sqsClient = AmazonSQSClientBuilder.defaultClient();

        final String queueName = "jvm-sdk-test-queue-" + randomInt();
        final CreateQueueResult queueCreationResult = sqsClient.createQueue(queueName);

        queueUrl = queueCreationResult.getQueueUrl();
    }

    /**
     * Waits for the test message.
     */
    public static void waitForSubscriptionTestMessage() {
        assertEventually(() -> {
            final ReceiveMessageResult result = sqsClient.receiveMessage(queueUrl);
            assertThat(result).isNotNull();
            final List<Message> sqsMessages = result.getMessages();

            for (final Message sqsMessage : sqsMessages) {
                final ResourceCreatedPayload<Subscription> resourceCreatedPayload =
                        SphereJsonUtils.readObject(sqsMessage.getBody(), new TypeReference<ResourceCreatedPayload<Subscription>>() {});
                assertThat(resourceCreatedPayload).isNotNull();
                final Reference resource = resourceCreatedPayload.getResource();
                assertThat(resource).isNotNull();
                assertThat(resource.getTypeId()).isEqualTo(Subscription.referenceTypeId());

                sqsClient.deleteMessage(queueUrl, sqsMessage.getReceiptHandle());
            }
        });
    }

    @AfterClass
    public static void clean() throws Exception {
        final SubscriptionDeleteCommand deleteCommand = SubscriptionDeleteCommand.of(subscription);
        client().executeBlocking(deleteCommand);

        sqsClient.deleteQueue(queueUrl);

        sqsClient.shutdown();
    }
}
