package Epidem;

import java.awt.Point;

//so, in this project you're going to learn how to extensively use a user-defined class and their objects in some other piece of code
//this is the class for the agents that will be running around on the screen trying to not get infected
//figure out what the code is doing so that you can use it in appropriate ways in the main file
//later on you'll be making your own classes like this one for projects, but we're working up to that still
//when you do, I suggest making something that has this format
public class EpidemAgent {

	//class variables
	private boolean recovered; //true if the agent has recovered from the infection, false otherwise
	private boolean vaccinated; //true if the agent is vaccinated, false otherwise
	private int infected; //-1 if the agent is recovered or is not infected, the number of 'ticks' the agent has been infected for otherwise
	private Point loc; //the coordinates of the agent in the grid

	//constructors
	public EpidemAgent(boolean isVac) { //a constructor for when we just know if it should start out vaccinated
		recovered = false;
		vaccinated = isVac;
		infected = -1;
	}
	
	public EpidemAgent(boolean isVac, int x, int y) { //a constructor for when we know its vaccination and its starting location
		recovered = false;
		vaccinated = isVac;
		infected = -1;
		loc = new Point(x, y);
	}
	
	public EpidemAgent(boolean isVac, Point newLoc) { //an alternate constructor for when we know its vaccination and starting location as a point
		recovered = false;
		vaccinated = isVac;
		infected = -1;
		loc = new Point(newLoc);
	}
	
	//standard practice is to have get and set function pairs for each private variable for objects of a class
	//note that set functions are void and get functions have return types
	//in this case, some of the parameters are a little more complicated
	public boolean isRecovered() { return recovered; }
	public void recover() { recovered = true; infected = -1; }
	
	public boolean isVaccinated() { return vaccinated; }
	public void vaccinate() { vaccinated = true; }
	public void unvaccinate() { vaccinated = false; }
	
	public boolean isInfected() { return infected != -1; }
	public int infectLevel() { return infected; }
	public void advanceInfection() { infected++; } //couldn't think of a better name for this
	public void infect() { recovered = false; infected = 0; }
	
	public Point getLoc() { return loc; }
	public void setLoc(Point newLoc) { loc = new Point(newLoc); }
	public void setLoc(int x, int y) { loc = new Point(x, y); }
}
