package br.com.intro.grpc.server.deadline;

import br.com.intro.grpc.model.*;
import br.com.intro.grpc.server.rpctypes.AccountDatabase;
import br.com.intro.grpc.server.rpctypes.CashStreamingRequest;
import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class DeadlineService extends BankServiceGrpc.BankServiceImplBase {

    //unary request
    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        //super.getBalance(request, responseObserver);
        int accountNumber = request.getAccountNumber();
        Balance balance = Balance.newBuilder()
                .setAmount(AccountDatabase.getBalance(accountNumber))
                .build();

        //simulate time-consuming call
        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
        responseObserver.onNext(balance);
        responseObserver.onCompleted();
    }

    //streaming request
    @Override
    public void withDraw(WithDrawRequest request, StreamObserver<Money> responseObserver) {
        //super.withDraw(request, responseObserver);
        int accountNumber = request.getAccountNumber();
        int amount = request.getAmount(); //10 /20 /30 ...
        int balance = AccountDatabase.getBalance(accountNumber);

        if(balance < amount) {
            Status status = Status.FAILED_PRECONDITION.withDescription("Sem saldo suficiente. Saldo: " + balance);
            responseObserver.onError(status.asRuntimeException());
            return;
        }

        for(int i = 0; i < amount/10; i++){
            Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
            Money money = Money.newBuilder()
                    .setValue(10)
                    .build();

            if(!Context.current().isCancelled()) {
                responseObserver.onNext(money);
                System.out.println("Received: "+money.getValue());
                AccountDatabase.deductBalance(accountNumber, 10);
            }else{
                break;
            }

            System.out.println("Complete");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {
        return new CashStreamingRequest(responseObserver);
    }
}
