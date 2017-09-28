package io.sphere.sdk.queries;

import io.sphere.sdk.client.SphereClient;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

final class QueryAllImpl<T, C extends QueryDsl<T, C>> {
    private final QueryDsl<T, C> baseQuery;
    private final long pageSize;

    private QueryAllImpl(final QueryDsl<T, C> baseQuery, final long pageSize) {
        this.baseQuery = !baseQuery.sort().isEmpty() ? baseQuery : baseQuery.withSort(QuerySort.of("id asc"));
        this.pageSize = pageSize;
    }

    public CompletionStage<List<T>> run(final SphereClient client) {
        return run(client, entry -> entry);
    }

    /**
     * Given a {@code callback} {@link Function}, this method applies this callback on each page of the results from
     * {@link this} instance's {@code baseQuery} and returns a future containing a list containing the results of this
     * callback on each page.
     *
     * @param client        the CTP client that the query is run on.
     * @param resultsMapper the callback that gets called on each page of results.
     * @param <S>           the type of the result of the callback on each page.
     * @return a future containing a list of results of the callback on each page.
     */
    @Nonnull
    <S> CompletionStage<List<S>> run(final SphereClient client, final Function<T, S> resultsMapper) {
        return queryPage(client, 0).thenCompose(result -> {
            final Stream<S> firstStream = result.getResults().stream().map(resultsMapper);
            final Stream<CompletionStage<Stream<S>>> nextPagesCallbackResults = queryNextPages(client, result.getTotal(), resultsMapper);

            return streamOfStagesToStageOfStream(nextPagesCallbackResults)
                    .thenApply(nextStream -> concat(firstStream, nextStream)
                            .collect(toList()));
        });
    }

    /**
     * Given a {@link Consumer}, this method applies this consumer on each page of the results from
     * {@link this} instance's {@code baseQuery}.
     *
     * @param client          the CTP client that the query is run on.
     * @param resultsConsumer the consumer that gets called on each page of results.
     * @return an empty future.
     */
    @Nonnull
    CompletionStage<Void> run(final SphereClient client, final Consumer<T> resultsConsumer) {
        return queryPage(client, 0).thenAccept(result -> {
            result.getResults().forEach(resultsConsumer);
            queryNextPages(client, result.getTotal(), resultsConsumer)
                    //.parallel()
                    .forEach(stageStreamS -> stageStreamS.toCompletableFuture().join());
        });
    }

    /**
     * Given a callback {@link Function}, this method first calculates the total number of pages resulting from the
     * query, then it applies this callback on each page of the results from {@link this} instance's {@code baseQuery}
     * and returns a list containing the result of this callback on each page.
     *
     * @param client        the CTP client that the query is run on.
     * @param totalElements the total number of elements resulting from the query.
     * @param resultsMapper the callback to apply on each page of results.
     * @return a list of futures containing the results of applying the callback on each page of results.
     */
    @Nonnull
    private <S> Stream<CompletionStage<Stream<S>>> queryNextPages(final SphereClient client, final long totalElements,
                                                          final Function<T, S> resultsMapper) {
        final long totalPages = getTotalNumberOfPages(totalElements);
        return LongStream.range(1, totalPages)
                         .mapToObj(page -> queryPage(client, page)
                             .thenApply(results -> results.getResults().stream().map(resultsMapper)));
    }

    /**
     * Given a {@link Consumer}, this method first calculates the total numbr of pages resulting from the query, then
     * it applies this consumer on each page of the results from {@link this} instance's {@code baseQuery}
     * and returns a list of futures after applying of this consumer on each page.
     *
     * @param client        the CTP client that the query is run on.
     * @param totalElements the total number of elements resulting from the query.
     * @param consumer      the consumer to apply on each page of results.
     * @return a list of futures of applying the consumer on each page of results.
     */
    @Nonnull
    private Stream<CompletionStage<Void>> queryNextPages(final SphereClient client, final long totalElements,
                                                         final Consumer<T> consumer) {
        final long totalPages = getTotalNumberOfPages(totalElements);
        return LongStream.range(1, totalPages)
                .mapToObj(page -> queryPage(client, page)
                        .thenAccept(results -> results.getResults().forEach(consumer)));
    }

    /**
     * Given a total number of elements {@code totalElements} this method calculates the number of pages needed that
     * would be needed to cover all the elements with set {@code pageSize} of {@link this} instance.
     *
     * @param totalElements number of elements to get the number of pages for.
     * @return the total number of pages.
     */
    long getTotalNumberOfPages(final long totalElements) {
        return (long) Math.ceil((double) totalElements / pageSize);
    }

    /**
     * Gets the results of {@link this} instance's query for a specific page with {@code pageNumber}.
     *
     * @param client     the CTP client that the query is run on.
     * @param pageNumber the page number to get the results for.
     * @return a future containing the results of the requested page of applying the query.
     */
    @Nonnull
    private CompletionStage<PagedQueryResult<T>> queryPage(final SphereClient client, final long pageNumber) {
        final QueryDsl<T, C> query = baseQuery
                .withOffset(pageNumber * pageSize)
                .withLimit(pageSize);
        return client.execute(query);
    }

    /**
     * Given a list of futures, this method converts it to a future containing a list of results of these futures after
     * executing them in parallel with {@link CompletableFuture#allOf(CompletableFuture[])}.
     *
     * @param futures list of futures.
     * @param <S>     the type of the results of the futures.
     * @return a future containing a list of the results of the input futures.
     */
    @Nonnull
    private <S> CompletionStage<Stream<S>> streamOfStagesToStageOfStream(
            final Stream<CompletionStage<Stream<S>>> futures) {

        return supplyAsync(() -> futures
                //.parallel()
                .flatMap(stageStreamS -> stageStreamS.toCompletableFuture().join()));
    }

    @Nonnull
    static <T, C extends QueryDsl<T, C>> QueryAllImpl<T, C> of(@Nonnull final QueryDsl<T, C> baseQuery, final int pageSize) {
        return new QueryAllImpl<>(baseQuery, pageSize);
    }
}
