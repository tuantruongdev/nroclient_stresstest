import server.SQLManager;
import server.Util;
import service.Setting;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class Main {
//    String serverAddress = "127.0.0.1";
//
//    int serverPort = 14445;
    String serverAddress = "ipkyuccz.ngocrongkyuc.com";

    int serverPort = 11224;
    List<Account> accounts = new ArrayList<>();

    //        String serverAddress = "ipkyuccz.ngocrongkyuc.com";
    //        int serverPort = 11223;
    public static void main(String[] args) throws SQLException, InterruptedException {
        SQLManager.create(Setting.DB_HOST, 3306, Setting.DB_DATE, Setting.DB_USER, Setting.DB_PASS);
        Main m = new Main();
        m.loadAccounts();
        m.loginAllZombies();

        // m.loginZombie("tuantruong", "truongtn1");

    }

    void loginAllZombies() throws InterruptedException {
        for (Account a : accounts){
            Thread thread = new Thread(() -> loginZombie(a.username,a.password));
            thread.start();
            Thread.sleep(400);
        }
    }


    void loginZombie(String username, String pass) {
        try {
            if (username.equals("admin")){
                return;
            }
            Socket socket = new Socket(serverAddress, serverPort);
            Session session = new Session();
            session.add(socket, new IMessageHandler() {
                @Override
                public void onMessage(Session ss, Message msg) {
                    //System.out.println("onMsg");
                }

                @Override
                public void onConnectOK() {
                    System.out.println("connect oK");
                }

                @Override
                public void onConnectionFail() {
                    System.out.println("connect fail");
                }

                @Override
                public void onDisconnected(Session ss) {
                    System.out.println("disconnect");
                }
            });
            session.sendSessionKey();
            Message msg = new Message(-29);
            msg.writer().writeByte(0);
            msg.writer().writeUTF(username.toLowerCase());
            msg.writer().writeUTF(pass.toLowerCase());
            msg.writer().writeUTF("2.2.5".toLowerCase());
            msg.writer().writeByte(0);
            msg.writer().flush();
            session.sendMessage(msg);
            //done load map
            msg = new Message(-28);
            msg.writer().writeByte(13);
            msg.writer().flush();
            session.sendMessage(msg);
            int i = 0;
            while (true) {
                i++;
                msg = new Message(44);
                byte[] array = new byte[15];
                new Random().nextBytes(array);
                String generatedString = new String(array, Charset.forName("UTF-8"));
                msg.writer().writeUTF("auto msg " + generatedString + i);
                msg.writer().flush();
                session.sendMessage(msg);
                Thread.sleep(4000);
                Random random = new Random();
                msg = new Message(-7);
                msg.writer().writeByte(0);
                msg.writer().writeShort(random.nextInt(500, 1000));
                msg.writer().writeShort(random.nextInt(300, 700));
                msg.writer().flush();
                session.sendMessage(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void loadAccounts() throws SQLException {
        Connection conn = SQLManager.getConnection2(Setting.DB_NICK);
        ResultSet rs = null;
        rs = conn.createStatement().executeQuery("SELECT `username`,`password` FROM `account`  LIMIT 800");
        if (rs != null) {
            while (rs.next()) {
                String username = rs.getString("username");
                String pass = rs.getString("password");
                accounts.add(new Account(username,pass));
            }
        }
        Util.log("loaded account size " +accounts.size());
    }

    class Account {
        public String username;
        public String password;

        Account(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

}