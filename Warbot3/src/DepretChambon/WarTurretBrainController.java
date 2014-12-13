package DepretChambon;

import java.util.ArrayList;

import edu.turtlekit3.warbot.agents.agents.WarTurret;
import edu.turtlekit3.warbot.agents.enums.WarAgentType;
import edu.turtlekit3.warbot.agents.percepts.WarPercept;
import edu.turtlekit3.warbot.brains.braincontrollers.WarTurretAbstractBrainController;

public class WarTurretBrainController extends WarTurretAbstractBrainController {
	
	public WarTurretBrainController() {
		super();
	}

	@Override
	public String action() {
		// Develop behaviour here
		getBrain().setRandomHeading();
		
		return WarTurret.ACTION_IDLE;
	}
	
	private void attaquerEnnemy()
	{
		ArrayList<WarPercept> ennemy = getBrain().getPerceptsEnemiesByType(WarAgentType.WarKamikaze);
		
		if (ennemy.size() > 0)
		{
			
		}
		else
		{
			ennemy = getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher);
			
			if (ennemy.size() > 0)
			{
				
			}
			else
			{
				ennemy = getBrain().getPerceptsEnemiesByType(WarAgentType.WarEngineer);
				
				if (ennemy.size() > 0)
				{
					
				}
				else
				{
					ennemy = getBrain().getPerceptsEnemies();
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
	
}
