package io.tomahawkd.blockchain.smartcontract;

public enum AssetStatus {
    /**
     * seller is checking the package
     */
    CONFIRMING,
    /**
     * transfer package to another org
     */
    TRANSFERRING,
    /**
     * when a new package created,
     * or transferring status complete,
     * or a discard has been agreed for both
     */
    APPROVED,
    /**
     * occurs while undo the transferring procedure
     */
    DISCARDING,
    /**
     * invalid if the package status is not match
     */
    INVALID,
    /**
     * forbidding if the target is not the owner
     */
    FORBIDDEN,
    /**
     * package is not available
     */
    NOT_AVAILABLE;
}
