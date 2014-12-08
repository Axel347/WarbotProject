package myTeam;

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
	

	ArrayList<WarMessage> messages;
	
	public WarRocketLauncherBrainController() {
		super();
	}
	
	@Override
	public String action() {
		// Develop behaviour here
		
		toReturn = null;
		this.messages = getBrain().getMessages();
		
		handleMessages();
		
		// if(iAbleToFireBase)
		//	 attaquerBase();
		
		if(!iAbleToFireBase)
			attackRocketLaunchers();
		
		wiggle();
		
		if(toReturn == null){
			if (getBrain().isBlocked())
				getBrain().setRandomHeading();
			toReturn = WarRocketLauncher.ACTION_MOVE;
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
		
		getBrain().setDebugStringColor(Color.blue);
		getBrain().setDebugString("Attack launchers");
		
		ArrayList<WarPercept> percept = getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher);
		
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
}