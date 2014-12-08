package teamName;

import edu.turtlekit3.warbot.agents.agents.WarKamikaze;
import edu.turtlekit3.warbot.brains.braincontrollers.WarKamikazeAbstractBrainController;

public class WarKamikazeBrainController extends WarKamikazeAbstractBrainController {
	
	public WarKamikazeBrainController() {
		super();
	}

	@Override
	public String action() {
		// Develop behaviour here
		
		if (getBrain().isBlocked())
			getBrain().setRandomHeading();
		return WarKamikaze.ACTION_MOVE;
	}
}
