pragma solidity ^0.4.0;

import "./ChannelLibrary.sol";

contract ChannelContract {
    using ChannelLibrary for ChannelLibrary.Data;
    ChannelLibrary.Data data;

    event ChannelNewBalance(address token_address, address participant, uint balance, uint block_number);
    event ChannelClosed(address closing_address, uint block_number);
    event TransferUpdated(address node_address, uint block_number);
    event ChannelSettled(uint block_number);
    event ChannelSecretRevealed(bytes32 secret, address receiver_address);

    modifier settleTimeoutNotTooLow(uint t) {
        require(t >= 6);
        _;
    }

    function ChannelContract(
        address token_address,
        address sender,
        address receiver,
        uint timeout)
        settleTimeoutNotTooLow(timeout)
    {
        require (sender != receiver);

        data.sender = sender;
        data.receiver = receiver;

        data.token = StandardToken(token_address);
        data.settle_timeout = timeout;
        data.opened = block.number;
    }

    /// @notice Caller makes a deposit into their channel balance.
    /// @param amount The amount caller wants to deposit.
    /// @return True if deposit is successful.
    function deposit(uint256 amount) returns (bool) {
        bool success;
        uint256 balance;

        (success, balance) = data.deposit(amount);

        if (success == true) {
            ChannelNewBalance(data.token, msg.sender, balance, 0);
        }

        return success;
    }

    /// @notice Get the address and balance of both partners in a channel.
    /// @return The address and balance pairs.
    function addressAndBalance()
        constant
        returns (
        address sender,
        address receiver,
        uint balance)
    {
        sender = data.sender;
        receiver = data.receiver;
        balance = data.balance;
    }

    /// @notice Close the channel. Can only be called by a participant in the channel.
    /// @param theirs_encoded The last transfer recieved from our partner.
//    function close(bytes theirs_encoded) {
//        data.close(theirs_encoded);
//        ChannelClosed(msg.sender, data.closed);
//    }

    /// @notice Dispute the state after closing, called by the counterparty (the
    ///         participant who did not close the channel).
    /// @param theirs_encoded The transfer the counterparty believes is the valid
    ///                       state of the first participant.
//    function updateTransfer(bytes theirs_encoded) {
//        data.updateTransfer(theirs_encoded);
//        TransferUpdated(msg.sender, block.number);
//    }

    /// @notice Settle the transfers and balances of the channel and pay out to
    ///         each participant. Can only be called after the channel is closed
    ///         and only after the number of blocks in the settlement timeout
    ///         have passed.
//    function settle() {
//        data.settle();
//        ChannelSettled(data.settled);
//    }

    /// @notice Returns whole state of contract as single call
    function state() constant returns (
        uint,
        uint,
        uint,
        uint,
        address,
        address,
        address,
        address,
        uint256,
        uint256,
        uint256
    ) {
        return (data.settle_timeout,
            data.opened,
            data.closed,
            data.settled,
            data.closing_address,
            data.token,
            data.sender,
            data.receiver,
            data.balance,
            data.sender_update.completed_transfers,
            data.receiver_update.completed_transfers
        );
    }

    /// @notice Returns the number of blocks until the settlement timeout.
    /// @return The number of blocks until the settlement timeout.
    function settleTimeout() constant returns (uint) {
        return data.settle_timeout;
    }

    /// @notice Returns the address of the token.
    /// @return The address of the token.
    function tokenAddress() constant returns (address) {
        return data.token;
    }

    /// @notice Returns the block number for when the channel was opened.
    /// @return The block number for when the channel was opened.
    function opened() constant returns (uint) {
        return data.opened;
    }

    /// @notice Returns the block number for when the channel was closed.
    /// @return The block number for when the channel was closed.
    function closed() constant returns (uint) {
        return data.closed;
    }

    /// @notice Returns the block number for when the channel was settled.
    /// @return The block number for when the channel was settled.
    function settled() constant returns (uint) {
        return data.settled;
    }

    /// @notice Returns the address of the closing participant.
    /// @return The address of the closing participant.
    function closingAddress() constant returns (address) {
        return data.closing_address;
    }

    function () { revert(); }
}
