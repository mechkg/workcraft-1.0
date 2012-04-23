package workcraft.petri;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import workcraft.Tool;
import workcraft.ToolType;
import workcraft.WorkCraftServer;
import workcraft.editor.Editor;

public class PetriDotGSaver4 implements Tool {
	public static final String _modeluuid = "65f89260-641d-11db-bd13-0800200c9a66";
	public static final String _displayname = "Net as .g (transitions as STG)";

	private String saveTransition(String str) {
		Pattern p = Pattern.compile("(.+)\\_(minus||plus)([0-9])");
		Matcher m;
		m = p.matcher(str);
		if (m.find()) {
			return m.group(1)+((m.group(2).equals("plus"))?"+":"-")+"/"+m.group(3);
		}
		return str;
	}
	
	public void writeFile (String path, PetriModel doc) throws IOException {
		PrintWriter out = new PrintWriter(new FileWriter(path));

		out.println("# File generated by Workcraft.");
		out.print(".internal"); // new mpsat does not like dummies :D
		
		String ts;
		Pattern pt = Pattern.compile("(.+)\\_(minus||plus)([0-9])");
		Matcher m;
		
		HashSet<String> names = new HashSet<String>();
		
		for(EditablePetriTransition t: doc.transitions) {
			ts = t.getId();
			m = pt.matcher(ts);
			if (m.find()) {
				if (!names.contains(m.group(1))) {
					names.add(m.group(1));
					out.print(" "+m.group(1));
				}
			} else out.print(ts);
		}

		out.println();
		out.println(".graph");

		for(EditablePetriTransition t: doc.transitions)
		{
			for(EditablePetriPlace prev: t.getIn()) {
				out.print(prev.getId()+" "+saveTransition(t.getId()));
				out.println("");
			}

			for(EditablePetriPlace next: t.getOut()) {
				out.println(saveTransition(t.getId())+" "+next.getId());
			}
		}					

		out.print(".marking {");

		for(EditablePetriPlace p: doc.places) if (p.getTokens()>0) out.print(" "+p.getId());
		out.println(" }");
		out.println(".end");
		out.close();
	}

	public void run(Editor editor, WorkCraftServer server) {
		PetriModel doc = (PetriModel) (editor.getDocument());
		String last_directory = editor.getLastDirectory();

		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new GFileFilter());
		if (last_directory != null)
			fc.setCurrentDirectory(new File(last_directory));
		if (fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
		{
			String path = fc.getSelectedFile().getPath();
			if (!path.endsWith(".g")) path += ".g";
			{
				// saving in .g format
				try
				{
					writeFile(path, doc);
				}
				catch (IOException e)
				{
					JOptionPane.showMessageDialog(null, "File could not be opened for writing.");
					return;
				}					
			}
		}
	}

	public void init(WorkCraftServer server) {
	}

	public boolean isModelSupported(UUID modelUuid) {
		return false;
	}

	public void deinit(WorkCraftServer server) {
		// TODO Auto-generated method stub
		
	}

	public ToolType getToolType() {
		return ToolType.EXPORT;
	}
}
