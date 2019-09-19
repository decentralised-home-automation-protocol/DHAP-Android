package me.aidengaripoli.dhap.status;

public interface StatusLeaseCallbacks {

    void leaseResponse(float leaseLength, float updatePeriod);

    boolean shouldRenewStatusLease();
}
