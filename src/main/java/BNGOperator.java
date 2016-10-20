import java.awt.*; // For Graphics

import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files

public class BNGOperator extends BNGWidget 
{
	public int operator_type = 0; // 0 = plus, 1 = arrow, 2 = double_arrow
	private Image image;
	BNGLabel top_label;
	BNGLabel bottom_label;
	
	public BNGOperator( BNGWidgetPanel panel, int operator_type )
	{
		super(panel);
		height = 16;
		width = 16;
		setTopLabel("k");
		setBottomLabel("k");
		this.operator_type = operator_type;
		
		if ( operator_type == 0 )
		{
			image = loadImage("plus_op.gif");
		}
		else if ( operator_type == 1 )
		{
			image = loadImage("arrow_op.gif");
		}
		else if ( operator_type == 2 )
		{
			image = loadImage("double_arrow_op.gif");
		}	
	}
	
    public void display( Component c, Graphics2D g2d )
    {

	double panel_height = panel.applet.getContentPane().getHeight();

	// Center the BNGlabel
	if (top_label != null) top_label.x_offset = width/2-top_label.getWidth()/2;
	if (bottom_label != null) bottom_label.x_offset = width/2-bottom_label.getWidth()/2;
	
	if ( this.selected )
	    {

		g2d.setColor( Color.blue );

        int s_y = y-1;

        int s_height = height+1;

        g2d.drawRect( x-1, s_y, width+1, s_height );
	    }
	
	/*
	g2d.setColor(Color.green);

	Stroke def = g2d.getStroke();

	g2d.setStroke (new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
				     1f, new float[] {2f}, 0f));

	g2d.drawLine( x+width/2-1, 0, x+width/2-1, (int)panel_height );

	g2d.setStroke ( def );
	 */
	
	// Draw the operator icon    
	g2d.drawImage(image, x, y, width, height, panel);

    }
    
    public Image loadImage( String image_url ) 
    {
        URL url = this.getClass().getResource(image_url);
        ImageIcon icon = null;
        Image image = null;

        icon = new ImageIcon(url);
        image = icon.getImage();
       
        width = icon.getIconWidth();
        height = icon.getIconHeight();
        return image;
    }

    // override widget setlabel() to customize label placement
    public void setTopLabel( String new_label )
	{
		//containing_panel.removeFlickrLabel( this.label );
		if ( top_label == null )
		{
			top_label = new BNGLabel( new_label, this, x, y, panel, false );
			top_label.setFont(new Font("Arial", Font.PLAIN, 10));
			top_label.x_offset = width/2+top_label.width/2;
			top_label.y_offset = -10;
		}
		else
		{
			top_label.setString( new_label );  // = new FlickrLabel( new_label, getX(), getY()+getHeight()+12, containing_panel );
			top_label.x_offset = width/2+top_label.width/2;
			top_label.y_offset = -10;
		}            
		
		if (panel != null ) panel.repaint();
	}
    
    public void setBottomLabel( String new_label )
	{
		//containing_panel.removeFlickrLabel( this.label );
		if ( bottom_label == null )
		{
			bottom_label = new BNGLabel( new_label, this, x, y, panel, false );
			bottom_label.setFont(new Font("Arial", Font.PLAIN, 10));
			bottom_label.x_offset = width/2+bottom_label.width/2;
			bottom_label.y_offset = height+10;
		}
		else
		{
			bottom_label.setString( new_label );  // = new FlickrLabel( new_label, getX(), getY()+getHeight()+12, containing_panel );
			bottom_label.x_offset = width/2+bottom_label.width/2;
			bottom_label.y_offset = height+10;
		}            
		
		if (panel != null ) panel.repaint();
	}
    
	public void setX(int x)
	{ 
		this.x = x; 
		if (top_label != null) top_label.setX( x+width-top_label.x_offset );
        if (bottom_label != null) bottom_label.setX( x+width-bottom_label.x_offset );
	}
	
	public void setY(int y)
	{ 
		this.y = y; 
		if (top_label != null) top_label.setY( y+height-top_label.y_offset );
		if (bottom_label != null) bottom_label.setY( y+height-bottom_label.y_offset );
	}
	
	public BNGLabel getBottomLabel()
	{
		return bottom_label;
	}
	
	public BNGLabel getTopLabel()
	{
		return top_label;
	}
}
