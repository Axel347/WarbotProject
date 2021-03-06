package DepretChambon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;



import edu.turtlekit3.warbot.agents.agents.WarBase;
import edu.turtlekit3.warbot.agents.agents.WarRocketLauncher;
import edu.turtlekit3.warbot.agents.enums.WarAgentType;
import edu.turtlekit3.warbot.agents.percepts.WarPercept;
import edu.turtlekit3.warbot.agents.resources.WarFood;
import edu.turtlekit3.warbot.brains.braincontrollers.WarBaseAbstractBrainController;
import edu.turtlekit3.warbot.communications.WarMessage;
import edu.turtlekit3.warbot.tools.CoordPolar;

public class WarBaseBrainController extends WarBaseAbstractBrainController {



	private WarAgentType lastCreateUnit = null;
	private String toReturn;
	private int cptEspionMort = 0;
	private int cptCreation = 0;
	private ArrayList<WarMessage> msgs;
	private HashMap<Double,String> anglesTourelles;
	private HashMap<Double,Integer> etatsTourelles;
	private ArrayList<WarMessage> offreInge;
	private ArrayList<WarMessage> offreAtqBase;
	
	private CoordPolar coordonneeBase = null;
	private int cptTickNbTank = 0;
	int cptMedicMort = 0;
	private boolean groupeExistant = false;
	private int cptAppelOffre = 0;
	private int cptMortEnnemie = 0;
	private int cptDetectionBaseEnnemy = 0;
	private int cptTank = 0;
	
	
	private static final int COMPTEUR_MORT_ENNEMIE_MAX = 3; 
	private static final int DELAI_ESPION = 3;
	private static final int CREATION_TANKS = 6;
	private static final int CREATION_INGE = 1;
	private static final int CREATION_KAM = 8;
	private static final int COMPTEUR_MAX_TURRET = 3;
	private static final int COMPTEUR_REBOOT = 15;
	private static final int COMPTEUR_MAX_TANK = 10;
	private static final int NB_TANK_ATQ = 5;
	private static final int DELAI_DETECTION_EB = 300;
	
	
	
	private static final int MIN_HEATH_TO_CREATE = (int) (WarBase.MAX_HEALTH * 0.8);
	
	
	
	
	public WarBaseBrainController() {
		super();
		
		anglesTourelles = new HashMap<Double,String>();
		etatsTourelles = new HashMap<Double,Integer>();
		offreInge = new ArrayList<WarMessage>();
		offreAtqBase = new ArrayList<WarMessage>();
		
		for (int i=1; i<=360; i++)
		{
			if (i%45 == 0)
			{
				anglesTourelles.put(new Double(i), "");
				etatsTourelles.put(new Double(i), new Integer(0));
			}
		}
		
	}


	@Override
	public String action() {
		
		toReturn = null;
		msgs = getBrain().getMessages();
		getBrain().broadcastMessageToAll(Constants.here, "");

		baseAttaque();
		
		attribuerRole();
		
		handleMessages();
		
		healMySelf();
		
		determinerCreation();
		
		if (!this.IngenieurEmpty())
		{
			this.appelOffreIngenieur();
			
			this.deroulementAppelOffreIngenieur();
			
			this.etatConstructionTurret();
		}
		
		//this.etatTourelles();
		
		this.cptTank = nbTankAllieInBase();
		
		this.compterMort();
		
		WarMessage mess = getMessageAboutEnnemyBase();
		
		if (mess != null)
		{
			cptDetectionBaseEnnemy = 0;
			
			if (cptTickNbTank == 0)
			{
				if (this.offreAtqBase.size() < NB_TANK_ATQ)
				{
					appelOffreTankAtqBase();
					recruterOffreAtqBase(NB_TANK_ATQ);
				}
			}
			else
			{
				System.out.println(offreAtqBase.size() + " < " + (this.cptTank/2));
				
				if (!groupeExistant)
				{
					if (this.offreAtqBase.size() < (this.cptTank/2))
					{
						System.out.println(offreAtqBase.toString());
						cptTickNbTank=0;
						
						appelOffreTankAtqBase();
						recruterOffreAtqBase(this.cptTank/2);
					}
					else
					{
						recruterOffreAtqBase(this.cptTank/2);
					}
				}
				
			}
			
		}
		else
		{
			cptDetectionBaseEnnemy++;
			
			if (cptDetectionBaseEnnemy >= DELAI_DETECTION_EB)
			{
				groupeExistant=false;
				cptDetectionBaseEnnemy = 0;
				this.offreAtqBase = new ArrayList<WarMessage>();
			}
		}
		
		
		
		if(toReturn == null)
			toReturn = WarBase.ACTION_IDLE;
		
		return toReturn;
	}

