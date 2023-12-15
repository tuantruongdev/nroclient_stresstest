import server.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;



public class Session implements ISession {

    public boolean isLogin, isCreateChar, isResource, isRead;

    public int countDie;

    public boolean sendKeyComplete, connected;

    //private static byte[] keys = "GENKAI".getBytes();
    private static  byte []keys = new byte[]{9, 71, 46, 27, 30, 7, 30, 27, 89, 2};
    private byte curR, curW;

    protected Socket socket;

    protected DataInputStream dis;
    protected DataOutputStream dos;
    public Sender sender;
    private MessageCollector collector;

    IMessageHandler messageHandler;

    public int userId = -1, active = -1;
    public String account;
    public String pass;
    public byte typeClient, zoom;
    public short version = 199;
    public String ipAddress;
    public int SocketID;

    public void add(Socket socket, IMessageHandler handler) {
        try {
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.sender = new Sender();
            this.collector = new MessageCollector();
            this.socket = socket;
            this.SocketID = socket.getPort();
            this.ipAddress = socket.getInetAddress().getHostAddress();
            this.messageHandler = handler;
            this.collector.active();
            connected = true;
        } catch (Exception e) {
            System.out.println("add(Socket socket, IMessageHandler handler)");
            e.printStackTrace();
        }
    }

    public void onRecieveMsg(Message message) {
        messageHandler.onMessage(this, message);
    }


    @Override
    public void remove_point(int a) {

    }

    @Override
    public long get_point() {
        return 0;
    }

    @Override
    public int get_active() {
        return active;
    }

    @Override
    public int get_act() {
        return 0;
    }

    @Override
    public void update_active() {

    }


    @Override
    public void sendMessage(Message msg) {
        sender.AddMessage(msg);
    }

    @Override
    public int get_zoom() {
        return zoom;
    }

    @Override
    public short get_version() {
        return version;
    }

    @Override
    public int get_gold() {
        return 0;
    }

    @Override
    public int get_money() {
        return 0;
    }

    @Override
    public int get_vnd() {
        return 0;
    }

    @Override
    public void remove_money(int quantity) {

    }

    @Override
    public void remove_gold(int quantity) {

    }

