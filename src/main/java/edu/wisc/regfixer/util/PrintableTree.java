package edu.wisc.regfixer.util;

import java.util.LinkedList;
import java.util.List;

public abstract class PrintableTree {
	public abstract List<PrintableTree> getChildren ();

	public static String toString (PrintableTree tree) {
		List<PrintableTree> children = tree.getChildren();
		int total = children.size();
		String out = PrintableTree.brace(tree.toString(), total == 0);
		for (int i = 0; i < total; i++) {
			String childString = PrintableTree.toString(children.get(i));
			if (i < total-1) {
				out += "\n" + PrintableTree.indentAndAttach(childString, Character.toString(BoxChars.tee_e), Character.toString(BoxChars.vert));
			} else {
				out += "\n" + PrintableTree.indentAndAttach(childString, Character.toString(BoxChars.corner_sw), " ");
			}
		}
		return out;
	}

	private static String brace (String source, boolean close) {
		List<String> outlines = new LinkedList<>();
		outlines.add(String.format("%c%c", BoxChars.corner_nw, BoxChars.horiz));

		String[] lines = source.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (i == 0) {
				outlines.add(String.format("%c %s", BoxChars.tee_w, line));
			} else {
				outlines.add(String.format("%c %s", BoxChars.vert, line));
			}
		}

		if (close) {
			outlines.add(String.format("%c%c", BoxChars.corner_sw, BoxChars.horiz));
		}

		return String.join("\n", outlines);
	}

	private static String indentAndAttach (String source, String connector, String rest) {
    String[] lines = source.split("\n");
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (i == 0) {
        lines[i] = String.format("%c %s", BoxChars.vert, line);
      } else if (i == 1) {
        lines[i] = String.format("%s%c%s", connector, BoxChars.horiz, line);
      } else {
        lines[i] = String.format("%s %s", rest, line);
      }
    }
    return String.join("\n", lines);
  }
}
