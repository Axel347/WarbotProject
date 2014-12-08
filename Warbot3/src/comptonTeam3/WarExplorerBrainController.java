package comptonTeam3;

import java.awt.Color;
import java.util.ArrayList;

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
	private String toReturn = null;
	private boolean appelOffreBaseEnnemy = false;
	private int compteurOffre = 0;
	private ArrayList<WarMessage> listeDesProposes = new ArrayList<WarMessage>();
	
	public WarExplorerBrainController() {
		super();
	}

	@Override
	public String action() {
		// Develop behaviour here
		
		//toReturn = null;
		
		if (!this.appelOffreBaseEnnemy)
		{
			this.detectionBaseEnnemy();
		}
		
		if (this.appelOffreBaseEnnemy)
		{
			this.etudieOffre();
		}
		
		if (this.compteurOffre > 0 && this.appelOffreBaseEnnemy)
		{
			this.compteurOffre--;
			this.envoieMessage();
		}
//		else if (this.appelOffreBaseEnnemy && this.compteurOffre == 0)
//		{
//			this.appelOffreBaseEnnemy = false;
//			listeDesProposes = new ArrayList<WarMessage>();
//		}
		
		handleMessages();
		
		
		//getFood();
		
		//returnFood();
		
		if(toReturn == null){
			if (getBrain().isBlocked())
				getBrain().setRandomHeading();
			return WarExplorer.ACTION_MOVE;
		} else {
			return toReturn;
		}
		
		
	}
		
		
	private void handleMessages(){
		
	}
	
	private WarMessage getMessageAboutFood() {
		for (WarMessage m : getBrain().getMessages()) {
			if(m.getMessage().equals("foodHere"))
				return m;
		}
		return null;
	}
	
	private WarMessage getMessageFromBase() {
		for (WarMessage m : getBrain().getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarBase))
				return m;
		}
		
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, "");
		return null;
	}

	private void getFood() {
		if(getBrain().isBagFull()){
			imGiving = true;
			return;
		}
		
		if(imGiving)
			return;
		
		if(getBrain().isBlocked())
			getBrain().setRandomHeading();
		
		getBrain().setDebugStringColor(Color.BLACK);
		getBrain().setDebugString("Searching food");
		
		ArrayList<WarPercept> foodPercepts = getBrain().getPerceptsResources();
		
		//Si il y a de la nouriture
		if(foodPercepts != null && foodPercepts.size() > 0){
			WarPercept foodP = foodPercepts.get(0); //le 0 est le plus proche normalement
			
			if(foodP.getDistance() > ControllableWarAgent.MAX_DISTANCE_GIVE){
				getBrain().setHeading(foodP.getAngle());
				toReturn = MovableWarAgent.ACTION_MOVE;
			}else{
				toReturn = MovableWarAgent.ACTION_TAKE;
			}
		} else {
			toReturn = MovableWarAgent.ACTION_MOVE;
		}
	}
	
	private void returnFood() {
		if(!imGiving)
			return;
		
		if(getBrain().isBagEmpty()){
			imGiving = false;
			getBrain().setHeading(getBrain().getHeading() + 180);
			return;
		}
			
		getBrain().setDebugStringColor(Color.green.darker());
		getBrain().setDebugString("Returning Food");
		
		if(getBrain().isBlocked())
			getBrain().setRandomHeading();

		ArrayList<WarPercept> basePercepts = getBrain().getPerceptsAlliesByType(WarAgentType.WarBase);
		
		//Si je ne voit pas de base
		if(basePercepts == null | basePercepts.size() == 0){
			
			WarMessage m = this.getMessageFromBase();
			//Si j'ai un message de la base je vais vers elle
			if(m != null)
				getBrain().setHeading(m.getAngle());
			
			//j'envoie un message aux bases pour savoir où elle sont..
			getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, (String[]) null);
			
			toReturn = MovableWarAgent.ACTION_MOVE;
			
		}else{//si je vois une base
			WarPercept base = basePercepts.get(0);
			
			if(base.getDistance() > MovableWarAgent.MAX_DISTANCE_GIVE){
				getBrain().setHeading(base.getAngle());
				toReturn = MovableWarAgent.ACTION_MOVE;
			}else{
				getBrain().setIdNextAgentToGive(base.getID());
				toReturn = MovableWarAgent.ACTION_GIVE;
			}
			
		}
		
	}
	
	public void detectionBaseEnnemy()
	{
		ArrayList<WarPercept> baseEnnemy = this.getBrain().getPerceptsEnemiesByType(WarAgentType.WarBase);
		
		if (baseEnnemy != null && baseEnnemy.size() > 0)
		{
			toReturn = WarExplorer.ACTION_IDLE;
			
			this.envoieMessage();
			
			this.appelOffreBaseEnnemy=true;
			
			this.compteurOffre = 2;
		}
		
	}
	
	public void etudieOffre()
	{
		ArrayList<WarMessage> messages = this.getBrain().getMessages();
		ArrayList<WarMessage> listeDesAccepts = new ArrayList<WarMessage>(2);
		
		for (WarMessage m : messages)
		{
			if (m.getMessage().equals("PROPOSE"))
			{
				listeDesProposes.add(m);
			}
		}
		
		if (this.compteurOffre == 1)
		{
			if (listeDesProposes.size() >= 2)
			{
				if (listeDesProposes.size() == 2)
				{
					getBrain().reply(listeDesProposes.get(0), "ACCEPT", "");
					getBrain().reply(listeDesProposes.get(1), "ACCEPT", "");
				}
				else
				{
					
					for (WarMessage m : listeDesProposes)
					{
						if (listeDesAccepts.size() == 2)
						{
							if (new Integer(listeDesAccepts.get(0).getContent()[0]).intValue() >  new Integer(listeDesAccepts.get(1).getContent()[0]).intValue())
							{
								if (new Integer(m.getContent()[0]).intValue() > new Integer(listeDesAccepts.get(1).getContent()[0]).intValue())
								{
									listeDesAccepts.set(1, m);
								}
							}
							else
							{
								if (new Integer(m.getContent()[0]).intValue() > new Integer(listeDesAccepts.get(0).getContent()[0]).intValue())
								{
									listeDesAccepts.set(0, m);
								}
							}
						}
						else
						{
							listeDesAccepts.add(m);
						}
						
					}
				
				}
			}
			
			for (WarMessage m : listeDesAccepts)
			{
				getBrain().reply(m, "ACCEPT", "");
			}
		}
			
	}
	
	public void envoieMessage()
	{
		this.getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, "CFP", "");
	}
}