    @Override
    public void set_version(short v) {
        version = v;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public String get_client_account() {
        return this.account;
    }

    @Override
    public String get_client_pass() {
        return this.pass;
    }

    @Override
    public byte get_type_client() {
        return typeClient;
    }

    @Override
    public void set_type_client(byte t) {
        typeClient = t;
    }

    @Override
    public int get_user_id() {
        return userId;
    }

    @Override
    public void set_user_id(int u) {
        userId = u;
    }


    class Sender {

        Vector<Message> sendingMessage = new Vector<>();

        public void AddMessage(Message message) {
            sendingMessage.addElement(message);
        }

        public void removeAllMessage() {
            if (sendingMessage != null) {
                sendingMessage.removeAllElements();
            }
        }

        public Timer timer;
        public TimerTask task;
        public boolean actived = false;

        public void close() {
            try {
                actived = false;
                task.cancel();
                timer.cancel();
                task = null;
                timer = null;
            } catch (Exception e) {
                task = null;
                timer = null;
            }
        }

        public void active() {
            if (!actived) {
                actived = true;
                this.timer = new Timer("Sender ");
                task = new TimerTask() {
                    @Override
                    public void run() {
                        Sender.this.update();
                    }
                };
                this.timer.schedule(task, 5, 5);
            }
        }

        public void update() {
            try {
                if (!sendingMessage.isEmpty() && connected) {
                    Message message = (Message) sendingMessage.elementAt(0);
                    sendingMessage.removeElementAt(0);
                    doSendMessage(message);
                    message.cleanup();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MessageCollector {

        public Timer timer;
        public TimerTask task;
        public boolean actived = false;

        public void close() {
            try {
                actived = false;
                task.cancel();
                timer.cancel();
                task = null;
                timer = null;
            } catch (Exception e) {
                task = null;
                timer = null;
            }
        }

        public void active() {
            if (!actived) {
                actived = true;
                this.timer = new Timer("messagecollecotr ");
                task = new TimerTask() {
                    @Override
                    public void run() {
                        MessageCollector.this.update();
                    }
                };
                this.timer.schedule(task, 5, 5);
            }
        }

        public void update() {
            try {
                Message msg = readMessage();
                if (msg != null) {
                    onRecieveMsg(msg);
                    msg.cleanup();
                } else {
                    Session.this.disconnect();
                }
            } catch (Exception e) {
                Session.this.disconnect();
            }
        }

        private Message readMessage() {
            try {
                if (dis != null) {
                    byte cmd;
                    cmd = dis.readByte();
                    if (sendKeyComplete) {
                        cmd = readKey(cmd);
                    }
                    int size;
                    if (sendKeyComplete) {
                        final byte b1 = dis.readByte();
                        final byte b2 = dis.readByte();
                        size = (readKey(b1) & 255) << 8 | readKey(b2) & 255;
                    } else {
                        size = dis.readUnsignedShort();
                    }
                    final byte data[] = new byte[size];
                    int len = 0;
                    int byteRead = 0;
                    while (len != -1 && byteRead < size) {
                        len = dis.read(data, byteRead, size - byteRead);
                        if (len > 0) {
                            byteRead += len;
                        }
                    }
                    if (sendKeyComplete) {
                        for (int i = 0; i < data.length; i++) {
                            data[i] = readKey(data[i]);
                        }
                    }
                    return new Message(cmd, data);
                }
            } catch (IOException ex) {
//                Util.logException(Session.class, ex);
            }
            return null;
        }
    }

//    public boolean checkSizeIP() {
//        @SuppressWarnings("unchecked")
//        List<Session> ss = new ArrayList();
//        for (int i = 0; i < ServerManager.Sessions.size(); i++) {
//            Session session = ServerManager.Sessions.get(i);
//            if (session!=null && session.ipAddress.equals(this.ipAddress)) {
//                ss.add(session);
//            }
//        }
//        return ss.size() > Setting.MAX_IP;
//    }

    public void close() {
        userId = -1;
        active = -1;
        account = null;
        pass = null;
    }

    public synchronized void doSendMessage(Message msg) {
        try {
            final byte[] data = msg.getData();
            if (sendKeyComplete) {
                byte b = writeKey(msg.getCommand());
                dos.writeByte(b);
            } else {
                dos.writeByte(msg.getCommand());
            }
            if (data != null) {
                int size = data.length;
                if (msg.getCommand() == -32 || msg.getCommand() == -66 || msg.getCommand() == -74 || msg.getCommand() == 11 || msg.getCommand() == -67 || msg.getCommand() == -87 || msg.getCommand() == 66) {
                    final byte b = writeKey((byte) (size));
                    dos.writeByte(b - 128);
                    final byte b2 = writeKey((byte) (size >> 8));
                    dos.writeByte(b2 - 128);
                    final byte b3 = writeKey((byte) (size >> 16));
                    dos.writeByte(b3 - 128);
                } else if (sendKeyComplete) {
                    final int byte1 = writeKey((byte) (size >> 8));
                    dos.writeByte(byte1);
                    final int byte2 = writeKey((byte) (size & 255));
                    dos.writeByte(byte2);
                } else {
                    dos.writeShort(size);
                }
                if (sendKeyComplete) {
                    for (int i = 0; i < data.length; i++) {
                        data[i] = writeKey(data[i]);
                    }
                }
                dos.write(data);
            } else {
                dos.writeShort(0);
            }
            dos.flush();
        } catch (Exception e) {

        }
    }

    private byte writeKey(byte b) {
        final byte i = (byte) ((keys[curW++] & 255) ^ (b & 255));
        if (curW >= keys.length) {
            curW %= keys.length;
        }
        return i;
    }

    private byte readKey(byte b) {
        final byte i = (byte) ((keys[curR++] & 255) ^ (b & 255));
        if (curR >= keys.length) {
            curR %= keys.length;
        }
        return i;
    }

    public void sendSessionKey() {
        Message msg = new Message(-27);
        try {
            msg.writer().writeByte(keys.length);
            msg.writer().writeByte(keys[0]);
            for (int i = 1; i < keys.length; i++) {
                msg.writer().writeByte(keys[i] ^ keys[i - 1]);
            }
//            String[] Server = Setting.SERVER_NAME.split(":");
//            msg.writer().writeUTF(Server[1]);//IP2
//            msg.writer().writeInt(Integer.valueOf(Server[2]));//Port
//            msg.writer().writeByte(1);//Connext (0 = false; 1 = true)
            doSendMessage(msg);
            isRead = true;
            sendKeyComplete = false;
            sender.active();
        } catch (Exception e) {
           System.out.println(e.toString());
        }
    }


}
