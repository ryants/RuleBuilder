
import java.awt.*; // For Graphics
import java.util.*; // For Iterators and Vectors


public class BNGContainer extends BNGWidget {
	
	public Vector components = new Vector(); 
    private Rectangle resize_lower_right = new Rectangle();
    private Rectangle resize_upper_right = new Rectangle();
    private Rectangle resize_upper_left = new Rectangle();
    private Rectangle resize_lower_left = new Rectangle();
    
    public Point start_resize_drag;
    public int original_height;
    public int original_width;
    public int original_y;
    public int original_x;
    transient private Rectangle last_pressed_anchor;
	
    public BNGContainer( BNGWidgetPanel panel )
	{
		super(panel);
        width = 75;
    	height = 50;
		setLabel("Container");
	}
	
	public void display(Component c, Graphics2D g2d)
	{
		   
		     if ( selected ) 
		     {
		    	 displayResizeHandles( c, g2d );
		    	 g2d.setColor( Color.BLUE ); 
		     }
		     else
		     {
		    	 g2d.setColor( Color.BLACK ); 
		     }
		     
		     g2d.drawRoundRect( x, y, width, height, 50, 50 );
		     
	}
	
	public void setSelected( boolean selected )
	{		
		this.selected = selected;
		
		// Mark all associated components
		Iterator components_itr = components.iterator();
		while ( components_itr.hasNext() )
		{
			((BNGComponent)components_itr.next()).setSelected(selected);
		}
		
		panel.repaint();
	}
	
	public void setX(int x)
	{ 
		this.x = x; 
		
		// Update the x for all contained components
		 Iterator i = components.iterator();
		    while( i.hasNext() )
			{		    
			    BNGComponent component = (BNGComponent)i.next();
			    component.setX(x + component.x_offset);
			}

		    label.setX( label.getX() + x );
		    
	      panel.repaint();
		
	}
	
	public void setY(int y)
	{ 
		this.y = y; 
		
		// Update the y for all contained components
		 Iterator i = components.iterator();
		    while( i.hasNext() )
			{		    
			    BNGComponent component = (BNGComponent)i.next();
			    component.setY(y + component.y_offset);
			}

		    if ( label != null )
		    {
		    	this.label.x_offset = 0;
		    	this.label.y_offset = height+8;
		    }
		    
	      panel.repaint();
	}
	
	public void setHeight(int h)
	{ 
		this.height = h; 
				    
		if ( label != null )
		{
			this.label.x_offset = 0;
			this.label.y_offset = height+8;
		}
    	      
		panel.repaint();
	}
	
	public boolean addComponent( BNGComponent component )
	{
		// Make sure the component is not already in the container
        if ( components.indexOf( component ) != -1 ) 
        {
            return false; 
        }
		
        // If the component is already in another container return false
        if ( component.container != null ) return false;
        
        // Add the component and bail if there was a problem
		if ( components.add(component) == false ) return false;
		
		// Calculate the relative position of the component to the container so this is preserved when the container moves
		component.x_offset = component.x - x;
		component.y_offset = component.y - y;
		component.container = this;
		
		return true;
	}
	
	public boolean removeComponent( BNGComponent component )
	{
		component.x_offset = 0;
		component.y_offset = 0;
		component.container = null;
		boolean result = components.remove( component );
		panel.applet.model_stub.writeBNGL();
		return result;
	}
	
	public boolean removeComponentsFromPanel()
	{
		Iterator component_itr = components.iterator();
		while ( component_itr.hasNext() )
		{
			BNGComponent component = (BNGComponent)component_itr.next();
			panel.labels.remove(component.getLabel());
		}
		return panel.components.removeAll(components);
	}
	
	public boolean disconnectAllComponentEdges()
	{
		Iterator component_itr = components.iterator();
		while ( component_itr.hasNext() )
		{
			BNGComponent component = (BNGComponent)component_itr.next();
			
			Iterator edges_itr = component.edges.iterator();
			while ( edges_itr.hasNext() )
			{
				BNGEdge edge = (BNGEdge)edges_itr.next();
				if (component != edge.start ) edge.start.edges.remove(edge); // "If" to guard against concurrent self modification
				if (component != edge.end) edge.end.edges.remove(edge);
				panel.edges.remove(edge);
				panel.labels.remove(edge.getLabel());
			}
		}
		
		return true;
	}
	
	public boolean removeAllComponents()
	{
		removeComponentsFromPanel();
		
		disconnectAllComponentEdges();
		
		return components.removeAll(components);
	}
	
	public boolean releaseAllComponents()
	{	
		return components.removeAll(components);
	}
	
	 String resizeAnchor(int mouse_x, int mouse_y)
	    {
	        Rectangle anchor = getPressedResizeAnchor( mouse_x, mouse_y );
	        
	        if ( anchor == null ) return null;
	        
	        if ( anchor == resize_lower_left )
	        {
	            return "SW";
	        }
	        else if ( anchor == resize_lower_right )
	        {
	            return "SE";
	        }
	        else if ( anchor == resize_upper_left )
	        {
	            return "NW";
	        }
	        else if ( anchor == resize_upper_right )
	        {
	            return "NE";
	        }
	            
	        return null;
	    }
	    
