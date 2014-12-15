package DepretChambon;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;




import edu.turtlekit3.warbot.agents.MovableWarAgent;
import edu.turtlekit3.warbot.agents.agents.WarBase;
import edu.turtlekit3.warbot.agents.agents.WarRocketLauncher;
import edu.turtlekit3.warbot.agents.enums.WarAgentType;
import edu.turtlekit3.warbot.agents.percepts.WarPercept;
import edu.turtlekit3.warbot.agents.projectiles.WarBomb;
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
	private int compteur_tick_defense = 0;
	private int compteur_tick_atqBase = 0;
	private boolean modeAtqBase = false;
	
	private static final int COMPTEUR_ATQ_BASE_MAX = 300;
	private static final int COMPTEUR_DEFENSE_MAX = 10;
	private static final int ANGLE_EVITER = 90;
	private static final int COMPTEUR_EVITER = 20;
	private static final int COMPTEUR_CERCLE = 10;
	private static final int DISTANCE_BASE = 10;
	private static final int ANGLE_CERCLE = 20;
	private static final int MIN_ENERGY = (int) (WarRocketLauncher.MAX_HEALTH * 0.7);
	private static final int BORNE_MAX_EXPLOSION = 2;
	

	ArrayList<WarMessage> messages;
	
	//FSM *****************************
		private TaskRocketLauncher ctask;
		
		private TaskRocketLauncher defense = new TaskRocketLauncher(this){
			void exec(WarRocketLauncherBrainController bc){
				
				getBrain().setDebugStringColor(Color.BLUE);
				getBrain().setDebugString("defense");
				
				acceptAppelOffreAtqBase();
				
				
				WarMessage m = getFormatedMessageAboutEnemyTankToKill();
				WarMessage message = appelOffreObtenuAtqBase();
				
				if((getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher)).size() > 0 || m != null){
					ctask = attaquer;
				}
				else if (message != null)
				{
					//System.out.println(message.toString());
					ctask = attaquerBase;
				}
				else
				{
					
					if (getBrain().isBlocked())
					{
						getBrain().setRandomHeading();
					}
					else
					{
						compteur_tick++;
						if(compteur_tick == COMPTEUR_CERCLE){
							compteur_tick = 0;	
							getBrain().setHeading(getBrain().getHeading() - ANGLE_CERCLE);
						}
						
						
					}
					
					garderDistanceBase();
					
					eviterObstacle();
					
					toReturn = WarRocketLauncher.ACTION_MOVE;
				}
				
			} 
		};
		
		private TaskRocketLauncher attaquer = new TaskRocketLauncher(this){
			void exec(WarRocketLauncherBrainController bc){
				
				getBrain().setDebugStringColor(Color.RED);
				getBrain().setDebugString("attaquer");
				
				ArrayList<WarPercept> rocket = getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocket);
				
				if (rocket.size() > 0 && rocket.get(0).getDistance() <= WarRocket.EXPLOSION_RADIUS + 5)
				{
					ctask = eviter;
				}
				else
				{
					//attackRocketLaunchers(WarAgentType.WarExplorer);
					
					WarMessage m = getFormatedMessageAboutEnemyTankToKill();
					
					//System.out.println(m);
					
					if((getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher)).size() == 0 && m == null){
						
						compteur_tick_defense++;
						
						if (compteur_tick_defense == COMPTEUR_DEFENSE_MAX)
						{
							ctask = defense;
							compteur_tick_defense = 0;
						}
//						else
//						{
//							attackRocketLaunchers(WarAgentType.WarRocketLauncher);
//						}
					}
					else
					{
						compteur_tick_defense = 0;
						attackRocketLaunchers(WarAgentType.WarRocketLauncher);
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
					if (getBrain().isBlocked())
					{
						eviterObstacle();
						
					}
					else
					{
						getBrain().setHeading(getBrain().getHeading() + ANGLE_EVITER);
					}
					
					compteur_tick_rocket = COMPTEUR_EVITER;
					
				}
				else
				{
					compteur_tick_rocket--;
					eviterObstacle();
				}
				
				
				if (compteur_tick_rocket == 0)
				{
					if (modeAtqBase)
					{
						ctask = attaquerBase;
					}
					else
					{
						ctask = attaquer;
					}
					
				}
				
				toReturn = WarRocketLauncher.ACTION_MOVE;
				
			}
		};
		
		private TaskRocketLauncher attaquerBase = new TaskRocketLauncher(this){
			void exec(WarRocketLauncherBrainController bc){
				
				getBrain().setDebugStringColor(Color.PINK);
				getBrain().setDebugString("attaquerBase");
				
				
				if(!getBrain().isReloaded() && !getBrain().isReloading())
				{
					toReturn =  WarRocketLauncher.ACTION_RELOAD;
				}
				
				ArrayList<WarPercept> percept = getBrain().getPerceptsEnemiesByType(WarAgentType.WarBase);
				
				if(percept != null && percept.size() > 0)
				{
					
					if(getBrain().isReloaded())
					{	
						getBrain().setHeading(percept.get(0).getAngle());
						toReturn = WarRocketLauncher.ACTION_FIRE;
					}
					else
					{
						getBrain().setHeading(getBrain().getHeading()+180);
						toReturn = WarRocketLauncher.ACTION_IDLE;
					}
					
				}
				else
				{
					percept = getBrain().getPerceptsEnemiesByType(WarAgentType.WarBomb);
					
					if (percept != null && percept.size() > 0)
					{
						int indMinDistRocket = perceptMinimumDistanceRocket(percept);
						
						if (percept.get(indMinDistRocket).getDistance() <= WarBomb.EXPLOSION_RADIUS + BORNE_MAX_EXPLOSION)
						{
							ctask = eviter;
						}
						else
						{
							//getBrain().setHeading(getBrain().getHeading()+180);
							//toReturn = WarRocketLauncher.ACTION_IDLE;
						}
					}
					else
					{	
						percept = getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocket);
						
						if (percept != null && percept.size() > 0)
						{
							int indMinDistRocket = perceptMinimumDistanceRocket(percept);
							
							if (percept.get(indMinDistRocket).getDistance() <= WarRocket.EXPLOSION_RADIUS + BORNE_MAX_EXPLOSION)
							{
								ctask = eviter;
							}
							else
							{
								//getBrain().setHeading(getBrain().getHeading()+180);
								//toReturn = WarRocketLauncher.ACTION_IDLE;
							}
						}
						else
						{
							WarMessage m = getMessageAboutEnemyBase();
							
							if(m != null)
							{
								compteur_tick_atqBase = 0;
								CoordPolar p = getBrain().getIndirectPositionOfAgentWithMessage(m);
								getBrain().setHeading(p.getAngle());
								toReturn = WarRocketLauncher.ACTION_MOVE;
							}
							else
							{
								compteur_tick_atqBase++;
							}
						}
					}
				}
				
				if (compteur_tick_atqBase >= COMPTEUR_ATQ_BASE_MAX)
				{
					modeAtqBase = false;
					ctask=defense;
					compteur_tick_atqBase = 0;
				}
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
		
		//Chaque tour on envoi un message a la base pour dire qu'on est toujours vivant
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.rocketLauncherAlive, "");
		
		
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, "");
		
		this.messages = getBrain().getMessages();
		
		checkEnergy();
		ctask.exec(this);
		
		if(toReturn == null){
			if (getBrain().isBlocked())
				getBrain().setRandomHeading();
			toReturn = WarRocketLauncher.ACTION_MOVE;
		}
		
		return toReturn;
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
			//getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.enemyTankHere, String.valueOf(percept.get(0).getDistance()), String.valueOf(percept.get(0).getAngle()));
			
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
			
			ArrayList<WarMessage> mess = getFormatedMessageAboutEnemyInBase();
			
			if(mess.size() > 0){
				
				
				CoordPolar p = getBrain().getIndirectPositionOfAgentWithMessage(mess.get(0));
				double distance = p.getDistance();
				double angle = p.getAngle();
				
				for (int i=1 ;i<mess.size(); i++)
				{
					p = getBrain().getIndirectPositionOfAgentWithMessage(mess.get(i));
					if (distance > p.getDistance())
					{
						distance = p.getDistance();
						angle = p.getAngle();
					}
				}
				
				getBrain().setHeading(angle);
				if (distance <= WarRocketLauncher.DISTANCE_OF_VIEW)
				{
					//System.out.println(p.getDistance() + "  " + WarRocketLauncher.DISTANCE_OF_VIEW);
					toReturn = WarRocketLauncher.ACTION_IDLE;
				}
				else
				{
					//System.out.println(p.getDistance() + "  " + WarRocketLauncher.DISTANCE_OF_VIEW);
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
	
	private void checkEnergy(){
		if(getBrain().getHealth() < MIN_ENERGY){
			getBrain().broadcastMessageToAgentType(WarAgentType.WarExplorer, Constants.lowEnergy, "");
		}
	}
	
	private ArrayList<WarMessage> getFormatedMessageAboutEnemyInBase() {
		
		ArrayList<WarMessage> mess = new ArrayList<WarMessage>();
		for (WarMessage m : this.messages) {
			if(m.getMessage().equals(Constants.enemyTankHere) && m.getContent() != null && m.getContent().length == 2){
				mess.add(m);
			}
		}
		return mess;
	}
	
	private void eviterObstacle()
	{
		ArrayList<WarPercept> percept = getBrain().getPerceptsAlliesByType(WarAgentType.WarRocketLauncher);
		
		for (WarPercept p : percept)
		{
			if (p.getDistance() < WarRocket.EXPLOSION_RADIUS + 5)
			{
				getBrain().setHeading(getBrain().getHeading() + 180);
				return;
			}
		}
	}
	
	private void acceptAppelOffreAtqBase() {
		
		for (WarMessage m : this.messages) {
			if(m.getMessage().equals(Constants.appelOffreAtqBase)){
				if (!modeAtqBase)
				{
					getBrain().reply(m, Constants.acceptOffreAtqBase, "");
					modeAtqBase = true;
				}
				
				return;
			}
		}
	}
	
	private WarMessage appelOffreObtenuAtqBase() {
		
		for (WarMessage m : this.messages) {
			if(m.getMessage().equals(Constants.offreAtqBaseConfirme)){
				return m;
			}
		}
		
		return null;
	}
	
	private int perceptMinimumDistanceRocket(ArrayList<WarPercept> e)
	{
		double distance = e.get(0).getDistance();
		int j=0;
		
		for (int i=1; i<e.size(); i++)
		{
			if (distance > e.get(i).getDistance())
			{
				distance = e.get(i).getDistance();
				j=i;
			}
		}
		
		return j;
	}
}