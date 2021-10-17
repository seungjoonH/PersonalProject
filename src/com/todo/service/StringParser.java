package com.todo.service;

import java.util.ArrayList;

public class StringParser {
	
	public static ArrayList<String> parse(String str) {
		char[] chrs = str.toCharArray();
		char dq = '\"', sp = ' ';
		int srt = -1, end = -1;
		ArrayList<String> parsedList = new ArrayList<String>();
		boolean isInQuote = false;
		
		// Error handling
		if (countChar(str, dq) % 2 == 1) {
			System.out.println("Syntax Error Occurred [ has odd double quotes ]");
			return null;
		}
		
		for (int i = 0; i < chrs.length; i++) {
			if (chrs[i] == dq) isInQuote = !isInQuote;
			if (chrs[i] == sp && !isInQuote) {
				end = i - 1;
				parsedList.add(new String(chrs, srt + 1, end - srt));
				srt = i;
			}
		}
		parsedList.add(new String(chrs, srt + 1, chrs.length - srt - 1));
		
		for (int i = 0; i < parsedList.size(); i++) {
			if (parsedList.get(i).trim().equals("")) { parsedList.remove(i); i--; continue; }
			parsedList.set(i, parsedList.get(i).replace("\"", "").trim());
		}
		return parsedList;
	}
	public static int countChar(String str, char ch) {
		return str.length() - str.replace(String.valueOf(ch), "").length();
	}
}
