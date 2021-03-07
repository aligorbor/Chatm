package org.openjfx.server;

import java.sql.SQLException;
import java.util.ArrayList;

public interface AuthService {
    void start() throws SQLException, ClassNotFoundException;

    String getNickByLoginPass(String login, String pass);

    String changeNick(String oldNick, String newNick);

    String registerNew(String login, String pass, String nick);

    void stop();

    ArrayList<String> getLogins();

}
