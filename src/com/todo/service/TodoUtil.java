package com.todo.service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.todo.dao.TodoItem;
import com.todo.dao.TodoList;
import com.todo.menu.Menu;

public class TodoUtil {
	
	public static void updateStates(TodoList l) { 
		for (TodoItem item : l.getList()) l.updateStates(item); 
	}
	
	private static ArrayList<String> fieldInput(ArrayList<String> cmds) { return fieldInput(1, cmds, null); }
	private static ArrayList<String> fieldInput(int srtIdx, ArrayList<String> cmds, TodoItem item) {
		Scanner sc = new Scanner(System.in);
		ArrayList<String> inputList = new ArrayList<String>();
		String[] caption = {
			// can be variable
			"카테고리(category)", "제목(title)", "내용(description)", "마감일자(due date)"
		};
		
		for (int i = 0; i < caption.length; i++) {
			try { inputList.add(cmds.get(i + srtIdx)); }
			catch (IndexOutOfBoundsException e) { 
				while (true) { 
					Menu.prompt(caption[i]);
					String tempStr = sc.nextLine().trim(); 
					boolean parseable = true;

					if (tempStr.toLowerCase().equals("quit") || tempStr.toLowerCase().equals("q")) { System.out.println("\n취소하였습니다"); return null; }
					if (i == 3) {
						if (StringParser.countChar(tempStr, '/') == 2) { 
							String[] tempList = tempStr.split("/");
							if (tempList[0].length() == 4 && tempList[1].length() == 2 && tempList[2].length() == 2) {
								try { Integer.parseInt(tempList[0]); Integer.parseInt(tempList[1]); Integer.parseInt(tempList[2]); }
								catch (NumberFormatException er) { parseable = false; }
							}
							else parseable = false;
						}
						else parseable = false;
					}
					if (!parseable) { System.out.println("\n마감일자의 형식이 올바르지 않습니다"); return null; }
					if (tempStr.equals("=") && item != null) { inputList.add(item.getField(i)); break; }
					if (!tempStr.equals("")) { inputList.add(tempStr); break; }
				}
			}
		}
		
		return inputList;
	}
	
	public static void createItem(TodoList l, ArrayList<String> cmds) {
		
		String category, title, description, dueDate;
		Scanner sc = new Scanner(System.in);
		
		System.out.println("\n=== 항목 추가 ===");
		
		ArrayList<String> inputList = fieldInput(cmds);
		if (inputList == null) return;
		
	    category = inputList.get(0);
	    title = inputList.get(1); 
	    description = inputList.get(2); 
	    dueDate = inputList.get(3);
		
		TodoItem t = new TodoItem(category, title, description, dueDate);
		if (l.addItem(t) > 0) System.out.println(String.format("\n'%s' 항목이 추가되었습니다", title));
		
		updateStates(l);
	}

