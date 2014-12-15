package DepretChambon;

import java.awt.Color;
import java.util.ArrayList;

import edu.turtlekit3.warbot.agents.agents.WarBase;
import edu.turtlekit3.warbot.agents.agents.WarEngineer;
import edu.turtlekit3.warbot.agents.enums.WarAgentType;
import edu.turtlekit3.warbot.agents.percepts.WarPercept;
import edu.turtlekit3.warbot.brains.braincontrollers.WarEngineerAbstractBrainController;
import edu.turtlekit3.warbot.communications.WarMessage;
import edu.turtlekit3.warbot.tools.CoordCartesian;
import edu.turtlekit3.warbot.tools.CoordPolar;

public class WarEngineerBrainController extends WarEngineerAbstractBrainController {
	
	private String toReturn;
	private ArrayList<WarMessage> messages;
	private double angleTourelleConstruct;
	private String etatTourelle;
	
	private static final int MIN_HEATH_TO_CREATE = (int) (WarEngineer.MAX_HEALTH * 0.5);
	private static final int ANGLE_INTERET = 315;
	private static final int BORNE_MIN = 10;
	private static final int BORNE_MAX = 15;
	private static final int BORNE_MIN_ANGLE = 120;
	private static final int BORNE_MAX_ANGLE = 150;
	private static final int MIN_ENERGY = (int) (WarEngineer.MAX_HEALTH * 0.7);
	
	
	//FSM *****************************
	private TaskWarEngineer ctask;
	
	private TaskWarEngineer deplacement = new TaskWarEngineer(this) {
		void exec(WarEngineerBrainController e)
		{
			getBrain().setDebugStringColor(Color.BLUE);
			getBrain().setDebugString("deplacement");
			
			e.placementBase();
			e.reponseAppelOffreBase();
			
			if (e.angleTourelleConstruct != -1)
			{
				ctask = constructionTourelle;
			}
			
			
		}
	};
	
	private TaskWarEngineer constructionTourelle = new TaskWarEngineer(this) {
		void exec(WarEngineerBrainController e)
		{
			getBrain().setDebugStringColor(Color.GREEN);
			getBrain().setDebugString("constructionTourelle");
			
			
			WarMessage m = getMessageAboutAOT();
			
			if (!etatTourelle.equals(""))
			{
				if (m != null)
				{
					System.out.println(etatTourelle);
					getBrain().reply(m, etatTourelle, String.valueOf(angleTourelleConstruct));
					
					etatTourelle = "";
					angleTourelleConstruct = -1;
					
					ctask = deplacement;
					
				}
			}
			else
			{
				constructionTourelle();
			}
			
		}
	};
	
	//FSM *****************************
	
	public WarEngineerBrainController() {
		super();
		this.messages = new ArrayList<WarMessage>();
		this.angleTourelleConstruct = -1;
		this.etatTourelle = "";
		ctask = deplacement;
		
	}

	@Override
	public String action() {
		// Develop behaviour here
		toReturn = null;
		messages = getBrain().getMessages();
		
		this.seSignaler();
		
		checkEnergy();
		
		ctask.exec(this);
		
		if (toReturn == null)
		{
			if (getBrain().isBlocked())
				getBrain().setRandomHeading();
			
			toReturn = WarEngineer.ACTION_MOVE;
		}
		
		return toReturn;
		
	}
	
	private void createUnit(WarAgentType a1) {
		
		
		if(getBrain().getHealth() > MIN_HEATH_TO_CREATE){
			
			getBrain().setNextAgentToCreate(a1);
			getBrain().setDebugString("Create: "+a1.name());
			
			toReturn = WarEngineer.ACTION_CREATE;
		}
		
	}
	
	private WarMessage messageFromBase()
	{
		for (WarMessage m : this.messages)
		{
			if (m.getSenderType().equals(WarAgentType.WarBase) && m.getMessage().equals(Constants.here))
			{
				return m;
			}
		}
		
		return null;
	}
	
