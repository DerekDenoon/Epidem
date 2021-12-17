package Epidem;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

@SuppressWarnings("serial")
public class Epidem extends JPanel implements ActionListener {

	//standard SIR model for herd immunity/epidemiology simulator
	//agent based variant
	private JButton startBtn;  // start button
	private JButton stopBtn;  // stop button
	private JButton resetBtn; // resets board
	private JButton speedBtn; // changes the speed of the simulation
	private JButton imageBtn; // takes a picture of the games state
	private JTextField sizeTxt; // sets the size
	private JTextField immRateTxt; // sets the immunization rate
	private JTextField unvacInfRateTxt; // sets the unvacInfRate
	private JTextField genSkipTxt; // sets the generation skip
	private JTextField vacInfRateTxt; // sets the vaccinated infection rate
	private JTextField durationTxt;  // sets the duration of infection
	private JTextField densityTxt;  // sets the density
	private JLabel vacLabel;		//states number of vaccinated
	private JLabel unvacLabel;		//states number of unvaccinated
	private JLabel infLabel;		//states number of infected
	private JLabel recLabel; 		//states number of recovered
	private JLabel picLabel;		//where the drawing happens
	private int vOffset;			// stores vertical offset
	private int hOffset;			// stores horizontal offset
	private int vMax;				//
	private int hMax;
	private int genSkip;
	//	private int[] mouseCoords;
	private int size;
	private int gridWidth;
	private int gridHeight;
	private ArrayList<EpidemAgent> agents;
	private int[][] occupied;				//the 2d array that represents the location of each agent. Similar to, but not with the same functionality, as the cells array from previous projects
	private int[] speeds = new int[4];
	private int speedIndex;  // stores the speed that the simulation runs at (as a location in the speeds array
	private Image pic;
	
	private double density;					//how densely filled to make the population
	private double immRate;					//percent of population that are immunized
	private double unvacInfRate; 			//percent chance unvaccinated people get infected
	private double vacInfRate;  			//percent chance vaccinated people get infected
	private int duration;					//how many ticks an infected person stays infectious
	
	private int vaccinatedCount;			//counts for the various states for display purposes
	private int unvaccinatedCount;
	private int infectedCount;
	private int recoveredCount;
	
	private Timer timer;
	private boolean isRunning;
	private int ticks;						//to handle speed appropriately

	public static final Color EMPTY = Color.WHITE;		//bypassing the number-is-a-color middleman of the sociology project
	public static final Color RECOVERED = Color.BLUE;
	public static final Color VAC = Color.ORANGE;
	public static final Color UNVAC = Color.RED;
	public static final Color INFECTED = Color.GREEN;


	// priority
	// recovered
	// vac or unvac
	// empty

	public Epidem(int xDim, int yDim) {
		super(new GridBagLayout());                       				// set up graphics window
		setBackground(Color.LIGHT_GRAY);
		addMouseListener(new MAdapter());
		addMouseMotionListener(new MAdapter());
		setFocusable(true);
		setDoubleBuffered(true);
		pic = new BufferedImage(xDim, yDim, BufferedImage.TYPE_INT_RGB);
		picLabel = new JLabel(new ImageIcon(pic));
		initBtns();
		initTxt();
		initLabels();
		addThingsToPanel();
		genSkip = 1;
		vMax = yDim;
		hMax = xDim;
		immRate = Double.parseDouble(immRateTxt.getText()) / 100;			//percent of agents vaccinated at start of run
		unvacInfRate = Double.parseDouble(unvacInfRateTxt.getText()) / 100;	//percent chance for unvaccinated agent to get infected
		vacInfRate = Double.parseDouble(vacInfRateTxt.getText()) / 100;		//percent chance for vaccinated agent to get infected
		duration = Integer.parseInt(durationTxt.getText());					//length of time an infection lasts for an unvaccinated agent
		size = Integer.parseInt(sizeTxt.getText());							//pixel size of agent cells
		density = Double.parseDouble(densityTxt.getText()) / 100;			//density of the population in the grid
		isRunning = false;
		for (int i = 0; i < 4; i++) {					//set the speed variation
			speeds[3-i] = 75 * i * i;
		}
		gridWidth = hMax / size;
		gridHeight = vMax / size;
		agents = new ArrayList<EpidemAgent>();			//the arraylist that holds all the agents running around on the screen
		resetSim();
		drawAgents(pic.getGraphics());
		timer = new Timer(1, this);						//initialize the timer
		timer.start();									//start up the sim
	}

