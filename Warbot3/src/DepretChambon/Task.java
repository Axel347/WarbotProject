package DepretChambon;

import DepretChambon.WarExplorerBrainController;

public abstract class Task {
	
	WarExplorerBrainController bc;
	
	abstract void exec(WarExplorerBrainController bc);
	
	public Task(WarExplorerBrainController b){
		this.bc = b;
	}
}