	private void healMySelf() {
		if(toReturn != null)
			return;
		
		if(getBrain().isBagEmpty())
			return;
		
		if(getBrain().getHealth() <= WarBase.MAX_HEALTH - WarFood.HEALTH_GIVEN)
			toReturn = WarBase.ACTION_EAT;
	}

	private void createUnit(WarAgentType a1) {
		if(toReturn != null)
			return;
		
		if(getBrain().getHealth() > MIN_HEATH_TO_CREATE){
				getBrain().setNextAgentToCreate(a1);
				getBrain().setDebugString("Create: "+a1.name());
				cptCreation++;
			
			toReturn = WarBase.ACTION_CREATE;
		}
		
	}

	private void handleMessages() {
		
		for(WarMessage msg : msgs) {
			if (msg.getMessage().equals(Constants.whereAreYou)) {
				getBrain().sendMessage(msg.getSenderID(), Constants.here, "");
			}
			if(msg.getMessage().equals(Constants.enemyBaseHere)){
				CoordPolar p = getBrain().getIndirectPositionOfAgentWithMessage(msg);
				coordonneeBase = p;
				getBrain().broadcastMessageToAgentType(WarAgentType.WarKamikaze, Constants.baseToAttack, msg.getContent()[0], msg.getContent()[1]);
			}
		}
				
	}
	
	private void attribuerRole(){
		boolean espionMort = true;
		boolean medicMort = true;
		
		for(WarMessage m : msgs){
			if(m.getMessage().equals(Constants.espion)){
				espionMort = false;
				cptEspionMort = 0;
			}
			if(m.getMessage().equals(Constants.medic)){
				medicMort = false;
				cptMedicMort = 0;
			}
		}
		
		if(espionMort){
			cptEspionMort++;
		}
		if(medicMort){
			cptMedicMort++;
		}
		
		if(cptEspionMort > DELAI_ESPION){
			
			for(WarMessage m : msgs){
				if(m.getSenderType().equals(WarAgentType.WarExplorer)){
					getBrain().reply(m, Constants.espionMort, "");
					cptEspionMort = 0;
					break;
				}
			}
		}
		if(cptMedicMort > DELAI_ESPION){
			for(WarMessage m : msgs){
				if(m.getSenderType().equals(WarAgentType.WarExplorer)){
					getBrain().reply(m, Constants.medicMort, "");
					cptMedicMort = 0;
					break;
				}
			}
		}
	}
	
