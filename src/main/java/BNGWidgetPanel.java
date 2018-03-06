
import java.net.*; //For URL image loading from Jar files
import javax.swing.*; // For graphical interface tools
import java.util.*; //For vector data structure
import java.awt.*; // For Graphics
import java.awt.event.*; // For mouse interactions
import javax.swing.event.MouseInputAdapter;
import java.awt.geom.*; // For Line2D
import java.awt.image.*; // For BufferedImage
import javax.imageio.ImageIO; // For ImageIO
import java.io.*; // For File classes

public class BNGWidgetPanel extends JPanel {

	protected Vector components = new Vector();
	protected Vector labels = new Vector();
	protected Vector containers = new Vector();
	protected Vector edges = new Vector();
	protected Vector operators = new Vector();
	
	private boolean resize_selected_container = false;
	
	protected BNGEdge selected_edge;
	protected BNGContainer selected_container;
	protected BNGComponent selected_component;
	protected BNGLabel selected_label;
	protected BNGOperator selected_operator;
	
	// Temporary edge end points
	protected BNGComponent start, end;

	public RuleBuilder applet; // Pointer to the applet

	protected MouseControl mouse_control = new MouseControl( this );
	protected KeyboardControl keyboard_control = new KeyboardControl();

	protected class KeyboardControl implements KeyListener 
	{ 
		public void keyPressed(KeyEvent evt) 
		{
			int key = evt.getKeyCode();  // keyboard code for the key that was pressed


		}

		public void keyReleased(KeyEvent evt) 
		{

		}

		public void keyTyped(KeyEvent evt) 
		{

		}
	}

	private class MouseControl extends MouseInputAdapter
	{
		transient protected boolean mouse_over = false;

		public int pointer_x;
		public int pointer_y;
		
		private BNGWidgetPanel panel;

		BNGWidget previously_clicked = null;

		public MouseControl( BNGWidgetPanel panel ) 
		{
			this.panel = panel;
		}

