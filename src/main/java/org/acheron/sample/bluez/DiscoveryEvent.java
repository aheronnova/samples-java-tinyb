package org.acheron.sample.bluez;

public class DiscoveryEvent {

    private final String address;

    public DiscoveryEvent(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }
}
