package com.bitdubai.smartwallet.platform.layer._7_network_service;

/**
 * Created by ciencias on 20.01.15.
 */
public interface NetworkSubsystem {
    public void start () throws CantStartSubsystemException;
    public NetworkService getNetworkService();
}
