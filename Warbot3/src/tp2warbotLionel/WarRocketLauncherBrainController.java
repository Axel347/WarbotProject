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
import edu.turtlekit3.warbot.agents.projectiles.WarRocket;
import edu.turtlekit3.warbot.brains.braincontrollers.WarRocketLauncherAbstractBrainController;
import edu.turtlekit3.warbot.communications.WarMessage;
import edu.turtlekit3.warbot.teams.demo.WarKamikazeBrainController;
import edu.turtlekit3.warbot.tools.CoordPolar;

public class WarRocketLauncherBrainController extends WarRocketLauncherAbstractBrainController {

	private String toReturn = null;
	private boolean imGiving = false;
	boolean iAbleToFireBase = false;
	ArrayList<WarMessage> messages;  
	
	public WarRocketLauncherBrainController() {
		super();
	}
	
	@Override
	public String action() {
		// Develop behaviour here
		//getBrain().setRandomHeading();  
		
		toReturn = null;
		this.messages = getBrain().getMessages();  
		handleMessages(); 
		// if(iAbleToFireBase)
		//	 attaquerBase();
		ArrayList<WarPercept> Baseennemie = getBrain().getPerceptsEnemiesByType(WarAgentType.WarBase); 
		if(!Baseennemie.isEmpty())
		{
			AttatckBase(Baseennemie); 
		}
		
		ArrayList<WarPercept> EnemyTurret = getBrain().getPerceptsEnemiesByType(WarAgentType.WarTurret);
		//if(!EnemyTurret.isEmpty()) 
			//toReturn = AttackTurret(EnemyTurret); 
		//return AttackTurret(EnemyTurret);
		
		if(!iAbleToFireBase)
			attackRocketLaunchers();
		ArrayList<WarPercept> EnemyLauncher = getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher); 
		if(EnemyLauncher.isEmpty() && Baseennemie.isEmpty())
			getFood(); 
		//ArrayList<WarPercept> base = getBrain().getPerceptsAlliesByType(WarAgentType.WarBase);
		for (WarMessage m : messages) 
		{
			if (m.getSenderType().equals(WarAgentType.WarBase) && m.getMessage().equals(Constants.baseIsAttack) && m.getDistance() < 6.5*WarBase.DISTANCE_OF_VIEW)
			{
				String[] Content = m.getContent(); 
				Double angle = Double.valueOf(Content[1]).doubleValue(); 
				Double distance = Double.valueOf(Content[0]).doubleValue(); 
				CoordPolar EnemyLauncherPosition = getBrain().getTargetedAgentPosition(m.getAngle(), m.getDistance(), angle, distance);
				getBrain().setDebugString(Constants.baseIsAttack);
				getBrain().setDebugStringColor(Color.red); 
				if(EnemyLauncher.isEmpty())
				{
					/*
					if(m.getDistance() < EnemyLauncherPosition.getDistance())
						getBrain().setHeading(m.getAngle()); 
					else */ 
						getBrain().setHeading(EnemyLauncherPosition.getAngle()); 
					if (m.getDistance() + WarBase.DISTANCE_OF_VIEW == WarBase.DISTANCE_OF_VIEW)
						getBrain().setHeading(EnemyLauncherPosition.getAngle()); 
				} 
			}
			else
			if(m.getSenderType().equals(WarAgentType.WarBase) && m.getMessage().equals(Constants.positionEnnemyBaseFound) && m.getDistance() >= 4*WarBase.DISTANCE_OF_VIEW)
			{
				String[] Content = m.getContent(); 
				Double angle = Double.valueOf(Content[1]).doubleValue(); 
				Double distance = Double.valueOf(Content[0]).doubleValue(); 
				CoordPolar EnemyBasePosition = getBrain().getTargetedAgentPosition(m.getAngle(), m.getDistance(), angle, distance);  
				getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.positionEnnemyBaseFound, String.valueOf(EnemyBasePosition.getDistance()), String.valueOf(EnemyBasePosition.getAngle())); 
				getBrain().setDebugString(Constants.positionEnnemyBaseFound); 
				getBrain().setDebugStringColor(Color.CYAN);
				ArrayList<WarPercept> AlliesLauncher = getBrain().getPerceptsAlliesByType(WarAgentType.WarRocketLauncher);  
				if (EnemyLauncher.isEmpty()) 
				getBrain().setHeading(EnemyBasePosition.getAngle());  
				//if (!AlliesLauncher.isEmpty()) 
					//getBrain().setHeading(EnemyBasePosition.getAngle()); // + WarRocketLauncher.ANGLE_OF_VIEW); 
			} 
		} 
		
		for(WarMessage m : messages)
		{
			if(m.getSenderType().equals(WarAgentType.WarExplorer) && m.getMessage().equals(Constants.enemyBaseHere) && m.getDistance() < 2 * WarBase.DISTANCE_OF_VIEW) 
			{
				String[] Content = m.getContent(); 
				Double angle = Double.valueOf(Content[1]).doubleValue(); 
				Double distance = Double.valueOf(Content[0]).doubleValue(); 
				CoordPolar EnemyBasePosition = getBrain().getTargetedAgentPosition(m.getAngle(), m.getDistance(), angle, distance);
				getBrain().setDebugString(Constants.positionEnnemyBaseFound);
				getBrain().setDebugStringColor(Color.cyan); 
				if(EnemyLauncher.isEmpty())
					getBrain().setHeading(EnemyBasePosition.getAngle()); 
			}
		}
		
		//wiggle();
		 
		
		if(toReturn == null){
			if (getBrain().isBlocked())
				getBrain().setRandomHeading();
			toReturn = WarRocketLauncher.ACTION_MOVE;
		}
		
		if (WarRocketLauncher.MAX_HEALTH <= WarRocketLauncher.MAX_HEALTH*0.55)
		{
			getBrain().broadcastMessageToAgentType(WarAgentType.WarExplorer, Constants.needFood, ""); 
		}
		
		return toReturn;
	}
	
	private void attackRocketLaunchers() {
		if(toReturn != null)
			return;
		
		if(!getBrain().isReloaded() && !getBrain().isReloading()){
			toReturn =  WarRocketLauncher.ACTION_RELOAD;
			return;
		}
		
		//getBrain().setDebugStringColor(Color.blue);
		//getBrain().setDebugString("Attack launchers");
		
		ArrayList<WarPercept> percept = getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher);
		
		// Je un agentType dans le percept
		if(percept != null && percept.size() > 0){
			
			//je le dit aux autres
			getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.enemyTankHere, String.valueOf(percept.get(0).getDistance()), String.valueOf(percept.get(0).getAngle()));
			getBrain().setDebugStringColor(Color.blue);
			getBrain().setDebugString("Attack launchers");
			if(getBrain().isReloaded()){
				
				getBrain().setHeading(percept.get(0).getAngle());
				toReturn = WarRocketLauncher.ACTION_FIRE;
			}else{
				
				//si je suis pas trop pres de l'enemy je m'approche
				
				if(percept.get(0).getDistance() > WarRocket.EXPLOSION_RADIUS + 1)
					toReturn = WarRocketLauncher.ACTION_MOVE;
				else
					toReturn = WarRocketLauncher.ACTION_IDLE;
			}
		}else{
			//si j'ai un message me disant qu'il y a  un autre tank a tuer
			
			WarMessage m = getFormatedMessageAboutEnemyTankToKill();
			if(m != null){
				CoordPolar p = getBrain().getIndirectPositionOfAgentWithMessage(m);
				getBrain().setHeading(p.getAngle());
				toReturn = WarRocketLauncher.ACTION_MOVE;
			}
		}		
	}
	
	private void wiggle() {
		if(toReturn != null)
			return;
		
		if(getBrain().isBlocked())
		{
			//getBrain().setHeading(180);
			getBrain().setRandomHeading();
		}
		
		getBrain().setDebugStringColor(Color.black);
		getBrain().setDebugString("Looking for ennemies");
		
		double angle = getBrain().getHeading() + new Random().nextInt(10) - new Random().nextInt(10);
		
		getBrain().setHeading(angle);
	
		toReturn = MovableWarAgent.ACTION_MOVE;		
	}

	private WarMessage getFormatedMessageAboutEnemyTankToKill() {
		for (WarMessage m : this.messages) {
			//if(m.getMessage().equals(Constants.enemyTankHere) && m.getContent() != null && m.getContent().length == 2){
			if(m.getSenderType().equals(WarAgentType.WarBase) && m.getMessage().equals(Constants.positionEnnemyBaseFound)){
				return m;
			}
		}
		return null;
	}

	private WarMessage getMessageAboutEnemyBase() {
		for (WarMessage m : this.messages) {
			if(m.getMessage().equals(Constants.enemyBaseHere))
				return m;
		}
		return null;
	}

	private void handleMessages() {
		for (WarMessage m : this.messages) {
			if(m.getSenderType().equals(WarAgentType.WarKamikaze) && m.getMessage().equals(WarKamikazeBrainController.I_Exist))
				this.iAbleToFireBase = true;
		}
	}
	
	private WarMessage getMessageFromExplorer() {
		for (WarMessage m : messages) {
			if(m.getSenderType().equals(WarAgentType.WarExplorer) && m.getMessage().equals(Constants.enemyBaseHere))
				return m;
		}
		return null;	
	}
	
	private void AttatckBase(ArrayList<WarPercept> Baseennemie)
	{
		//if(!Baseennemie.isEmpty())
		//{ 
			iAbleToFireBase = true; 
				getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.enemyBaseHere, String.valueOf(Baseennemie.get(0).getDistance()), String.valueOf(Baseennemie.get(0).getAngle()));
				getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.enemyBaseHere, String.valueOf(Baseennemie.get(0).getDistance()), String.valueOf(Baseennemie.get(0).getAngle()));
				getBrain().setDebugString(Constants.enemyBaseHere); 
				getBrain().setDebugStringColor(Color.red); 
				getBrain().setHeading(Baseennemie.get(0).getAngle()); 
				if (!getBrain().isReloaded())
				{
					toReturn = WarRocketLauncher.ACTION_RELOAD; 
				} 
				else
				toReturn = WarRocketLauncher.ACTION_FIRE; 
		//} 
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
		
		//getBrain().setDebugStringColor(Color.BLACK);
		//getBrain().setDebugString("Searching food");
		
		ArrayList<WarPercept> foodPercepts = getBrain().getPerceptsResources();
		
		//Si il y a de la nouriture
		if(!foodPercepts.isEmpty() && WarRocketLauncher.MAX_HEALTH <17*WarRocketLauncher.MAX_HEALTH/20){
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
	
	private String AttackTurret(ArrayList<WarPercept> EnemyTurret)
	{
		getBrain().setHeading(EnemyTurret.get(0).getAngle()); 
		if (!getBrain().isReloaded())
		{
			return WarRocketLauncher.ACTION_RELOAD; 
		} 
		else
		return  WarRocketLauncher.ACTION_FIRE;
	}
	
}