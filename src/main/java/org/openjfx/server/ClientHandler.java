package org.openjfx.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class ClientHandler {
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            new Thread(() -> {
                try {
                    sendLogins();
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    //  e.printStackTrace();
                } finally {
                    closeConnection();
                }

            }).start();

        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");
        }
    }

    public void authentication() throws IOException {
        socket.setSoTimeout(MyServer.SO_TIMEOUT_AUTH);
        while (true) {
            String str = null;
            try {
                str = in.readUTF();
            } catch (IOException e) {
                throw new IOException();
            }
            System.out.println(str);
            if (str.startsWith(MyServer.CMD_PREF_AUTH)) {
                String[] parts = str.split("\\s");
                String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {
                        sendMsg(MyServer.CMD_PREF_AUTHOK + " " + nick);
                        name = nick;
                        sendConnectedNicks();
                        myServer.subscribe(this);
                        myServer.broadcastMsg(MyServer.CMD_PREF_NICK + " " + name);
                        return;
                    } else {
                        sendMsg("Учетная запись уже используется");
                    }
                } else {
                    sendMsg("Неверные логин/пароль");
                }
            }
        }
    }

    public void sendConnectedNicks() {
        ArrayList<String> listNicks = myServer.getConnectedNicks();
        for (String nick : listNicks) {
            sendMsg(MyServer.CMD_PREF_NICKLIST + " " + nick);
        }
    }

    public void sendLogins() {
        ArrayList<String> listLogins = myServer.getAuthService().getLogins();
        for (String login : listLogins) {
            sendMsg(login);
        }
        sendMsg(MyServer.CMD_PREF_LOGINEND);
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        myServer.unsubscribe(this);
        if (!name.isBlank())
            myServer.broadcastMsg(MyServer.CMD_PREF_NICKEND + " " + name);
        System.out.println("Клиент " + name + " отключился");
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMessages() throws IOException {
        socket.setSoTimeout(MyServer.SO_TIMEOUT_INACTIVITY);
        while (true) {
            String strFromClient = null;
            try {
                strFromClient = in.readUTF();
            } catch (IOException e) {
                throw new IOException();
            }
            System.out.println("от " + name + ": " + strFromClient);
            if (strFromClient.equals(MyServer.CMD_PREF_END)) {
                return;
            }
            if (strFromClient.startsWith(MyServer.CMD_PREF_CHANGENICK)) {
                String[] parts = strFromClient.split("\\s");
                String newNick = parts[2];
                String newNickAuth = myServer.getAuthService().changeNick(name, newNick);
                if (newNickAuth != null) {
                    name = newNickAuth;
                    myServer.broadcastMsg(strFromClient);
                } else {
                    sendMsg("Ник " + newNick + " уже используется, попробуйте другой.");
                }
            } else {
                if (strFromClient.startsWith(MyServer.CMD_PREF_INDIVID))
                    myServer.individualMsg(name, strFromClient);
                else
                    myServer.broadcastMsg(name + ": " + strFromClient);
            }
        }
    }
}
