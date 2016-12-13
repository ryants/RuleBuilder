import java.util.regex.*;
import java.util.*; //For vector data structure
import java.awt.Color;

public class BNGModelStub
{
	private BNGViewer applet;
	private BNGWidgetPanel panel;

	private TreeMap edge_pairs_lhs = new TreeMap();
	private TreeMap edge_pairs_rhs = new TreeMap();
	private TreeMap map_pairs = new TreeMap();

	String molecule = "Lig(l,l).Lyn(U,SH2)";

	int operand_offset = 0;
	int operand_backstep = -38;
	int max_height = 0;

	class XComparer implements Comparator
	{
		public int compare(Object obj1, Object obj2)
		{
			BNGWidget w1 = (BNGWidget)obj1;
			BNGWidget w2 = (BNGWidget)obj2;

			if ( w1.x == w2.x ) return 0;
			else if ( w1.x < w2.x ) return -1;
			else return 1;
		}
	}

	public BNGModelStub( BNGViewer applet )
	{
		this.applet = applet;
		panel = applet.editor_panel;
	}


	public void writeBNGL()
	{
		try
		{

			applet.textbox.setForeground(Color.black);
			applet.error_bar.setText("");

			edge_pairs_lhs.clear();
			edge_pairs_rhs.clear();
			map_pairs.clear();

			String for_rate = "";
			String rev_rate = "";

			String bngl_string = "";

			Vector edges = new Vector();

			// Merge container vector and operator vector
			Vector containers = applet.editor_panel.containers;
			Vector operators = applet.editor_panel.operators;
			Vector widgets = new Vector();
			widgets.addAll(operators);
			widgets.addAll(containers);

			Collections.sort(widgets,new XComparer());

			boolean need_dot = false;

			boolean saw_operator_last = false;

			if (widgets.isEmpty()) bngl_string = "";

			Iterator widget_itr = widgets.iterator();
			while ( widget_itr.hasNext() )
			{

				BNGWidget widget = (BNGWidget)widget_itr.next();

				if ( widget.getClass().getName().equals("BNGOperator") )
				{

					if ( saw_operator_last )
					{
						throwFatalError("Operators must be separated by containers");
					}

					need_dot = false;

					BNGOperator operator = (BNGOperator)widget;
					if ( operator.operator_type == 0 )
					{
						bngl_string += " + ";
					}
					else if ( operator.operator_type == 1 )
					{
						bngl_string += " -> ";
						for_rate = operator.getTopLabel().getString();
					}
					else if ( operator.operator_type == 2 )
					{
						bngl_string += " <-> ";
						rev_rate = operator.getBottomLabel().getString();
						for_rate = operator.getTopLabel().getString();
					}

					saw_operator_last = true;
				}
				else if( widget.getClass().getName().equals("BNGContainer") )
				{
					BNGContainer container = ((BNGContainer)(widget));

					if (need_dot) bngl_string += ".";
					bngl_string = bngl_string + container.getLabel().getString() + "(";

					Vector components = container.components;
					Iterator components_itr = components.iterator();
					while ( components_itr.hasNext() )
					{
						BNGComponent component = ((BNGComponent)(components_itr.next()));

						if ( component != components.firstElement() ) bngl_string = bngl_string + ",";
						bngl_string = bngl_string + component.getLabel().getString();

						// Write edges
						Iterator edge_itr = component.edges.iterator();
						while( edge_itr.hasNext() )
						{
							BNGEdge edge = (BNGEdge)edge_itr.next();
							int index = edges.indexOf(edge);
							if ( index == -1 )
							{
								edges.add( edge );
								index = edges.indexOf(edge);
							}

							if ( edge.isMap() )  bngl_string += "%" + index;
							else bngl_string += "!" + index;
						}

						// write wildcard edges
						if ( component.getBindingState().equalsIgnoreCase("additional bonds") )
						{
							bngl_string += "!+";
						}
						else if ( component.getBindingState().equalsIgnoreCase("allow additional bonds") )
						{
							bngl_string += "!?";
						}
					}

					saw_operator_last = false;

					bngl_string = bngl_string + ")";

					need_dot = true;
				}
			}

			if (!for_rate.equals("")) bngl_string +=" " + for_rate;
			if (!rev_rate.equals("")) bngl_string += "," + rev_rate;

			if (bngl_string.equals("")) return;

			applet.textbox.setText(bngl_string);

			if ( countSubstring(bngl_string, "->") > 1) throwFatalError("Too many arrow operators");

			if ( !applet.editor_panel.checkMapsAndEdges() )
			{
				throwFatalError("Edges cannot span operators. Maps must span the arrow operator.");
			}
		}
		catch (BNGLInputMalformedException e)
		{

		}
	}

