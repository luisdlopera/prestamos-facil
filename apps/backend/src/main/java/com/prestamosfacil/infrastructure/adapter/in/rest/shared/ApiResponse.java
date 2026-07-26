package com.prestamosfacil.infrastructure.shared.rest;

import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.infrastructure.adapter.in.rest.configuration.SwaggerDocs;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = SwaggerDocs.API_RESPONSE)
public class ApiResponse<T> {

    @Schema(description = SwaggerDocs.RESPONSE_OK, example = SwaggerDocs.EX_TRUE)
    private final boolean ok;

    @Schema(description = SwaggerDocs.RESPONSE_DATA)
    private final T data;

    @Schema(description = SwaggerDocs.RESPONSE_MESSAGE, example = SwaggerDocs.EX_MSG_SUCCESS)
    private final String message;

    @Schema(description = SwaggerDocs.RESPONSE_TIMESTAMP)
    private final Instant timestamp;

    @Schema(description = SwaggerDocs.RESPONSE_ERRORS)
    private final List<ErrorDetail> errors;

    protected ApiResponse(boolean ok, T data, String message, List<ErrorDetail> errors) {
        this.ok = ok;
        this.data = data;
        this.message = message;
        this.timestamp = Instant.now();
        this.errors = errors;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, Messages.SUCCESS_OPERATION.getValue(), null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    public static <T> ApiResponse<T> success(T data, Messages message) {
        return new ApiResponse<>(true, data, message.getValue(), null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, null);
    }

    public static <T> ApiResponse<T> error(Messages message) {
        return new ApiResponse<>(false, null, message.getValue(), null);
    }

    public static <T> ApiResponse<T> error(String message, List<ErrorDetail> errors) {
        return new ApiResponse<>(false, null, message, errors);
    }

    public static <T> ApiResponse<T> error(Messages message, List<ErrorDetail> errors) {
        return new ApiResponse<>(false, null, message.getValue(), errors);
    }

    public boolean isOk() { return ok; }
    public T getData() { return data; }
    public String getMessage() { return message; }
    public Instant getTimestamp() { return timestamp; }
    public List<ErrorDetail> getErrors() { return errors; }
}
