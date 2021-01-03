package nhom11ltm;

import java.util.*;
import java.net.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AgentServerThread extends Thread {
    //the main server who assigned this agent

    Server father;
    Socket socket;
    // data input and output streams
    DataInputStream input;
    DataOutputStream output;
    //thread indicator
    boolean connected = true;
    //Constructor
    private Connection con;
    public Vector move  = new Vector();

    public AgentServerThread(Server father, Socket sc) {
        this.father = father;
        this.socket = sc;
        getDBConnection("chess", "root", "");
        try {
            input = new DataInputStream(sc.getInputStream());
            output = new DataOutputStream(sc.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Users getUser(String username, String password) throws Exception {
        Users u =new Users();
        String query = "Select * FROM users WHERE  username  ='" + username
                + "' AND password ='" + password + "'";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                
                    u.setUsername(rs.getString(2));
                    u.setPassword(rs.getString(3));
                  u.setPoint((float) (rs.getFloat(4)));
               
                return u;
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    private void getDBConnection(String dbName, String username, String password) {
        String dbUrl = "jdbc:mysql://localhost:3306/" + dbName;
        String dbClass = "com.mysql.jdbc.Driver";
        try {
            Class.forName(dbClass);
            con = DriverManager.getConnection(dbUrl, username, password);
            if (con == null) {
                System.out.println("Ket noi khong thanh cong");
            } else {
                System.out.println("Ket noi thanh cong");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (connected) {
            try {
//msg.startsWith  là phương thức kiểm tra giá trị tiền tố 
                String msg = input.readUTF().trim();//take in message from client
//				msg.startsWith(prefix)
                if (msg.startsWith("<#NEW__USER#>")) {//new user
                    this.newUser(msg);
                } else if (msg.startsWith("<#CLIENT_LEAVE#>")) {//user leaves
                    this.clientLeave(msg);
                } else if (msg.startsWith("<#CHALLENGE#>")) {//challenge
                    this.challenge(msg);
                } else if (msg.startsWith("<#CHALACC#>")) {//challenge accepted
                    this.acceptChallenge(msg);
                } else if (msg.startsWith("<#CHAREJECT#>")) {//challenge rejected
                    this.declineChallenge(msg);
                } else if (msg.startsWith("<#BUSY#>")) {//other player is busy
                    this.busy(msg);
                } else if (msg.startsWith("<#MOVE#>")) {//a player's move
                    this.move(msg);
                } else if (msg.startsWith("<#GIVEUP#>")) {//surrender
                    this.surrender(msg);
                } else if (msg.startsWith("<#LOGIN#>")) {//loginn
                    this.checkLogin(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void checkLogin(String msg) {//a
        try {
            String[] user = msg.substring(9).split("/");//admin/admin

            // 
            String username = user[0];
            String password = user[1];
//            if (username.equals("admin") && password.equals("admin")) {
//                output.writeUTF("<#LOGIN#>OK");
////				                        newUser(username);
//
//            } 
            Users u= getUser(username, password);
            
            System.out.println(u.getUsername());
             if (u!=null) {
                output.writeUTF("<#LOGIN#>OK/"+u.getPoint());
				                        newUser(username);

            } 
            else {
                 System.out.println("check");
                output.writeUTF("<#LOGIN#>NO");
                input.close();
                output.close();
                socket.close();
                connected = false;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    public void newUser(String msg) {
        try {
//            String name = msg.substring(13);
                        String name = msg;
            this.setName(name);// đặt tên luồng đang phục vụ cho client
            Vector v = father.users;
            boolean nameExists = false;
            int size = v.size();
            //search to see if name already exists
            for (int i = 0; i < size; ++i) {
                AgentServerThread tempSat = (AgentServerThread) v.get(i);
                if (tempSat.getName().equals(name)) {
                    nameExists = true;
                    break;
                }
            }
            if (nameExists) {
                output.writeUTF("<#NAMEEXISTALREA#>");
                input.close();
                output.close();
                socket.close();
                connected = false;
            } else {
                v.add(this);
                father.refreshUsers(null);
                String nickListMsg = "";
                size = v.size();
                //format the concatenated string
                for (int i = 0; i < size; ++i) {
                    AgentServerThread tempSat = (AgentServerThread) v.get(i);
                    nickListMsg = nickListMsg + "|" + tempSat.getName();
                }
                nickListMsg = "<#NICK_LIST#>" + nickListMsg;
                Vector tempv = father.users;
                size = tempv.size();
                //send new user list and online message to everyone
                for (int i = 0; i < size; ++i) {
                    AgentServerThread satTemp = (AgentServerThread) tempv.get(i);
                    satTemp.output.writeUTF(nickListMsg);
                    if (satTemp != this) {
                        satTemp.output.writeUTF("<#MSG#>" + this.getName() + " is online!");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void clientLeave(String msg) {
        try {
            Vector tempv = father.users;
            tempv.remove(this);
            int size = tempv.size();
            String nl = "<#NICK_LIST#>";
            //send offline message and get refreshed list of users

            for (int i = 0; i < size; ++i) {
                AgentServerThread satTemp = (AgentServerThread) tempv.get(i);
                satTemp.output.writeUTF("<#MSG#>" + this.getName() + " is offline!");
                nl = nl + "|" + satTemp.getName();
            }
            for (int i = 0; i < size; ++i) {
                AgentServerThread satTemp = (AgentServerThread) tempv.get(i);
                satTemp.output.writeUTF(nl);
            }
            this.connected = false;
            father.refreshUsers(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void challenge(String msg) {
        try {
            String name1 = this.getName();//challenger's name

            String name2 = msg.substring(13);//challenged player's name
            Vector v = father.users;
            int size = v.size();
            //search for the user who is being challenged
            for (int i = 0; i < size; ++i) {
                AgentServerThread satTemp = (AgentServerThread) v.get(i);
                if (satTemp.getName().equals(name2)) {
                    satTemp.output.writeUTF("<#CHALLENGE#>" + name1);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void acceptChallenge(String msg) {
        try {
            String name = msg.substring(11);//challenger's name
            Vector v = father.users;
            int size = v.size();
            //locate the user
            for (int i = 0; i < size; ++i) {
                AgentServerThread satTemp = (AgentServerThread) v.get(i);
                if (satTemp.getName().equals(name)) {
                    satTemp.output.writeUTF("<#CHALACC#>");
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void declineChallenge(String msg) {
        try {
            String name = msg.substring(13);//challenger's name
            Vector v = father.users;
            int size = v.size();
            //locate the user
            for (int i = 0; i < size; ++i) {
                AgentServerThread satTemp = (AgentServerThread) v.get(i);
                if (satTemp.getName().equals(name)) {
                    satTemp.output.writeUTF("<#CHAREJECT#>");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void busy(String msg) {
        try {
            String name = msg.substring(8);//challenger's name
            Vector v = father.users;
            int size = v.size();
            //locate the user
            for (int i = 0; i < size; ++i) {
                AgentServerThread satTemp = (AgentServerThread) v.get(i);
                if (satTemp.getName().equals(name)) {
                    satTemp.output.writeUTF("<#BUSY#>");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void move(String msg) {
        try {
            String name = msg.substring(8, msg.length() - 4);//tên đối thủ
//            System.out.println( msg.substring(8));
            father.move.add("Move : "+ msg.substring(8));
            
            Vector v = father.users;
            int size = v.size();
            father.refreshUsers(father.move);
            //locate opponent
            for (int i = 0; i < size; ++i) {
                AgentServerThread satTemp = (AgentServerThread) v.get(i);
                if (satTemp.getName().equals(name)) {
                    satTemp.output.writeUTF(msg);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public float updatePoint(String name, float point){
     Users u =new Users();
        String query = "Select * FROM users WHERE  username  ='" +name +"'";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                
                    u.setUsername(rs.getString(2));
                    u.setPassword(rs.getString(3));
                    u.setPoint((float) (rs.getFloat(4)+point));
               
      
            }
        } catch (Exception e) {
          
        }
        PreparedStatement updateSales = null;String updateString = "update users " +"set point = ? where username = ?";
        try {updateSales = con.prepareStatement(updateString);
        updateSales.setFloat(1, u.getPoint());
        updateSales.setString(2, name);
        updateSales.executeUpdate();} catch (SQLException e ) 
        {
        
            System.out.println(e.getMessage());
        }
        
      return u.getPoint();
       
    
    }
    public void surrender(String msg) {
        try {
            String name = msg.substring(10);//opponent's name
            father.move.add(name+" win !");
            father.refreshUsers(father.move);
            Vector v = father.users;
            int size = v.size();
            //locate the user
            for (int i = 0; i < size; ++i) {
                AgentServerThread satTemp = (AgentServerThread) v.get(i);
                if (satTemp.getName().equals(name)) {
                    
                 float point=   satTemp.updatePoint(name, 1.0f);
                 String result= msg+"/"+point;
                                   System.out.println(result);
                    satTemp.output.writeUTF(result);
  
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
