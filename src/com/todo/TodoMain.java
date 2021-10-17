package com.todo;

import java.util.ArrayList;
import java.util.Scanner;

import com.todo.dao.TodoList;
import com.todo.menu.Menu;
import com.todo.service.StringParser;
import com.todo.service.TodoUtil;

public class TodoMain {
	
	public static void start() {
	
		Scanner sc = new Scanner(System.in);
		TodoList l = new TodoList();
		boolean quit = false;
		String keyword;
		
		// Data import completed
//		l.importData("todolist.txt"); 
		
		Menu.displayMenu();
		TodoUtil.updateStates(l);
		
		do {
			ArrayList<String> cmds;
			do {
				Menu.prompt();
				cmds = StringParser.parse(sc.nextLine());
			} while (cmds.size() == 0);
			
			switch (cmds.get(0)) {

			case "add":
				TodoUtil.createItem(l, cmds);
				break;
			
			case "del":
				TodoUtil.deleteItem(l, cmds);
				break;
				
			case "edit":
				TodoUtil.updateItem(l, cmds);
				break;

			case "comp":
				TodoUtil.completeItem(l, cmds);
				break;

			case "ess":
			case "essential":
				TodoUtil.essentializeItem(l, cmds);
				break;
				
			case "find":
				TodoUtil.findItem(l, cmds);
				break;
				
			case "list":
				TodoUtil.listItems(l, cmds);
				break;
				
			case "sort":
				TodoUtil.sortItems(l, cmds);
				break;
			
			case "stt":
			case "state":
				TodoUtil.listState(l, cmds);
				break;
				
			case "json":
				TodoUtil.controlJson(l, cmds, "todolist.json");
				break;
				
			case "help":
				Menu.helpMenu(cmds);
				break;

			case "q":
			case "quit":
			case "exit": 
				quit = true;
				System.out.println("\n프로그램 종료 중 ...");
				break;

			default:
				System.out.println(String.format("\n'%s' 명령어가 존재하지 않습니다 (도움말 - help)", cmds.get(0)));
				break;
			}
			
		} while (!quit);
		
//		TodoUtil.saveList(l, "todolist.txt");
		
		TodoList.closeConnection();
		
	}
}
