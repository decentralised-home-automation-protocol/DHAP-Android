package me.aidengaripoli.dhap.joining.callbacks;

public interface SendCredentialsCallbacks extends BaseJoiningCallbacks {
    void credentialsAcknowledged();

    void sendCredentialsTimeout();
}
