package DepretChambon;

import java.awt.Color;
import java.util.ArrayList;

import edu.turtlekit3.warbot.agents.ControllableWarAgent;
import edu.turtlekit3.warbot.agents.MovableWarAgent;
import edu.turtlekit3.warbot.agents.agents.WarBase;
import edu.turtlekit3.warbot.agents.agents.WarExplorer;
import edu.turtlekit3.warbot.agents.enums.WarAgentType;
import edu.turtlekit3.warbot.agents.percepts.WarPercept;
import edu.turtlekit3.warbot.brains.braincontrollers.WarExplorerAbstractBrainController;
import edu.turtlekit3.warbot.communications.WarMessage;

public class WarExplorerBrainController extends WarExplorerAbstractBrainController {
	

	private boolean imGiving = false;
	private String toReturn;
	private int role = -1; // Espion (1) ou ceuilleur (0)
	ArrayList<WarMessage> messages = new ArrayList<WarMessage>();
	
	
	//FSM *************************
	private Task ctask;
	
	private Task searchForFood = new Task(this){
		void exec(WarExplorerBrainController bc){
			getBrain().setDebugStringColor(Color.RED);
			getBrain().setDebugString(role + "    searchForFood");
			getFood();
		} 
	};
	
	private Task goBackHome = new Task(this){

		@Override
		void exec(WarExplorerBrainController bc) {
			getBrain().setDebugStringColor(Color.BLUE);
			getBrain().setDebugString(role + "    goBackHome");
			returnFood();
		}
	
	};
	
	//FSM **************************
	
	public WarExplorerBrainController() {
		super();
		ctask = searchForFood;
	}

	@Override
	public String action() {
		messages = getBrain().getMessages();
		toReturn = null;
		//On prévient de son role et on vérifie que la base ne nous ait pas demandé de chanegr de role
		prevenirBaseRole();
		setRole();
		
		
		if(role == 1){
			getBrain().setDebugString("ESPION");
			this.detectEnemy();
			
		}
		else if(role == 0){
			ctask.exec(this);
		}
		
		
		
		if(toReturn == null){
			if (getBrain().isBlocked())
				getBrain().setRandomHeading();
			return WarExplorer.ACTION_MOVE;
		} else {
			return toReturn;
		}
	}
		
		
	
	private WarMessage getMessageAboutFood() {
		for (WarMessage m : messages) {
			if(m.getMessage().equals("foodHere"))
				return m;
		}
		return null;
	}
	
	private WarMessage getMessageFromBase() {
		for (WarMessage m : messages) {
			if(m.getSenderType().equals(WarAgentType.WarBase))
				{return m;}
		}
		
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, "");
		return null;
	}

	private void getFood() {
		if(getBrain().isBagFull()){
			imGiving = true;
			ArrayList<WarPercept> foodAround = getBrain().getPerceptsResources();
			if(foodAround != null && foodAround.size() > 0){
				getBrain().broadcastMessageToAgentType(WarAgentType.WarExplorer, Constants.foodHere, "");
			}
			ctask = goBackHome;
			return;
		}
		
		if(imGiving)
			return;
		
		if(getBrain().isBlocked())
			getBrain().setRandomHeading();
		
		//getBrain().setDebugStringColor(Color.BLACK);
		//getBrain().setDebugString("Searching food");
		
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
			WarMessage m = getMessageAboutFood();
			if(m != null){
				getBrain().setHeading(m.getAngle());
			}
			toReturn = MovableWarAgent.ACTION_MOVE;
		}
	}
	
	private void returnFood() {
		if(!imGiving)
			return;
		
		if(getBrain().isBagEmpty()){
			imGiving = false;
			getBrain().setHeading(getBrain().getHeading() + 180);
			ctask = searchForFood;
			return;
		}
		//getBrain().setDebugStringColor(Color.green.darker());
		//getBrain().setDebugString("Returning Food");
		
		if(getBrain().isBlocked())
			getBrain().setRandomHeading();

		ArrayList<WarPercept> basePercepts = getBrain().getPerceptsAlliesByType(WarAgentType.WarBase);
		
		//Si je ne voit pas de base
		if(basePercepts == null | basePercepts.size() == 0){
			
			WarMessage m = this.getMessageFromBase();
			//Si j'ai un message de la base je vais vers elle
			if(m != null)
				getBrain().setHeading(m.getAngle());
			
			//j'envoie un message aux bases pour savoir où elle sont..
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
	
	private void setRole(){
		if(role == -1){
			role = 0;
			}
		
		for (WarMessage m : messages){
			if(m.getMessage().equals(Constants.espionMort)){
				this.role = 1;
			}
		}
		
		
	}
	
	private void prevenirBaseRole(){
		if(role == 1){ //1 correspond à espion
			getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.espion, "");
		}
		
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.here, "");
	}
	
	private void detectEnemy(){

		for(WarPercept p : getBrain().getPerceptsEnemies()){
			if(p.getType().equals(WarAgentType.WarBase)){
				getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.enemyBaseHere, "");
			}
		}
	}
}