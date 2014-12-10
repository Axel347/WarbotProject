package DepretChambon;

import DepretChambon.WarRocketLauncherBrainController;

public abstract class TaskRocketLauncher {
	
	WarRocketLauncherBrainController bc;
	
	abstract void exec(WarRocketLauncherBrainController bc);
	
	public TaskRocketLauncher(WarRocketLauncherBrainController b){
		this.bc = b;
	}
}

