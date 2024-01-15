package nl.tudelft.sem.template.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

public class WireMockConfig {
    static final int USERS_PORT = 8081;

    static final int ORDERS_PORT = 8082;

    public static WireMockServer usersMicroservice;

    public static WireMockServer ordersMicroservice;

    /**
     * Starts the WireMock user server.
     */
    public static void startUserServer() {
        if (usersMicroservice == null || !usersMicroservice.isRunning()) {
            usersMicroservice = new WireMockServer(WireMockConfiguration.options().port(USERS_PORT));
            usersMicroservice.start();
        }
    }

    /**
     * Stops the WireMock user server if it is running.
     */
    public static void stopUserServer() {
        if (usersMicroservice != null && usersMicroservice.isRunning()) {
            usersMicroservice.stop();
        }
    }

    /**
     * Starts the WireMock orders server.
     */
    public static void startOrdersServer() {
        if (ordersMicroservice == null || !ordersMicroservice.isRunning()) {
            ordersMicroservice = new WireMockServer(WireMockConfiguration.options().port(ORDERS_PORT));
            ordersMicroservice.start();
        }
    }

    /**
     * Stops the WireMock orders server if it is running.
     */
    public static void stopOrdersServer() {
        if (ordersMicroservice != null && ordersMicroservice.isRunning()) {
            ordersMicroservice.stop();
        }
    }
}
