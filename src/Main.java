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
    String serverAddress = "127.0.0.1";

    int serverPort = 14445;
//    String serverAddress = "103.170.121.55";
//
//    int serverPort = 11223;
    List<Account> accounts = new ArrayList<>();

    //        String serverAddress = "ipkyuccz.ngocrongkyuc.com";
    //        int serverPort = 11223;
    public static void main(String[] args) throws SQLException, InterruptedException {
        SQLManager.create(Setting.DB_HOST, 3306, Setting.DB_DATE, Setting.DB_USER, Setting.DB_PASS);
        Main m = new Main();
        //m.loadAccounts();
        //m.loginAllZombies();
        m.loginZombie("tuantruong", "truongtn1");
    }

    void loginAllZombies() throws InterruptedException {
        for (Account a : accounts){
            Thread thread = new Thread(() -> loginZombie(a.username,a.password));
            thread.start();
            Thread.sleep(300);
        }
    }


    void loginZombie(String username, String pass) {
        try {
//            if (username.equals("admin")||username.equals("tuantruong")){
//                return;
//            }
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

//            msg = new Message(-40);
//            msg.writer().writeByte(1);
//            msg.writer().writeByte(0);
//            msg.writer().flush();
//            session.sendMessage(msg);
/*            Thread.sleep(20000);
            msg = new Message(-86);//gd
            msg.writer().writeByte(1);//accept
            msg.writer().writeInt(1007279);
            msg.writer().flush();
            session.sendMessage(msg);
            System.out.println("done acp");

            //add item
            Thread.sleep(2000);
            msg = new Message(-86);
            msg.writer().writeByte(2);//add item
            msg.writer().writeByte(0);//item index
            msg.writer().writeInt(500);//item quantity
            msg.writer().flush();
            session.sendMessage(msg);
            System.out.println("done add item");
            Thread.sleep(2000);


            //lock item
            Thread.sleep(2000);
            msg = new Message(-86);
            msg.writer().writeByte(5);//lock item
            msg.writer().flush();
            session.sendMessage(msg);

            //cat item index
            Thread.sleep(1000);
            msg = new Message(-40);
            msg.writer().writeByte(1);
            msg.writer().writeByte(0);
            msg.writer().flush();
            session.sendMessage(msg);

            //accept  trade
            Thread.sleep(10000);
            msg = new Message(-86);
            msg.writer().writeByte(7);//accept trade
            msg.writer().flush();
            session.sendMessage(msg);*/

            //open ba hat mit
            Thread.sleep(1000);
            msg = new Message(56);
            msg.writer().writeShort(21);
            msg.writer().flush();
            session.sendMessage(msg);

            //select pha le hoa
            Thread.sleep(1000);
            msg = new Message(32);
            msg.writer().writeShort(56);//npc id
            msg.writer().writeByte(3);//select thu 4
            msg.writer().flush();
            session.sendMessage(msg);

//            //select bang ngoc
//            Thread.sleep(1000);
//            msg = new Message(32);
//            msg.writer().writeShort(21);//npc id
//            msg.writer().writeByte(0);//select o dau tien
//            msg.writer().flush();
//            session.sendMessage(msg);

            //select item
            Thread.sleep(1000);
            msg = new Message(-81);
            msg.writer().writeByte(1);//action
            msg.writer().writeByte(1);//so luong item
            msg.writer().writeByte(0);//index item, quan kaio
            msg.writer().flush();
            session.sendMessage(msg);



            //cat item index
            Thread.sleep(1000);
            msg = new Message(-40);
            msg.writer().writeByte(1);
            msg.writer().writeByte(0);
            msg.writer().flush();
            session.sendMessage(msg);

            //upgrade item
            Thread.sleep(1000);
            msg = new Message(32);
            msg.writer().writeShort(5);//npc id
            msg.writer().writeByte(0);//select x100
            msg.writer().flush();
            session.sendMessage(msg);

            Util.log("done all");


           while (true) {
               if (true){
                   return;
               }
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
        rs = conn.createStatement().executeQuery("SELECT `username`,`password` FROM `account`  LIMIT 1500");
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