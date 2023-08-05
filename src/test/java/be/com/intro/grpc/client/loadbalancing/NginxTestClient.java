package be.com.intro.grpc.client.loadbalancing;

import be.com.intro.grpc.client.rpctypes.BalanceStreamObserver;
import br.com.intro.grpc.model.Balance;
import br.com.intro.grpc.model.BalanceCheckRequest;
import br.com.intro.grpc.model.BankServiceGrpc;
import br.com.intro.grpc.model.DepositRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NginxTestClient {
    private BankServiceGrpc.BankServiceBlockingStub bankServiceBlockingStub;
    private BankServiceGrpc.BankServiceStub bankServiceStub;

    @BeforeAll
    public void setup(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8585)
                .usePlaintext()
                .build();

        this.bankServiceBlockingStub = BankServiceGrpc.newBlockingStub(channel);
        this.bankServiceStub = BankServiceGrpc.newStub(channel);
    }

    @Test
    public void balanceTest(){

        for (int i = 1; i < 11; i++) {
            BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(i)
                    .build();

            Balance balance = this.bankServiceBlockingStub.getBalance(balanceCheckRequest);
            System.out.println("Received: " + balance.getAmount());
        }
    }

    @Test
    public void cashStreamingRequest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<Balance> balanceStreamObserver = new BalanceStreamObserver(latch);
        StreamObserver<DepositRequest> depositRequestStreamObserver = this.bankServiceStub.cashDeposit(balanceStreamObserver);
        for (int i = 0; i < 10; i++) {
            DepositRequest depositRequest = DepositRequest.newBuilder()
                    .setAccount(7)
                    .setAmount(10)
                    .build();
            depositRequestStreamObserver.onNext(depositRequest);
        }
        depositRequestStreamObserver.onCompleted();
        latch.await();;
    }
}
