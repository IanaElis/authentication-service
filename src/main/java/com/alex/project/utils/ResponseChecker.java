package com.alex.project.utils;

import com.alex.project.controllers.ChatControllerImpl;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;

public class ResponseChecker {
    public static void ensureOk(Response response, String errorMessage, Logger log) {
        if (response == null || response.getStatus() != Response.Status.OK.getStatusCode()) {
            log.error(errorMessage);
            throw new BadRequestException(errorMessage);
        }
    }
}
