package tp2warbotLionel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import edu.turtlekit3.warbot.agents.ControllableWarAgent;
import edu.turtlekit3.warbot.agents.MovableWarAgent;
import edu.turtlekit3.warbot.agents.agents.WarBase;
import edu.turtlekit3.warbot.agents.agents.WarExplorer;
import edu.turtlekit3.warbot.agents.agents.WarRocketLauncher;
import edu.turtlekit3.warbot.agents.enums.WarAgentType;
import edu.turtlekit3.warbot.agents.percepts.WarPercept;
import edu.turtlekit3.warbot.brains.braincontrollers.WarExplorerAbstractBrainController;
import edu.turtlekit3.warbot.communications.WarMessage;

public class WarExplorerBrainController extends WarExplorerAbstractBrainController {
	

	private boolean imGiving = false;
	private String toReturn = WarExplorer.ACTION_MOVE;
	ArrayList<WarMessage> messages; 
	
	private static Task Balade = new Task () {
		public void exec(WarExplorerBrainController b) 
		{
			System.out.println("Balade - " + b.getBrain().toString());
			b.getBrain().setRandomHeading(); 
			ArrayList<WarPercept> ressource = b.getBrain().getPerceptsResources(); 
			if(!ressource.isEmpty() && !b.getBrain().isBagFull())
			{
				b.getBrain().setHeading(ressource.get(0).getAngle()); 
				if(ressource.get(0).getDistance() > WarExplorer.MAX_DISTANCE_GIVE)
					b.setToReturn(WarExplorer.ACTION_MOVE); 
				else 
					b.setToReturn(WarExplorer.ACTION_TAKE); 
			}
			if(b.getBrain().isBagFull())
			{
				b.getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, ""); 
				b.setCTask(retourBase);
			} 
		}
	}; 
	
	private static Task retourBase = new Task () { 
		public void exec(WarExplorerBrainController b) 
		{
			System.out.println("retourBase - " + b.getBrain().toString());
			b.setMessage(b.getBrain().getMessages()); 
			for(WarMessage m : b.getMessage())
			{
				if(m.getSenderType().equals(WarAgentType.WarBase) && m.getMessage().equals(Constants.here))
				{
					b.getBrain().setHeading(m.getAngle()); 
					ArrayList<WarPercept> Baseennemie = b.getBrain().getPerceptsEnemiesByType(WarAgentType.WarBase); 
					if(!Baseennemie.isEmpty())
					{
						//getBrain().setHeading(Baseennemie.get(0).getAngle()); 
						b.getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.enemyBaseHere, String.valueOf(Baseennemie.get(0).getDistance()), String.valueOf(Baseennemie.get(0).getAngle()));
						b.getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.enemyBaseHere, String.valueOf(Baseennemie.get(0).getDistance()), String.valueOf(Baseennemie.get(0).getAngle())); 
						b.getBrain().setDebugString(Constants.enemyBaseHere); 
						b.getBrain().setDebugStringColor(Color.red);
					}
					b.returnFood(); 
					b.setCTask(Balade);
				}
			}
		}
	}; 
	
	private Task ctask = Balade;
	
	//private Task ctask = Balade;
	
	public WarExplorerBrainController() {
		super();
	}
	

	@Override
	public String action() {
		// Develop behaviour here
		
		if (ctask == null)
		{
			System.out.println("null");
		}
		else
		{
			System.out.println(this.toString());
		}
		//toReturn = null;
		ctask.exec(this); 
		return toReturn; 
	}
		
		
	private void handleMessages(){
		
	}
	
	private WarMessage getMessageAboutFood() {
		for (WarMessage m : getBrain().getMessages()) {
			if(m.getMessage().equals("foodHere"))
				return m;
		}
		return null;
	}
	
	private WarMessage getMessageFromBase() {
		for (WarMessage m : getBrain().getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarBase))
				return m;
		}
		
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, "");
		return null;
	}
	
	private WarMessage getMessageFromLauncher() {
		for (WarMessage m : getBrain().getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarRocketLauncher) && m.getMessage().equals(Constants.needFood) )
				return m;
		}
		
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, "");
		return null;
	}
	

	private void getFood() {
		if(getBrain().isBagFull()){
			imGiving = true;
			return;
		}
		
		if(imGiving)
			return;
		
		if(getBrain().isBlocked())
			getBrain().setRandomHeading();
		
		getBrain().setDebugStringColor(Color.BLACK);
		getBrain().setDebugString("Searching food");
		
		ArrayList<WarPercept> foodPercepts = getBrain().getPerceptsResources();
		
		//Si il y a de la nouriture
		if(foodPercepts != null && foodPercepts.size() > 0){
			WarPercept foodP = foodPercepts.get(0); //le 0 est le plus proche normalement
			
			if(foodP.getDistance() > ControllableWarAgent.MAX_DISTANCE_GIVE){
				getBrain().setHeading(foodP.getAngle());
				toReturn = MovableWarAgent.ACTION_MOVE;
			}else{
				toReturn = MovableWarAgent.ACTION_TAKE;
			}
		} else {
			toReturn = MovableWarAgent.ACTION_MOVE;
		}
	}
	
	private void returnFood() {
		if(!imGiving)
			return;
		
		if(getBrain().isBagEmpty()){
			imGiving = false;
			getBrain().setHeading(getBrain().getHeading() + 180);
			return;
		}
			
		getBrain().setDebugStringColor(Color.green.darker());
		getBrain().setDebugString("Returning Food");
		
		if(getBrain().isBlocked())
			getBrain().setRandomHeading();

		ArrayList<WarPercept> basePercepts = getBrain().getPerceptsAlliesByType(WarAgentType.WarBase);
		
		//Si je ne voit pas de base
		if(basePercepts == null | basePercepts.size() == 0){
			
			WarMessage m = this.getMessageFromBase();
			//Si j'ai un message de la base je vais vers elle
			if(m != null)
				getBrain().setHeading(m.getAngle());
			
			//j'envoie un message aux bases pour savoir oÃ¹ elle sont..
			getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, (String[]) null);
			
			toReturn = MovableWarAgent.ACTION_MOVE;
			
		}else{//si je vois une base
			WarPercept base = basePercepts.get(0);
			
			if(base.getDistance() > MovableWarAgent.MAX_DISTANCE_GIVE){
				getBrain().setHeading(base.getAngle());
				toReturn = MovableWarAgent.ACTION_MOVE;
			}else{
				getBrain().setIdNextAgentToGive(base.getID());
				toReturn = MovableWarAgent.ACTION_GIVE;
			}
			
		}
	
	}
	
	private void helpRocketLauncher()
	{
		for (WarMessage m : messages)
		{
			if(m.getSenderType().equals(WarAgentType.WarRocketLauncher) && m.getMessage().equals(Constants.needFood) && m.getDistance() <= 12*WarRocketLauncher.DISTANCE_OF_VIEW)
			{
				if(!getBrain().isBagEmpty())
				{
					getBrain().setHeading(m.getAngle());
					if (m.getDistance() <= 0.5)
						toReturn = WarExplorer.ACTION_GIVE;  
				} 
				else getFood(); 
			}
		}
	}
	/*
	private void returnFood2() {
		if(!imGiving)
			return;
		
		if(getBrain().isBagEmpty()){
			imGiving = false;
			getBrain().setHeading(getBrain().getHeading() + 180);
			return;
		}
			
		getBrain().setDebugStringColor(Color.green.darker());
		getBrain().setDebugString("Returning Food");
		
		if(getBrain().isBlocked())
			getBrain().setRandomHeading();

		ArrayList<WarPercept> LauncherPercepts = getBrain().getPerceptsAlliesByType(WarAgentType.WarRocketLauncher);
		
		//Si je ne voit pas de lanceur
		if(LauncherPercepts == null | LauncherPercepts.size() == 0){
			
			WarMessage m = this.getMessageFromLauncher();
			//Si j'ai un message de la base je vais vers elle
			if(m != null)
				getBrain().setHeading(m.getAngle());
			
			//j'envoie un message aux bases pour savoir oÃ¹ elle sont..
			getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, (String[]) null);
			
			toReturn = MovableWarAgent.ACTION_MOVE;
			
		}else{//si je vois une base
			WarPercept Launcher = basePercepts.get(0);
			
			if(base.getDistance() > MovableWarAgent.MAX_DISTANCE_GIVE){
				getBrain().setHeading(base.getAngle());
				toReturn = MovableWarAgent.ACTION_MOVE;
			}else{
				getBrain().setIdNextAgentToGive(base.getID());
				toReturn = MovableWarAgent.ACTION_GIVE;
			}
			
		}
	
	}
*/	
	
	public void setCTask(Task t)
	{
		this.ctask = t;
	}
	
	public void setMessage(ArrayList<WarMessage> m)
	{
		this.messages = m;
	}
	
	public ArrayList<WarMessage> getMessage()
	{
		return this.messages;
	}
	
	public void setToReturn(String s)
	{
		this.toReturn = s;
	}
} 