	private void baseAttaque()
	{
		ArrayList<WarPercept> ennemy = getBrain().getPerceptsEnemies();
		
		if (ennemy != null && ennemy.size() > 0)
		{
			for (int i=0; i<ennemy.size(); i++)
			{
				
				if (!ennemy.get(i).getType().equals(WarAgentType.WarExplorer))
				{
					String[] posEnnemy = new String[2];
					posEnnemy[0] = "" + ennemy.get(i).getDistance();
					posEnnemy[1] = "" + ennemy.get(i).getAngle();
					getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.enemyTankHere, posEnnemy);
				}
				
				
			}
			
		}
		
	}
	
	private void determinerCreation(){
		
		//Comptage du nombre de rocket launchers
		cptTank  = 0;
		for(WarMessage msg : msgs) {
			if (msg.getMessage().equals(Constants.rocketLauncherAlive)) {
				cptTank++;
			}
			
		}
		
		/* SI ON A PAS ASSEZ DE ROCKETLAUNCHERS, ON EN CREE DE NOUVEAUX
		if(cptTank < NOMBRE_MIN_ROCKETLAUNCHERS){
			createUnit(WarAgentType.WarRocketLauncher);
		}
		else{*/
			//SINON ON CREE D'AUTRES UNITES SELON COMBIEN ON EN A DEJA CREE
			if(cptCreation < CREATION_INGE){
				createUnit(WarAgentType.WarEngineer);
			}
			else if(cptCreation < CREATION_TANKS){
				createUnit(WarAgentType.WarRocketLauncher);
			}
			else if(cptCreation < CREATION_KAM){
				createUnit(WarAgentType.WarKamikaze);
			}
			else{
				createUnit(WarAgentType.WarRocketLauncher);
			}
			
			if(cptCreation > COMPTEUR_REBOOT){
				cptCreation = 0;
			}
		
	}
	
	private void appelOffreIngenieur()
	{
		int cpt = 0;
		
		for(Entry<Double, String> entry : anglesTourelles.entrySet()) {
		    Double cle = entry.getKey();
		    String valeur = entry.getValue();
		    
		    if (cle.doubleValue() % 45 == 0 && cle.doubleValue() % 90 != 0)
		    {
		    	if (valeur.equals(""))
		    	{
		    		getBrain().broadcastMessageToAgentType(WarAgentType.WarEngineer, Constants.appelOffreTurret, String.valueOf(cle.doubleValue()));
		    		cpt++;
		    	}
		    }
		}
		
		if (cpt == 0)
		{
			for(Entry<Double, String> entry : anglesTourelles.entrySet()) {
			    Double cle = entry.getKey();
			    String valeur = entry.getValue();
			    
			    if (cle.doubleValue() % 90 == 0)
			    {
			    	if (valeur.equals(""))
			    	{
			    		getBrain().broadcastMessageToAgentType(WarAgentType.WarEngineer, Constants.appelOffreTurret, String.valueOf(cle.doubleValue()));
			    	}
			    }
			}
		}
		
		//System.out.println(this.anglesTourelles.toString());
	}
	
	private void deroulementAppelOffreIngenieur()
	{
		for (WarMessage m : this.msgs)
		{
			if (m.getSenderType().equals(WarAgentType.WarEngineer))
			{
				if (m.getMessage().equals(Constants.etatConsTurretEnCours))
				{
					offreInge.add(m);
					this.anglesTourelles.put(new Double(m.getContent()[0]), Constants.etatConsTurretEnCours);
				}
				else if (m.getMessage().equals(Constants.etatConsTurretImpossible))
				{
					for (int i = 0; i < offreInge.size(); i++)
					{
						if (offreInge.get(i).getSenderID() == m.getSenderID())
						{
							offreInge.remove(i);
						}
					}
					
					this.anglesTourelles.put(new Double(m.getContent()[0]), Constants.etatConsTurretImpossible);
				}
				else if (m.getMessage().equals(Constants.etatConsTurretConstruit))
				{
					for (int i = 0; i < offreInge.size(); i++)
					{
						if (offreInge.get(i).getSenderID() == m.getSenderID())
						{
							offreInge.remove(i);
						}
					}
					
					this.anglesTourelles.put(new Double(m.getContent()[0]), Constants.etatConsTurretConstruit);
				}
			}
		}
	}
	
	private void etatTourelles()
	{
		if (!this.turretEmpty())
		{
			for(Entry<Double, String> entry : anglesTourelles.entrySet()) {
			    Double cle = entry.getKey();
			    String valeur = entry.getValue();
			    
			    if (valeur.equals(Constants.etatConsTurretConstruit))
			    {
			    	boolean estPresent = false;
			    	for (WarMessage m : this.msgs)
			    	{
			    		if (m.getSenderType().equals(WarAgentType.WarTurret))
			    		{
			    			if (Math.round(m.getAngle()) == Math.round(cle.doubleValue()))
			    			{
			    				estPresent = true;
			    				this.etatsTourelles.put(cle, Integer.valueOf(0));
			    			}
			    		}
			    	}
			    	
			    	if (!estPresent)
			    	{
			    		this.etatsTourelles.put(cle, Integer.valueOf(this.etatsTourelles.get(cle).intValue() + 1));
			    	}
			    	
			    	if (this.etatsTourelles.get(cle).intValue() == COMPTEUR_MAX_TURRET)
			    	{
			    		this.anglesTourelles.put(cle, "");
			    		this.etatsTourelles.put(cle, Integer.valueOf(0));
			    	}
			    }
			}
		}
		
	}
	
	private boolean turretEmpty()
	{
		for(Entry<Double, String> entry : anglesTourelles.entrySet()) {
			 String valeur = entry.getValue();
			
			 if (valeur.equals(Constants.etatConsTurretConstruit))
			 {
				 return false;
			 }
		}
		
		return true;
	}
	
	private boolean IngenieurEmpty()
	{
		
		for (WarMessage m : this.msgs)
		{
			if (m.getSenderType().equals(WarAgentType.WarEngineer))
			{
				return false;
			}
		}

		return true;
	}
	
	private void etatConstructionTurret()
	{	
		for (WarMessage m : this.offreInge)
		{
			getBrain().reply(m, Constants.turretIsBuilt, "");
		}
	}
	
	private void enoughEnergy(){
		if(getBrain().getHealth() > MIN_HEATH_TO_CREATE){
			getBrain().broadcastMessageToAgentType(WarAgentType.WarExplorer, Constants.enoughEnergy, "");
		}
	}
	
	private void appelOffreTankAtqBase()
	{
		ArrayList<WarPercept> tanks = getBrain().getPerceptsAlliesByType(WarAgentType.WarRocketLauncher);
		
		if (tanks.size() >= COMPTEUR_MAX_TANK && offreAtqBase.size() < NB_TANK_ATQ )
		{
			getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.appelOffreAtqBase, "");
			cptTickNbTank=0;
			
		}
		else if (cptMortEnnemie >= COMPTEUR_MORT_ENNEMIE_MAX && offreAtqBase.size() < (this.cptTank/2)) 
		{
			getBrain().broadcastMessageToAgentType(WarAgentType.WarRocketLauncher, Constants.appelOffreAtqBase, "");
			cptTickNbTank++;
		}
	}
	
	private void recruterOffreAtqBase(int nbTankAtq)
	{
		for (WarMessage m : this.msgs)
		{
			if (m.getMessage().equals(Constants.acceptOffreAtqBase) && offreAtqBase.size() < nbTankAtq)
			{
				System.out.println(m.toString());
				offreAtqBase.add(m);
			}
		}
		
		
		if (offreAtqBase.size() == nbTankAtq)
		{
			System.out.println(offreAtqBase.size() + " == " + nbTankAtq);
			for (WarMessage m : offreAtqBase)
			{
				getBrain().reply(m, Constants.offreAtqBaseConfirme,"");
			}
			
			
			groupeExistant = true;
			offreAtqBase = new ArrayList<WarMessage>();
		}
	}
	
	private void compterMort()
	{
		System.out.println("**********" + cptMortEnnemie);
		
		if (cptMortEnnemie < COMPTEUR_MORT_ENNEMIE_MAX)
		{
			ArrayList<WarPercept> ennemie = getBrain().getPerceptsEnemiesByType(WarAgentType.WarRocketLauncher);
			
			System.out.println("....." + ennemie.size());
			
			for (WarPercept p : ennemie)
			{
				if (p.getHealth() <= ((int) WarRocketLauncher.MAX_HEALTH * 0.1) && cptMortEnnemie < COMPTEUR_MORT_ENNEMIE_MAX)
				{
					cptMortEnnemie++;
				}
			}
		}
		
	}
	
	private WarMessage getMessageAboutEnnemyBase() {
		
		for(WarMessage msg : msgs) 
		{
			if(msg.getMessage().equals(Constants.enemyBaseHere))
			{
				return msg;
			}
		}
		
		return null;
	}
	
	private int nbTankAllieInBase()
	{
		ArrayList<WarPercept> tanks = getBrain().getPerceptsAlliesByType(WarAgentType.WarRocketLauncher);
		
		return tanks.size();
	}

}
