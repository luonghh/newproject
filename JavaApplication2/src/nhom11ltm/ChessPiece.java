package nhom11ltm;

import java.awt.*;

public class ChessPiece {
	//basic properties of a chess piece
	private Color color;
	private String name;
	//location
	private int x;
	private int y;
	private boolean selected = false;//if piece is selected by mouse
	
	//Constructors
	public ChessPiece(){
	}
	
	public ChessPiece(Color color, String name, int x, int y){
		this.color = color;
		this.name = name;
		this.x = x;
		this.y = y;
		this.selected = false;
	}
	
	//getters and setters
	public Color getColor(){
		return this.color;
	}
	public void setColor(Color color){
		this.color = color;
	}
	
	public String getName(){
		return this.name;
	}
	public void setName(String name){
		this.name = name;
	}
	
	public int getX(){
		return this.x;
	}
	public void setX(int x){
		this.x = x;
	}
	public int getY(){
		return this.y;
	}
	public void setY(int y){
		this.y = y;
	}
	
	public boolean getSelected(){
		return selected;
	}
	public void setSelected(boolean selected){
		this.selected = selected;
	}
	
}
