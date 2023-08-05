package br.com.intro.grpc.server.rpctypes;

import br.com.intro.grpc.model.Account;
import br.com.intro.grpc.model.TransferRequest;
import br.com.intro.grpc.model.TransferResponse;
import br.com.intro.grpc.model.TransferStatus;
import io.grpc.stub.StreamObserver;

public class TransferStreamingRequest implements StreamObserver<TransferRequest> {

    private StreamObserver<TransferResponse> transferResponseStreamObserver;

    public TransferStreamingRequest(StreamObserver<TransferResponse> transferResponseStreamObserver) {
        this.transferResponseStreamObserver = transferResponseStreamObserver;
    }

    @Override
    public void onNext(TransferRequest transferRequest) {
        int fromAccount = transferRequest.getFromAccount();
        int toAccount = transferRequest.getToAccount();
        int amount = transferRequest.getAmount();
        int balance = AccountDatabase.getBalance(fromAccount);
        TransferStatus status = TransferStatus.FAILED;

        if(balance >= amount && fromAccount != toAccount){
            AccountDatabase.deductBalance(fromAccount, amount);
            AccountDatabase.addBalance(toAccount, amount);
            status = TransferStatus.SUCCESS;
        }

        Account fromAccountInfo = Account.newBuilder()
                .setAccountNumber(fromAccount)
                .setAmount(AccountDatabase.getBalance(fromAccount))
                .build();

        Account toAccountInfo = Account.newBuilder()
                .setAccountNumber(toAccount)
                .setAmount(AccountDatabase.getBalance(toAccount))
                .build();

        TransferResponse transferResponse = TransferResponse.newBuilder()
                .setTransferStatus(status)
                .addAccounts(fromAccountInfo)
                .addAccounts(toAccountInfo)
                .build();

        this.transferResponseStreamObserver.onNext(transferResponse);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Erro na requisição");
    }

    @Override
    public void onCompleted() {
        AccountDatabase.printAccountDetails();
        this.transferResponseStreamObserver.onCompleted();
    }
}
