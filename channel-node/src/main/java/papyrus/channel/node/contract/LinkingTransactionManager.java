package papyrus.channel.node.contract;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.datatypes.Address;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.TransactionManager;

import papyrus.channel.node.config.EthRpcProperties;

public class LinkingTransactionManager extends TransactionManager {
    private static final Logger log = LoggerFactory.getLogger(LinkingTransactionManager.class);

    private final Map<String, Address> predeployed = new HashMap<>();
    private final TransactionManager manager;
    private final Web3j web3j;

    public LinkingTransactionManager(Web3j web3j, EthRpcProperties rpcProperties, Credentials credentials) {
        super(web3j, rpcProperties.getAttempts(), (int) rpcProperties.getSleep().toMillis());
        this.web3j = web3j;
        this.manager = new FastRawTransactionManager(web3j, credentials);
    }

    public void provide(String name, Address contractAddress) {
        log.info("Provided contract {} -> {}", name, contractAddress);
        predeployed.put(name, contractAddress);
    }

    @Override
    public EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) throws IOException {
        if (to == null) {
            //this is contract creation
            ContractLinker contractLinker = new ContractLinker(data);
            Set<String> libraries = contractLinker.getLibraries();
            if (!libraries.isEmpty()) {
                for (String library : libraries) {
                    Address address = predeployed.get(library);
                    if (address == null) {
                        throw new IllegalStateException("Library address not provided: " + library);
                    }
                    log.debug("Linking {} -> {}", library, address);
                    contractLinker.link(library, address);
                }
                data = contractLinker.getBinary();
            }
        }
        return manager.sendTransaction(gasPrice, gasLimit, to, data, value);
    }

    @Override
    public String getFromAddress() {
        return manager.getFromAddress();
    }

    public Web3j getWeb3j() {
        return web3j;
    }
}
