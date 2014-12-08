package comptonTeam2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import edu.turtlekit3.warbot.agents.ControllableWarAgent;
import edu.turtlekit3.warbot.agents.MovableWarAgent;
import edu.turtlekit3.warbot.agents.agents.WarBase;
import edu.turtlekit3.warbot.agents.agents.WarExplorer;
import edu.turtlekit3.warbot.agents.enums.WarAgentType;
import edu.turtlekit3.warbot.agents.percepts.WarPercept;
import edu.turtlekit3.warbot.brains.braincontrollers.WarExplorerAbstractBrainController;
import edu.turtlekit3.warbot.communications.WarMessage;

public class WarExplorerBrainController extends WarExplorerAbstractBrainController {
	

	private boolean imGiving = false;
	private String toReturn;
	private Activity ctask = new SeBalader();
	private int poste;
	
	public WarExplorerBrainController() {
		super();
		Random r = new Random();
		this.poste = r.nextInt(2);
	}

	@Override
	public String action() {
		// Develop behaviour here
		
		toReturn = WarExplorer.ACTION_MOVE;
		
		this.todo();
		
		
		return toReturn;
		
	}
		
	
	public abstract class Activity
	{
		public abstract void exec(WarExplorerBrainController explorer);
	}
	
	public class SeBalader extends Activity
	{
		public SeBalader()
		{
			
		}
		
		public void exec(WarExplorerBrainController explorer)
		{
			explorer.setToReturn(MovableWarAgent.ACTION_MOVE);
			
			explorer.getBrain().setDebugStringColor(Color.BLACK);
			explorer.getBrain().setDebugString("se balader");
			
			
			if (explorer.getBrain().isBlocked())
			{
				explorer.getBrain().setRandomHeading();
			}
			
			//ArrayList<WarPercept> percepts = explorer.getBrain().getPerceptsEnemiesByType(WarAgentType.WarBase);
			
			if (explorer.getPoste()==1)
			{
				ArrayList<WarPercept> percepts = explorer.getBrain().getPerceptsEnemies();
				
				if (percepts.size() > 0)
				{
					AlerteBase ab = new AlerteBase();
					explorer.changerEtat(ab);
				}
			}
			else
			{
				ArrayList<WarPercept> foodPercepts = getBrain().getPerceptsResources();
				
				if (foodPercepts.size() > 0)
				{
					PrendreNourriture pn = new PrendreNourriture();
					explorer.changerEtat(pn);
				}
			}
			
		}
	}
	
	public class AlerteBase extends Activity
	{
		public AlerteBase()
		{
			
		}
		
		public void exec(WarExplorerBrainController explorer)
		{
			explorer.setToReturn(MovableWarAgent.ACTION_MOVE);
			
			explorer.getBrain().setDebugStringColor(Color.RED);
			explorer.getBrain().setDebugString("alerter base");
			
			explorer.getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.enemyBaseHere, "Ennemie trouve !!!");
			
			SeBalader sb = new SeBalader();
			explorer.changerEtat(sb);
		}
	}
	
	public class PrendreNourriture extends Activity
	{
		public PrendreNourriture()
		{
			
		}
		
		public void exec(WarExplorerBrainController explorer)
		{
			explorer.getBrain().setDebugStringColor(Color.GREEN);
			explorer.getBrain().setDebugString("collecter nourriture");
			
			ArrayList<WarPercept> foodPercepts = explorer.getBrain().getPerceptsResources();
			
			if (foodPercepts.size() > 0)
			{
				WarPercept foodP = foodPercepts.get(0);
				
				explorer.getBrain().setHeading(foodP.getAngle());
				
				if(foodP.getDistance() > ControllableWarAgent.MAX_DISTANCE_GIVE)
				{
					explorer.getBrain().setHeading(foodP.getAngle());
					explorer.setToReturn(MovableWarAgent.ACTION_MOVE);
				}
				else
				{
					explorer.setToReturn(MovableWarAgent.ACTION_TAKE);
				}
				
				if (explorer.getBrain().isBagFull())
				{
					RentrerBase rb = new RentrerBase();
					explorer.changerEtat(rb);
				}
			}
			else
			{

				if (explorer.getBrain().isBagFull())
				{
					RentrerBase rb = new RentrerBase();
					explorer.changerEtat(rb);
				}
				else
				{
					SeBalader sb = new SeBalader();
					explorer.changerEtat(sb);
				}
			}
			
			
			
		}
	}
	
	public class RentrerBase extends Activity
	{
		double angleBase = 0.0;
		
		public RentrerBase()
		{
			
		}
		
		public void exec(WarExplorerBrainController explorer)
		{
			explorer.getBrain().setDebugStringColor(Color.BLUE);
			explorer.getBrain().setDebugString("rentrer base");
			
			
//			double distance = 0.0;
//			int id = 0;
			for (WarMessage m : explorer.getBrain().getMessages()) 
			{
				if(m.getSenderType().equals(WarAgentType.WarBase))
				{
					if (angleBase == 0.0)
					{
						angleBase = m.getAngle();
					}
//					distance = m.getDistance();
//					id = m.getSenderID();
					
					break;
				}
			}
		
			ArrayList<WarPercept> basePercepts = explorer.getBrain().getPerceptsAlliesByType(WarAgentType.WarBase);
			
			if (basePercepts.size() > 0)
			{
				if (basePercepts.get(0).getDistance() > MovableWarAgent.MAX_DISTANCE_GIVE)
				{
					explorer.getBrain().setHeading(basePercepts.get(0).getAngle());
					explorer.setToReturn(MovableWarAgent.ACTION_MOVE);
				}
				else
				{
					explorer.getBrain().setIdNextAgentToGive(basePercepts.get(0).getID());
					explorer.setToReturn(MovableWarAgent.ACTION_GIVE);
					
				}
			}
			else
			{
				System.out.println(angleBase);
				explorer.getBrain().setHeading(angleBase);
				explorer.setToReturn(MovableWarAgent.ACTION_MOVE);
			}
			
			
			if (explorer.getBrain().isBagEmpty())
			{
				SeBalader sb = new SeBalader();
				explorer.changerEtat(sb);
			}
			
		}
	}
	
	
	public void todo()
	{
		this.ctask.exec(this);
	}
	
	public void changerEtat(Activity a)
	{
		this.ctask = a;
	}
	
	public void setToReturn(String ToReturn)
	{
		this.toReturn = ToReturn;
	}
	
	public int getPoste()
	{
		return this.poste;
	}
	
	public void setPoste(int role)
	{
		this.poste = role;
	}
	
	public void EnvoieMessageBaseRassure()
	{
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.here, "je suis en vie !");
	}
	
