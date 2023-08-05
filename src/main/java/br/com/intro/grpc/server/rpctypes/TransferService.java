package br.com.intro.grpc.server.rpctypes;

import br.com.intro.grpc.model.TransferRequest;
import br.com.intro.grpc.model.TransferResponse;
import br.com.intro.grpc.model.TransferServiceGrpc;
import io.grpc.stub.StreamObserver;

public class TransferService extends TransferServiceGrpc.TransferServiceImplBase {
    @Override
    public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
        return new TransferStreamingRequest(responseObserver);
    }
}
