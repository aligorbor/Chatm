package org.openjfx.server;

import java.sql.*;
import java.util.ArrayList;

public class BaseAuthService implements AuthService {
    private final String strDriver;
    private final String strUrl;
    private Connection connection;

    public BaseAuthService() {
        strDriver = "org.sqlite.JDBC";
        strUrl = "jdbc:sqlite:src/main/resources/mainDb.sqlite";
    }

    public BaseAuthService(String strDriver, String strUrl) {
        this.strDriver = strDriver;
        this.strUrl = strUrl;
    }

    @Override
    public void start() throws SQLException, ClassNotFoundException {
        setConnection(true);
        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        String str = null;
        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement("SELECT nick FROM logins WHERE login = ? and pass = ?");
            pst.setString(1, login);
            pst.setString(2, pass);
            ResultSet rs = pst.executeQuery();
            rs.next();
            str = rs.getString(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return str;
    }

    @Override
    public String changeNick(String oldNick, String newNick) {
        String str = null;
        PreparedStatement pst = null;

        try {
            pst = connection.prepareStatement("SELECT COUNT (*) FROM logins WHERE nick = ?");
            pst.setString(1, newNick);
            ResultSet rs = pst.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                pst = connection.prepareStatement("update logins set nick=? where nick=?");
                pst.setString(1, newNick);
                pst.setString(2, oldNick);
                if (pst.executeUpdate() > 0) return newNick;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    @Override
    public void stop() {
        try {
            setConnection(false);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        System.out.println("Сервис аутентификации остановлен");
    }

    @Override
    public ArrayList<String> getLogins() {
        ArrayList<String> arrayList = new ArrayList<>();
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM logins");
            while (rs.next()) {
                arrayList.add(rs.getString(1));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return arrayList;
    }

    private void setConnection(boolean connect) throws ClassNotFoundException, SQLException {
        if (connect) {
            Class.forName(strDriver);
            connection = DriverManager.getConnection(strUrl);
        } else {
            if (connection != null)
                connection.close();
        }
    }

}