	public static void deleteItem(TodoList l, ArrayList<String> cmds) {
		
		Scanner sc = new Scanner(System.in);

		int index, size = l.getList().size();
		String indexInput = "", notExistIndices = "'";
		String[] delIndices;
		
		System.out.println("\n=== 항목 삭제 ===");

		if (size == 0) {
			System.out.println("\n삭제할 항목이 없습니다");
			return;
		}
	
		try {
			if (cmds.get(1).equals("*")) {
				for (TodoItem item : l.getList())
					indexInput += item.getId() + " ";
				indexInput = indexInput.trim();
			}
			else {
				for (int i = 1; i < cmds.size(); i++) {
					indexInput += cmds.get(i) + " ";
				}
				indexInput = indexInput.trim();
			}
		}
		catch (IndexOutOfBoundsException e) {
			while (true) { 
				Menu.prompt("삭제할 번호(index)");
				indexInput = sc.nextLine(); 
				if (indexInput.toLowerCase().equals("quit") || indexInput.toLowerCase().equals("q")) {
					System.out.println("\n취소하였습니다"); return;
				}
				
				if (indexInput.equals("*")) {
					for (TodoItem item : l.getList())
						indexInput += item.getId() + " ";
					indexInput = indexInput.trim();
				}
				if (!indexInput.equals("")) break;
			}
		}
		
		int[] maxWidth = {8, 5, 11};
		delIndices = indexInput.split(" ");
		maxWidth = getMaxWidth(l, maxWidth);
		
		try {
			boolean isAllExist = true;
			
			for (int i = 0; i < delIndices.length; i++) {
				if (!l.isExist(Integer.parseInt(delIndices[i]))) isAllExist = false;
			}
			
			for (int i = 0; i < delIndices.length; i++) {
				if (isAllExist) {
					TodoItem item = l.getItem(Integer.parseInt(delIndices[i]));
					listLine(item, maxWidth);
				}
				else if (!l.isExist(Integer.parseInt(delIndices[i]))) 
					notExistIndices += (notExistIndices.equals("'") ? "" : ", '") + delIndices[i] + "'";
			}
		}
		catch (NumberFormatException er) { System.out.println("\n숫자를 입력해주세요"); return; }
		
		if (!notExistIndices.equals("'")) {
			System.out.println(String.format("%s 항목이 없습니다", notExistIndices));
			return;
		}
	
		String choice, idxStr = "'";
		
		do {
			System.out.print("\n위 항목들을 삭제하시겠습니까? (y/n) > ");
			
			choice = sc.next().toLowerCase();
			
			switch (choice) {
			case "y":
				int listSize = l.getList().size();
				for (int i = 0; i < delIndices.length; i++) {
					if (l.deleteItem(Integer.parseInt(delIndices[i])) == 0) return;
					idxStr += (i == 0 ? "" : ", '") + delIndices[i] + "'";
				}
				if (delIndices.length == listSize) idxStr = "모든";
				System.out.println(String.format("\n%s 항목이 삭제되었습니다", idxStr));
				break;
			
			case "n": 
				System.out.println("\n취소하였습니다");
				break;
			
			default:
				System.out.println("잘못 입력했습니다");
				break;
			}
		} while (!choice.equals("y") && !choice.equals("n"));
	}

	public static void updateItem(TodoList l, ArrayList<String> cmds) {
		
		String new_title, new_description, new_category, new_dueDate;
		Scanner sc = new Scanner(System.in);

		int index = -1, size = l.getCount();
		
		System.out.println("\n=== 항목 수정 ===");
		
		if (size == 0) {
			System.out.println("\n수정할 항목이 없습니다");
			return;
		}

	    try { 
	    	index = Integer.parseInt(cmds.get(1)); 
			if (!l.isExist(index)) { System.out.println(String.format("\n'%d' 항목이 없습니다", index)); return; }
	    }
		catch (IndexOutOfBoundsException e) {  
			Menu.prompt("수정할 번호(index)"); 
			try { 
				String tempStr = sc.next(); 
				if (tempStr.toLowerCase().equals("quit") || tempStr.toLowerCase().equals("q")) {
					System.out.println("\n취소하였습니다"); return;
				}
				index = Integer.parseInt(tempStr.trim()); 
				if (!l.isExist(index)) { System.out.println(String.format("\n'%d' 항목이 없습니다", index)); return; }
			}
			catch (NumberFormatException er) { System.out.println("\n숫자를 입력하세요"); return; }
		}
		catch (NumberFormatException e) { System.out.println("\n숫자를 입력하세요"); return; }

		TodoItem item = l.getItem(index);
		
		int[] maxWidth = {8, 5, 11};
		maxWidth = getMaxWidth(l, maxWidth);
		listLine(item, maxWidth);
		System.out.println();
		
		ArrayList<String> inputList = fieldInput(2, cmds, item);
		if (inputList == null) return;
		
	    new_category = inputList.get(0);
	    new_title = inputList.get(1); 
	    new_description = inputList.get(2); 
	    new_dueDate = inputList.get(3);
	    
	    System.out.println(new_category + ", " + new_title + ", " + new_description + ", " + new_dueDate);
		
		TodoItem t = new TodoItem(new_category, new_title, new_description, new_dueDate);
		t.setId(index);
		if (l.editItem(t) > 0) System.out.println(String.format("\n'%d'번 항목이 수정되었습니다", index));

		updateStates(l);
	}
	
