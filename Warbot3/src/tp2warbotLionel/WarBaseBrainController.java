package tp2warbotLionel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import edu.turtlekit3.warbot.agents.agents.WarBase;
import edu.turtlekit3.warbot.agents.enums.WarAgentType;
import edu.turtlekit3.warbot.agents.percepts.WarPercept;
import edu.turtlekit3.warbot.agents.resources.WarFood;
import edu.turtlekit3.warbot.brains.braincontrollers.WarBaseAbstractBrainController;
import edu.turtlekit3.warbot.communications.WarMessage;
import edu.turtlekit3.warbot.tools.CoordPolar;

public class WarBaseBrainController extends WarBaseAbstractBrainController {



	private WarAgentType lastCreateUnit = null;
	private String toReturn;
	
	private static final int MIN_HEATH_TO_CREATE = (int) (WarBase.MAX_HEALTH * 0.8);
	
	public WarBaseBrainController() {
		super();
	}


	@Override
	public String action() {
		
		ArrayList<WarMessage> messages = getBrain().getMessages(); 
		toReturn = null;
		
		handleMessages();
		
		healMySelf();
		
		createUnit(WarAgentType.WarRocketLauncher);  
		ArrayList<WarPercept> EnemyRocketLauncher = getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher);  
		ArrayList<WarPercept> EnemyKamikase = getBrain().getPerceptsEnemiesByType(WarAgentType.WarKamikaze);
		
		if (!EnemyKamikase.isEmpty()) 
		{  
			for (int i=0; i<EnemyKamikase.size(); i++) 
			{
				Double distance = EnemyKamikase.get(i).getDistance(); 
				Double angle = EnemyKamikase.get(i).getAngle();  
				getBrain().broadcastMessageToAgentType(WarAgentType.WarTurret, Constants.baseIsAttack, String.valueOf(distance), String.valueOf(angle));
				getBrain().setDebugString(Constants.baseIsAttack); 
				getBrain().setDebugStringColor(Color.CYAN);
			}
		}
		
		if (getBrain().getHealth() < 17 * WarBase.MAX_HEALTH / 20 && !EnemyRocketLauncher.isEmpty() || !EnemyKamikase.isEmpty()) 
		{
			healMySelf();  
			for (int i=0; i<EnemyRocketLauncher.size(); i++) 
			{
				Double distance = EnemyRocketLauncher.get(i).getDistance(); 
				Double angle = EnemyRocketLauncher.get(i).getAngle(); 
				getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.baseIsAttack, String.valueOf(distance), String.valueOf(angle)); 
				getBrain().broadcastMessageToAgentType(WarAgentType.WarTurret, Constants.baseIsAttack, String.valueOf(distance), String.valueOf(angle));
				getBrain().setDebugString(Constants.baseIsAttack); 
				getBrain().setDebugStringColor(Color.CYAN);
				//return WarBase.ACTION_IDLE; 
			}
		}
		else 
		{
			// j'examine l'ensembles des msg au tick actuel
			for (WarMessage m : messages) 
			{
				//si je recoit un msg d'un explorateur qui dit avoir trouv� la base ennemie, alors j'avertis les rocket launcher
				if(m.getSenderType().equals(WarAgentType.WarExplorer) && m.getMessage().equals(Constants.enemyBaseHere))
				{
					String[] Content = m.getContent(); 
					Double angle = Double.valueOf(Content[1]).doubleValue(); 
					Double distance = Double.valueOf(Content[0]).doubleValue();
					CoordPolar EnemyBasePosition = getBrain().getTargetedAgentPosition(m.getAngle(), m.getDistance(), angle, distance); 
					getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.positionEnnemyBaseFound, String.valueOf(EnemyBasePosition.getDistance()), String.valueOf(EnemyBasePosition.getAngle())); 
					getBrain().setDebugString(Constants.positionEnnemyBaseFound); 
					getBrain().setDebugStringColor(Color.CYAN); 
				}
				
				if (m.getSenderType().equals(WarAgentType.WarExplorer) && m.getMessage().equals(Constants.whereAreYou))
						getBrain().sendMessage(m.getSenderID(), Constants.here, ""); 
			}
		}
		//ArrayList<WarPercept> EnemyRocketLauncher = getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher);
		
		if(toReturn == null)
			toReturn = WarBase.ACTION_IDLE;
		
		return toReturn;
	}

	private void healMySelf() {
		if(toReturn != null)
			return;
		
		if(getBrain().isBagEmpty())
			return;
		
		if(getBrain().getHealth() <= WarBase.MAX_HEALTH - WarFood.HEALTH_GIVEN)
			toReturn = WarBase.ACTION_EAT;
	}

	private void createUnit(WarAgentType a1) {
		if(toReturn != null)
			return;
		
		if(getBrain().getHealth() > MIN_HEATH_TO_CREATE + MIN_HEATH_TO_CREATE/90){
				getBrain().setNextAgentToCreate(a1);
				getBrain().setDebugString("Create: "+a1.name()); 
			
			toReturn = WarBase.ACTION_CREATE;
		}
		
	}
	

	//repond pr�sent quand on lui demande o� il est 
	private void handleMessages() {
		ArrayList<WarMessage> msgs = getBrain().getMessages();
		for(WarMessage msg : msgs) {
			if (msg.getMessage().equals(Constants.whereAreYou)) {
				getBrain().sendMessage(msg.getSenderID(), Constants.here, "");
			}
		}
				
	}
	
}
