package org.openjfx.server;

import java.io.IOException;
import java.sql.SQLException;

public class ServerApp {
    private static final int PORT = 8189;

    public static void main(String[] args) {
        int port = PORT;
        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }
        try {
            new MyServer(port);
        } catch (IOException | SQLException | ClassNotFoundException e) {
            System.out.println("Ошибка");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
