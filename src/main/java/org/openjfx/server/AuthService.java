package org.openjfx.server;

import java.sql.SQLException;
import java.util.ArrayList;

public interface AuthService {
    void start() throws SQLException, ClassNotFoundException;

    String getNickByLoginPass(String login, String pass);

    String changeNick(String oldNick, String newNick);

    void stop();

    ArrayList<String> getLogins();

}

