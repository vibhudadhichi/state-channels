package papyrus.channel.node.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@EnableConfigurationProperties(EthProperties.class)
@Configuration
public class EthereumConfig {
    private static final Logger log = LoggerFactory.getLogger(EthereumConfig.class);
    
    private final Credentials credentials;
    private final Web3j web3j;

    public EthereumConfig(EthProperties properties) throws IOException, CipherException {
        if (properties.getKeyLocation() != null) {
            credentials = WalletUtils.loadCredentials(properties.getKeyPassword(), properties.getKeyLocation());
        } else if (properties.getPrivateKey() != null) {
            credentials = Credentials.create(properties.getPrivateKey());
        } else {
            throw new IllegalStateException("Either node.eth.key-location or node.eth.private-key required");
        }
        log.info("Using address {} and rpc server {}", credentials.getAddress(), properties.getNodeUrl());
        web3j = Web3j.build(new HttpService(properties.getNodeUrl()));
    }

    @Bean
    public Web3j getWeb3j() {
        return web3j;
    }

    @Bean
    public Credentials getCredentials() {
        return credentials;
    }
}