	    Rectangle getPressedResizeAnchor(int mouse_x, int mouse_y)
	    {
	    	
		last_pressed_anchor = null;
		//boolean on_anchor = mouse_x > x + width - 7 && mouse_x < x + width && mouse_y < y + height && mouse_y > y + height - 7;
		
	        if ( resize_lower_right.contains( mouse_x, mouse_y ) ) last_pressed_anchor = resize_lower_right;
	        else if ( resize_upper_right.contains( mouse_x, mouse_y ) ) last_pressed_anchor = resize_upper_right;
	        else if ( resize_upper_left.contains( mouse_x, mouse_y ) ) last_pressed_anchor = resize_upper_left;
	        else if ( resize_lower_left.contains( mouse_x, mouse_y ) ) last_pressed_anchor = resize_lower_left;
	        
		// Remember the start location so we can determine how mich to change the image size by in resize().
		if ( last_pressed_anchor != null )
	            {
	                start_resize_drag = new Point( mouse_x, mouse_y );
	                original_height = height;
	                original_width = width;
	                original_y = y;
	                original_x = x;
	            }
		
		return last_pressed_anchor;
	    }
	    
	    void resize( Dimension d )
	    {
	        int new_height = 0;
		int new_width = 0;
		
		new_height = (int)d.getHeight();
		new_width = (int)d.getWidth();
		
	                
		// Don't let the user make the container too small or too big
		if (new_height < 50) new_height = 50;
		if ( new_width < 50 ) new_width = 50;
		
		height = new_height;
		width = new_width;
		
	        //label.x_offset = width;
	        //label.y_offset = -label.font.getSize();
	        //refreshLocation();
	        
		if ( panel != null ) panel.revalidate();
	    }
	    
	    void resize( int mouse_x, int mouse_y )
	    {
	        //Rectangle last_pressed_anchor = getPressedResizeAngle( mouse_x, mouse_y );
	        
		int new_height = original_height;
		int new_width = original_width;
		int new_x = original_x;
	    int new_y = original_y;
	            
	        // Determine where the mouse pointer is so we can grow or shrink the container in the right direction
	        if ( last_pressed_anchor == resize_lower_right ) 
	        {
	            new_height = original_height + ( mouse_y - (int)start_resize_drag.getY() );
	            new_width = original_width + ( mouse_x - (int)start_resize_drag.getX() );
	        }
	        else if ( last_pressed_anchor == resize_upper_left ) 
	        {
	            new_height = original_height - ( mouse_y - (int)start_resize_drag.getY() );
	            new_width = original_width - ( mouse_x - (int)start_resize_drag.getX() );
	            
	            new_x = mouse_x;
	            new_y = mouse_y;
	        }
	        else if ( last_pressed_anchor == resize_upper_right ) 
	        {
	            new_height = original_height - ( mouse_y - (int)start_resize_drag.getY() );
	            new_width = original_width + ( mouse_x - (int)start_resize_drag.getX() );
	        
	            new_x = x;
	            new_y = mouse_y;
	        }
	        else if ( last_pressed_anchor == resize_lower_left ) 
	        {
	            new_height = original_height + ( mouse_y - (int)start_resize_drag.getY() );
	            new_width = original_width - ( mouse_x - (int)start_resize_drag.getX() );
	            
	            new_x = mouse_x;
	            new_y = y;
	        }
	        else
	        {
	            return;
	        }
	        
	        
		// Don't let the user make the container too small or too big
		
	        /*
	        if (new_height < 50) 
	        {
	            new_x = getX();
	            new_y = getY();
	            new_height = 50;
	        }
		if ( new_width < 50 ) 
	        {
	            new_x = getX();
	            new_y = getY();
	            new_width = 50;
	        }
		//if ( new_height > panel_height-y ) new_height = panel_height-y - 10;
		//if ( new_width > panel_width-x ) new_width = panel_width-x - 5;
		
		if (debug_statements) System.out.println(new_height + " " + new_width );
		
	         
	         */
	        
	        if ( new_height > 50 ) 
	        {
	            setHeight( new_height );
	            setY(new_y);
	        }
	        
	        if ( new_width > 50 )
	        {
	            setWidth( new_width );
	            setX(new_x);
	        }

	        //label.setXOffset(width);
	        //label.setYOffset(-label.font.getSize());
	        //refreshLocation();
	        
			this.label.x_offset = 0;
			this.label.y_offset = height+10;
	        
	        if ( panel != null ) panel.revalidate();
	    }

	    private void displayResizeHandles( Component c, Graphics2D g2d )
	    {
	        g2d.setColor(Color.gray);
	        placeResizeHandles();
	        //g2d.drawLine(x+width-7, y+height-7, x+width-5, y+height-5 );
	        g2d.drawRect( (int)resize_upper_left.getX(), (int)resize_upper_left.getY(), (int)resize_upper_left.getWidth(), (int)resize_upper_left.getHeight() );
	        g2d.drawRect( (int)resize_lower_left.getX(), (int)resize_lower_left.getY(), (int)resize_lower_left.getWidth(), (int)resize_lower_left.getHeight() );
	        g2d.drawRect( (int)resize_upper_right.getX(), (int)resize_upper_right.getY(), (int)resize_upper_right.getWidth(), (int)resize_upper_right.getHeight() );
	        g2d.drawRect( (int)resize_lower_right.getX(), (int)resize_lower_right.getY(), (int)resize_lower_right.getWidth(), (int)resize_lower_right.getHeight() );
	    }
	    
	    private void placeResizeHandles() 
	    { 
	        resize_lower_right.setRect(x+getWidthWithLabel()-5, y+getHeightWithLabel()-5, (int)(12), (int)(12));
	        resize_upper_right.setRect(x+getWidthWithLabel()-5, y-5, (int)(12), (int)(12));
	        resize_upper_left.setRect(x-5, y-5, (int)(12), (int)(12));
	        resize_lower_left.setRect(x-5, y+getHeightWithLabel()-5, (int)(12), (int)(12));
	    }
	    
	    public boolean contains(int x, int y) 
	    {
	        return super.contains( x, y ) || (resizeAnchor( x, y ) != null );
	    }
}
