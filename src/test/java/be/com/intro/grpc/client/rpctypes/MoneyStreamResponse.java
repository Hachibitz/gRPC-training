package be.com.intro.grpc.client.rpctypes;

import be.com.intro.grpc.client.metadata.ClientConstants;
import br.com.intro.grpc.model.Money;
import br.com.intro.grpc.model.WithdrawalError;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.CountDownLatch;

public class MoneyStreamResponse implements StreamObserver<Money> {

    private CountDownLatch countDownLatch;

    public MoneyStreamResponse(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void onNext(Money money) {
        System.out.println("Received async: " + money.getValue());
    }

    @Override
    public void onError(Throwable throwable) {
        //Status status = Status.fromThrowable(throwable);
        Metadata metadata = Status.trailersFromThrowable(throwable);
        WithdrawalError withdrawalError = metadata.get(ClientConstants.WITHDRAWAL_ERROR_KEY);

        System.out.println(withdrawalError.getAmount() + ":" + withdrawalError.getErrorMessage());
        countDownLatch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("Done processing!");
        countDownLatch.countDown();
    }
}
