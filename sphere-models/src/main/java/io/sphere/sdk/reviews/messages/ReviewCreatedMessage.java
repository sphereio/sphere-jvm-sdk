package io.sphere.sdk.reviews.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.sphere.sdk.messages.GenericMessageImpl;
import io.sphere.sdk.messages.Message;
import io.sphere.sdk.messages.MessageDerivateHint;
import io.sphere.sdk.reviews.Review;

import java.time.ZonedDateTime;

@JsonDeserialize(as = ReviewCreatedMessage.class)//important to override annotation in Message class
public class ReviewCreatedMessage extends GenericMessageImpl<Review> {
    public static final String MESSAGE_TYPE = "ReviewCreated";
    public static final MessageDerivateHint<ReviewCreatedMessage> MESSAGE_HINT =
            MessageDerivateHint.ofSingleMessageType(MESSAGE_TYPE, ReviewCreatedMessage.class);

    private final Review review;

    @JsonCreator
    private ReviewCreatedMessage(final String id, final Long version, final ZonedDateTime createdAt, final ZonedDateTime lastModifiedAt, final JsonNode resource, final Long sequenceNumber, final Long resourceVersion, final String type, final Review review) {
        super(id, version, createdAt, lastModifiedAt, resource, sequenceNumber, resourceVersion, type, Review.class);
        this.review = review;
    }

    /**
     * Gets the review object at creation time. This output can differ from an expanded {@link Message#getResource()}.
     *
     * @return the review at creation time
     */
    public Review getReview() {
        return review;
    }
}
