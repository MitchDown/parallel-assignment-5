/*
 * Mit
 *
 *
 * CPSC 5600, Seattle University

 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.*;

import javax.swing.JButton;
import javax.swing.JFrame;


public class HeatMapDriver {
	private static final int DIM = 150;
	private static final String REPLAY = "Replay";
	public static final String FILENAME = "observation_test.dat";
	private static JFrame application;
	private static JButton button;
	private static Color[][] grid;
	private static Double[][][] data;
	private static Double[][][] map;

	public static void main(String[] args) throws FileNotFoundException, InterruptedException {
		data = data();
		grid = new Color[DIM][DIM];
		map = new Double[256][DIM][DIM];
		application = new JFrame();
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		HeatSub scanReduce = new HeatSub(data,DIM);
		scanReduce.getReduction(0);
		scanReduce.getScan(map);
		fillGrid(grid);

		ColoredGrid gridPanel = new ColoredGrid(grid);
		application.add(gridPanel, BorderLayout.CENTER);

		button = new JButton(REPLAY);
		button.addActionListener(new BHandler());
		application.add(button, BorderLayout.PAGE_END);

		application.setSize(DIM * 4, (int)(DIM * 4.4));
		application.setVisible(true);
		application.repaint();
		animate();
	}

	private static void animate() throws InterruptedException {
		button.setEnabled(false);
		for (int i = 0; i < 255; i++) {
			fillGrid(grid);
			application.repaint();
			Thread.sleep(50);
		}
		button.setEnabled(true);
		application.repaint();
	}

	static class BHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (REPLAY.equals(e.getActionCommand())) {
				new Thread() {
					public void run() {
						try {
							animate();
						} catch (InterruptedException e) {
							System.exit(0);
						}
					}
				}.start();
			}
		}
	};

	static private final Color COLD = new Color(0x0a, 0x37, 0x66), HOT = Color.RED;
	static private int timer = 0;
	private static void fillGrid(Color[][] grid) {
		int pixels = grid.length * grid[0].length;
		for (int r = 0; r < grid.length; r++)
			for (int c = 0; c < grid[r].length; c++)
			{
				grid[r][c] = interpolateColor((map[timer][r][c])/ (DIM /15), COLD, HOT);
			}
		timer++;
	}

	private static Color interpolateColor(double ratio, Color a, Color b) {
		if(ratio > 1.0)
			ratio = 0.99;
		int ax = a.getRed();
		int ay = a.getGreen();
		int az = a.getBlue();
		int cx = ax + (int) ((b.getRed() - ax) * ratio);
		int cy = ay + (int) ((b.getGreen() - ay) * ratio);
		int cz = az + (int) ((b.getBlue() - az) * ratio);
		return new Color(cx, cy, cz);
	}

	private static Double[][][] data()
	{
		Double[][][] readIn = new Double[256][150][2];
		try
		{
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILENAME));
			int count = 0;
			int time = 0;
			Observation obs = (Observation) in.readObject();
			while (!obs.isEOF())
			{
				if(obs.time != time)
				{
					time = (int)obs.time;
					count = 0;
				}
				readIn[time][count][0] = obs.x;
				readIn[time][count++][1] = obs.y;
				obs = (Observation) in.readObject();
			}
			in.close();
		} catch (IOException | ClassNotFoundException e)
		{
			System.out.println("reading from " + FILENAME + "failed: " + e);
			e.printStackTrace();
			System.exit(1);
		}
		return readIn;
	}

}
