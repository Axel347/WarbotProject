package teamName;

import edu.turtlekit3.warbot.agents.agents.WarRocketLauncher;
import edu.turtlekit3.warbot.brains.braincontrollers.WarRocketLauncherAbstractBrainController;

public class WarRocketLauncherBrainController extends WarRocketLauncherAbstractBrainController {

	public WarRocketLauncherBrainController() {
		super();
	}
	
	@Override
	public String action() {
		// Develop behaviour here
		
		if (getBrain().isBlocked())
			getBrain().setRandomHeading();
		return WarRocketLauncher.ACTION_MOVE;
	}
}