	public void addThingsToPanel() {					//read at your own risk
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(1, 1, 0, 1);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 5;
		c.gridheight = 14;
		add(picLabel, c);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(0, 2, 0, 2);
		c.gridx = 0;
		c.gridy = 0;
		add(startBtn, c);
		c.gridx = 1;
		c.gridy = 0;
		add(stopBtn, c);
		c.gridx = 2;
		add(resetBtn, c);
		c.gridx = 3;
		add(speedBtn, c);
		c.insets = new Insets(0, 10, 0, 10);
		c.gridx = 6;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(imageBtn, c);
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		add(new JLabel("Skip Generations"), c);
		c.gridy = 3;
		add(new JLabel("% immunized"), c);
		c.gridy = 4;
		add(new JLabel("% infect chance unvac"), c);
		c.gridy = 5;
		add(new JLabel("% infect chance vac"), c);
		c.gridy = 6;
		add(new JLabel("Infectious duration"), c);
		c.gridy = 7;
		add(new JLabel("Cell size"), c);
		c.gridy = 8;
		add(new JLabel("% density"), c);
		c.gridy = 9;
		add(new JLabel(" "), c);
		c.gridy = 10;
		add(new JLabel("Vaccinated"), c);
		c.gridy = 11;
		add(new JLabel("Unvaccinated"), c);
		c.gridy = 12;
		add(new JLabel("Infected"), c);
		c.gridy = 13;
		add(new JLabel("Recovered"), c);
		c.gridx = 7;
		c.gridy = 2;
		add(genSkipTxt, c);    	
		c.gridy = 3;
		add(immRateTxt, c);
		c.gridy = 4;
		add(unvacInfRateTxt, c);
		c.gridy = 5;
		add(vacInfRateTxt, c);
		c.gridy = 6;
		add(durationTxt, c);
		c.gridy = 7;
		add(sizeTxt, c);
		c.gridy = 8;
		add(densityTxt, c);
		c.gridy = 10;
		add(vacLabel, c);
		c.gridy = 11;
		add(unvacLabel, c);
		c.gridy = 12;
		add(infLabel, c);
		c.gridy = 13;
		add(recLabel, c);
	}