	public void parseOperand( String string, boolean isLHS ) throws BNGLInputMalformedException
	{

		System.out.println( "Molecule: " + string );
		String containers[] = string.split("\\.");
        if (!isLHS) {
            containers[containers.length - 1] = containers[containers.length - 1].replaceAll("(?<=\\))\\s+.*\\z", "");
        }
		// Process the containers
		int container_x_offset = operand_offset;
		for ( int i = 0; i < containers.length; i++ )
		{
			BNGContainer container = parseContainer( containers[i], isLHS );

			container.setX( container_x_offset );
			panel.addContainer( container );

			// remember max height
			if ( container.height > max_height ) max_height = container.height;

			container_x_offset += 100;
		}

		operand_offset = container_x_offset + 25;
	}

	public void parseBNGL( String string )
	{
		// The information text is displayed if there are no errors
		String information = "";

		try
		{
			operand_offset = 0;
			max_height = 0;

			panel.initialize();

			panel.applet.textbox.setForeground(Color.black);

			// Reset the edge pairs
			edge_pairs_lhs.clear();
			edge_pairs_rhs.clear();
			map_pairs.clear();

			// Nothing to process
			if ( string.equals("")) return;

            //Strip out line continuation characters
            string = string.replaceAll("\\\\\\s*","");
            
			// Split on arrow operators
			if ( string.contains("->") ) // -> includes <->
			{
				// Make sure there is only one occurrence of the arrow operator
				if ( countSubstring( string, "->" ) > 1 )
				{
					throwFatalError("Too many arrow operators");
				}

				String rule[];
				BNGOperator arrow;

				if ( string.contains("<->") )
				{
					arrow = new BNGOperator( panel, 2 );
					rule = string.split("<->");
				}
				else
				{
					arrow = new BNGOperator( panel, 1 );
					rule = string.split("->");
				}

				String operands[] = rule[0].split("(?<!!)\\+");
                boolean isLHS = true;
				for ( int i = 0; i < operands.length; i++ )
				{
					parseOperand(operands[i],isLHS);

					if ( i < operands.length - 1 )
					{
						BNGOperator operator = new BNGOperator( panel, 0 );
						operator.setX(operand_offset+operand_backstep);
						operator.setY(max_height/2);
						panel.operators.add(operator);
					}
				}

				// Add the arrow operator
				arrow.setX(operand_offset+operand_backstep);
				arrow.setY(max_height/2);

				// Read reaction rates
				try
                {
                    if (arrow.operator_type == 2)
                    {
                        java.util.regex.Pattern rates_pattern =
                                java.util.regex.Pattern.compile("\\s+([\\w\\)\\(/\\.\\*\\-]+),\\s*([\\w\\)\\(/\\.\\*\\-]+)\\s*\\z");
                        Matcher rates_fit = rates_pattern.matcher( rule[1] );

                        if ( !rates_fit.find() )
                        {
                            // Try to match at least one rate
                            java.util.regex.Pattern rate_pattern =
                                    java.util.regex.Pattern.compile("\\s+([\\w\\)\\(/\\.\\*\\-]+)\\s*\\z");
                            Matcher rate_fit = rate_pattern.matcher( rule[1] );

                            if ( !rate_fit.find() )
                            {
                                arrow.setTopLabel("kf");
                                arrow.setBottomLabel("kr");
                                string += " kf,kr";
                                int caret_position = applet.textbox.getCaretPosition();
                                applet.textbox.setText(string);
                                applet.textbox.setCaretPosition(caret_position);
                            }
                            else
                            {
                                arrow.setTopLabel( rate_fit.group( 1 ) );

                                information = "Rules require rates. Automatically added rate \'k-\'";
                                string += ",kr";
                                int caret_position = applet.textbox.getCaretPosition();
                                applet.textbox.setText(string);
                                applet.textbox.setCaretPosition(caret_position);

                            }
                        }
                        else
                        {

                            arrow.setTopLabel( rates_fit.group( 1 ) );
                            arrow.setBottomLabel( rates_fit.group( 2 ) );
                        }
                    }
                    else
                    {
                        java.util.regex.Pattern rates_pattern = java.util.regex.Pattern.compile("\\s+([\\w\\)\\(\\.\\*/\\-]+),\\s*([\\w/\\)\\(\\.\\*\\-]+)\\s*\\z");
                        Matcher rates_fit = rates_pattern.matcher( rule[1] );

                        if ( rates_fit.find() )
                        {
                            throwFatalError("forward arrow cannot receive a reverse rate");
                        }

                        java.util.regex.Pattern rate_pattern = java.util.regex.Pattern.compile("\\s+([\\w/\\)\\(\\.\\*\\-\\+]+)\\s*\\z");
                        Matcher rate_fit = rate_pattern.matcher( rule[1] );

                        if ( !rate_fit.find() )
                        {
                            //String error_msg = "Could not parse reaction rate";
                            //displayError(error_msg);
                            information = "Rules require rates. Automatically added \'k\'";

                            string += " k";
                            int caret_position = applet.textbox.getCaretPosition();
                            applet.textbox.setText(string);
                            applet.textbox.setCaretPosition(caret_position);
                        }
                        else
                        {
                            arrow.setTopLabel( rate_fit.group( 1 ) );
                        }

                    }

                    panel.addOperator(arrow);
                    operands = rule[1].split("(?<!!)\\+");
                    isLHS = false;
				}
				catch ( ArrayIndexOutOfBoundsException e)
				{
					throwFatalError("rule needs products");
				}

				for ( int i = 0; i < operands.length; i++ )
				{
					parseOperand(operands[i],isLHS);

					if ( i < operands.length - 1 )
					{
						BNGOperator operator = new BNGOperator( panel, 0 );
						operator.setX(operand_offset+operand_backstep);
						operator.setY(max_height/2);
						panel.addOperator(operator);
					}
				}

			}
			else
			{
				String operands[] = string.split("(?<!!)\\+");
                boolean isLHS = true;
				for ( int i = 0; i < operands.length; i++ )
				{
					parseOperand(operands[i],isLHS);

					if ( i < operands.length - 1 )
					{
						BNGOperator operator = new BNGOperator( panel, 0 );
						operator.setX(operand_offset+operand_backstep);
						operator.setY(max_height/2);
						panel.operators.add(operator);
					}
				}
			}

			// Add LHS edges
			Iterator i_lhs = edge_pairs_lhs.entrySet().iterator();
			// Display elements
			while(i_lhs.hasNext())
			{
				Map.Entry me = (Map.Entry)i_lhs.next();
				System.out.print(me.getKey() + ": ");
				System.out.println(me.getValue());
				BNGComponent[] component_pair = (BNGComponent[])me.getValue();

				if ( component_pair[1] == null )
				{
					String error_msg = "Unmatched edge endpoint: " + me.getKey();
					throwFatalError( error_msg );
				}

				BNGEdge edge = new BNGEdge(panel, component_pair[0], component_pair[1]);
				//component_pair[0].addEdge(edge);
				//component_pair[1].addEdge(edge);
				panel.edges.add(edge);
			}
            // Add RHS edges
            Iterator i_rhs = edge_pairs_rhs.entrySet().iterator();
            // Display elements
            while(i_rhs.hasNext())
            {
                Map.Entry me = (Map.Entry)i_rhs.next();
                System.out.print(me.getKey() + ": ");
                System.out.println(me.getValue());
                BNGComponent[] component_pair = (BNGComponent[])me.getValue();

                if ( component_pair[1] == null )
                {
                    String error_msg = "Unmatched edge endpoint: " + me.getKey();
                    throwFatalError( error_msg );
                }

                BNGEdge edge = new BNGEdge(panel, component_pair[0], component_pair[1]);
                //component_pair[0].addEdge(edge);
                //component_pair[1].addEdge(edge);
                panel.edges.add(edge);
            }

			// Add maps
			Iterator mapi = map_pairs.entrySet().iterator();
			// Display elements
			while(mapi.hasNext())
			{
				Map.Entry me = (Map.Entry)mapi.next();
				System.out.print(me.getKey() + ": ");
				System.out.println(me.getValue());
				BNGComponent[] component_pair = (BNGComponent[])me.getValue();

				if ( component_pair[1] == null )
				{
					String error_msg = "Unmatched edge endpoint: " + me.getKey();
					throwFatalError( error_msg );
				}

				BNGEdge edge = new BNGEdge(panel, component_pair[0], component_pair[1]);
				//component_pair[0].addEdge(edge);
				//component_pair[1].addEdge(edge);

				// Make it a map
				edge.setMap(true);

				panel.edges.add(edge);
			}

			if ( !applet.editor_panel.checkMapsAndEdges() )
			{
				throwFatalError("Edges cannot span operators. Maps must span the arrow operator");
			}

			applet.error_bar.setText(information);
		}
		catch ( BNGLInputMalformedException e )
		{
			panel.initialize();
			panel.applet.textbox.setForeground(Color.red);
		}
	}

