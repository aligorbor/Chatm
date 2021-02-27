package org.openjfx.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MyServer {

    private List<ClientHandler> clients;
    private AuthService authService;
    private static final Logger logger = LogManager.getLogger((MyServer.class));

    public static final String CMD_PREF_AUTH = "/auth";
    public static final String CMD_PREF_AUTHOK = "/authok";
    public static final String CMD_PREF_NICK = "/nick";
    public static final String CMD_PREF_NICKLIST = "/nicklist";
    public static final String CMD_PREF_NICKEND = "/nickend";
    public static final String CMD_PREF_LOGINEND = "/loginend";
    public static final String CMD_PREF_END = "/end";
    public static final String CMD_PREF_INDIVID = "/w";
    public static final String CMD_PREF_INDIVIDBACK = "/wback";
    public static final String CMD_PREF_CHANGENICK = "/changenick";

    public static final int SO_TIMEOUT_AUTH = 120000;
    public static final int SO_TIMEOUT_INACTIVITY = 3600000;

    public AuthService getAuthService() {
        return authService;
    }

    public MyServer(int port) throws IOException, SQLException, ClassNotFoundException {
        readServerCommand();
        try (ServerSocket server = new ServerSocket(port)) {
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
//                System.out.println("Сервер ожидает подключения");
                logger.info("Сервер ожидает подключения");
                Socket socket = server.accept();
//                System.out.println("Клиент подключился");
                logger.info("Сервер ожидает подключения");
                new ClientHandler(this, socket);
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            //           System.out.println("Ошибка в работе сервера");
            logger.error("Ошибка в работе сервера");
            throw e;
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    public synchronized void individualMsg(String nameFrom, String msg) {
        //    /w nick2: bcvxncxvbnxcv
        String nameTo = msg.substring(MyServer.CMD_PREF_INDIVID.length() + 1, msg.indexOf(":"));
        for (ClientHandler o : clients) {
            if (o.getName().equals(nameTo)) {
                o.sendMsg(msg.replaceFirst(nameTo, nameFrom));
            }
            if (o.getName().equals(nameFrom)) {
                o.sendMsg(msg.replaceFirst(MyServer.CMD_PREF_INDIVID, MyServer.CMD_PREF_INDIVIDBACK));
            }
        }
    }

    public synchronized void unsubscribe(ClientHandler o) {
        clients.remove(o);
    }

    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
    }

    public synchronized ArrayList<String> getConnectedNicks() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (ClientHandler o : clients) {
            arrayList.add(o.getName());
        }
        return arrayList;
    }

    public void readServerCommand() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String str = scanner.nextLine();
                if (str.equalsIgnoreCase(MyServer.CMD_PREF_END)) {
                    authService.stop();
                    System.exit(0);
                    break;
                }
            }
        }).start();
    }
}
