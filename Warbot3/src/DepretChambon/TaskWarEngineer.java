package DepretChambon;

import DepretChambon.WarEngineerBrainController;

public abstract class TaskWarEngineer {
	
	WarEngineerBrainController bc;
	
	abstract void exec(WarEngineerBrainController bc);
	
	public TaskWarEngineer(WarEngineerBrainController b){
		this.bc = b;
	}
}
