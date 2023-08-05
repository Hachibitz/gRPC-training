package br.com.intro.grpc.server.deadline;

import br.com.intro.grpc.server.rpctypes.TransferService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(7565)
                .addService(new DeadlineService())
                .build();

        server.start();
        server.awaitTermination();
    }
}