	private BNGContainer parseContainer( String string, boolean isLHS ) throws BNGLInputMalformedException
	{
		System.out.println("Container String: " + string);

		if ( string.equals(""))
		{
			throwFatalError("A container is missing");
		}

		// strip trailing spaces
		string = string.replaceFirst("\\s*\\z", "");

		// strip trailing spaces
		string = string.replaceFirst("^\\s*", "");

		if ( countSubstring(string, "(") > 1 || countSubstring( string, ")") > 1 )
		{
			throwFatalError("Too many parentheses in container");
		}

		java.util.regex.Pattern container_label_pattern = java.util.regex.Pattern.compile("^\\s*([A-Za-z]\\w*)(?=\\()");
		Matcher container_label_fit = container_label_pattern.matcher( string );

		if ( !container_label_fit.find() )
		{
			String error_msg = "Container \""+string+"\" is malformed";
			throwFatalError(error_msg);
		}

		String container_label = container_label_fit.group( 1 );

		System.out.println("Container Label: " + container_label );

		BNGContainer container = new BNGContainer( panel );

		container.setLabel(container_label);

		// Match components
		java.util.regex.Pattern components_pattern = java.util.regex.Pattern.compile("\\((\\S*)\\)(\\S*)");
		Matcher components_fit = components_pattern.matcher( string );

		if ( !components_fit.find() )
		{
			// Handle label-only container
			if ( string.equals(container_label)) return container;
			else
			{
				String error_msg = "The container \"" + string + "\" is malformed";
				throwFatalError( error_msg );
			}
		}

		String components_str = components_fit.group(1);

		String leftover_str = components_fit.group(2);

		if (!leftover_str.equals(""))
		{
			throwFatalError("Extraneous characters \""+leftover_str+"\" in container");
		}

		int current_x_offset = 20;
		int current_y_offset = 20;

		String[] components = components_str.split(",");
		for ( int i = 0; i < components.length; i++ )
		{
			String component_string = components[i];
			if (component_string.equals(""))
			{
				break;
			}

			System.out.println( "Model: Processing component: " + component_string + " (size="+component_string.length()+")");

			BNGComponent new_component = new BNGComponent(panel);

			java.util.regex.Pattern component_pattern = java.util.regex.Pattern.compile("(\\w+)(~\\w+)?(?!\\W)");
			Matcher fit = component_pattern.matcher(component_string);

			//Confirm only one state per site
			if (component_string.split("~",-1).length > 2){
				String error_msg = "Only have 1 state per site (multiple '~' chars found)";
				throwFatalError(error_msg);
			}

			if ( !fit.matches() )
			{
				String error_msg = "Component "+component_string+" is malformed";
				throwFatalError( error_msg );
			}

			String component_label = fit.group(1);
			String state = fit.group(2);

			System.out.println( "Model: Processing component: state = " + state );

			// Match edges
			java.util.regex.Pattern edge_pattern = java.util.regex.Pattern.compile("!(\\d+|\\+|\\?)");
			Matcher edge_fit = edge_pattern.matcher(component_string);

			while ( edge_fit.find() )
			{
				String edge_id = edge_fit.group(1);

				System.out.println("Found edge " + edge_id + " attached to component " + component_label );

				if ( edge_id.equals("?") )
				{
					new_component.setBindingState("allow additional bonds");
				}
				else if ( edge_id.equals("+") )
				{
					new_component.setBindingState("additional bonds");
				}
				else
				{
					new_component.setBindingState("no additional bonds");

					BNGComponent[] component_pair =
							(isLHS) ? (BNGComponent[])edge_pairs_lhs.get(edge_id) : (BNGComponent[])edge_pairs_rhs.get(edge_id);

					if ( component_pair == null )
					{
						component_pair = new BNGComponent[2];
						component_pair[0] = new_component;
						if (isLHS) {
							edge_pairs_lhs.put(edge_id, component_pair);
						} else {
							edge_pairs_rhs.put(edge_id, component_pair);
						}
					}
					else
					{
						if ( component_pair[0] == null )
						{
							System.out.println("Error (Sanity Check Failed): component_pair should not be non-null when zeroth element is null");
						}
						else if ( component_pair[1] == null )
						{
							component_pair[1] = new_component;
						}
						else
						{
							throwFatalError("Edge \"" + edge_id + "\" has more than two endpoints");
						}
					}
				}
			}

			// Match maps
			java.util.regex.Pattern map_pattern = java.util.regex.Pattern.compile("%(\\d+)");
			Matcher map_fit = map_pattern.matcher(component_string);

			//new_component.setBindingState("No Additional Bonds");

			while ( map_fit.find() )
			{
				String edge_id = map_fit.group(1);

				System.out.println("Found map " + edge_id + " attached to component " + component_label );

				BNGComponent[] component_pair = (BNGComponent[])map_pairs.get(edge_id);

				if ( component_pair == null )
				{
					component_pair = new BNGComponent[2];
					component_pair[0] = new_component;
					map_pairs.put( edge_id, component_pair );
				}
				else
				{
					if ( component_pair[0] == null )
					{
						System.out.println("Error (Sanity Check Failed): component_pair should not be non-null when zeroth element is null");
					}
					else
					{
						component_pair[1] = new_component;
					}
				}
			}

			container.addComponent(new_component);

			if (state != null) component_label += state;

			new_component.setLabel(component_label);
			new_component.x = current_x_offset;
			new_component.y = current_y_offset;
			new_component.x_offset = current_x_offset  - container.x;
			new_component.y_offset = current_y_offset  - container.y;

			container.setHeight( current_y_offset + 50);

			// remember max height
			if ( current_y_offset > max_height ) max_height = current_y_offset;

			panel.addComponent(new_component);

			current_y_offset += 50;
		}


		return container;
	}

	public void throwFatalError( String string ) throws BNGLInputMalformedException
	{
		applet.error_bar.setText(string);
		applet.textbox.setForeground(Color.red);
		throw new BNGLInputMalformedException(string);
	}

	public void displayError( String string )
	{
		applet.error_bar.setText(string);
		applet.textbox.setForeground(Color.red);
	}

	public int countSubstring(String str, String substr)
	{
		String temp=str;
		int count=0;
		int i=temp.indexOf(substr);
		while(i>=0){
			count++;
			temp=temp.substring(i+1);
			i=temp.indexOf(substr);
		}
		return count;
	}
}
