package org.acheron.sample.bluez;

import tinyb.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.acheron.sample.bluez.BufferUtils.dump;

public class Notification {

    private static boolean running = true;

    private final Map<String, Sensor> map = new HashMap<>();

    private Sensor register(BluetoothDevice device) {
        synchronized (map) {
            final String address = device.getAddress();

            if (map.containsKey(address)) {
                return map.get(address);
            }
            Sensor sensor = new Sensor(device);
            map.put(address, sensor);
            return sensor;
        }
    }

    private void update(List<BluetoothDevice> devices, DiscoveryEventListener listener) {
        for (BluetoothDevice device : devices) {
            final Sensor sensor = register(device);
            listener.onEvent(new DiscoveryEvent(sensor.getAddress()));
        }
    }

    private void discover(DiscoveryEventListener listener) throws InterruptedException {
        /*
         * To start looking of the device, we first must initialize the TinyB library. The way of interacting with the
         * library is through the BluetoothManager. There can be only one BluetoothManager at one time, and the
         * reference to it is obtained through the getBluetoothManager method.
         */
        final BluetoothManager manager = BluetoothManager.getBluetoothManager();

        for (BluetoothAdapter adapter : manager.getAdapters()) {
            System.out.printf("Adapter found: /%s/%s%n", adapter.getName(), adapter.getInterfaceName());
        }

        final List<BluetoothGattService> services = manager.getServices();
        if (services != null) {
            for (BluetoothGattService service : services) {
                final BluetoothDevice device = service.getDevice();
                System.out.println("Device found: " + device.getName());
                final List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                if (characteristics != null) {
                    for (BluetoothGattCharacteristic c : characteristics) {
                        System.out.printf("Characteristic %s: %s%n", c.getUUID(), dump(c.getValue()));
                    }
                }
            }
        }


        /*
         * The manager will try to initialize a BluetoothAdapter if any adapter is present in the system. To initialize
         * discovery we can call startDiscovery, which will put the default adapter in discovery mode.
         */
        boolean discoveryStarted = manager.startDiscovery();

        if (!discoveryStarted) {
            System.err.println("Failed to start discovery");
        }

        final CountDownLatch latch = new CountDownLatch(1);

        new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                update(manager.getDevices(), listener);
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    running = false;
                }
            }
            latch.countDown();
        }, "Master Thread").start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> running = false));

        latch.await();

        if (!manager.stopDiscovery()) {
            System.err.println("Failed to stop discovery");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final Notification notification = new Notification();
        notification.discover(Notification::onSensor);
    }

    private static void onSensor(DiscoveryEvent event) {
        System.out.println("Sensor found: " + event.getAddress());
    }
}
