package main;

import java.util.*;
import java.util.regex.*;
import java.io.*;

public class TISC {
	public static Hashtable<String, Integer> HT = new Hashtable<String, Integer>();
	public static Hashtable<String, Integer> JT = new Hashtable<String, Integer>();
	public static String[] prog;
	public static int pointer;
	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("usage: tisc input_file");
			System.exit(0);
		}
		File file = new File(args[0]);
		try {
			pointer = 0;
			Scanner read = new Scanner(file);
			read.useDelimiter("\\Z");
			prog = read.next().split("\n");
			read.close();
		} catch (FileNotFoundException e) {
			System.out.println("tisc: can't open file '" + args[0] + "': No such file");
			System.exit(0);
		}
		for(int i = 0; i < prog.length; i++) {
			if(prog[i].contains(":")) {
				if(prog[i].split(":")[0].contains("\\s")) {
					System.out.println("tisc: invalid label at line " + pointer + ": label name can not contain white space");
				}
				String[] splits = prog[i].replaceAll("\\s","").split(":");
				if(splits.length != 1) {
					System.out.println("tisc: invalid label at line " + pointer + ": label can not have instruction following label declaration");
					System.exit(0);
				}
				if(splits[0].length() > 16) {
					System.out.println("tisc: invalid label at line " + pointer + ": label name can not be longer than 16 characters");
					System.exit(0);
				}
				if(splits[0].matches("[A-Z]+")) {
					if(JT.containsKey(splits[0])) {
						System.out.println("tisc: invalid label at line " + pointer + ": label is already used");
						System.exit(0);
					}
					else {
						JT.put(splits[0], i);
					}
				}
				else {
					System.out.println("tisc: invalid label at line " + pointer + ": label name can only contain letters");
				}
			}
		}
		HT.put("ACC", 0);
		HT.put("BAK", 0);
		while(true) {
			try {
				String opcode = prog[pointer].split("\\s")[0];
				String[] arguments = {};
				if(opcode.length() != prog[pointer].trim().length())
					arguments = prog[pointer].substring(opcode.length() + 1, prog[pointer].length()).trim().split("\\s+");
				parse(opcode, arguments);
			}
			catch(ArrayIndexOutOfBoundsException e) {
				System.out.println("tisc: improper program halt at line " + pointer + ": program requires a halt instruction");
				System.exit(0);
			}
		}
		
	}
	public static void parse(String line, String[] args) {
		if(line.length() == 0 || line.charAt(0) == '#' ) {
			pointer++;
			return;
		}
		if(line.contains(":")) {
			pointer++;
			return;
		}
		switch(line) {
		case "SAV":
			if(args.length != 0) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 0 arguments");
				System.exit(0);
			}
			int acc = HT.get("ACC");
			HT.put("BAK", acc);
			pointer++;
			break;
		case "NEG":
			if(args.length != 1) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 0 arguments");
				System.exit(0);
			}
			acc = HT.get("ACC");
			HT.put("ACC", acc * -1);
			pointer++;
			break;
		case "SWP":
			if(args.length != 0) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 0 arguments");
				System.exit(0);
			}
			int a = HT.get("ACC");
			HT.put("ACC", HT.get("BAK"));
			HT.put("BAK", a);
			pointer++;
			break;
		case "JRO":
			if(args.length != 1) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 1 argument");
				System.exit(0);
			}
			if(args[0].matches("-?(0|[1-9]\\d*)")) {
				pointer += Integer.parseInt(args[0]);
			}
			else if(validOpcode(args[0])) {
				pointer += HT.get(args[0]);
			}
			else {
				System.out.println("tisc: invalid register at line " + pointer + ": '" + args[0] + "' not a valid register");
				System.exit(0);
			}
			break;
		case "JMP":
			if(args.length != 1) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 1 argument");
				System.exit(0);
			}
			if(JT.containsKey(args[0])) {
				pointer = JT.get(args[0]);
			}
			else {
				System.out.println("tisc: invalid label at line " + pointer + ": label does not exist");
				System.exit(0);
			}
			break;
		case "JEZ":
			if(args.length != 1) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 1 argument");
				System.exit(0);
			}
			if(JT.containsKey(args[0])) {
				if(HT.get("ACC") == 0)
					pointer = JT.get(args[0]);
				else
					pointer++;
			}
			else {
				System.out.println("tisc: invalid label at line " + pointer + ": label does not exist");
				System.exit(0);
			}
			break;
		case "JNZ":
			if(args.length != 1) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 1 argument");
				System.exit(0);
			}
			if(JT.containsKey(args[0])) {
				if(HT.get("ACC") != 0)
					pointer = JT.get(args[0]);
				else
					pointer++;
			}
			else {
				System.out.println("tisc: invalid label at line " + pointer + ": label does not exist");
				System.exit(0);
			}
			break;
		case "JGZ":
			if(args.length != 1) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 1 argument");
				System.exit(0);
			}
			if(JT.containsKey(args[0])) {
				if(HT.get("ACC") > 0)
					pointer = JT.get(args[0]);
				else
					pointer++;
			}
			else {
				System.out.println("tisc: invalid label at line " + pointer + ": label does not exist");
				System.exit(0);
			}
			break;
		case "JLZ":
			if(args.length != 1) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 1 argument");
				System.exit(0);
			}
			if(JT.containsKey(args[0])) {
				if(HT.get("ACC") < 0)
					pointer = JT.get(args[0]);
				else
					pointer++;
			}
			else {
				System.out.println("tisc: invalid label at line " + pointer + ": label does not exist");
				System.exit(0);
			}
			break;
		case "MOV":
			if(args.length != 2) {
				System.out.println("tisc: invalid argument count at line " + pointer  + ": instruction takes 2 arguments");
				System.exit(0);
			}
			int movable;
			if(args[0].matches("-?(0|[1-9]\\d*)")) {
				movable = Integer.parseInt(args[0]);
			}
			else {
				if(!validOpcode(args[0])) {
					System.out.println("tisc: invalid register at line " + pointer + ": '" + args[0] + "' not a valid register");
					System.exit(0);
				}
				movable = HT.get(args[0]);
			}
			if(args[1].equals("OPC")) {
				System.out.print((char) movable);
			}
			else if(args[1].equals("OPI")) {
				System.out.print((int)movable);
			}
			else if(validOpcode(args[1])) {
				HT.put(args[1], movable);
			}
			else {
				System.out.println("tisc: invalid register at line " + pointer + ": '" + args[1] + "' not a valid register");
				System.exit(0);
			}
			pointer++;
			break;
		case "ADD":
			if(args.length != 1) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 1 argument");
				System.exit(0);
			}
			if(args[0].matches("-?(0|[1-9]\\d*)")) {
				int adding = HT.get("ACC");
				HT.put("ACC", adding + Integer.parseInt(args[0]));
			}
			else if(validOpcode(args[0])) {
				int adding = HT.get("ACC");
				HT.put("ACC", adding + HT.get(args[0]));
			}
			else {
				System.out.println("tisc: invalid register at line " + pointer + ": '" + args[0] + "' not a valid register");
				System.exit(0);
			}
			pointer++;
			break;
		case "SUB":
			if(args.length != 1) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 1 argument");
				System.exit(0);
			}
			if(args[0].matches("-?(0|[1-9]\\d*)")) {
				int adding = HT.get("ACC");
				HT.put("ACC", adding - Integer.parseInt(args[0]));
			}
			else if(validOpcode(args[0])) {
				int adding = HT.get("ACC");
				HT.put("ACC", adding - HT.get(args[0]));
			}
			else {
				System.out.println("tisc: invalid register at line " + pointer + ": '" + args[0] + "' not a valid register");
				System.exit(0);
			}
			pointer++;
			break;
		case "NOP":
			if(args.length != 0) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 0 arguments");
				System.exit(0);
			}
			pointer++;
			break;
		case "HALT":
			if(args.length != 0) {
				System.out.println("tisc: invalid argument count at line " + pointer + ": instruction takes 0 arguments");
				System.exit(0);
			}
			System.exit(0);
		default:
			System.out.println("tisc: invalid instruction at line " + pointer + ": '" + line + "' not a valid instruction");
			System.exit(0);
		}

	}
	public static boolean validOpcode(String arg) {
		if(arg.equals("ACC") || arg.equals("SAV")) {
			return true;
		}
		if(!arg.contains("REG")) {
			return false;
		}
		Pattern p = Pattern.compile("REG(\\d+)");
		Matcher m = p.matcher(arg);
		m.find();
		try{ 
			String regid = m.group(1);
			if(regid.matches("-?(0|[1-9]\\d*)")) {
				int id = Integer.parseInt(regid);
				if(id < 10)
					return arg.length() == 4;
				else
					return id < 16 && arg.length() == 5;
			}
		} catch(IllegalStateException e) {
			return false;
		}
		return false;
	}
}
