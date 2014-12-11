package DepretChambon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;


import edu.turtlekit3.warbot.agents.MovableWarAgent;
import edu.turtlekit3.warbot.agents.agents.WarBase;
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
	boolean iAbleToFireBase = false;
	private int compteur_tick = 0;
	private int compteur_tick_rocket = 0;
	
	private static final int ANGLE_EVITER = 60;
	private static final int COMPTEUR_EVITER = 20;
	private static final int COMPTEUR_CERCLE = 10;
	private static final int DISTANCE_BASE = 10;
	private static final int ANGLE_CERCLE = 20;
	

	ArrayList<WarMessage> messages;
	
	//FSM *****************************
		private TaskRocketLauncher ctask;
		
		private TaskRocketLauncher defense = new TaskRocketLauncher(this){
			void exec(WarRocketLauncherBrainController bc){
				
				getBrain().setDebugStringColor(Color.BLUE);
				getBrain().setDebugString("defense");
				
				WarMessage m = getFormatedMessageAboutEnemyTankToKill();
				
				if((getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher)).size() > 0 || m != null){
					ctask = attaquer;
				}
				else
				{
					compteur_tick++;
					if(compteur_tick == COMPTEUR_CERCLE){
						compteur_tick = 0;	
						getBrain().setHeading(getBrain().getHeading() - ANGLE_CERCLE);
					}
					garderDistanceBase();
					
					if (getBrain().isBlocked())
						getBrain().setRandomHeading();
					toReturn = WarRocketLauncher.ACTION_MOVE;
				}
			} 
		};
		
		private TaskRocketLauncher attaquer = new TaskRocketLauncher(this){
			void exec(WarRocketLauncherBrainController bc){
				
				getBrain().setDebugStringColor(Color.RED);
				getBrain().setDebugString("attaquer");
				
				ArrayList<WarPercept> rocket = getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocket);
				
				if (rocket.size() > 0)
				{
					ctask = eviter;
				}
				else
				{
					attackRocketLaunchers(WarAgentType.WarRocketLauncher);
					attackRocketLaunchers(WarAgentType.WarExplorer);
					
					WarMessage m = getFormatedMessageAboutEnemyTankToKill();
					
					if((getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher)).size() == 0 && m == null){
						ctask = defense;
					}
				}
			} 
		};
		
		private TaskRocketLauncher eviter = new TaskRocketLauncher(this){
			void exec(WarRocketLauncherBrainController bc){
				
				getBrain().setDebugStringColor(Color.GREEN);
				getBrain().setDebugString("eviter");
				
				if (compteur_tick_rocket == 0)
				{	
					getBrain().setHeading(getBrain().getHeading() + ANGLE_EVITER);
					compteur_tick_rocket = COMPTEUR_EVITER;
				}
				
				compteur_tick_rocket--;
				
				if (compteur_tick_rocket == 0)
				{
					ctask = attaquer;
				}
				
				toReturn = WarRocketLauncher.ACTION_MOVE;
			}
		};
		
		//FSM *****************************
	
	public WarRocketLauncherBrainController() {
		super();
		ctask = defense;
	}
	
	
	
	
	@Override
	public String action() {
		// Develop behaviour here
		
		toReturn = null;
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, "");
		
		this.messages = getBrain().getMessages();
		
	
		ctask.exec(this);
		
		
		/*handleMessages();
		
		// if(iAbleToFireBase)
			attaquerBase();
		
		if(!iAbleToFireBase)
			attackRocketLaunchers();
		
		wiggle();*/
		
		System.out.println(toReturn + " " + getBrain().getDebugString());
		if(toReturn == null){
			if (getBrain().isBlocked())
				getBrain().setRandomHeading();
			toReturn = WarRocketLauncher.ACTION_MOVE;
		}
		
		return toReturn;
	}
	
	private void attaquerBase() {
		if(toReturn != null)
			return;
		
		if(!getBrain().isReloaded() && !getBrain().isReloading()){
			toReturn =  WarRocketLauncher.ACTION_RELOAD;
			return;
		}
		
		getBrain().setDebugStringColor(Color.blue);
		getBrain().setDebugString("Attack launchers");
		
		ArrayList<WarPercept> percept = getBrain().getPerceptsEnemiesByType(WarAgentType.WarBase);
		
		// Je un agentType dans le percept
		if(percept != null && percept.size() > 0){
			
			//je le dit aux autres
			getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.enemyTankHere, String.valueOf(percept.get(0).getDistance()), String.valueOf(percept.get(0).getAngle()));
			
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
	
	private void attackRocketLaunchers(WarAgentType w) {
		if(toReturn != null)
			return;
		
		if(!getBrain().isReloaded() && !getBrain().isReloading()){
			toReturn =  WarRocketLauncher.ACTION_RELOAD;
			return;
		}
		
		getBrain().setDebugStringColor(Color.blue);
		getBrain().setDebugString("Attack launchers");
		
		ArrayList<WarPercept> percept = getBrain().getPerceptsEnemiesByType(w);
		
		// Je un agentType dans le percept
		if(percept != null && percept.size() > 0){
			
			//je le dit aux autres
			getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.enemyTankHere, String.valueOf(percept.get(0).getDistance()), String.valueOf(percept.get(0).getAngle()));
			
			if(getBrain().isReloaded()){
				
				int indMinLife = perceptMinimumVie(percept);
				getBrain().setHeading(percept.get(indMinLife).getAngle());
				if (!detectionTirAllie(percept.get(indMinLife)))
				{
					toReturn = WarRocketLauncher.ACTION_FIRE;
				}
				
			}else{
				
				//si je suis pas trop pres de l'enemy je m'approche
				
//				if(percept.get(0).getDistance() > WarRocket.EXPLOSION_RADIUS + 1)
//					toReturn = WarRocketLauncher.ACTION_MOVE;
//				else
					toReturn = WarRocketLauncher.ACTION_IDLE;
			}
		}else{
			//si j'ai un message me disant qu'il y a  un autre tank a tuer
			
			WarMessage m = getFormatedMessageAboutEnemyTankToKill();
			if(m != null){
				
				CoordPolar p = getBrain().getIndirectPositionOfAgentWithMessage(m);
				getBrain().setHeading(p.getAngle());
				if (p.getDistance() <= WarRocketLauncher.DISTANCE_OF_VIEW)
				{
					System.out.println(p.getDistance() + "  " + WarRocketLauncher.DISTANCE_OF_VIEW);
					toReturn = WarRocketLauncher.ACTION_IDLE;
				}
				else
				{
					System.out.println(p.getDistance() + "  " + WarRocketLauncher.DISTANCE_OF_VIEW);
					toReturn = WarRocketLauncher.ACTION_MOVE;
				}
				
			}
		}		
	}
	
	private void wiggle() {
		if(toReturn != null)
			return;
		
		if(getBrain().isBlocked())
			getBrain().setRandomHeading();
		
		getBrain().setDebugStringColor(Color.black);
		getBrain().setDebugString("Looking for ennemies");
		
		double angle = getBrain().getHeading() + new Random().nextInt(10) - new Random().nextInt(10);
		
		getBrain().setHeading(angle);
	
		toReturn = MovableWarAgent.ACTION_MOVE;		
	}

	private WarMessage getFormatedMessageAboutEnemyTankToKill() {
		for (WarMessage m : this.messages) {
			if(m.getMessage().equals(Constants.enemyTankHere) && m.getContent() != null && m.getContent().length == 2){
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
	
	private void garderDistanceBase(){
		for(WarMessage m : this.messages){
			if(m.getSenderType().equals(WarAgentType.WarBase)){
				if(WarBase.DISTANCE_OF_VIEW + DISTANCE_BASE < m.getDistance()){
					getBrain().setHeading(m.getAngle());
				}
				else if(m.getDistance() > WarBase.DISTANCE_OF_VIEW + DISTANCE_BASE){
					getBrain().setHeading(m.getAngle() + 180);
				}
			}
		}
	}
	
	private int perceptMinimumVie(ArrayList<WarPercept> e)
	{
		int life = e.get(0).getHealth();
		int j=0;
		
		for (int i=1; i<e.size(); i++)
		{
			if (life > e.get(i).getHealth())
			{
				life = e.get(i).getHealth();
				j=i;
			}
		}
		
		return j;
	}
	
	private boolean detectionTirAllie(WarPercept wp)
	{
		ArrayList<WarPercept> e = getBrain().getPerceptsAllies();
		
		if (e.size() > 0)
		{
			for (int i=0; i<e.size(); i++)
			{
				if (e.get(i).getDistance() < wp.getDistance())
				{
					return true;
				}
			}
			
			return false;
		}
		else
		{
			return false;
		}
			
	}
	
}