	public void initTxt() {								//note that all parameters that are entered as percents are treated as doubles between 0 and 1 in the code
		genSkipTxt = new JTextField("1", 4);
		genSkipTxt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				genSkip = Integer.parseInt(genSkipTxt.getText());
			}
		});
		immRateTxt = new JTextField("50", 4);
		immRateTxt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				immRate = Double.parseDouble(immRateTxt.getText()) / 100;
				resetSim();
				drawAgents(pic.getGraphics());
			}
		});
		unvacInfRateTxt = new JTextField("90", 4);
		unvacInfRateTxt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				unvacInfRate = Double.parseDouble(unvacInfRateTxt.getText()) / 100;
			}
		});
		vacInfRateTxt = new JTextField("10", 4);
		vacInfRateTxt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				vacInfRate = Double.parseDouble(vacInfRateTxt.getText()) / 100;
			}
		});
		durationTxt = new JTextField("10", 4);
		durationTxt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				duration = Integer.parseInt(durationTxt.getText());
			}
		});
		sizeTxt = new JTextField("10", 4);
		sizeTxt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				size = Integer.parseInt(sizeTxt.getText());
				resetSim();
				drawAgents(pic.getGraphics());
			}
		});
		densityTxt = new JTextField("40", 4);
		densityTxt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				density = Double.parseDouble(densityTxt.getText()) / 100;
				resetSim();
				drawAgents(pic.getGraphics());
			}
		});
	}

	public void initLabels() {
		vacLabel = new JLabel(" " + vaccinatedCount);
		unvacLabel = new JLabel(" " + unvaccinatedCount);
		infLabel = new JLabel(" " + infectedCount);
		recLabel = new JLabel(" " + recoveredCount);
		vacLabel.setForeground(VAC);
		unvacLabel.setForeground(UNVAC);
		infLabel.setForeground(INFECTED);
		recLabel.setForeground(RECOVERED);
	}

	public void initBtns() {
		startBtn = new JButton("Start");
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isRunning = true;
			}
		});

		stopBtn = new JButton("Stop");
		stopBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isRunning = false;
			}
		});

		resetBtn = new JButton("Reset");
		resetBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetSim();
				drawAgents(pic.getGraphics());
			}
		});    	

		imageBtn = new JButton("Save Picture");
		imageBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Calendar c = Calendar.getInstance();
					String fileName = ".\\d=" + immRate + " a=" + unvacInfRate + " r=" + duration + " @" + c.get(Calendar.HOUR) + "." + c.get(Calendar.MINUTE) + "." + c.get(Calendar.SECOND)+ ".png";
					System.out.println(fileName);
					File outputFile = new File(fileName);
					outputFile.createNewFile();
					ImageIO.write((RenderedImage) pic, "png", outputFile);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		speedBtn = new JButton("Speed = Fast");
		speedIndex = 2;
		speedBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// increments speed index by 1 and returns its remainder when div by 4
				speedIndex = (speedIndex + 1) % 4;
				switch (speedIndex) {
				case 0 : {
					speedBtn.setText("Speed = Slow");
					break;
				}
				case 1 : {
					speedBtn.setText("Speed = Med");
					break;
				}
				case 2 : {
					speedBtn.setText("Speed = Fast");
					break;
				}
				case 3 : {
					speedBtn.setText("Speed = Whoa");
					break;
				}
				}
			}
		});
	}

	public Epidem() {
		super();
		setBackground(EMPTY);
		addMouseListener(new MAdapter());
		setFocusable(true);
		setDoubleBuffered(true);
	}

	public void paintComponent(Graphics g) { 	                 // draw graphics in the panel
		super.paintComponent(g);                              	 // call superclass' method to make panel display correctly
	}

	public void drawAgents(Graphics g) {	//slightly different, but why?
		g.setColor(EMPTY);
		g.fillRect(0, 0, hMax, vMax);
		for (EpidemAgent a : agents) {

			//priority
			// Recovered
			// Infected
			// Vaccination Status
			// Empty

			if(a.isRecovered()){
				g.setColor(RECOVERED);
			}else if(a.isInfected()){
				g.setColor(INFECTED);
			}else if(a.isVaccinated()){
				g.setColor(VAC);
			}else if(!a.isVaccinated()){
				g.setColor(UNVAC);
			}else{
				g.setColor(EMPTY);
			}



			g.fillRect(a.getLoc().x * size, a.getLoc().y * size, size, size); //draw the square for the agent
		}
	}

	public void resetSim() { //reset all count parameters and the agents
		agents.clear();
		vaccinatedCount = 0;
		unvaccinatedCount = 0;
		infectedCount = 0;
		recoveredCount = 0;
		gridWidth = hMax / size;
		gridHeight = vMax / size;
		occupied = new int[gridWidth][gridHeight]; //reset location information
		for (int i = 0; i < gridWidth; i++) {
			for (int j = 0; j < gridHeight; j++) {
				
				if (Math.random() < density) {


					//your code goes here, similar to Neighbors, just make sure you also update the appropriate counts
					//use a random number on a vaccination check
					//add an agent to the agents variable accordingly at location i, j

//					occupied[i][j] = agents.size(); //occupied holds an indexing of the agents to their locations

					boolean isVac;
					if(Math.random() < immRate){
						isVac = true;
						vaccinatedCount++;
					}else{
						isVac = false;
						unvaccinatedCount++;
					}

					EpidemAgent a = new EpidemAgent(isVac,i,j);
					agents.add(a);

					occupied[i][j] = agents.size();
				}
			}
		}
		isRunning = false;
	}

	public boolean canMove(int xtar, int ytar) {
		return occupied[xtar][ytar] == 0;
		//your one line of code replaces the existing line here...if you've understood everything so far you'll know what to do
	}

	public Point findEmpty(EpidemAgent a) { //should be remarkably similar to Neighbors' findEmpty code...so I'll let you write it
		Point result = a.getLoc();


		//
		int sum = 0;
		int searchCol = 0;
		int searchRow = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				searchCol = ((int) (result.getX()) + i + occupied.length) % occupied.length;
				searchRow = ((int) (result.getY()) + j + occupied.length) % occupied.length;

				if (occupied[searchCol][searchRow] == 0){
					result.setLocation(searchCol,searchRow);
					break;
				}
			}
		}
		//

		//your code goes here
		
		return result;
	}

	public ArrayList<EpidemAgent> infectNeighbors(EpidemAgent a) { //comment the code inside the double for loop to demonstrate you know what it's doing
		ArrayList<EpidemAgent> result = new ArrayList<EpidemAgent>();
		int targIndex = 0;
		for(int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				if (!((i == 0) && (j == 0))) {
					targIndex = occupied[(gridWidth + a.getLoc().x + i) % gridWidth][(gridHeight + a.getLoc().y + j) % gridHeight] - 1;
					if (targIndex >= 0) {
						if (!agents.get(targIndex).isRecovered() && !agents.get(targIndex).isInfected()) {
							if ((!agents.get(targIndex).isVaccinated() && (Math.random() <= unvacInfRate)) || (agents.get(targIndex).isVaccinated() && (Math.random() <= vacInfRate))) {
								result.add(agents.get(targIndex));
							}
						}
					}
				}
			}
		}
		return result;
	}

	public void moveAllAgents() { //moves all agents into an adjacent empty spot, when possible (or, if your findEmpty is non-deterministic, it may not move _all_ agents, but calling this 'tryToMoveAllAgentsAndMaybeSucceed' seemed bad) 
		Point reloc;
		for (EpidemAgent a : agents) {
			reloc = findEmpty(a);
			occupied[a.getLoc().x][a.getLoc().y] = 0;         //update occupied appropriately
			a.setLoc(reloc);
			occupied[reloc.x][reloc.y] = agents.indexOf(a)+1; //if it doesn't actually move, occupied just gets what it had gotten before
		}
	}

	public void updateAgents() {
		ArrayList<EpidemAgent> changed = new ArrayList<EpidemAgent>();
		ArrayList<EpidemAgent> newlyInfected = new ArrayList<EpidemAgent>();
		for (int gens = 0; gens < genSkip; gens++) { //for the generation skip functionality
			
			//your code goes here
			//1) clear the changed list
			//2) for every agent, check to see if they're infected
			//3) if they are, see if their infection's duration has lapsed (add them to the changed list if so)
			//4) advance an infected agent's infection (yum!)
			//5) let an infected agent try to infect nearby agents...add anything they infect to changed
			
			//after that loop, in another loop...
			//6) for every agent that needs to change...
			//7) change their state - if they're infected, they become recovered, and if they're not infected they become infected
			//8) update all counts accordingly
			
			//your code goes here


			//
			// (1)
			changed.clear();

			//(2)


			//

			moveAllAgents();  //finally, move movable agents
			isRunning = (infectedCount != 0); //don't keep running needlessly
		}
	}

	public void updateLabels() {	//updates labels with the totally accurate and correct counts
		vacLabel.setText(" " + vaccinatedCount);
		unvacLabel.setText(" " + unvaccinatedCount);
		infLabel.setText(" " + infectedCount);
		recLabel.setText(" " + recoveredCount);
	}

	@Override
	public void actionPerformed(ActionEvent e) { //I promise this works
		if (isRunning) {
			ticks++;
			if (ticks >= speeds[speedIndex]) {
				updateAgents();			
				drawAgents(pic.getGraphics());
				ticks = 0;
			}
		}
		hOffset = picLabel.getLocationOnScreen().x - getLocationOnScreen().x;
		vOffset = picLabel.getLocationOnScreen().y - getLocationOnScreen().y;
		updateLabels();
		repaint();
	}

	//where the mouse handler goes
	private class MAdapter extends MouseAdapter {

		@Override
		//good news, you don't have to write this...bad news, you have to comment it (this should give you some good hints, though, so read it carefully)
		public void mousePressed(MouseEvent e) { //if you're not sure why something is here, please do ask
			Point p = new Point((e.getX() - hOffset) / size, (e.getY() - vOffset) / size);
			try {
				int agentIndex = occupied[p.x][p.y];
				if (agentIndex != 0) {
					EpidemAgent a = agents.get(occupied[p.x][p.y] - 1);
					if (!a.isInfected()) {
						if (a.isVaccinated()) {
							vaccinatedCount--;
						}
						if (!a.isVaccinated()) {
							unvaccinatedCount--;
						}
						if (a.isRecovered()) {
							recoveredCount--;
						}
						infectedCount++;
						a.infect();
					}
				} else {
					agents.add(new EpidemAgent(false, p.x, p.y));
					agents.get(agents.size() - 1).infect();
					occupied[p.x][p.y] = agents.size();
					infectedCount++;
				}
				drawAgents(pic.getGraphics());
			} catch (ArrayIndexOutOfBoundsException e2) {
			}
		}

		// I left this stuff here if you want to do something with it		
		//		@Override
		//		public void mouseMoved(MouseEvent e) {
		//			Point p = new Point((e.getX() - hOffset) / size, (e.getY() - vOffset) / size);
		//			System.out.println(hOffset + " " + e.getXOnScreen() + ", " + e.getYOnScreen() + " grid " + p.x*size + ", " + p.y*size);
		//			mouseCoords[0] = p.x;
		//			mouseCoords[1] = p.y;			
		//		}
		//		
		//		@Override
		//		public void mouseDragged(MouseEvent e) {
		//			Point p = new Point((e.getX() - hOffset) / size, (e.getY() - vOffset) / size);
		//			mouseCoords[0] = p.x;
		//			mouseCoords[1] = p.y;			
		//			try {
		//				if (mouseDraw) {
		//					cells[p.x][p.y] = 1; 
		//				} else {
		//					cells[p.x][p.y] = 0;
		//				}
		//				drawCells(pic.getGraphics());
		//			} catch (ArrayIndexOutOfBoundsException e2) {
		//			}
		//		}

		//		@Override
		//		public void mouseReleased(MouseEvent e) {
		//		}
	}
}

