package com.pollinate.ordermanagement.exception;

import java.util.List;

public class InvalidOrderException extends RuntimeException {

    private final List<Long> missingProductIds;

    public InvalidOrderException(String message) {
        super(message);
        this.missingProductIds = List.of();
    }

    public InvalidOrderException(String message, List<Long> missingProductIds) {
        super(message);
        this.missingProductIds = missingProductIds;
    }

    public List<Long> getMissingProductIds() {
        return missingProductIds;
    }
}
