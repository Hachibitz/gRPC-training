package be.com.intro.grpc.client.metadata;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

import java.util.concurrent.Executor;

public class UserSessionToken extends CallCredentials {

    private String jwt;

    public UserSessionToken(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        appExecutor.execute(() -> {
            Metadata metadata = new Metadata();
            metadata.put(ClientConstants.USER_TOKEN, this.jwt);
            applier.apply(metadata);
            //applier.fail(Status.UNAUTHENTICATED);
        });
    }

    @Override
    public void thisUsesUnstableApi() {
        //may change in future
    }
}
