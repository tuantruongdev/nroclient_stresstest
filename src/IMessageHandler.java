

public interface IMessageHandler {

    void onMessage(Session ss, Message msg);

    void onConnectOK();

    void onConnectionFail();

    void onDisconnected(Session ss);
}
