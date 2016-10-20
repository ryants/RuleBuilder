import java.awt.*; // For Graphics
import java.util.*; //For vector data structure


public class BNGComponent extends BNGWidget 
{
	public BNGContainer container;
	public Vector edges = new Vector();
	private String binding_state;
	
	public BNGComponent( BNGWidgetPanel panel )
	{
		super(panel);
		height = 16;
		width = 16;
		setLabel("C");
		binding_state = "no additional bonds";
		
	}
	
	public void display(Component c, Graphics2D g2d)
	{
				
		if ( binding_state.equalsIgnoreCase("additional bonds") ) 
		{
				if ( selected ) 
				 {	
					g2d.setColor( Color.BLUE ); 
				 }
				else
				{
					g2d.setColor( Color.BLACK ); 
				}
				
				g2d.fillOval( x, y, width,height );
				
		}
		else if ( binding_state.equalsIgnoreCase("no additional bonds") )
		{
			g2d.setColor(Color.white);
			
			g2d.fillOval( x, y, width, height );
	
			
			if ( selected ) 
			 {	
				g2d.setColor( Color.BLUE ); 
			 }
			else
			{
				g2d.setColor( Color.BLACK ); 
			}
			
			g2d.drawOval( x, y, width, height );
			
			}
		
		
		if ( binding_state.equalsIgnoreCase("allow additional bonds") )	
		{
			g2d.setColor( Color.white );
			g2d.fillOval(x, y, width, height);
			
			if ( selected ) 
			 {	
				g2d.setColor( Color.BLUE ); 
			 }
			else
			{
				g2d.setColor( Color.BLACK ); 
			}
			
			g2d.fillArc(x, y, width, height, -90,180);
			g2d.drawOval( x, y, width, height );
		}
	}
	
	// Override the widget defined setX and setY so that contained components move too
	
	
	public void setSelected( boolean selected )
	{
		this.selected = selected;
		
		// Mark all associated edges
		Iterator edges_itr = edges.iterator();
		while ( edges_itr.hasNext() )
		{
			((BNGEdge)edges_itr.next()).setSelected(selected);
		}
		
		panel.repaint();
	}
	
	public boolean addEdge( BNGEdge edge )
	{
		boolean result = edges.add( edge );
		return result;
	}
	
	public boolean removeEdge( BNGEdge edge )
	{
		return edges.remove( edge );
	}

	public Point getEdgeAttachPoint() 
    {
        return new Point( x + width/2, y + height/2 );
    } 
	
	public void disconnectEdges()
	{
		// Remove all the edges connected to this component from other components and the panel
		
		Iterator itr = edges.iterator();
		while ( itr.hasNext() )
		{
			BNGEdge edge = (BNGEdge)itr.next();
			if (edge.end != this) edge.end.edges.remove(edge); // Guards to prevent concurrent access
			if (edge.start != this) edge.start.edges.remove(edge);
		}
		
		panel.edges.removeAll(edges);
		edges.removeAllElements();
	}
	
	public void setBindingState( String binding_state )
	{
		this.binding_state = binding_state;
	}

	public String getBindingState()
	{
		return this.binding_state;
	}

	
}