	public static void findItem(TodoList l, ArrayList<String> cmds) {
		Scanner sc = new Scanner(System.in);
		String field, keyword;
		
		try { 
			field = cmds.get(1); 
	    	if (!Menu.isFieldExist(Menu.abbrToOrigin(field))) {
	    		System.out.println(String.format("\n'%s' 필드가 없습니다\n", field)); return;
	    	}
		}
	    catch (IndexOutOfBoundsException e) { 
	    	Menu.prompt("필드(field)"); field = sc.next(); 
	    	if (field.equals("q")) { System.out.println("\n취소하였습니다"); return; }
	    	if (!Menu.isFieldExist(Menu.abbrToOrigin(field))) {
	    		System.out.println(String.format("\n'%s' 필드가 없습니다\n", field)); return;
	    	}
	    }
    	try { keyword = cmds.get(2); }
	    catch (IndexOutOfBoundsException e) { Menu.prompt("검색어(keyword)"); keyword = sc.next(); }
		
    	field = Menu.abbrToOrigin(field);
		System.out.println(String.format("\n=== '%s' 에서 '%s' 검색 ===", field.equals("*") ? "모든 필드" : field, keyword));
		
		int count = 0;
		int[] maxWidth = {8, 5, 11};
		maxWidth = getMaxWidth(l, maxWidth);
		
		for (TodoItem item : l.getList(field, keyword)) { listLine(item, maxWidth); count++; }
		
		if (count == 0) System.out.println(String.format("\n항목을 찾지 못했습니다", count));
		else System.out.println(String.format("\n총 %d개의 항목을 찾았습니다", count));
	}
	
	private static int countKor(String str) {
		int cnt = 0;
		for (int i = 0; i < str.length(); i++) {
			if (Character.toString(str.charAt(i)).matches("^[가-힣]*$")) cnt++;
		}
		return cnt;
	}
	
	private static int[] getMaxWidthJson(TodoList l, int[] maxWidth) {
		int c_kor, t_kor, d_kor;
		
		for (TodoItem item : l.getListJson()) {
			c_kor = countKor(item.getCategory()); 
			t_kor = countKor(item.getTitle()); 
			d_kor = countKor(item.getDescription());
			
			maxWidth[0] = Math.max(item.getCategory().length() + c_kor, maxWidth[0]);
			maxWidth[1] = Math.max(item.getTitle().length() + t_kor, maxWidth[1]);
			maxWidth[2] = Math.max(item.getDescription().length() + d_kor, maxWidth[2]);
		}
		
		return maxWidth;
	}
	
	private static int[] getMaxWidth(TodoList l, int[] maxWidth) {
		int c_kor, t_kor, d_kor;
		
		for (TodoItem item : l.getList()) {
			c_kor = countKor(item.getCategory()); 
			t_kor = countKor(item.getTitle()); 
			d_kor = countKor(item.getDescription());
			
			maxWidth[0] = Math.max(item.getCategory().length() + c_kor, maxWidth[0]);
			maxWidth[1] = Math.max(item.getTitle().length() + t_kor, maxWidth[1]);
			maxWidth[2] = Math.max(item.getDescription().length() + d_kor, maxWidth[2]);
		}
		
		return maxWidth;
	}
	
	public static void listLine(TodoItem item, int[] maxWidth) {
		int[] width = new int[3]; 
		width[0] = maxWidth[0] - countKor(item.getCategory());
		width[1] = maxWidth[1] - countKor(item.getTitle());
		width[2] = maxWidth[2] - countKor(item.getDescription());
		
		System.out.println(item.toString(width));
	}
	
	public static void listAllJson(TodoList l) { listAll(l, "", -2); }
	public static void listAll(TodoList l) { listAll(l, "", -1); }
	public static void listAll(TodoList l, String orderby, int ordering) {
		int contents = ordering == -2 ? l.getCountJson() : l.getCount();
		
		if (contents == 0) {
			System.out.println("\n표시할 항목이 없습니다");
			return;
		}
		
		int total_width = 0;
		int[] maxWidth = {8, 5, 11};
		
		maxWidth = ordering == -2 ? getMaxWidthJson(l, maxWidth) : getMaxWidth(l, maxWidth);
		
		total_width = maxWidth[0] + maxWidth[1] + maxWidth[2] + 69;
		String ls = String.format(" 전체 목록 (총 %-2d개) ", contents);
		int ls_len = ls.length() + countKor(ls);
		
		// counting "="
		for (int i = 0; i < (total_width - ls_len) / 2; i++) System.out.print("=");
		System.out.print(ls);
		for (int i = 0; i < (total_width - ls_len) / 2; i++) System.out.print("=");
		System.out.println((total_width - ls_len) % 2 == 0 ? "" : "=");
		
		// formatting Header
		System.out.println(String.format(
			" %2s | %-3s | %-3s | %-" + maxWidth[0] + "s | %-" + maxWidth[1] + "s | %-" + maxWidth[2] + "s | %-10s | %-7s | %s", 
			"No", "Dne", "Esl", "Category", "Title", "Description", "Due date", "State", "Written time"
		));
		
		
		if (orderby.equals("") && ordering < 0) 
			for (TodoItem item : ordering == -2 ? l.getListJson() : l.getList()) listLine(item, maxWidth);
		else if (ordering == 0 || ordering == 1) 
			for (TodoItem item : l.getOrderedList(orderby, ordering)) listLine(item, maxWidth);
	}
	
