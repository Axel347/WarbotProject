package DepretChambon;

import java.util.ArrayList;

import edu.turtlekit3.warbot.agents.agents.WarKamikaze;
import edu.turtlekit3.warbot.agents.agents.WarRocketLauncher;
import edu.turtlekit3.warbot.agents.enums.WarAgentType;
import edu.turtlekit3.warbot.agents.percepts.WarPercept;
import edu.turtlekit3.warbot.brains.braincontrollers.WarKamikazeAbstractBrainController;
import edu.turtlekit3.warbot.communications.WarMessage;
import edu.turtlekit3.warbot.tools.CoordPolar;

public class WarKamikazeBrainController extends WarKamikazeAbstractBrainController {
	
	private String toReturn = null;
	private ArrayList<WarMessage> messages;
	private double baseToAttack = 0;
	
	public WarKamikazeBrainController() {
		super();
	}

	@Override
	public String action() {
		
		messages = getBrain().getMessages();
		toReturn = null;
		attaquerBase();
		
		if(toReturn == null){
			if (getBrain().isBlocked())
				getBrain().setRandomHeading();
			toReturn = WarRocketLauncher.ACTION_MOVE;
		}
		
		return toReturn;
	}
	
	
	//si on reçoit un message avec les coordonnées d'une base enemies, on va vers celle ci pour la détruire
	public void attaquerBase(){
		
		//si la base est assez proche, on attaque
		ArrayList<WarPercept> percept = getBrain().getPerceptsEnemiesByType(WarAgentType.WarBase);
		if(percept != null && percept.size() > 0 && percept.get(0).getDistance() < WarKamikaze.HITBOX_RADIUS){
			getBrain().setHeading(percept.get(0).getAngle());
			toReturn = WarKamikaze.ACTION_FIRE;
		}
		
	
		//sinon on vérifie nos messages pour voir si on a reçu les coordonnées de la base, si oui on se dirige vers elle
		for(WarMessage msg : messages) {
			if(msg.getMessage().equals(Constants.enemyBaseHere)){
				CoordPolar p = getBrain().getIndirectPositionOfAgentWithMessage(msg);
				baseToAttack = p.getAngle();
			}
				
		}
		
		if(baseToAttack != 0){
			getBrain().setHeading(baseToAttack);
			System.out.println(baseToAttack);
		}
		
	}
	
}
