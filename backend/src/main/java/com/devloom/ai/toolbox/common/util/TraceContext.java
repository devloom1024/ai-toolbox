package com.devloom.ai.toolbox.common.util;

import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TraceContext {

    private static Tracer tracer;

    @Autowired
    public void setTracer(@Autowired(required = false) Tracer tracer) {
        TraceContext.tracer = tracer;
    }

    public static String getTraceId() {
        if (tracer == null) {
            return null;
        }
        var span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        var traceContext = span.context();
        if (traceContext == null) {
            return null;
        }
        return traceContext.traceId();
    }
}
