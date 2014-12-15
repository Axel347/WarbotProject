package DepretChambon;

import java.util.ArrayList;

import edu.turtlekit3.warbot.agents.agents.WarRocketLauncher;
import edu.turtlekit3.warbot.agents.agents.WarTurret;
import edu.turtlekit3.warbot.agents.enums.WarAgentType;
import edu.turtlekit3.warbot.agents.percepts.WarPercept;
import edu.turtlekit3.warbot.brains.braincontrollers.WarTurretAbstractBrainController;
import edu.turtlekit3.warbot.communications.WarMessage;

public class WarTurretBrainController extends WarTurretAbstractBrainController {
	
	private String toReturn;
	private ArrayList<WarMessage> messages;

	public WarTurretBrainController() {
		super();
	}

	@Override
	public String action() {
		// Develop behaviour here
		toReturn = null;
		
		this.messages = getBrain().getMessages();
		
		this.prevenirPresence();
		
		this.attaquerEnnemy();
		
		if (toReturn == null)
		{
			WarMessage m = this.messageFromBase();
			
			if (m != null)
			{
				getBrain().setHeading(m.getAngle()+180);
			}
			
			
			toReturn = WarTurret.ACTION_IDLE;
		}
		
		return toReturn;
	}
	
	private void attaquerEnnemy()
	{
		if(!getBrain().isReloaded() && !getBrain().isReloading()){
			toReturn =  WarRocketLauncher.ACTION_RELOAD;
			return;
		}
		
		if(getBrain().isReloaded()){
			
		
			ArrayList<WarPercept> ennemy = getBrain().getPerceptsEnemiesByType(WarAgentType.WarKamikaze);
			
			if (ennemy.size() > 0)
			{
				int i = ennemyLePlusProche(ennemy);
				
				getBrain().setHeading(ennemy.get(i).getAngle());
				
				toReturn = WarRocketLauncher.ACTION_FIRE;
			}
			else
			{
				ennemy = getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher);
				
				if (ennemy.size() > 0)
				{
					int i = ennemyLePlusProche(ennemy);
					
					getBrain().setHeading(ennemy.get(i).getAngle());
					
					toReturn = WarRocketLauncher.ACTION_FIRE;
				}
				else
				{
					ennemy = getBrain().getPerceptsEnemiesByType(WarAgentType.WarEngineer);
					
					if (ennemy.size() > 0)
					{
						int i = ennemyLePlusProche(ennemy);
						
						getBrain().setHeading(ennemy.get(i).getAngle());
						
						toReturn = WarRocketLauncher.ACTION_FIRE;
					}
					else
					{
						ennemy = getBrain().getPerceptsEnemies();
						
						if (ennemy.size() > 0)
						{
							int i = ennemyLePlusProche(ennemy);
							
							getBrain().setHeading(ennemy.get(i).getAngle());
							
							toReturn = WarRocketLauncher.ACTION_FIRE;
						}
							
					}
				}
			}
		}
	}
	
	private void prevenirPresence()
	{
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.here, "");
	}
	
	private int ennemyLePlusProche(ArrayList<WarPercept> p)
	{
		int indDistMin = 0;
		double distanceMin = p.get(0).getDistance();
		
		for (int i=0; i<p.size(); i++)
		{
			if (distanceMin > p.get(i).getDistance())
			{
				distanceMin = p.get(i).getDistance();
				indDistMin = i;
			}
		}
		
		return indDistMin;
	}
	
	private WarMessage messageFromBase()
	{
		for (WarMessage m : this.messages)
		{
			if (m.getSenderType().equals(WarAgentType.WarBase) && m.getMessage().equals(Constants.here))
			{
				return m;
			}
		}
		
		return null;
	}
	
}