		public void mousePressed(MouseEvent e) 
		{ 
			
			// See if we are in manipulation mode
			if ( !(applet.mode.equals("manipulate") ) ) return;
			
			// Compensate for zoom and translation
			int mouse_x = (int)(e.getX());
			int mouse_y = (int)(e.getY()); 
						
			// See if a container resize anchor was pressed
			if (selected_container != null)
			{
				String  anchor = selected_container.resizeAnchor(mouse_x, mouse_y);
				if ( anchor != null ) 
				{
					if ( anchor.equals("NE") )
					{
						panel.setCursor( Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR ) );
						resize_selected_container = true;
					}
					else if ( anchor.equals("NW") )
					{
						panel.setCursor( Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR ) );
						resize_selected_container = true;
					}
					else if ( anchor.equals("SE") )
					{
						panel.setCursor( Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR ) );
						resize_selected_container = true;
					}
					else if ( anchor.equals("SW") )
					{
						panel.setCursor( Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR ) );
						resize_selected_container = true;
					}
				}
			}
              
			Iterator label_itr = labels.iterator();
			while ( label_itr.hasNext() ) 
			{
				BNGLabel label = (BNGLabel)label_itr.next();
				if ( label.contains( e.getX(), e.getY() ) ) 
				{
					clearPreviousSelection();

					selected_label = label;

					selected_label.setSelected( true );
					selected_label.setEditable(true);					
					repaint();
					return;
				}
			}

			Iterator operator_itr = operators.iterator();
			while ( operator_itr.hasNext() ) 
			{
				BNGOperator o = (BNGOperator)operator_itr.next();
				if ( o.contains(mouse_x, mouse_y) )
				{
					clearPreviousSelection();
					o.setSelected(true);
					selected_operator = o;
					repaint();
					return;
				}
			}
			
			Iterator component_itr = components.iterator();
			while ( component_itr.hasNext() ) 
			{
				BNGComponent c = (BNGComponent)component_itr.next();
				if ( c.contains( mouse_x, mouse_y ) ) 
				{	
					clearPreviousSelection();
					
					c.setSelected(true);

					selected_component = c;
					
					repaint();	
					return;
				}
			}

			Iterator container_itr = containers.iterator();
			while ( container_itr.hasNext() ) 
			{
				BNGContainer c = (BNGContainer)container_itr.next();
				if ( c.contains( mouse_x, mouse_y ) ) 
				{
					clearPreviousSelection();
					c.setSelected(true);
			
					selected_container = c;
					
					repaint();

					return;
				}
				
			}

			Iterator i = edges.iterator();
			while ( i.hasNext() ) 
			{

				BNGEdge edge = (BNGEdge)i.next();

				if ( edge.contains(e.getX(), e.getY() ) )
				{
					clearPreviousSelection();
					selected_edge = edge;
					selected_edge.setSelected(true);

					repaint();					
					return;
				}
			}

			clearPreviousSelection();			
		}

		public void mouseDragged(MouseEvent e) 
		{
			if ( selected_operator != null )
			{
				int width = selected_operator.width;
				int height = selected_operator.height;
				
				selected_operator.setX( (int)e.getX() - width/2 );
				selected_operator.setY( (int)e.getY() - height/2 );
				repaint();
			}
			else if ( selected_component != null )
			{
				int width = selected_component.width;
				int height = selected_component.height;
				
				selected_component.setX( (int)e.getX() - width/2 );
				selected_component.setY( (int)e.getY() - height/2 );
				repaint();
			}
			else if ( selected_container != null )
			{
				if (resize_selected_container) 
				{
                    selected_container.resize(e.getX(), e.getY());
				}
				else // Move the container
				{
				int width = selected_container.width;
				int height = selected_container.height;
				selected_container.setX( (int)e.getX() - width/2 );
				selected_container.setY( (int)e.getY() - height/2 );
				}
				
				repaint();
				
			}
			
			applet.model_stub.writeBNGL();
		}

		public void mouseReleased(MouseEvent e) 
		{
			// Check for components contained by containers
			// For each component see if the two opposite corners are within a container
			Iterator components_itr = components.iterator();
			while( components_itr.hasNext() )
			{
				boolean component_contained = false;
				BNGComponent component = (BNGComponent)components_itr.next();
				int x1 = component.x;
				int y1 = component.y;
				int x2 = component.width + x1;
				int y2 = component.height + y1;
				
				Iterator containers_itr = containers.iterator();
				while( containers_itr.hasNext() )
				{
					BNGContainer container = (BNGContainer)containers_itr.next();
					if ( container.contains( x1, y1 ) && container.contains( x2, y2 ) )
					{
						container.addComponent( component );
						component_contained = true;
						break; // so we don't add the same component to more than one container
					}
				}

				// Handle components that were contained but are no longer
				if (!component_contained && component.container != null ) 
				{
					component.container.removeComponent( component );  
				}
				
				//Update relative position to container if inside one
				if ( component.container != null )
				{
					component.x_offset = component.x - component.container.x;
					component.y_offset = component.y - component.container.y;
				}
			}
			
			// Reset the resize flag
			resize_selected_container = false;
			
			// reset the pointer icon
			setCursorAccordingToMode();
			
			// Update the BNGL textbox
			applet.model_stub.writeBNGL();
		}

