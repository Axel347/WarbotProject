package DepretChambon;

import java.awt.Color;
import java.util.ArrayList;

import edu.turtlekit3.warbot.agents.ControllableWarAgent;
import edu.turtlekit3.warbot.agents.MovableWarAgent;
import edu.turtlekit3.warbot.agents.agents.WarBase;
import edu.turtlekit3.warbot.agents.agents.WarExplorer;
import edu.turtlekit3.warbot.agents.agents.WarRocketLauncher;
import edu.turtlekit3.warbot.agents.enums.WarAgentType;
import edu.turtlekit3.warbot.agents.percepts.WarPercept;
import edu.turtlekit3.warbot.brains.braincontrollers.WarExplorerAbstractBrainController;
import edu.turtlekit3.warbot.communications.WarMessage;

public class WarExplorerBrainController extends WarExplorerAbstractBrainController {
	

	private boolean imGiving = false;
	private boolean nearEnemyBase = false;
	private String toReturn;
	private int role = -1; // Espion (1) ou ceuilleur (0) ou Medecin(2)
	ArrayList<WarMessage> messages = new ArrayList<WarMessage>();
	private int compteur_tick = 0;
	private int compteur_base = 0;

	
<<<<<<< HEAD
	
	private static final int COMPTEUR_CERCLE = 50;
	private static final int MAX_DELAI_BASE_ENNEMIE = 1000;
=======
	private static final int COMPTEUR_CERCLE = 50; 
	private static final int MAX_DELAI_BASE_ENNEMIE = 500; //delai au bout du quel la base enemi est considérée comme morte
>>>>>>> dd966f7c2e3f42f4b870b49d354e880e7d42a3a3
	
	
	//FSM *************************
	private Task ctask;
	
	//methode des ceuilleurs, ils se déplacent et récoltent de la nourriture
	private Task searchForFood = new Task(this){
		void exec(WarExplorerBrainController bc){
			getBrain().setDebugStringColor(Color.RED);
			getBrain().setDebugString(role + "    searchForFood");
			getFood();
		} 
	};
	
	//ramènent la nourriture
	private Task goBackHome = new Task(this){

		@Override
		void exec(WarExplorerBrainController bc) {
			getBrain().setDebugStringColor(Color.BLUE);
			getBrain().setDebugString(role + "    goBackHome");
			returnFood();
		}
	
	};
	
	private Task soigne = new Task(this){

		@Override
		void exec(WarExplorerBrainController bc) {
			getBrain().setDebugStringColor(Color.BLUE);
			getBrain().setDebugString(role + "    SOIGNE");
			helpOthers();
		}
	
	};
	
	
	
	//FSM **************************
	
	public WarExplorerBrainController() {
		super();
		ctask = searchForFood; // initialisation de la premiere ctask
	}

	@Override
	public String action() {
		messages = getBrain().getMessages();
		toReturn = null;
		//On prévient de son role et on vérifie que la base ne nous ait pas demandé de chanegr de role
		prevenirBaseRole();
		setRole();
		
		//si on est un espion, on n'utilise pas la FSM car un seul état
		if(role == 1){
			getBrain().setDebugString("ESPION");
			this.detectEnemy();
			
		}
		//sinon les ceuilleurs et médecins utilisent la FSM
		if(role == 0 || role == 2){
			ctask.exec(this);
		}

		
		
		
		if(toReturn == null){
			if (getBrain().isBlocked())
				getBrain().setRandomHeading();
			return WarExplorer.ACTION_MOVE;
		} else {
			return toReturn;
		}
	}
		
		
	
	private WarMessage getMessageAboutFood() {
		for (WarMessage m : messages) {
			if(m.getMessage().equals("foodHere"))
				return m;
		}
		return null;
	}
	