	public static void listToggleable(TodoList l, String field) {
		Scanner sc = new Scanner(System.in);

		if (field.equals("isCompleted")) System.out.println(String.format("\n=== 완료된 항목 검색 ==="));
		else if (field.equals("essential")) System.out.println(String.format("\n=== 필수 항목 검색 ==="));
		
		int count = 0;
		int[] maxWidth = {8, 5, 11};
		maxWidth = getMaxWidth(l, maxWidth);
		
		for (TodoItem item : l.getToggleableList(field)) { listLine(item, maxWidth); count++; }
		
		
		if (count == 0) System.out.println(String.format("\n항목을 찾지 못했습니다", count));
		else System.out.println(String.format("\n총 %d개의 항목을 찾았습니다", count));
	
	}
	
	public static void completeItem(TodoList l, ArrayList<String> cmds) { checkItem(l, cmds, "isCompleted"); }
	public static void essentializeItem(TodoList l, ArrayList<String> cmds) { checkItem(l, cmds, "essential"); }
	public static void checkItem(TodoList l, ArrayList<String> cmds, String field) {
		Scanner sc = new Scanner(System.in);

		int size = l.getList().size();
		String[] inputIndices = new String[size];
		String indexInput = "", notExistIndices = "\n'";
		int cnt = 0, notExistCnt = 0;
		int value = -1;

		if (size == 0) {
			if (field.equals("isCompleted")) System.out.println("완료할 항목이 없습니다");
			else if (field.equals("essential")) System.out.println("체크할 항목이 없습니다");
			return;
		}

		try {
			if (cmds.get(1).equals("*") || cmds.get(1).equals("all")) {
				indexInput = "all";
				value = (cmds.get(2).equals("0") || cmds.get(2).toLowerCase().equals("false")) ? 0 : 1;
			}
			else {
				for (int i = 1; i < cmds.size(); i++) indexInput += cmds.get(i) + " ";
				indexInput = indexInput.trim();
			}
		}
	    catch (IndexOutOfBoundsException e) { 
	    	try { if (cmds.get(1).equals("*")); }
	    	catch (IndexOutOfBoundsException er) {
				while (true) { 
					if (field.equals("isCompleted")) Menu.prompt("완료/해제할 번호(index)");
					else if (field.equals("essential")) Menu.prompt("체크/해제할 번호(index)");
					indexInput = sc.nextLine(); 
					if (indexInput.equals("q")) { System.out.println("\n취소하였습니다"); return; }
					if (!indexInput.equals("")) break;
				}
	    	}
	    }

		int isChecked = -1;
		String compStr = "\n'";
		
		if (indexInput.equals("all")) {
			for (TodoItem item : l.getList()) {
				if (field.equals("isCompleted")) 
					l.toggleItem(item.getId(), value < 0 ? 1 - item.getIsCompleted() : value, field);
				else if (field.equals("essential"))
					l.toggleItem(item.getId(), value < 0 ? 1 - item.getEssential() : value, field);
			}
		}
		else if (!indexInput.equals("")) {
		    if (StringParser.countChar(indexInput, ' ') > 0) inputIndices = indexInput.split(" ");
		    else inputIndices[0] = indexInput;
		    
			for (String idx : inputIndices) { 
				if (idx == null) break;
				if (!l.isExist(Integer.parseInt(idx))) 
					notExistIndices += (notExistCnt++ > 0 ? ", '" : "") + idx + "'";
			}
			
			if (notExistCnt > 0) {
				System.out.println(String.format("%s 항목이 없습니다", notExistIndices));
				return;
			}
			
			for (String idx : inputIndices) {
				if (idx == null) break;
				if (field.equals("isCompleted")) isChecked = l.getList().get(idToIndex(l, Integer.parseInt(idx))).getIsCompleted();
				else if (field.equals("essential")) isChecked = l.getList().get(idToIndex(l, Integer.parseInt(idx))).getEssential();
				
				l.toggleItem(Integer.parseInt(idx), 1 - isChecked, field);
				compStr += (cnt++ > 0 ? ", '" : "") + idx + "'";
			}
	    }
		
		if (compStr.equals("\n'")) compStr = "\n모든";
		System.out.println(compStr + " 항목이 변경되었습니다");
	}

