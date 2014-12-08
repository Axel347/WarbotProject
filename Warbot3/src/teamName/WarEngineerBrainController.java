package teamName;

import edu.turtlekit3.warbot.agents.agents.WarEngineer;
import edu.turtlekit3.warbot.brains.braincontrollers.WarEngineerAbstractBrainController;

public class WarEngineerBrainController extends WarEngineerAbstractBrainController {
	
	public WarEngineerBrainController() {
		super();
	}

	@Override
	public String action() {
		// Develop behaviour here
		
		if (getBrain().isBlocked())
			getBrain().setRandomHeading();
		return WarEngineer.ACTION_MOVE;
	}
}