	private WarMessage getMessageFromBase() {
		for (WarMessage m : messages) {
			if(m.getSenderType().equals(WarAgentType.WarBase))
				{return m;}
		}
		
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.whereAreYou, "");
		return null;
	}

	private void getFood() {
		if(getBrain().isBagFull()){
			imGiving = true;
			ArrayList<WarPercept> foodAround = getBrain().getPerceptsResources();
			if(foodAround != null && foodAround.size() > 0){
				getBrain().broadcastMessageToAgentType(WarAgentType.WarExplorer, Constants.foodHere, "");
			}
			if(role ==2){
			ctask = soigne;
			}
			else{
			ctask = goBackHome;
			}
			return;
		}
		
		if(imGiving)
			return;
		
		if(getBrain().isBlocked())
			getBrain().setRandomHeading();
		
		
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
			WarMessage m = getMessageAboutFood();
			if(m != null){
				getBrain().setHeading(m.getAngle());
			}
			toReturn = MovableWarAgent.ACTION_MOVE;
		}
	}
	
	private void returnFood() {
		if(!imGiving)
			return;
		
		if(getBrain().isBagEmpty()){
			imGiving = false;
			getBrain().setHeading(getBrain().getHeading() + 180);
			ctask = searchForFood;
			return;
		}
		//getBrain().setDebugStringColor(Color.green.darker());
		//getBrain().setDebugString("Returning Food");
		
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
			
			toReturn = WarExplorer.ACTION_MOVE;
			
		}else{//si je vois une base
			WarPercept base = basePercepts.get(0);
			
			if(base.getDistance() > WarExplorer.MAX_DISTANCE_GIVE){
				getBrain().setHeading(base.getAngle());
				toReturn = WarExplorer.ACTION_MOVE;
			}else{
				getBrain().setIdNextAgentToGive(base.getID());
				toReturn = WarExplorer.ACTION_GIVE;
			}
			
		}
		
	}
	
	//Mets le role par défaut à ceuilleur, puis crée un médecin ou un espion s'il n'y en a plus de vivant
	private void setRole(){
		if(role == -1){
			role = 0;
			}
		
		for (WarMessage m : messages){
			if(m.getMessage().equals(Constants.espionMort)){
				this.role = 1;
			}
			else if(m.getMessage().equals(Constants.medicMort)){
				this.role = 2;
			}
		}
		
		
	}
	
	//à chaque tour, on préviens la base qu'on est encore vivant, pour pas qu'elle ne crée d'espion supplémentaire
	private void prevenirBaseRole(){
		if(role == 1){ //1 correspond à espion
			getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.espion, "");
		}
		if(role == 2){
			getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.medic, "");
		}
		
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.here, "");
	}
	
	//Role de l'espion, dès qu'il passe près d'une base enemie, il reste autour et envoie en permanence des messages avec les coordonnées de la base
	private void detectEnemy(){
		for(WarPercept p : getBrain().getPerceptsEnemiesByType(WarAgentType.WarBase)){
				getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.enemyBaseHere ,String.valueOf(p.getDistance()), String.valueOf(p.getAngle()));
				getBrain().broadcastMessageToAgentType(WarAgentType.WarKamikaze, Constants.enemyBaseHere ,String.valueOf(p.getDistance()), String.valueOf(p.getAngle()));
				getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.enemyBaseHere ,String.valueOf(p.getDistance()), String.valueOf(p.getAngle()));
				nearEnemyBase = true;
				compteur_base = 0;
		}
		if(nearEnemyBase){getBrain().setDebugString("OK");}
		else{getBrain().setDebugString("NON");}
		
		//on vérifie que la base autour de laquelle on tourne est toujours vivante, si elle est détruite, on pars vers d'autres bases enemies
		if(nearEnemyBase){
			enemyBaseDestroyed();
			compteur_tick++;
			if(compteur_tick == COMPTEUR_CERCLE){
				compteur_tick = 0;	
				getBrain().setHeading(getBrain().getHeading() + 180);
			}
				enemyBaseDestroyed();
				if (getBrain().isBlocked())
					getBrain().setRandomHeading();
				toReturn = WarExplorer.ACTION_MOVE;	
		}

	}
	
	//Verification si la base aux alentours est toujours vivante
	private void enemyBaseDestroyed(){
		compteur_base++;
		if(compteur_base > MAX_DELAI_BASE_ENNEMIE){
			nearEnemyBase = false;
		}
	}
	
	
	//méthode pour le médecin, qui lorsqu'il est appelé par une unité blessée, va vers celle ci pour lui donner de l'energie
	private void helpOthers(){
		
		if(getBrain().isBagEmpty()){
			getBrain().setHeading(getBrain().getHeading() + 180);
			ctask = searchForFood;
			return;
			//si notre sac est vide, on retourne chercher de la nourriture
		}
		
		for (WarMessage m : messages){
			//A EXECUTER UNIQUEMENT SI LA BASE A ASSEZ D'ENERGIE
			if(m.getMessage().equals(Constants.lowEnergy)){
				//System.out.println("ACTION POUR LE MEDIC");
				getBrain().setHeading(m.getAngle());
				toReturn = WarExplorer.ACTION_MOVE;
				
				WarPercept engi = null;
				WarPercept rocket = null;
				ArrayList<WarPercept> engiPercepts = getBrain().getPerceptsAlliesByType(WarAgentType.WarBase);
				if(engiPercepts.size()>0){
				engi = engiPercepts.get(0);
				}
				ArrayList<WarPercept> rocketPercepts = getBrain().getPerceptsAlliesByType(WarAgentType.WarBase);
				if(rocketPercepts.size()>0){
				rocket = rocketPercepts.get(0);
				}
				
				//si on voit un ingénieur blessé on va le soigner
				if(engiPercepts.size() > 0){
					if(engi.getDistance() > WarExplorer.MAX_DISTANCE_GIVE){
						getBrain().setHeading(engi.getAngle());
						toReturn = WarExplorer.ACTION_MOVE;
					}else{
						getBrain().setIdNextAgentToGive(engi.getID());
						toReturn = WarExplorer.ACTION_GIVE;
					}
				}
				//si on voit un lance roquette blessé on va le soigner
				else if(rocketPercepts.size()>0){
					if(rocket.getDistance() > WarExplorer.MAX_DISTANCE_GIVE){
						getBrain().setHeading(rocket.getAngle());
						toReturn = WarExplorer.ACTION_MOVE;
					}else{
						getBrain().setIdNextAgentToGive(rocket.getID());
						toReturn = WarExplorer.ACTION_GIVE;
					}
				}

			}
		}
	}
}
