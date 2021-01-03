package nhom11ltm;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;

public class ChineseChess extends JFrame implements ActionListener{
	
	public static final Color backGround = new Color(245,250,160);
	public static final Color selectedBackground = new Color(242,242,242);
	public static final Color selectedTextBackground = new Color(96,95,91);
	public static final Color redColor = new Color(249,183,172);
	public static final Color whiteColor= Color.white;
	//graphical user interface setup
	JLabel hostLabel = new JLabel("HostIP");
	JLabel portLabel = new JLabel("Port");
	JLabel point = new JLabel("Point");
	JLabel nickName = new JLabel("Name");
	JTextField hostT = new JTextField("192.168.43.167");//default - host name
	JTextField portT = new JTextField("1111");//default - port number
	JTextField nickNameT = new JTextField("Player1");//default
	JButton connect = new JButton("Connect");
	JButton disconnect = new JButton("Disconnect");
	JButton surrender = new JButton("surrender");
	JButton challenge = new JButton("challenge");
	JComboBox otherUsersList = new JComboBox();// list user online
	JButton acceptChallenge = new JButton("Accept");
	JButton declineChallenge = new JButton("Decline");
	int width = 60;//distance between lines
	ChessPiece[][] chessPieces = new ChessPiece[9][10];
	Board board = new Board(chessPieces, width, this);
	JPanel jpRight = new JPanel();
	JSplitPane spane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, board, jpRight);
	
	boolean myTurn = false;
	int myColor = 0;//0 is red, 1 is white
	Socket socket;
	AgentclientThread act;
	 JButton btnLogin;
	 JTextField txtUsername;
	 JPasswordField txtPassword;
	public ChineseChess(String a) {
		super("Login");
        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        txtPassword.setEchoChar('*');
        btnLogin = new JButton("DangNhap");
        JPanel content = new JPanel();
        content.setLayout(new FlowLayout());
        content.add(new JLabel("Username:"));
        content.add(txtUsername);
        content.add(new JLabel("Password:"));
        content.add(txtPassword);
        content.add(hostLabel);
        content.add(hostT);
        content.add(portLabel);
        content.add(portT);
        content.add(btnLogin);
        btnLogin.addActionListener(this);
        this.setContentPane(content);
        this.pack();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
      this.addWindowListener(
			new WindowAdapter(){
                            
				public void windowClosing(WindowEvent e){
                                    
					if(act == null){
//                                            showMessage("acp null");
						System.exit(0);
                                                     
						return;
                                           
					}
					try{
						if(act.challenger != null){//playing with someone
                                                    
//                                                  showMessage("acp exit");
							try{
								//surrender message
								act.output.writeUTF("<#GIVEUP#>"+act.challenger);
							}
							catch(Exception ee){
								ee.printStackTrace();
							}
						}
						act.output.writeUTF("<#CLIENT_LEAVE#>");//leave message
//                                                act.output.close();
//                                                act.input.close();
						act.connected = false;//end client
						act = null;	
					}
					catch(Exception ee){
						ee.printStackTrace();
					}
					System.exit(0);
				}
				
			});
		
        this.setVisible(true);
		
	}
	  public void showMessage(String msg){
	        JOptionPane.showMessageDialog(this, msg);
	    }
	public ChineseChess(){
		this.initialComponent();//add components
		this.addComponentListener();//add listeners
		this.intialState();
		this.initialPieces();
		this.initialFrame();
	}
	public void showView() {
		
		this.initialComponent();//add components
		this.addComponentListener();//add listeners
		this.intialState();
		this.initialPieces();
		this.initialFrame();
		
	}
	
	public void initialComponent(){
		jpRight.setLayout(null);
		this.hostLabel.setBounds(10,10,100,20);
		jpRight.add(this.hostLabel);
//		this.hostT.setBounds(70,10,80,20);
//		jpRight.add(this.hostT);
		this.portLabel.setBounds(10,40,100,20);
		jpRight.add(this.portLabel);
//		this.portT.setBounds(70,40,80,20);
//		jpRight.add(this.portT);
		this.nickName.setBounds(10,70,100,20);
		jpRight.add(this.nickName);
                
                this.point.setBounds(110, 70, 100, 20);
//                this.point.setText("Point : " + );
                jpRight.add(this.point);
                
//		this.nickNameT.setBounds(70,70,80,20);
//		jpRight.add(this.nickNameT);
		this.connect.setBounds(10,100,80,20);
		jpRight.add(this.connect);
		this.disconnect.setBounds(100,100,80,20);
		jpRight.add(this.disconnect);
		this.otherUsersList.setBounds(20,130,130,20);
		jpRight.add(this.otherUsersList);
		this.challenge.setBounds(10,160,80,20);
		jpRight.add(this.challenge);
		this.surrender.setBounds(100,160,80,20);
		jpRight.add(this.surrender);
		this.acceptChallenge.setBounds(5,190,86,20);
		jpRight.add(this.acceptChallenge);
		this.declineChallenge.setBounds(100,190,86,20);
		jpRight.add(this.declineChallenge);
		board.setLayout(null);
		board.setBounds(0,0,700,700);
	}
	
	public void addComponentListener(){// call function lister
		this.connect.addActionListener(this);
		this.disconnect.addActionListener(this);
		this.challenge.addActionListener(this);
		this.surrender.addActionListener(this);
		this.acceptChallenge.addActionListener(this);
		this.declineChallenge.addActionListener(this);
	}
	
	public void intialState(){
		this.disconnect.setEnabled(true);
                this.connect.setEnabled(false);
		this.challenge.setEnabled(true);
		this.acceptChallenge.setEnabled(false);
		this.declineChallenge.setEnabled(false);
		this.surrender.setEnabled(false);
                this.hostLabel.setText("Host IP : " + hostT.getText());
                this.hostLabel.setForeground(Color.BLUE);
                this.portLabel.setText("Port : 1111 " );
                this.portLabel.setForeground(Color.BLUE);
                this.nickName.setText("Name : " + txtUsername.getText());
                this.nickName.setForeground(Color.RED);
                
	}
	
	public void initialPieces(){
		chessPieces[0][0] = new ChessPiece(redColor,"車",0,0);//xe
		chessPieces[1][0] = new ChessPiece(redColor,"馬",1,0);// mã
		chessPieces[2][0] = new ChessPiece(redColor,"相",2,0);	// tượng
		chessPieces[3][0] = new ChessPiece(redColor,"仕",3,0);// sĩ
		chessPieces[4][0] = new ChessPiece(redColor,"将",4,0);// tướng
		chessPieces[5][0] = new ChessPiece(redColor,"仕",5,0);// sĩ
		chessPieces[6][0] = new ChessPiece(redColor,"相",6,0);// tượng
		chessPieces[7][0] = new ChessPiece(redColor,"馬",7,0);// mã
		chessPieces[8][0] = new ChessPiece(redColor,"車‡",8,0);// xe
		chessPieces[1][2] = new ChessPiece(redColor,"炮",1,2);// pháo
		chessPieces[7][2] = new ChessPiece(redColor,"炮",7,2);// pháo
		chessPieces[0][3] = new ChessPiece(redColor,"兵",0,3);// tốt
		chessPieces[2][3] = new ChessPiece(redColor,"兵",2,3);
		chessPieces[4][3] = new ChessPiece(redColor,"兵",4,3);
		chessPieces[6][3] = new ChessPiece(redColor,"兵",6,3);
		chessPieces[8][3] = new ChessPiece(redColor,"兵",8,3);// tốt
		chessPieces[0][9] = new ChessPiece(whiteColor,"車",0,9);// xe
		chessPieces[1][9] = new ChessPiece(whiteColor,"馬",1,9);//mã
		chessPieces[2][9] = new ChessPiece(whiteColor,"象",2,9);//tượng
		chessPieces[3][9] = new ChessPiece(whiteColor,"士",3,9);// sĩ
		chessPieces[4][9] = new ChessPiece(whiteColor,"將",4,9);// tướng
		chessPieces[5][9] = new ChessPiece(whiteColor,"士",5,9);// sĩ
		chessPieces[6][9] = new ChessPiece(whiteColor,"象",6,9);//tượng
		chessPieces[7][9] = new ChessPiece(whiteColor,"馬",7,9);// mã
		chessPieces[8][9] = new ChessPiece(whiteColor,"車",8,9);//xe
		chessPieces[1][7] = new ChessPiece(whiteColor,"砲",1,7);// pháo
		chessPieces[7][7] = new ChessPiece(whiteColor,"砲",7,7);// pháo
		chessPieces[0][6] = new ChessPiece(whiteColor,"卒",0,6);// tốt
		chessPieces[2][6] = new ChessPiece(whiteColor,"卒",2,6);
		chessPieces[4][6] = new ChessPiece(whiteColor,"卒",4,6);
		chessPieces[6][6] = new ChessPiece(whiteColor,"卒",6,6);
		chessPieces[8][6] = new ChessPiece(whiteColor,"卒",8,6);// tốt
	}
	
	public void initialFrame(){
		this.setTitle("Chinese chess!");
	
//		this.add(this.jpRight);
//		this.setContentPane(this.jpRight);
		this.add(this.spane);
		this.setContentPane(this.spane);
//		this.getContentPane().add(this.jpRight);
		this.pack();
		
		
	
	
		spane.setDividerLocation(730);
		spane.setDividerSize(4);
		this.setBounds(30,30,930,730);
		this.repaint();             //Ensures that the frame swaps to the next panel and doesn't get stuck.
		this.revalidate();  
		this.setVisible(true);
		
		this.addWindowListener(
			new WindowAdapter(){
                            
				public void windowClosing(WindowEvent e){
                                    
					if(act == null){
//                                            showMessage("acp null");
						System.exit(0);
                                                     
						return;
                                           
					}
					try{
						if(act.challenger != null){//playing with someone
                                                    
//                                                  showMessage("acp exit");
							try{
								//surrender message
//								act.output.writeUTF("<#GIVEUP#>"+act.challenger);
							}
							catch(Exception ee){
								ee.printStackTrace();
							}
						}
						act.output.writeUTF("<#CLIENT_LEAVE#>");//leave message
//                                                act.output.close();
//                                                act.input.close();
						act.connected = false;//end client
						act = null;	
					}
					catch(Exception ee){
						ee.printStackTrace();
					}
					System.exit(0);
				}
				
			});
		
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == this.connect){
			this.connectEvent();
		}else if(e.getSource() == this.disconnect){
			this.disconnectEvent();
		}else if(e.getSource() == this.challenge){
			this.challengeEvent();
		}else if(e.getSource() == this.acceptChallenge){
			this.acceptChallengeEvent();
		}else if(e.getSource() == this.declineChallenge){
			this.declineChallengeEvent();
		}else if(e.getSource() == this.surrender){
			this.surrenderEvent();
		}
		else if(e.getSource().equals(btnLogin))
	        {
			this.dispose();
			this.setVisible(false);
			login();
//	        new ChineseChess();
	        }
	}
	
	public void login() {
		
		int port = 0;
		try{//get port id
			port=Integer.parseInt(this.portT.getText().trim());
		}catch(Exception ee){//not whole number
			JOptionPane.showMessageDialog(this,"Whole numbers only","Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(port > 65535 || port < 0){
			JOptionPane.showMessageDialog(this,"Port id should be from 0 to 65535","Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
//		String name = this.nickNameT.getText().trim();//get nick name
//		if(name.length() == 0){//empty nick name
//			JOptionPane.showMessageDialog(this,"Name cannot be empty!","Error!", JOptionPane.ERROR_MESSAGE);
//			return;
//		}
//		String password=String.valueOf(this.txtPassword.getPassword());
//		String username=this.txtUsername.getText();
		
	
		try {
			//adjusting setup
//			socket=new Socket(this.hostT.getText().trim(),port);
			socket=new Socket(this.hostT.getText().trim(),port);
			act=new AgentclientThread(this);
			act.start();	
//			this.hostT.setEnabled(false);
//			this.portT.setEnabled(false);
//			this.nickNameT.setEnabled(false);
//			this.connect.setEnabled(false);
//			this.disconnect.setEnabled(true);
//			this.challenge.setEnabled(true);
//			this.acceptChallenge.setEnabled(false);
//			this.declineChallenge.setEnabled(false);
//			this.surrender.setEnabled(false);
//			JOptionPane.showMessageDialog(this,"Connected to server!","Message", JOptionPane.INFORMATION_MESSAGE);
		}
		catch(Exception ee){
			JOptionPane.showMessageDialog(this,"Fssil to connect!","Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
	}
	public void connectEvent(){
		int port = 0;
		try{//get port id
			port=Integer.parseInt(this.portT.getText().trim());
		}catch(Exception ee){//not whole number
			JOptionPane.showMessageDialog(this,"Whole numbers only","Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(port > 65535 || port < 0){
			JOptionPane.showMessageDialog(this,"Port id should be from 0 to 65535","Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String name = this.nickNameT.getText().trim();//get nick name
		if(name.length() == 0){//empty nick name
			JOptionPane.showMessageDialog(this,"Name cannot be empty!","Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		try {
			//adjusting setup
//			socket=new Socket(this.hostT.getText().trim(),port);
//			String name = this.nickNameT.getText().trim();// để thằng này bên chinachess
			act.output.writeUTF("<#NEW__USER#>"+name);// flag
			this.hostT.setEnabled(false);
			this.portT.setEnabled(false);
			this.nickNameT.setEnabled(false);
			this.connect.setEnabled(false);
			this.disconnect.setEnabled(true);
			this.challenge.setEnabled(true);
			this.acceptChallenge.setEnabled(false);
			this.declineChallenge.setEnabled(false);
			this.surrender.setEnabled(false);
			JOptionPane.showMessageDialog(this,"Success","Message", JOptionPane.INFORMATION_MESSAGE);
		}
		catch(Exception ee){
			JOptionPane.showMessageDialog(this,"Fssil to connect!","Error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
	
	public void disconnectEvent(){
		try{
			//changing setup
			this.act.output.writeUTF("<#CLIENT_LEAVE#>");//send offline message
			this.act.connected = false;//end client agent
			this.act = null;
			this.hostT.setEnabled(!false);
			this.portT.setEnabled(!false);
			this.nickNameT.setEnabled(!false);
			this.connect.setEnabled(!false);
			this.disconnect.setEnabled(!true);
			this.challenge.setEnabled(!true);
			this.acceptChallenge.setEnabled(false);
			this.declineChallenge.setEnabled(false);
			this.surrender.setEnabled(false);
		}
		catch(Exception ee){
			ee.printStackTrace();
		}
	}
	
	public void challengeEvent(){
		//receive the selected opponent
		Object o = this.otherUsersList.getSelectedItem();
		if(o == null || ((String)o).equals("")) {
			JOptionPane.showMessageDialog(this,"Please choose the opponent's name","Error!", JOptionPane.ERROR_MESSAGE);
		}
		else{
			String opponent=(String)this.otherUsersList.getSelectedItem();
			try{
				this.hostT.setEnabled(false);
				this.portT.setEnabled(false);
				this.nickNameT.setEnabled(false);
				this.connect.setEnabled(false);
				this.disconnect.setEnabled(!true);
				this.challenge.setEnabled(!true);
				this.acceptChallenge.setEnabled(false);
				this.declineChallenge.setEnabled(false);
				this.surrender.setEnabled(false);
				this.act.challenger = opponent;
				this.myTurn = true;
				this.myColor = 0;
				this.act.output.writeUTF("<#CHALLENGE#>"+opponent);
				JOptionPane.showMessageDialog(this,"Challenge sent","message", JOptionPane.INFORMATION_MESSAGE);
			}
			catch(Exception ee){
				ee.printStackTrace();
			}
		}
	}
	
	public void acceptChallengeEvent(){
		try{	//deliver accept message
			this.act.output.writeUTF("<#CHALACC#>"+this.act.challenger);
			this.myTurn = false;
			this.myColor=1;
			this.hostT.setEnabled(false);
			this.portT.setEnabled(false);
			this.nickNameT.setEnabled(false);
			this.connect.setEnabled(false);
			this.disconnect.setEnabled(!true);
			this.challenge.setEnabled(!true);
			this.acceptChallenge.setEnabled(false);
			this.declineChallenge.setEnabled(false);
			this.surrender.setEnabled(!false);
		}
		catch(Exception ee){
			ee.printStackTrace();
		}
	}
	
	public void declineChallengeEvent(){
		try{//deliver decline message
			this.act.output.writeUTF("<#CHAREJECT#>"+this.act.challenger);
			this.act.challenger = null;
			this.hostT.setEnabled(false);
			this.portT.setEnabled(false);
			this.nickNameT.setEnabled(false);
			this.connect.setEnabled(false);
			this.disconnect.setEnabled(true);
			this.challenge.setEnabled(true);
			this.acceptChallenge.setEnabled(false);
			this.declineChallenge.setEnabled(false);
			this.surrender.setEnabled(false);
		}
		catch(Exception ee){
			ee.printStackTrace();
		}
	}
	
	public void surrenderEvent(){
		try{   //deliver surrender message
			this.act.output.writeUTF("<#GIVEUP#>"+this.act.challenger);
			this.act.challenger = null;
			this.myColor = 0;
			this.myTurn = false;
			this.next();//prepare for next round
			this.hostT.setEnabled(false);
			this.portT.setEnabled(false);
			this.nickNameT.setEnabled(false);
			this.connect.setEnabled(false);
			this.disconnect.setEnabled(true);
			this.challenge.setEnabled(true);
			this.acceptChallenge.setEnabled(false);
			this.declineChallenge.setEnabled(false);
			this.surrender.setEnabled(false);
		}
		catch(Exception ee){
			ee.printStackTrace();
		}	
	}
	
	public void next(){
		for(int i = 0;i < 9; ++i){//empty the chess pieces
			for(int j = 0;j < 10; ++j){
				this.chessPieces[i][j] = null;
			}
		}
		this.myTurn = false;
		this.initialPieces();
		this.repaint();
	}
	
	public static void main(String args[]){
		new ChineseChess("login");
//		new ChineseChess();
	}
}
