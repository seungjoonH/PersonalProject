package com.todo.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Menu {
	private static String[][] fieldData = {
		{ "isCompleted", "comp" },
		{ "essential", "ess" },
		{ "category", "cate" },
		{ "title", "ttl" },
		{ "description", "desc" },
		{ "dueDate", "due" },
		{ "state", "stt" },
		{ "currentDate", "cur" }
	};
	
	private static String[][] cmdData = {
		{ "add", "항목 추가", "[cat] [ttl] [des] [due]" }, 
		{ "del *", "모두 삭제", "" },
		{ "del", "항목 삭제", "[idx] (idx) ...", },
		{ "edit", "항목 수정", "[idx]" }, 
		{ "comp *", "모두 완료/해제", "(val)" }, 
		{ "comp", "항목 완료/해제", "[idx] (idx) ..." }, 
		{ "ess *", "전체 필수항목 지정/해제", "(val)" },
		{ "ess", "필수항목 지정/해제", "[idx] (idx) ..." },
		{ "find", "항목 검색",	 "[fld] [key]" }, 
		{ "list *", "전체 목록 출력", "" }, 
		{ "list", "목록 출력",	 "[fld]" }, 
		{ "sort", "목록 정렬",	 "[fld] (odr)" }, 
		{ "stt", "특정 상태 목록 출력", "(stt)" },
		{ "json", "json 파일 입출력", "[io]" },
		{ "json imp", "json 파일 import", "[ovw]" },
		{ "help *",	"전체 도움말", "" }, 
		{ "help", "도움말", "(cmd)" }, 
		{ "exit", "프로그램 종료", "" }
	};
	
	private static String[][] props = {
		{ "", "\n표시할 내용이 없습니다", "", "" },
		{ "[cat]", "category", "String", "항목의 카테고리" },
		{ "[ttl]", "title", "String", "항목의 제목" },
		{ "[des]", "desciption", "String", "항목의 내용" },
		{ "[due]", "due date", "String", "항목의 마감일" },
		{ "(val)", "value", "String", "완료 여부 적용" },
		{ "[idx]", "index", "Integer", "항목의 색인" },
		{ "[fld]", "field", "String", "필드명" },
		{ "[key]", "key", "String",	"검색어" },
		{ "(odr)", "order", "String", "정렬 차순 [asc, desc]" },
		{ "(stt)", "state", "String", "상태값 [expired, D-day]" },
		{ "[io]", "load/save", "String", "불러오기/저장하기 [load, save]" },
		{ "[ovw]", "overwrite", "Boolean", "덮어쓰기/이어쓰기 [true, false]" },
		{ "(cmd)", "command", "String",	"명령어 [" + String.join(", ", getCmdColumnList(cmdData, 0)) + "]" },
	};
	
	public static String abbrToOrigin(String field) {
		for (int i = 0; i < fieldData.length; i++) {
			if (fieldData[i][0].equals(field) || fieldData[i][1].equals(field))
			return fieldData[i][0];
		}
		return field;
	}
	
	public static boolean isFieldExist(String field) {
		for (int i = 0; i < fieldData.length; i++) 
			if (field.equals(fieldData[i][0])) return true;	
		return false;
	}
	
    public static void helpMenu(ArrayList<String> cmds) {
    	Scanner sc = new Scanner(System.in);
    	String cmd = "";
	    try { cmd = cmds.get(1); }
		catch (IndexOutOfBoundsException e) {}
	    
	    if (cmd.equals("")) { displayMenu(); return; }
	    if (cmd.equals("*") || cmd.equals("all")) { helpAllMenu(); return; }
	    
	    while (true) { 
			if (indexOf(getColumnList(cmdData, 0), cmd) != -1) break;
			System.out.println("\n해당 명령어가 없습니다\n");
			prompt("명령어"); 
			cmd = sc.next();
		}
	    
	    helpMenu(cmd);
    }
    
    public static void displayMenu() {
    	int width = 30;
        System.out.println("\n================================ 메뉴 ================================");
        for (int i = 0; i < cmdData.length; i++) {
        	System.out.println(String.format(
    			"%2d. %-8s %-" + width + "s : %s", i + 1, cmdData[i][0], cmdData[i][2], cmdData[i][1])
    		);
		}
    }
    
    private static String[] getCmdColumnList(String[][] list, int col) {
    	String[] columnList = new String[list.length];
    	int i, j;
    	for (i = 0, j = 0; i < list.length; i++) {
    		if (!list[i][col].contains("*")) columnList[j++] = list[i][col];
    	}
    	return Arrays.copyOf(columnList, j);
    }
    private static String[] getColumnList(String[][] list, int col) {
    	String[] columnList = new String[list.length];
    	for (int i = 0; i < list.length; i++) columnList[i] = list[i][col];
    	return columnList;
    }
    private static int indexOf(String[] list, String str) {
    	for (int i = 0; i < list.length; i++) if (list[i].equals(str)) return i;
    	return -1;
    }
    private static void helpMenu(String cmd) {
    	int idx = indexOf(getColumnList(cmdData, 0), cmd);
    	String[] propList = cmdData[idx][2].split(" ");
    	
    	System.out.println(String.format("\n============ [%s] 명령어 도움말 ================", cmd));
    	for (int i = 0; i < propList.length; i++) {
    		int tmpIdx = indexOf(getColumnList(props, 0), propList[i]);
    		if (tmpIdx < 0) continue;
    		System.out.println(String.format(
    			"%s  %-14s %-7s %s", propList[i], props[tmpIdx][1], props[tmpIdx][2], props[tmpIdx][3]
    		));
    	}
    	
    }
    private static void helpAllMenu() {
    	for (int i = 0; i < cmdData.length; i++) helpMenu(cmdData[i][0]);
    }
    
    public static void prompt() { prompt("\ncommand"); }
    public static void prompt(String input) {
    	System.out.print(String.format("%s > ", input));
    }
}