	private void placementBase()
	{
		WarMessage m = this.messageFromBase();
		
		
		if (m != null)
		{
			
			if ((m.getAngle() < BORNE_MAX_ANGLE && m.getAngle() > BORNE_MIN_ANGLE) && (m.getDistance() < BORNE_MAX && m.getDistance() > BORNE_MIN))
			{
				//System.out.println(m.getAngle() + " " + WarBase.DISTANCE_OF_VIEW);
				getBrain().setRandomHeading();
			}
			else
			{
				CoordPolar positionAllie = new CoordPolar(m.getDistance(), m.getAngle());
				CoordPolar positionEnnemi = new CoordPolar(((BORNE_MAX + BORNE_MIN)/2), ANGLE_INTERET);
				
				CoordCartesian vecteurPositionAllie = positionAllie.toCartesian();
				CoordCartesian vecteurPositionEnemie = positionEnnemi.toCartesian();

				
				CoordCartesian positionfinal = new CoordCartesian(vecteurPositionAllie.getX() + vecteurPositionEnemie.getX(), 
						vecteurPositionAllie.getY() + vecteurPositionEnemie.getY());

				CoordPolar pointInteret = positionfinal.toPolar();
				
				
				getBrain().setHeading(pointInteret.getAngle());
			}
		}
		else
		{
			getBrain().setRandomHeading();
		}
		
		toReturn = WarEngineer.ACTION_MOVE;
	}
	
	private void reponseAppelOffreBase()
	{
		for (WarMessage m : this.messages)
		{
			if (m.getSenderType().equals(WarAgentType.WarBase))
			{
				if (m.getMessage().equals(Constants.appelOffreTurret))
				{
					if (this.angleTourelleConstruct == -1 && getBrain().getHealth() > MIN_HEATH_TO_CREATE)
					{
						getBrain().reply(m, Constants.etatConsTurretEnCours, m.getContent()[0]);
						this.angleTourelleConstruct = Double.valueOf(m.getContent()[0]);
					}
				}
			}
		}
	}
	
	private void constructionTourelle()
	{
			WarMessage m = this.messageFromBase();
			
			
			if (m != null)
			{
				CoordPolar positionAllie = new CoordPolar(m.getDistance(), m.getAngle());
				CoordPolar positionEnnemi = new CoordPolar(WarBase.DISTANCE_OF_VIEW + BORNE_MAX, this.angleTourelleConstruct);
				
				CoordCartesian vecteurPositionAllie = positionAllie.toCartesian();
				CoordCartesian vecteurPositionEnemie = positionEnnemi.toCartesian();

				
				CoordCartesian positionfinal = new CoordCartesian(vecteurPositionAllie.getX() + vecteurPositionEnemie.getX(), 
						vecteurPositionAllie.getY() + vecteurPositionEnemie.getY());

				CoordPolar pointInteret = positionfinal.toPolar();
								
				if (Math.round(m.getDistance()) == WarBase.DISTANCE_OF_VIEW + BORNE_MAX && Math.round(pointInteret.getDistance()) == 0)
				{
					this.createUnit(WarAgentType.WarTurret);
					System.out.println("## "+angleTourelleConstruct);
				
					//getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.etatConsTurretConstruit, String.valueOf(angleTourelleConstruct));
					etatTourelle = Constants.etatConsTurretConstruit;
					
				}
				else
				{
					
					if (getBrain().isBlocked() && getBrain().getPercepts().size() == 0)
					{
						etatTourelle = Constants.etatConsTurretImpossible;
						
					}
					else
					{
						
						getBrain().setHeading(pointInteret.getAngle());
					}
					
					toReturn = WarEngineer.ACTION_MOVE;
				}
				
			}
	}
	
	private void checkEnergy(){
		if(getBrain().getHealth() < MIN_ENERGY){
			getBrain().broadcastMessageToAgentType(WarAgentType.WarExplorer, Constants.lowEnergy, "");
		}
	}
	
	private WarMessage getMessageAboutAOT()
	{
		for (WarMessage m : this.messages)
		{
			if (m.getMessage().equals(Constants.turretIsBuilt))
			{
				System.out.println("  lllllllllll");
				return m;
			}
		}
		
		return null;
	}
	
	private void seSignaler()
	{
		getBrain().broadcastMessageToAgentType(WarAgentType.WarBase, Constants.here, "");
	}
	
}