	private static int idToIndex(TodoList l, int id) {
		int cnt = 0;
		for (TodoItem item : l.getList()) {
			if (item.getId() == id) return cnt;
			cnt++;
		}
		return -1;
	}
	
	public static void listItems(TodoList l, ArrayList<String> cmds) {
		Scanner sc = new Scanner(System.in);
		String field;
		boolean nextInput = false, isDup;
		int count = 0, subCnt = 1;
		
	    try { 
	    	field = cmds.get(1); 
	    	if (!Menu.isFieldExist(Menu.abbrToOrigin(field)) && !field.equals("*") && !field.equals("all")) {
	    		System.out.println(String.format("\n'%s' 필드가 없습니다", field)); return;
	    	}
    	}
	    catch (IndexOutOfBoundsException e) { 
	    	Menu.prompt("필드(field)"); field = sc.next(); nextInput = true; 
	    	if (field.toLowerCase().equals("quit") || field.toLowerCase().equals("q")) {
	    		System.out.println("\n취소하였습니다"); return;
	    	}
	    	if (!Menu.isFieldExist(Menu.abbrToOrigin(field)) && !field.equals("*") && !field.equals("all")) {
	    		System.out.println(String.format("\n'%s' 필드가 없습니다", field)); return;
	    	}
	    }
    	
    	while (true) {
	    	if (Menu.isFieldExist(Menu.abbrToOrigin(field)) || field.equals("*")) break;
	    	System.out.println(String.format("\n'%s' 필드가 없습니다\n", field));
    		Menu.prompt("필드(field)"); field = sc.next();
    	}
    	
    	if (field.equals("*") || field.equals("all")) { TodoUtil.listAll(l); return; }
    	
    	field = Menu.abbrToOrigin(field);
    	if (field.equals("isCompleted") || field.equals("essential")) { TodoUtil.listToggleable(l, field); return; }
    	
    	
		ArrayList<String> fields = l.getFields(field);
		Collections.sort(fields);

		System.out.println(String.format("\n==== %s 목록 ====", field));
		
		for (String item : fields) System.out.println(item);
		
	}
	
//	public static void listCateAll(TodoList l) {
//		
//		System.out.println("\n=== 카테고리 목록 ===");
//
//		int count = 0;
//		
//		for (String item : l.getCategories()) { 
//			if (count != 0) System.out.print(" / ");
//			System.out.print(item);
//			count++;
//		}
//
//		if (count == 0) { System.out.println("\n등록된 카테고리가 없습니다"); return; }
//		System.out.println(String.format("\n\n총 %d개의 카테고리가 등록되어 있습니다", count));
//		
//	}
	
	public static void sortItems(TodoList l, ArrayList<String> cmds) {

		Scanner sc = new Scanner(System.in);
		String field, orderStr;
		int order = -1;
		boolean nextInput = false;
		
	    try { 
	    	field = cmds.get(1); 
	    	if (!Menu.isFieldExist(Menu.abbrToOrigin(field))) {
	    		System.out.println(String.format("\n'%s' 필드가 없습니다", field)); return;
	    	}
    	}
	    catch (IndexOutOfBoundsException e) { 
	    	Menu.prompt("필드(field)"); field = sc.next(); nextInput = true; 
	    	if (field.equals("q")) { System.out.println("\n취소하였습니다"); return; }
	    	if (!Menu.isFieldExist(Menu.abbrToOrigin(field))) {
	    		System.out.println(String.format("\n'%s' 필드가 없습니다", field)); return;
	    	}
	    }
    	try { 
    		if (cmds.get(2).equals("a") || cmds.get(2).equals("asc") || cmds.get(2).equals("ascending")) order = 1;
    		else if (cmds.get(2).equals("d") || cmds.get(2).equals("desc") || cmds.get(2).equals("descending")) order = 0;
    	}
	    catch (IndexOutOfBoundsException e) { 
	    	if (nextInput) {
	    		Menu.prompt("차순(order)"); 
	    		orderStr = sc.next(); 
	    		if (orderStr.equals("a") || orderStr.equals("asc") || orderStr.equals("ascending")) order = 1;
	    		else if (orderStr.equals("d") || orderStr.equals("desc") || orderStr.equals("descending")) order = 0;
	    		if (order < 0) {
		    		System.out.println("올바른 값을 입력하세요"); return;
	    		}
	    	}
	    	else order = 1;
	    }

    	field = Menu.abbrToOrigin(field);
		TodoUtil.listAll(l, field, order);
	}
	
