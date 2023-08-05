package br.com.intro.grpc.server.metadata;

import io.grpc.*;

import java.util.Objects;

public class AuthInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String clientToken = headers.get(ServerConstants.USER_TOKEN);
        System.out.println(headers);

        if(validateToken(clientToken)){
            UserRole userRole = this.getUserRole(clientToken);
            Context context = Context.current().withValue(ServerConstants.CONTEXT_USER_ROLE, userRole);
            return Contexts.interceptCall(context, call, headers, next);
            //return next.startCall(call, headers);
        }else{
            Status status = Status.UNAUTHENTICATED.withDescription("Invalid/expired token");
            call.close(status, headers);
        }

        //grpc documentation tells not to pass null as return statement, that's the because for this dummy call
        return new ServerCall.Listener<ReqT>() {
        };
    }

    public boolean validateToken(String token) {
        return Objects.nonNull(token) && (token.startsWith("user-secret-2") || token.startsWith("user-secret-3"));
    }

    public UserRole getUserRole(String jwt){
        return jwt.endsWith("prime") ? UserRole.PRIME : UserRole.STANDARD;
    }
}
