package org.acheron.sample.bluez;

import tinyb.BluetoothDevice;

import java.io.Closeable;
import java.util.Map;

import static org.acheron.sample.bluez.BufferUtils.dump;

public class Sensor implements Closeable {

    private final BluetoothDevice device;

    public Sensor(BluetoothDevice device) {
        this.device = device;
//        this.device.enableConnectedNotifications(this::onConnected);
//        this.device.enableServiceDataNotifications(this::enableServiceDataNotifications);
//        this.device.enableServicesResolvedNotifications(this::enableServicesResolvedNotifications);
        this.device.enableManufacturerDataNotifications(this::enableManufacturerDataNotifications);
        printDevice(this.device);
    }

    public String getAddress() {
        return device.getAddress();
    }

    private void printDevice(BluetoothDevice device) {
        System.out.print("Address = " + device.getAddress());
        System.out.print(" Name = " + device.getName());
        System.out.print(" Connected = " + device.getConnected());
        System.out.println();
        enableManufacturerDataNotifications(device.getManufacturerData());
        enableServiceDataNotifications(device.getServiceData());
    }

    private void onConnected(Boolean connected) {
        System.out.println("Connecting to " + device.getAddress() + ": " + connected);
    }

    private void enableServiceDataNotifications(Map<String, byte[]> stringMap) {
        if (stringMap == null) {
            return;
        }
        for (Map.Entry<String, byte[]> entry : stringMap.entrySet()) {
            System.out.println("Service data for " + device.getAddress() + ": " + entry.getKey() + dump(entry.getValue()));
        }
    }

    private void enableServicesResolvedNotifications(Boolean aBoolean) {
        System.out.println("Service resolved for " + device.getAddress() + ": " + aBoolean);
    }

    private void enableManufacturerDataNotifications(Map<Short, byte[]> stringMap) {
        if (stringMap == null) {
            return;
        }
        for (Map.Entry<Short, byte[]> entry : stringMap.entrySet()) {
            System.out.println("Manufacturer data for " + device.getAddress() + ": " + entry.getKey() + dump(entry.getValue()));
        }
    }


    @Override
    public void close() {
        device.disableConnectedNotifications();
        device.disableManufacturerDataNotifications();
        device.disableServiceDataNotifications();
        device.disableServicesResolvedNotifications();
    }
}
