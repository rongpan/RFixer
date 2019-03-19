package edu.wisc.regfixer.parser;

import java.util.List;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.Model;

public class Storage {
    public static int unknownCharCounter = -1;
    public static int unknownBoundCounter = -1;
    public static Context ctx;
    public static BoolExpr[][] charPreds;
    //public static BoolExpr[][] maxCharPreds;
    public static List<IntExpr> costArray;
    public static IntExpr[] boundPreds;
    public static char[] curExample;
    public static char[] allChars;
    //public static int nameIndex = 0;
    public static Model model;
    
    public Storage(){
    	
    }
    
    public static void reset() {
    	unknownCharCounter = -1;
    	unknownBoundCounter = -1;
    	//nameIndex = 0;
    }
}
