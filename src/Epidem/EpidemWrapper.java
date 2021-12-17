package Epidem;

import java.awt.EventQueue;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class EpidemWrapper extends JFrame {

	public final int FRAMESIZE = 600; //adjust this for more things! (or less things?)
	public final int BTNSPACE = 63;
	public final int HRZSPACE = 8;
	
	public EpidemWrapper() {
        setSize(3*FRAMESIZE/2+HRZSPACE, FRAMESIZE+BTNSPACE);
		add(new Epidem(FRAMESIZE, FRAMESIZE));
        setResizable(false);
        setTitle("SIR model, agent based"); //SIR stands for susceptible, infected, recovered
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	
	public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                EpidemWrapper go = new EpidemWrapper();
                go.setVisible(true);
            }
        });
	}
}
