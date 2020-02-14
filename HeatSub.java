import java.util.concurrent.*;
import java.lang.Math.*;

public class HeatSub extends GeneralScanReduce<Double[][], Double[][]>
{
	public HeatSub(Double[][][] data, int DIM)
	{
		super(data);
		this.DIM = DIM;
	}

	protected Double[][] init()
	{
		Double[][] nullVal = null;
		return nullVal;
	}
	protected Double[][] prepare(Double[][] datum)
	{
		Double[][] map = new Double[DIM][DIM];
		for(int i = 0; i < DIM; i++)
		{
			for(int j = 0; j < DIM; j++)
				map[i][j] = 0.0;
		}
		for(int i = 0; i < datum.length; i++)
		{
			int x = getLoc(datum[i][0]);
			int y = getLoc(datum[i][1]);

			map[x][y] += 1.0;
		}
		return map;
	}
	protected Double[][] combine(Double[][] left, Double[][] right)
	{
		Double[][] map = new Double[DIM][DIM];

		for(int i = 0; i < DIM; i++)
		{
			for(int j = 0; j < DIM; j++)
			{
				if(left == null)
					map[i][j] = right[i][j];
				else if(right == null)
					map[i][j] = left[i][j];
				else
					map[i][j] = left[i][j] + right[i][j];
			}
		}
		return map;
	}
	protected Double[][] gen(Double[][] tally) {return tally;}

	private int DIM;

	private int getLoc(Double value)
	{
		double step = 2.0 / DIM;
		int loc = (int)((value + 1.0) / step);
		return (loc < DIM ? loc : DIM - 1);
	}
}

