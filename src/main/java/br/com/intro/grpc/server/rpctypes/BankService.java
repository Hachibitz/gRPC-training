package br.com.intro.grpc.server.rpctypes;

import br.com.intro.grpc.model.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class BankService extends BankServiceGrpc.BankServiceImplBase {

    //unary request
    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        //super.getBalance(request, responseObserver);
        int accountNumber = request.getAccountNumber();
        Balance balance = Balance.newBuilder()
                .setAmount(AccountDatabase.getBalance(accountNumber))
                .build();

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
            Money money = Money.newBuilder()
                    .setValue(10)
                    .build();
            responseObserver.onNext(money);
            AccountDatabase.deductBalance(accountNumber, 10);
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
