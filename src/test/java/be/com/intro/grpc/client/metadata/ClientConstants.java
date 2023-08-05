package be.com.intro.grpc.client.metadata;

import br.com.intro.grpc.model.WithdrawalError;
import io.grpc.Metadata;
import io.grpc.protobuf.ProtoUtils;

public class ClientConstants {

    public static final Metadata.Key<WithdrawalError> WITHDRAWAL_ERROR_KEY = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
    public static final Metadata.Key<String> USER_TOKEN = Metadata.Key.of("user-token", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata METADATA = new Metadata();

    static {
        METADATA.put(
                Metadata.Key.of("client-token", Metadata.ASCII_STRING_MARSHALLER),
                "bank-client-secret"
        );
    }

    public static Metadata getClientToken(){
        return METADATA;
    }
}