//		 This method is required by MouseListener.
        public void mouseMoved(MouseEvent e) 
        {   
            if ( applet.mode.equals("add bond") )
            {
                if ( start != null )
                {
                	pointer_x = e.getX();
                	pointer_y = e.getY();
                    repaint();
                }
            }
            
            if ( applet.mode.equals("add maps") )
            {
                if ( start != null )
                {
                	pointer_x = e.getX();
                	pointer_y = e.getY();
                    repaint();
                }
            }
        }
		
		public void mouseClicked(MouseEvent e)
		{
			applet.model_stub.writeBNGL();
			
			if ( applet.mode.equals("manipulate") )
			{

			}
			else if ( applet.mode.equals("add bond") || applet.mode.equals("add maps") )
			{
				if (start != null ) // Start component has been selected
				{
					Iterator component_itr = components.iterator();
					while ( component_itr.hasNext() ) 
					{
						BNGComponent c = (BNGComponent)component_itr.next();
						if ( c.contains( e.getX(), e.getY() ) ) 
						{
							BNGComponent proposed_end = c;
							boolean arrow_found = false;
							
							// Figure out if the edge spans the arrow operator
							if ( !operators.isEmpty() )
							{
								Iterator op_itr = operators.iterator();
								while ( op_itr.hasNext() )
								{
									BNGOperator op = (BNGOperator)op_itr.next();
									if ( op.operator_type == 1 ||  op.operator_type == 2 )
									{
										arrow_found = true;
										
										if ( (op.x < start.x && op.x < proposed_end.x) 
										   ||(op.x > start.x && op.x > proposed_end.x) )
										{
											// make sure its an edge
											if ( applet.mode.equals("add maps") )
											{
												start = null;
												repaint();
												displayErrorDialog("Maps must span the arrow operator");
												return;
											}
											
											break;
										}
										else
										{
											// make sure its a map
											if ( applet.mode.equals("add bond") )
											{	start = null;
												repaint();
												displayErrorDialog("Bonds cannot span the arrow operator");
												return;
											}
											
											break;
										}
									}
								}
							}

							if ( applet.mode.equals("add maps") && !arrow_found )
							{
								start = null;
								repaint();
								displayErrorDialog("Maps must span the arrow operator");
								return;
							} 
							
							end = proposed_end;
							
							BNGEdge edge = new BNGEdge( panel, start, end );
							
							if ( applet.mode.equals("add maps") )
							{
								edge.setMap(true);
							}
							
							edges.add(edge);

							// Reset the temp end points
							end = null;
							start = null;

							applet.model_stub.writeBNGL();
							
							repaint();
							return;
						}
					}
				}
				else // Neither the start or end components have been selected
				{
					Iterator component_itr = components.iterator();
					while ( component_itr.hasNext() ) 
					{
						BNGComponent c = (BNGComponent)component_itr.next();
						if ( c.contains( e.getX(), e.getY() ) ) 
						{
							
							start = c;
							return;
						}
					}
				}
			}
			else if ( applet.mode.contains("add sites") )
			{
				BNGComponent component = new BNGComponent(panel); 
				component.setX( e.getX() - component.width/2 );
				component.setY( e.getY() - component.height/2 );
				addComponent(component);
		
				if (applet.mode.equals("add sites (arbitrary bond state)" ))
						component.setBindingState("allow additional bonds");
				else if (applet.mode.equals("add sites (unbound)" ))
						component.setBindingState("no additional bonds");
				else if (applet.mode.equals("add sites (unknown binding partner)" ))
						component.setBindingState("additional bonds");
			}
			else if ( applet.mode.equals("" +
					"add molecule") )
			{
				BNGContainer container = new BNGContainer(panel); 
				container.setX( e.getX() - container.width/2 );
				container.setY( e.getY() - container.height/2 );
				addContainer(container);
			}
			else if ( applet.mode.contains("add operator") )
			{
				BNGOperator operator = null;
				
				if ( applet.mode.contains("double") )
				{
					operator = new BNGOperator( panel, 2 );
					
				}
				else if ( applet.mode.contains("plus") )
				{
					operator = new BNGOperator( panel, 0 );				
				}
				else if ( applet.mode.contains("arrow") )
				{
					operator = new BNGOperator( panel, 1 );								
				}
				else
				{
					System.out.println("unknown action " + applet.mode);
					return;
				}
				
				operator.setX( e.getX() - operator.width/2 );
				operator.setY( e.getY() - operator.height/2 );
				addOperator( operator );
			}
			
			applet.model_stub.writeBNGL();
		}
	}

	public BNGWidgetPanel(RuleBuilder applet)
	{
		this.applet = applet;
		addMouseMotionListener(mouse_control);
		addMouseListener(mouse_control);	
        addKeyListener( keyboard_control );
	}

	protected void paintComponent(Graphics gr) 
	{
		// Setup the canvas
		Graphics2D g = (Graphics2D) ((Graphics2D)gr).create();

		RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		g.setRenderingHints(renderHints);

		//		 paint entire canvas white
		if (isOpaque()) 
		{ 
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight() );
		}
		
		if ( applet.mode.contentEquals("add bond") || applet.mode.contentEquals("add maps") )
		{
		 if (start != null)
		 {
			 g.setColor(Color.DARK_GRAY);
			 
			 Line2D line = new Line2D.Float( start.getEdgeAttachPoint().x, start.getEdgeAttachPoint().y, mouse_control.pointer_x, mouse_control.pointer_y );
			 
			 Stroke default_stroke = ((Graphics2D)g).getStroke();
             
         	((Graphics2D)g).setStroke(new BasicStroke( 1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] {2f}, 0f));
         	((Graphics2D)g).draw(line);
         	//((Graphics2D)g).drawArc(start.getEdgeAttachPoint().x, start.getEdgeAttachPoint().y, mouse_control.pointer_x, mouse_control.pointer_y, 60, 240);
         	
         	((Graphics2D)g).setStroke( default_stroke );
		 }
		}
		else
		{
			start = null;
		}

		// Display the drawing elements (Components, Edges, Containers, Operators, Labels, and the selection box)

		Iterator operator_itr = operators.iterator();
		while( operator_itr.hasNext() )
		{
			((BNGOperator)operator_itr.next()).display(this, g);
		}   
		
		Iterator edges_itr = edges.iterator();
		while( edges_itr.hasNext() )
		{
			((BNGEdge)edges_itr.next()).display(this, g);
		}   

		Iterator components_itr = components.iterator();
		while( components_itr.hasNext() )
		{
			((BNGComponent)components_itr.next()).display(this, g);
		}

		Iterator containers_itr = containers.iterator();
		while( containers_itr.hasNext() )
		{
			((BNGContainer)containers_itr.next()).display(this, g);
		}

		Iterator labels_itr = labels.iterator();
		while( labels_itr.hasNext() )
		{
			BNGLabel label = ((BNGLabel)labels_itr.next());
			label.display(this, g);
		}   

		g.dispose();
	}

	public boolean addSelectedComponent( BNGComponent bc ) 
	{
		if ( selected_component != null) 
		{
			selected_component.setSelected(false);
		}	

		selected_component = bc;
		selected_component.setSelected( true );
		return addComponent(bc);
	}

	public boolean addOperator( BNGOperator operator ) 
	{

		// Make sure the component is not already in the panel
		if ( operators.indexOf( operator ) != -1 ) {
			return false; 
		}

		operators.add( operator );

		// Add attendant label so we can manipulate it from the panel
		if (operator.operator_type == 1) 
		{
			addLabel( operator.getTopLabel() );
		}
		if (operator.operator_type == 2)
		{
			addLabel( operator.getTopLabel() );			
			addLabel( operator.getBottomLabel() );
		}

		return true;
	}	
	
	public boolean addComponent( BNGComponent component ) 
	{

		// Make sure the component is not already in the panel
		if ( components.indexOf( component ) != -1 ) {
			return false; 
		}

		components.add( component );

		// Add attendent label so we can manipulate it from the panel

		addLabel( component.getLabel() );

		// Check for components contained by containers
		// For each component see if the two opposite corners are within a container
		int x1 = component.x;
		int y1 = component.y;
		int x2 = component.width + x1;
		int y2 = component.height + y1;

		Iterator containers_itr = containers.iterator();
		while( containers_itr.hasNext() )
		{
			BNGContainer container = (BNGContainer)containers_itr.next();
			if ( container.contains( x1, y1 ) && container.contains( x2, y2 ) )
			{
				container.addComponent( component );
				break; // so we don't add the same component to more than one container
			}
		}

		revalidate();
		repaint();
		
		return true;
	}

	public boolean addContainer( BNGContainer container ) 
	{
		
		// Make sure the container is not already in the panel
		if ( containers.indexOf( container ) != -1 ) {
			return false; 
		}
		
		// Add attendent label so we can manipulate it from the panel
		if ( !addLabel( container.getLabel() ) )
				{
					System.out.println("Error: could not add label to panel");
				}
		
		containers.add( container );

		// Check for components contained by containers
		// For each component see if the two opposite corners are within a container



		Iterator components_itr = components.iterator();
		while( components_itr.hasNext() )
		{
			BNGComponent component = (BNGComponent)components_itr.next();
			int x1 = component.x;
			int y1 = component.y;
			int x2 = component.width + x1;
			int y2 = component.height + y1;

			if ( container.contains(x1,y1) && container.contains(x2, y2) )
			{
				container.addComponent( component );
			}
		}

		revalidate();
		repaint();
		
		return true;
	}

	public boolean addLabel( BNGLabel label ) 
	{
		if ( labels.indexOf( label ) != -1 ) 
		{
			return false;
		} 

		if ( label == null ) 
		{
			return false;
		} 

		
		labels.add( label );

		revalidate();
		repaint();
		return true;
	}

	public boolean removeSelectedWidget() 
	{
		boolean widget_removed = false;
		if ( selected_component != null )
		{
			selected_component.disconnectEdges();
			components.remove(selected_component);
			labels.remove(selected_component.getLabel());
			if ( selected_component.container != null ) selected_component.container.removeComponent(selected_component);
			selected_component = null;
			widget_removed = true;
		}
		else if ( selected_container != null )
		{
			selected_container.removeAllComponents();
			containers.remove(selected_container);
			selected_container.removeComponentsFromPanel();
			labels.remove(selected_container.getLabel());
			selected_container = null;
			widget_removed = true;
		}
		else if ( selected_edge != null )
		{
			selected_edge.start.edges.remove(selected_edge);
			selected_edge.end.edges.remove(selected_edge);
			selected_edge.start.removeEdge(selected_edge);
			selected_edge.end.removeEdge(selected_edge);
			edges.remove(selected_edge);
			selected_edge = null;
			widget_removed = true;
		}
		else if ( selected_operator != null )
		{
			selected_operator.setSelected(false);
			operators.remove(selected_operator);
			labels.remove(selected_operator.getTopLabel());
			labels.remove(selected_operator.getBottomLabel());
			
			selected_operator = null;
			widget_removed = true;
		}

		revalidate();
		repaint();

		applet.model_stub.writeBNGL();
		
		return widget_removed;
	}

	public KeyboardControl getKeyboardListener() 
	{
		return keyboard_control;
	}

    public Image loadImage(java.lang.String path) 
    {
        URL url = this.getClass().getResource(path);
        ImageIcon icon = null;
        
        try 
        {
            icon = new ImageIcon(url);
        }
        catch ( Exception e )
        {  
            return null;
        }
        
        Image  image = icon.getImage();
        return image;
    }
	
	public void setCursorAccordingToMode() 
    {
        if ( applet.mode.equals("manipulate") ) 
        {
           Cursor c = Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ); 
           setCursor( c );
        }
        else
        {
        	Cursor c = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ); 
            setCursor( c );    
        }    
    }
	
	public void clearPreviousSelection()
	{
		// Deselect previously selected widgets
		if (selected_container != null)
		{
			selected_container.setSelected(false);
			selected_container = null;
		}
		
		if (selected_component != null)
		{
			selected_component.setSelected(false);
			selected_component = null;
		}

		if (selected_operator != null)
		{
			selected_operator.setSelected(false);
			selected_operator = null;
		}
		
		if (selected_edge != null)
		{
			selected_edge.setSelected(false);
			selected_edge = null;
		}			
		
		if ( selected_label != null )
		{
			selected_label.setSelected(false);
			selected_label.setEditable(false);
			selected_label = null;
		}
		
		repaint();
	}
	
	public void initialize()
	{
		components.removeAllElements();
		labels.removeAllElements();
		containers.removeAllElements();
		edges.removeAllElements();
		operators.removeAllElements();
		
		resize_selected_container = false;
		
		selected_edge = null;
		selected_container = null;
		selected_component = null;
		selected_label = null;
		selected_operator = null;
		
		// Temporary edge end points
		start = null;
		end = null;

	}
	
	void displayErrorDialog( String error )
	{
		JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE); 
	}

	// Make sure maps span arrow operator and edges don't
	boolean checkMapsAndEdges()
	{
		BNGOperator arrow = null;
		Iterator op_itr = operators.iterator();
		while ( op_itr.hasNext() )
		{
			BNGOperator op = (BNGOperator)op_itr.next();
			if ( op.operator_type == 1 || op.operator_type == 2 )
			{
				arrow = op;
			}
		}
		
		// Make sure edge endpoints are on the same side of the arrow
		// and that an arrow exists for maps and that the end points span the arrow
		Iterator edge_itr = edges.iterator();
		while ( edge_itr.hasNext() )
		{
			BNGEdge edge = (BNGEdge)edge_itr.next();

			if ( arrow == null && edge.isMap() )
			{
				return false;
			}
			else if (arrow != null)
			if (( edge.start.x > arrow.x && edge.end.x > arrow.x )
				|| ( edge.start.x < arrow.x && edge.end.x < arrow.x ))
			{
				if ( edge.isMap() ) return false;
			}
			else
			{
				if ( !edge.isMap() ) return false;
			}	
		}
		
		// Check edges don't span any operators
		Iterator edge_itr2 = edges.iterator();
		while ( edge_itr2.hasNext() )
		{
			BNGEdge edge = (BNGEdge)edge_itr2.next();
			if ( !edge.isMap() )
			{
				Iterator op_itr2 = operators.iterator();
				while( op_itr2.hasNext() )
				{
					BNGOperator op = (BNGOperator)op_itr2.next();
					if (( edge.start.x > op.x && edge.end.x > op.x )
							|| ( edge.start.x < op.x && edge.end.x < op.x ))
					{
						continue;
					}
					else
					{
						return false;
					}
						
				}
			}
		}
		
		return true;
	}

	public boolean toImageFile( File file )
	{
        try {

        	ImageFilter filter = new ImageFilter();
        			
        	if ( !filter.accept(file) ) return false; 
        	
        	String ext = ""; // File extension
        	
        	String s = file.getName();
        	int i = s.lastIndexOf('.');
        	if (i > 0 && i < s.length() - 1) ext = s.substring(i + 1);
        	else return false;
        	
            int w = this.getWidth(), h = this.getHeight();
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            this.paint(g2);
            g2.dispose();
            ImageIO.write(image, ext, file);
        } catch (IOException e) {
            System.err.println(e);
        }
        return true;
	}
}
