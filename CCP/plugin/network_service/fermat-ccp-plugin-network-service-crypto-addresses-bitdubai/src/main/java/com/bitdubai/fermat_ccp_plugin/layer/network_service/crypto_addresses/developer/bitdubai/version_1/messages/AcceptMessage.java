package com.bitdubai.fermat_ccp_plugin.layer.network_service.crypto_addresses.developer.bitdubai.version_1.messages;

import com.bitdubai.fermat_api.layer.all_definition.money.CryptoAddress;
import com.bitdubai.fermat_ccp_plugin.layer.network_service.crypto_addresses.developer.bitdubai.version_1.enums.MessageTypes;

import java.util.UUID;

/**
 * The class <code>com.bitdubai.fermat_ccp_plugin.layer.network_service.crypto_addresses.developer.bitdubai.version_1.messages.AcceptMessage</code>
 * contains the structure of a accept message for this plugin.
 * <p/>
 * Created by Leon Acosta - (laion.cj91@gmail.com) on 17/10/2015.
 */
public class AcceptMessage extends NetworkServiceMessage {

    private final CryptoAddress cryptoAddress;

    public AcceptMessage(final UUID          requestId    ,
                         final CryptoAddress cryptoAddress,
                         String identitySender,
                         String actorDestination) {

        super(requestId,MessageTypes.ACCEPT,identitySender,actorDestination);

        this.cryptoAddress = cryptoAddress;
    }

    public CryptoAddress getCryptoAddress() {
        return cryptoAddress;
    }

    @Override
    public String toString() {
        return "AcceptMessage{" +
                "requestId=" + getRequestId() +
                ", cryptoAddress=" + cryptoAddress +
                '}';
    }

}
