public interface ISession {
    
    public void remove_point(int a);
    
    public long get_point();

    public int get_active();
    
    public int get_act();
    
    public void update_active();
    
    public int get_zoom();
    
    public int get_user_id();

    public void set_user_id(int u);

    public byte get_type_client();
    
    public String get_client_account();
    
    public String get_client_pass();

    public void set_type_client(byte t);

    public short get_version();
    
    public int get_gold();
    
    public int get_money();
    
    public int get_vnd();
    
    
    
   // public void changpass(String pass);
    
    public void remove_money(int quantity);
        
    public void remove_gold(int quantity);

    public void set_version(short v);

    public void disconnect();

    public void sendMessage(Message var1);
}
