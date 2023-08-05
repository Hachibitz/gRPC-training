package be.com.intro.grpc.client.deadline;

import be.com.intro.grpc.client.rpctypes.BalanceStreamObserver;
import be.com.intro.grpc.client.rpctypes.MoneyStreamResponse;
import br.com.intro.grpc.model.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.CountDownLatch;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeadlineClientTest {

    private BankServiceGrpc.BankServiceBlockingStub bankServiceBlockingStub;
    private BankServiceGrpc.BankServiceStub bankServiceStub;

    @BeforeAll
    public void setup(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 7565)
                .intercept(new DeadlineInterceptor())
                .usePlaintext()
                .build();

        this.bankServiceBlockingStub = BankServiceGrpc.newBlockingStub(channel);
        this.bankServiceStub = BankServiceGrpc.newStub(channel);
    }

    @Test
    public void balanceTest(){
        BalanceCheckRequest balanceCheckRequest = BalanceCheckRequest.newBuilder()
                .setAccountNumber(7)
                .build();

        try{
            Balance balance = this.bankServiceBlockingStub
                    //.withDeadline(Deadline.after(2, TimeUnit.SECONDS))
                    .getBalance(balanceCheckRequest);
        }catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void withDrawTest(){
        WithDrawRequest withDrawRequest = WithDrawRequest.newBuilder()
                .setAccountNumber(7)
                .setAmount(40)
                .build();

        try{
            this.bankServiceBlockingStub
                    //.withDeadline(Deadline.after(4, TimeUnit.SECONDS))
                    .withDraw(withDrawRequest)
                    .forEachRemaining(money -> System.out.println("Received: "+ money.getValue()));
        }catch (StatusRuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    public void withDrawAsyncTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        WithDrawRequest withDrawRequest = WithDrawRequest.newBuilder()
                .setAccountNumber(10)
                .setAmount(50)
                .build();
        MoneyStreamResponse streamObserver = new MoneyStreamResponse(latch);

        this.bankServiceStub.withDraw(withDrawRequest, streamObserver);
        latch.await();
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
