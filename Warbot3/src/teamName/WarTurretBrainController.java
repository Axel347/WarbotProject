package teamName;

import edu.turtlekit3.warbot.agents.agents.WarTurret;
import edu.turtlekit3.warbot.brains.braincontrollers.WarTurretAbstractBrainController;

public class WarTurretBrainController extends WarTurretAbstractBrainController {
	
	public WarTurretBrainController() {
		super();
	}

	@Override
	public String action() {
		// Develop behaviour here
		
		return WarTurret.ACTION_IDLE;
	}
}
