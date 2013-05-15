import java.util.List;

import processing.core.*;
import processing.data.XML;

@SuppressWarnings("serial")
public class Template extends PApplet {

	XML xml;

	public void setup() {

		xml = loadXML("tiles.xml");
		XML[] children = xml.getChildren("tile");

		for (int i = 0; i < children.length; i++) {
			int[] connections = new int[4];

			int id = children[i].getInt("topcodeID");
			String name = children[i].getChild("name").getContent();

			// connections are in order: TRBL
			connections[0] = children[i].getChild("connections").getInt("T");
			connections[1] = children[i].getChild("connections").getInt("R");
			connections[2] = children[i].getChild("connections").getInt("B");
			connections[3] = children[i].getChild("connections").getInt("L");

			println(id + " " + name + "  { L" + connections[3] + "; R"
					+ connections[1] + "; T" + connections[0] + "; B"
					+ connections[2] + " }");
		}
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "Template" });
	}
}