//	private void handleMessages(){
//		
//	}
//	
//	private WarMessage getMessageAboutFood() {
//		for (WarMessage m : getBrain().getMessages()) {
//			if(m.getMessage().equals("foodHere"))
//				return m;
//		}
//		return null;
//	}
//	
//	private WarMessage getMessageFromBase() {
//		for (WarMessage m : getBrain().getMessages()) {
//			if(m.getSenderType().equals(WarAgentType.WarBase))
//				return m;
//		}
//		
//		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, "");
//		return null;
//	}
//
//	private void getFood() {
//		if(getBrain().isBagFull()){
//			imGiving = true;
//			return;
//		}
//		
//		if(imGiving)
//			return;
//		
//		if(getBrain().isBlocked())
//			getBrain().setRandomHeading();
//		
//		getBrain().setDebugStringColor(Color.BLACK);
//		getBrain().setDebugString("Searching food");
//		
//		ArrayList<WarPercept> foodPercepts = getBrain().getPerceptsResources();
//		
//		//Si il y a de la nouriture
//		if(foodPercepts != null && foodPercepts.size() > 0){
//			WarPercept foodP = foodPercepts.get(0); //le 0 est le plus proche normalement
//			

//			}
//		} else {
//			toReturn = MovableWarAgent.ACTION_MOVE;
//		}
//	}
//	
//	private void returnFood() {
//		if(!imGiving)
//			return;
//		
//		if(getBrain().isBagEmpty()){
//			imGiving = false;
//			getBrain().setHeading(getBrain().getHeading() + 180);
//			return;
//		}
//			
//		getBrain().setDebugStringColor(Color.green.darker());
//		getBrain().setDebugString("Returning Food");
//		
//		if(getBrain().isBlocked())
//			getBrain().setRandomHeading();
//
//		ArrayList<WarPercept> basePercepts = getBrain().getPerceptsAlliesByType(WarAgentType.WarBase);
//		
//		//Si je ne voit pas de base
//		if(basePercepts == null | basePercepts.size() == 0){
//			
//			WarMessage m = this.getMessageFromBase();
//			//Si j'ai un message de la base je vais vers elle
//			if(m != null)
//				getBrain().setHeading(m.getAngle());
//			
//			//j'envoie un message aux bases pour savoir où elle sont..
//			getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, (String[]) null);
//			
//			toReturn = MovableWarAgent.ACTION_MOVE;
//			
//		}else{//si je vois une base
//			WarPercept base = basePercepts.get(0);
//			
//			if(base.getDistance() > MovableWarAgent.MAX_DISTANCE_GIVE){
//				getBrain().setHeading(base.getAngle());
//				toReturn = MovableWarAgent.ACTION_MOVE;
//			}else{
//				getBrain().setIdNextAgentToGive(base.getID());
//				toReturn = MovableWarAgent.ACTION_GIVE;
//			}
//			
//		}
//		
//	}
//	
//	private void detectionBaseEnnemy()
//	{
//		ArrayList<WarPercept> basePercepts = getBrain().getPerceptsEnemiesByType(WarAgentType.WarBase);
//		
//		if (basePercepts.size() > 0)
//		{
//			String content[] = new String[2];
//			content[0] = String.valueOf(basePercepts.get(0).getDistance());
//			content[1] = String.valueOf(basePercepts.get(0).getAngle());
//			
//			getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.enemyBaseHere, content);
//				
//		}
//	}
}