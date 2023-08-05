package br.com.intro.grpc.server.metadata;

import br.com.intro.grpc.model.*;
import br.com.intro.grpc.server.rpctypes.AccountDatabase;
import br.com.intro.grpc.server.rpctypes.CashStreamingRequest;
import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class MetadataService extends BankServiceGrpc.BankServiceImplBase {

    //unary request
    @Override
    public void getBalance(BalanceCheckRequest request, StreamObserver<Balance> responseObserver) {
        //super.getBalance(request, responseObserver);
        int accountNumber = request.getAccountNumber();
        int amount = AccountDatabase.getBalance(accountNumber);

        UserRole userRole = ServerConstants.CONTEXT_USER_ROLE.get();
        UserRole userRole1 = ServerConstants.CONTEXT_USER_ROLE1.get();
        amount = UserRole.PRIME.equals(userRole) ? amount : (amount - 15);

        System.out.println("userRole: "+userRole+"\n userRole1: "+userRole1);

        Balance balance = Balance.newBuilder()
                .setAmount(amount)
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

        if(amount < 10 || (amount % 10) != 0) {
            Metadata metadata = new Metadata();
            Metadata.Key<WithdrawalError> errorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
            WithdrawalError withdrawalError = WithdrawalError.newBuilder()
                    .setAmount(balance)
                    .setErrorMessage(ErrorMessage.ONLY_TEN_MULTIPLES)
                    .build();

            metadata.put(errorKey, withdrawalError);
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));
            return;
        }

        if(balance < amount) {
            Metadata metadata = new Metadata();
            Metadata.Key<WithdrawalError> errorKey = ProtoUtils.keyForProto(WithdrawalError.getDefaultInstance());
            WithdrawalError withdrawalError = WithdrawalError.newBuilder()
                    .setAmount(balance)
                    .setErrorMessage(ErrorMessage.INSUFFICIENT_FUNDS)
                    .build();

            metadata.put(errorKey, withdrawalError);
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException(metadata));
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
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<DepositRequest> cashDeposit(StreamObserver<Balance> responseObserver) {
        return new CashStreamingRequest(responseObserver);
    }
}
