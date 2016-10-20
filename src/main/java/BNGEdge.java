// Matthew Fricke 2008
import java.awt.*; // For Graphics
import java.awt.geom.*; // For line2d

public class BNGEdge extends BNGWidget {

	public BNGComponent start, end;
	private Line2D line = new Line2D.Float(0,0,0,0);
	private boolean is_map;
	
	public BNGEdge( BNGWidgetPanel panel, BNGComponent start, BNGComponent end )
	{
		super(panel);
		this.start = start;
		this.end = end;
		start.edges.add(this);
		end.edges.add(this);
		is_map = false;
	}
	
	public void display(Component c, Graphics2D g2d)
	{
		     if ( selected ) 
		     {
		    	 g2d.setColor( Color.BLUE ); 
		     }
		     else
		     {
		    	 g2d.setColor( Color.BLACK ); 
		     }
		     
		     Point start_point = start.getEdgeAttachPoint();
	         Point end_point = end.getEdgeAttachPoint();
	            
	         int start_x = (int)start_point.getX();
	         int start_y = (int)start_point.getY();
	         int end_x = (int)end_point.getX();
	         int end_y = (int)end_point.getY();
	         
	         line = new Line2D.Float( start_x, start_y, end_x, end_y );
	       
	         if ( isMap() )
	         {
	        	 Stroke stroke = g2d.getStroke();
	 		     
	        	 g2d.setStroke( new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 10) );
	        
	        	 ((Graphics2D)g2d).draw(line);
	        	 
	        	 g2d.setStroke( stroke );     	 
	         }
	         else
	         {
	        	 ((Graphics2D)g2d).draw(line); 
	         }
	}
	
	// Override widget contains method
	public boolean contains(int x, int y)
	{
	    return line.ptLineDist(x, y) < 2;
	}
	
	public boolean isMap()
	{
		return is_map;
	}
	
	public void setMap( boolean is_map )
	{
		this.is_map = is_map;
	}

}
