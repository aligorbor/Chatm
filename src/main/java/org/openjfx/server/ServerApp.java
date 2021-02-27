package org.openjfx.server;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerApp {
    private static final int PORT = 8189;
    private static final Logger logger = LogManager.getLogger(ServerApp.class);

    public static void main(String[] args) {
        int port = PORT;
        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }
        try {
            new MyServer(port);
        } catch (IOException | SQLException | ClassNotFoundException e) {

            logger.error("Ошибка", e);
            System.exit(1);
        }
    }

}
