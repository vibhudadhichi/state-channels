package papyrus.channel.node.server.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Address;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import papyrus.channel.node.AddParticipantRequest;
import papyrus.channel.node.AddParticipantResponse;
import papyrus.channel.node.ChannelAdminGrpc;
import papyrus.channel.node.CloseChannelRequest;
import papyrus.channel.node.CloseChannelResponse;
import papyrus.channel.node.RemoveParticipantRequest;
import papyrus.channel.node.RemoveParticipantResponse;
import papyrus.channel.node.server.channel.incoming.IncomingChannelManager;
import papyrus.channel.node.server.channel.outgoing.OutgoingChannelManager;
import papyrus.channel.node.server.channel.outgoing.OutgoingChannelProperties;

//TODO need to authenticate client
@Component
public class ChannelAdminImpl extends ChannelAdminGrpc.ChannelAdminImplBase {
    private static final Logger log = LoggerFactory.getLogger(ChannelAdminImpl.class);
    public static final int MAX_CHANNELS_PER_ADDRESS = 100;
    
    private OutgoingChannelManager outgoingChannelManager;
    private IncomingChannelManager incomingChannelManager;

    public ChannelAdminImpl(OutgoingChannelManager outgoingChannelManager, IncomingChannelManager incomingChannelManager) {
        this.outgoingChannelManager = outgoingChannelManager;
        this.incomingChannelManager = incomingChannelManager;
    }

    @Override
    public void addParticipant(AddParticipantRequest request, StreamObserver<AddParticipantResponse> responseObserver) {
        log.info("Add participant {} with channels: {}-{}", request.getParticipantAddress(), request.getMinActiveChannels(), request.getMaxActiveChannels());
        if (request.getMinActiveChannels() < 0 || request.getMinActiveChannels() > MAX_CHANNELS_PER_ADDRESS) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(String.format("Illegal min active channels: %d", request.getMinActiveChannels())).asException());
            return;
        }
        if (request.getMaxActiveChannels() < 0 || request.getMaxActiveChannels() > MAX_CHANNELS_PER_ADDRESS) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(String.format("Illegal min active channels: %d", request.getMaxActiveChannels())).asException());
            return;
        }
        
        OutgoingChannelProperties properties = new OutgoingChannelProperties(request);
        
        if (properties.getBlockchainProperties().getSettleTimeout() < 6) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(String.format("Illegal settle timeout: %d", properties.getBlockchainProperties().getSettleTimeout())).asException());
            return;
        }
        
        if (properties.getDeposit().signum() <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(String.format("Illegal deposit: %d", properties.getDeposit())).asException());
            return;
        }
        
        outgoingChannelManager.addParticipant(new Address(request.getParticipantAddress()), properties
        );
        
        responseObserver.onNext(AddParticipantResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeParticipant(RemoveParticipantRequest request, StreamObserver<RemoveParticipantResponse> responseObserver) {
        outgoingChannelManager.removeParticipant(new Address(request.getParticipantAddress()));
        responseObserver.onNext(RemoveParticipantResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void requestCloseChannel(CloseChannelRequest request, StreamObserver<CloseChannelResponse> responseObserver) {
        incomingChannelManager.requestCloseChannel(new Address(request.getChannelAddress()));
        outgoingChannelManager.requestCloseChannel(new Address(request.getChannelAddress()));
        responseObserver.onNext(CloseChannelResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
