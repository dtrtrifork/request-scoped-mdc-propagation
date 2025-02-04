package org.saladito;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.Vertx;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.function.Supplier;

@Path("/mdc-test")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        Uni.createFrom()
                .item(() -> {
                            for (int count = 0; count < 5; count++) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                String workerThreadTraceId = getCurrentTraceId();

                                Log.info("worker thread. iteration " + count + ". traceId is " + workerThreadTraceId);
                            }
                            return getCurrentTraceId();
                        }
                )
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .subscribeAsCompletionStage();

        String traceId = getCurrentTraceId();
        Log.info("traceId in main thread is " + traceId);

        return "Hi, traceId is " + traceId;
    }

    String getCurrentTraceId() {
        Span currentSpan = Span.current();
        SpanContext spanContext = currentSpan.getSpanContext();

        if (spanContext.isValid()) {
            return spanContext.getTraceId();
        } else {
            return null;
        }
    }
}
