package fi.livi.digitraffic.tie.controller.handler;

import java.sql.Timestamp;

public class ErrorResponse {

    public final Timestamp timestamp;

    public final int status;

    public final String error;

    public final String message;

    public final String path;

    public ErrorResponse(Timestamp timestamp, final int status, final String error, final String message, final String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}