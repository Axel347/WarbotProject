package teamName;

import edu.turtlekit3.warbot.agents.agents.WarBase;
import edu.turtlekit3.warbot.brains.braincontrollers.WarBaseAbstractBrainController;

public class WarBaseBrainController extends WarBaseAbstractBrainController {

	public WarBaseBrainController() {
		super();
	}

	@Override
	public String action() {
		// Develop behaviour here
		
		return WarBase.ACTION_IDLE;
	}
}
