package domainLayer;

import java.util.ArrayList;

import domainLayer.cards.Card;
import domainLayer.squares.PropertySquare;
import domainLayer.squares.Square;


import uiLayer.PropertyListener;

public class Player implements Publisher{


	public Piece piece;
	private String name;
	private Board board;
	private int cash;
	private boolean haveRolled = false;
	private Cup cup;
	private ArrayList<Square> myProperties= new ArrayList<Square>();
	private ArrayList<Card> inventory=new ArrayList<Card>();
	
	private boolean inJail=false;
	private int jailCounter=0;
	private int layer;
	
	private ArrayList<PropertyListener> myListeners=new ArrayList<PropertyListener>();

	public Player(String name, Board board) {
		this.name = name;
		this.board = board;
		this.addCash(1500);
		this.cup=board.getCup();
		piece= new Piece(board.getStartSquare());
	}
	
	private int whichlayer() {
		if(piece.getLocation().getIndex()<40) {
			layer=1;
			
		}else if(piece.getLocation().getIndex()<64) {
			layer=0;
		}else {
			layer=2;
		}
		return layer;
	}

	//TURN
	public void takeTurn() {
		if (!haveRolled) {
			cup.roll();
			int rollTotal = cup.get2RollValue();
			if (inJail) tryToGetOutOfJail();
			
			if(!inJail) {
				System.out.println("*****Player "+this.name+" rolled "+rollTotal+" Dual Roll?: "+cup.isDualRoll());
				moveOneByOneFor(rollTotal);		
			}
			
			
			haveRolled = true;
			if(cup.isDualRoll()) haveRolled=false;
			if(cup.isThirdDualRoll()) {
				haveRolled=true;
				
				 //10 is jail
			}
		}
	}
	
	public boolean haveRolled() {
		return haveRolled;
	}

	public void setHaveRolled(boolean haveRolled) {
		this.haveRolled = haveRolled;
	}

	public String getName() {
		return name;
	}

	
	
	//MOVEMENT
	public void moveOneByOneFor(int f) { 
		
		
		Square currentLoc=piece.getLocation();
		
		System.out.println("Player "+this.name+" passed on");
		for(int k=0;k<f;k++) {
			System.out.println("->"+currentLoc.getName()+" ");
			Square nextLoc=currentLoc.getNextSquare(f);
			piece.setLocation(nextLoc);	
			nextLoc.passedOn(this);
			currentLoc=nextLoc;
		}
		
		System.out.println(" and Player "+this.name+" landed on "+currentLoc.getName()+" at Index: ("+currentLoc.getIndex()+")");
		currentLoc.landedOn(this);
		publishPropertyEvent("Location");
		
	}
	
	
	public void teleportToLand(Square sq) {
		piece.setLocation(sq);
		sq.landedOn(this); //will landed on work on teleports? //Doruk thinks yes
		publishPropertyEvent("Location");
	}
	
	public void teleportNoLand(Square sq) {
		piece.setLocation(sq);
	}
	
	public void goToJail() {
		teleportNoLand(board.getSquareByIndex(10));
		this.inJail=true;
		System.out.println("Player "+this.name+" is in jail!");
	}
	
	public void tryToGetOutOfJail() { //how much cash is taken in 3 rd failed attempt
		
		if(cup.isDualRoll()) inJail=false;
		System.out.println("Player "+this.name+" tried to get out of jail. inJail = "+inJail );
		if(inJail) {
			jailCounter++;
			if(jailCounter>=3) {
				inJail=false;
				jailCounter=0;
				this.reduceCash(100); //??
			}
		}
		
	}

	public Square getLocation() {
		return piece.getLocation();
	}


	//CASH
	public void addCash(int amount) {
		System.out.println("Player " +this.name+" gained "+amount+" money!");
		cash += amount;
		publishPropertyEvent("Money");
	}

	public int getCash() {
		return cash;
	}

	public void reduceCash(int amount) {
		System.out.println("Player " +this.name+" lost "+amount+" money!");
		cash -= amount;
		publishPropertyEvent("Money");
	}

	public void attemptPurchase (PropertySquare psq) {
		
			boolean success=false;
			if (!psq.isOwned() && cash >= psq.getPrice()){
				reduceCash(psq.getPrice());
				addProperty(psq);
				psq.setOwner(this);
				success=true;
				
			}
			
			System.out.println("Purchase attempted,result is: "+success+"");
			if(success) {
				for(Square sq : myProperties) {
					System.out.println("--"+sq.getName());
				}
				System.out.println("Money left: "+this.cash);
				
				
			}
			
		}

	public void addProperty(Square sq) {
		if(!myProperties.contains(sq)) myProperties.add(sq);
	}
	
	public void removeProperty(Square sq) {
		if(myProperties.contains(sq)) myProperties.remove(sq);
		
	}
	
	public ArrayList<Square> getPlayerProperties(){
		return myProperties;
	}
	
	public boolean isMyProperty(Square sq) {
		return myProperties.contains(sq);
	}
	
	public void keepCard(Card card) {
		inventory.add(card);
	}
	
	public void useCard(Card card) {
		card.cardAction();
		if(inventory.contains(card)) inventory.remove(card);
	}
	
	public ArrayList<Card> getRollThreeCards(){
		ArrayList<Card> rtcards=new ArrayList<Card>();
		for(int i=0;i<inventory.size();i++) {
			if(inventory.get(i).getType()=="RollThreeCard") {
				rtcards.add(inventory.get(i));
			}
		}
		return rtcards;
	}

	public boolean haveRolledEven(){
		if(cup.rolledEven()==true)return true;
		return false;
	}
	
	public boolean haveRolledOdd(){
		if(cup.rolledOdd()==true)return true;
		return false;
	}
	@Override
	public void addPropertyListener(PropertyListener listener) {
		// TODO Auto-generated method stub
		myListeners.add(listener);
	}

	@Override
	public void removePropertyListener(PropertyListener listener) {
		// TODO Auto-generated method stub
		if(myListeners.contains(listener)) {
			myListeners.remove(listener);
		}
	}

	@Override
	public void publishPropertyEvent(String type) {
		// TODO Auto-generated method stub
		String value;
		if(type=="Money") {
			value=this.cash+"";
		}
		else if(type=="Location") {
			value=this.getLocation().getName();
		}
		else {
			value="error";
		}
		for(PropertyListener listener:myListeners) {
			listener.onPropertyEvent(this, type, value);
		}

	}
}