	public static void listState(TodoList l, ArrayList<String> cmds) {
		String state = "";

		int[] maxWidth = {8, 5, 11};
		maxWidth = getMaxWidth(l, maxWidth);
		
		try {
			if (cmds.get(1).equals("expired") || cmds.get(1).equals("e")) state = "expired";
			else if (cmds.get(1).equals("D-day") || cmds.get(1).equals("d")) state = "D-day";
		}
		catch (IndexOutOfBoundsException e) { state = "expired, D-day"; }
		
		System.out.println(String.format("==== %s 상태 목록 ====", state));
		
		for (TodoItem item : l.getList()) {
			if (!item.getState().equals("") && state.contains(item.getState())) listLine(item, maxWidth); 
		}
	}
	
	public static void saveList(TodoList l, String filename) {
		try {
			FileWriter fw = new FileWriter(filename);
			
			for (TodoItem i : l.getList()) {
				fw.write(i.toSaveString());
			}
			
			fw.close();
		} 
		catch (IOException e) { e.printStackTrace(); }
	}

	public static void loadList(TodoList l, String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			StringTokenizer st;
			String line;
			
			while ((line = br.readLine()) != null) {
				st = new StringTokenizer(line, "##");
				
				String category = st.nextToken();
				String title = st.nextToken();
				String description = st.nextToken();
				String dueDate = st.nextToken();
				String currentDate = st.nextToken();
				
				TodoItem t = new TodoItem(category, title, description, dueDate);
				t.setCurrentDate(currentDate);
				
				l.addItem(t);
			}
			
			int contents = l.getList().size();
			if (contents == 0) System.out.println("파일에 불러올 항목이 없습니다");
			else System.out.println(String.format("%d 개의 항목을 불러왔습니다", contents));
			br.close();
		} 
		catch (IOException e) { 
			System.out.println("파일이 없습니다");
		}
	}
	
	public static void controlJson(TodoList l, ArrayList<String> cmds, String filename) {
		
		Scanner sc = new Scanner(System.in);
		String io = null;
		
		try {
			io = cmds.get(1);
			if (!io.equals("load") && !io.equals("save") && !io.equals("list")) {
				System.out.println("\n잘못 입력하셨습니다"); return;
			}
		}
		catch (IndexOutOfBoundsException e) {
			Menu.prompt("json 입출력"); io = sc.next();
			if (io.equals("q")) { System.out.println("\n취소하였습니다"); return; } 
			if (!io.equals("load") && !io.equals("save") && !io.equals("list")) {
				System.out.println("\n잘못 입력하셨습니다"); return;
			}
		}
		
		if (io.equals("load")) { 
			l.setListJson(fromJson(filename));
			System.out.println(String.format("'%s'에서 정상적으로 불러왔습니다", filename));
		}
		else if (io.equals("save")) { 
			toJson(l, filename); 
			System.out.println(String.format("'%s'에 정상적으로 저장했습니다", filename));
		}
		else if (io.equals("list")) {
			listAllJson(l);
		}
	}
	
	public static void toJson(TodoList l, String filename) {
		
		ArrayList<TodoItem> list = l.getList();
		Gson gson = new Gson();
		
		String jsonStr = gson.toJson(list);
		
		try {
			FileWriter writer = new FileWriter(filename);
			
			writer.write(jsonStr);
			writer.close();
			
		} 
		catch (IOException e) { e.printStackTrace(); }
	}

	public static ArrayList<TodoItem> fromJson(String filename) {
		String jsonStr = null;
		Gson gson = new Gson();
		
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(filename));
			jsonStr = br.readLine();
			br.close();
		} 
		catch (FileNotFoundException e) { e.printStackTrace(); }
		catch (IOException e) { e.printStackTrace(); }
		
		return gson.fromJson(jsonStr, new TypeToken<ArrayList<TodoItem>>(){}.getType());
	}
}
