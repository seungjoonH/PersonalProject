package com.todo.dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.todo.service.DbConnect;

public class TodoList {
	private ArrayList<TodoItem> list;
	Connection conn;

	public TodoList() {
		this.list = new ArrayList<TodoItem>();
		this.conn = DbConnect.getConnection();
	}
	
	public int updateStates(TodoItem t) {
		t.updateState();
		
		String sql = "update list set state=? where id=?;";
		
		PreparedStatement pstmt;
		int count = 0;
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, t.getState());
			pstmt.setInt(2, t.getId());
			
			count = pstmt.executeUpdate();
			pstmt.close();			
			
		}
		catch (SQLException e) { e.printStackTrace(); }
		
		return count;
	}
	
	public int addItem(TodoItem t) {
		String sql = "insert into list (category, title, description, currentDate, dueDate) values (?, ?, ?, ?, ?);";
		PreparedStatement pstmt;
		int count = 0;
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, t.getTitle());
			pstmt.setString(2, t.getDescription());
			pstmt.setString(3, t.getCategory());
			pstmt.setString(4, t.getCurrentDate());
			pstmt.setString(5, t.getDueDate());
			
			count = pstmt.executeUpdate();
			pstmt.close();
			
		} catch (SQLException e) { e.printStackTrace(); }
		
		return count;
	}

	public int deleteItem(int index) {
		String sql = "delete from list where id=?;";
		PreparedStatement pstmt;
		int count = 0;
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, index);
			
			count = pstmt.executeUpdate();
			pstmt.close();			
		}
		catch (SQLException e) { e.printStackTrace(); }
		
		return count;
	}

	public int editItem(TodoItem t) {
		String sql = "update list set category=?, title=?, description=?, currentDate=?, dueDate=? where id=?;";
		PreparedStatement pstmt;
		int count = 0;
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, t.getCategory());
			pstmt.setString(2, t.getTitle());
			pstmt.setString(3, t.getDescription());
			pstmt.setString(4, t.getCurrentDate());
			pstmt.setString(5, t.getDueDate());
			pstmt.setInt(6, t.getId());
			
			count = pstmt.executeUpdate();
			pstmt.close();			
			
		}
		catch (SQLException e) { e.printStackTrace(); }
		
		return count;
	}

	public int completeItem(int index, int isCompleted) { return toggleItem(index, isCompleted, "isCompleted"); }
	public int essentializeItem(int index, int essential) { return toggleItem(index, essential, "essential"); }
	public int toggleItem(int id, int toSet, String field) {
		PreparedStatement pstmt;
		String sql = String.format("update list set %s=? where id=?;", field);
		
		int count = 0;
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, toSet);
			pstmt.setInt(2, id);
			
			count = pstmt.executeUpdate();
			
			pstmt.close();
			
		}
		catch (SQLException e) { e.printStackTrace(); }
		
		return count;
	}
	
	public TodoItem getItem(int index) {
		TodoItem item = new TodoItem();
		PreparedStatement pstmt;
		
		try {
			String sql = "select * from list where id=?;";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, index);
			ResultSet rs = pstmt.executeQuery();
			
			rs.next();
			int id = rs.getInt("id");
			int isCompleted = rs.getInt("isCompleted");
			int essential = rs.getInt("essential");
			String category = rs.getString("category");
			String title = rs.getString("title");
			String description = rs.getString("description");
			String dueDate = rs.getString("dueDate");
			String state = rs.getString("state");
			String currentDate = rs.getString("currentDate");
			
			item = new TodoItem(id, isCompleted, essential, category, title, description, dueDate, state, currentDate);
		
			pstmt.close();
		}
		catch (SQLException e) { e.printStackTrace(); }
		
		return item;
	}
	
	public ArrayList<String> getFields(String field) {
		ArrayList<String> list = new ArrayList<String>();
		PreparedStatement pstmt;
		
		try {
			String sql = String.format("select distinct %s from list;", field);
			
			pstmt = conn.prepareStatement(sql);
			
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) list.add(rs.getString(field));
			
		}
		catch (SQLException e) { e.printStackTrace(); }
		
		return list;
	}
	
	public void setListJson(ArrayList<TodoItem> list) { this.list = list; }
	public ArrayList<TodoItem> getListJson() { return list; }
	
	public ArrayList<TodoItem> getList() { return getList("*", ""); }
	public ArrayList<TodoItem> getList(String field, String keyword) {
		ArrayList<TodoItem> list = new ArrayList<TodoItem>();
		PreparedStatement pstmt;
		boolean isAll = field.equals("*");
		
		if (!isAll) keyword = "%" + keyword + "%";
		
		try {
			String sql = "select * from list";
			if (!isAll) sql += " where " + field + " like ?;";
			
			pstmt = conn.prepareStatement(sql);
			
			if (!isAll) pstmt.setString(1, keyword);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				int id = rs.getInt("id");
				int isCompleted = rs.getInt("isCompleted");
				int essential = rs.getInt("essential");
				String category = rs.getString("category");
				String title = rs.getString("title");
				String description = rs.getString("description");
				String dueDate = rs.getString("dueDate");
				String state = rs.getString("state");
				String currentDate = rs.getString("currentDate");
				
				list.add(new TodoItem(id, isCompleted, essential, category, title, description, dueDate, state, currentDate));
			}
			
			pstmt.close();
		}
		catch (SQLException e) { e.printStackTrace(); }
		
		return list;
	}
	
	public int getCountJson() { return list.size(); }
	
	public int getCount() {
		Statement stmt;
		int count = 0;
		
		try {
			stmt = conn.createStatement();
			String sql = "select count(id) from list;";
			ResultSet rs = stmt.executeQuery(sql);
			
			rs.next();
			count = rs.getInt("count(id)");
			stmt.close();
			
		} 
		catch (SQLException e) { e.printStackTrace(); }
		
		return count;
	}
	
	public ArrayList<String> getCategories() {
		ArrayList<String> list = new ArrayList<String>();
		Statement stmt;
		
		try {
			stmt = conn.createStatement();
			String sql = "select distinct category from list;";
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) list.add(rs.getString("category"));
		}
		catch (SQLException e) { e.printStackTrace(); }
		
		return list;
	}
	
	public ArrayList<TodoItem> getOrderedList(String orderby, int ordering) {
		ArrayList<TodoItem> list = new ArrayList<TodoItem>();
		Statement stmt;
		
		try {
			stmt = conn.createStatement();
			String sql = "select * from list order by " + orderby + (ordering == 1 ? ";" : " desc;");
			ResultSet rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				int id = rs.getInt("id");
				int isCompleted = rs.getInt("isCompleted");
				int essential = rs.getInt("essential");
				String category = rs.getString("category");
				String title = rs.getString("title");
				String description = rs.getString("description");
				String dueDate = rs.getString("dueDate");
				String state = rs.getString("state");
				String currentDate = rs.getString("currentDate");
				
				list.add(new TodoItem(id, isCompleted, essential, category, title, description, dueDate, state, currentDate));
			}
		}
		catch (SQLException e) { e.printStackTrace(); }

		return list;
	}
	
	public ArrayList<TodoItem> getToggleableList(String field) {
		ArrayList<TodoItem> list = new ArrayList<TodoItem>();
		Statement stmt;
		
		try {
			stmt = conn.createStatement();
			String sql = String.format("select * from list where %s=1;", field);
			ResultSet rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				int id = rs.getInt("id");
				int isCompleted = rs.getInt("isCompleted");
				int essential = rs.getInt("essential");
				String category = rs.getString("category");
				String title = rs.getString("title");
				String description = rs.getString("description");
				String dueDate = rs.getString("dueDate");
				String state = rs.getString("state");
				String currentDate = rs.getString("currentDate");
				
				list.add(new TodoItem(id, isCompleted, essential, category, title, description, dueDate, state, currentDate));
			}
		}
		catch (SQLException e) { e.printStackTrace(); }
		
		return list;
	}
	
	/* Useless for now */
	
//	public void listAll() {
//		System.out.println("\n"
//				+ "inside list_All method\n");
//		for (TodoItem myitem : list) {
//			System.out.println(myitem.getTitle() + myitem.getDescription());
//		}
//	}

//	public void sortByName(int order) {
//		Collections.sort(list, new TodoSortByName());
//		if (order < 0) Collections.reverse(list);
//	}
//
//	public void sortByDate(int order) {
//		Collections.sort(list, new TodoSortByDate());
//		if (order < 0) Collections.reverse(list);
//	}

//	public int indexOf(TodoItem t) {
//		return list.indexOf(t);
//	}

	public Boolean isDuplicate(String title) {
		for (TodoItem item : list) {
			if (title.equals(item.getTitle())) return true;
		}
		return false;
	}
	
	public void importData(String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			String sql = "insert into list (title, description, category, currentDate, dueDate) values (?, ?, ?, ?, ?);";
			
			int records = 0;
			while ((line = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, "##");
				String category = st.nextToken();
				String title = st.nextToken();
				String description = st.nextToken();
				String dueDate = st.nextToken();
				String currentDate = st.nextToken();
				
				PreparedStatement pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, title);
				pstmt.setString(2, description);
				pstmt.setString(3, category);
				pstmt.setString(4, currentDate);
				pstmt.setString(5, dueDate);
				
				int count = pstmt.executeUpdate();
				if (count > 0) records++;
				pstmt.close();
			}
			
			System.out.println(records + " records read!!");
			br.close();
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public boolean isExist(int index) {
		PreparedStatement pstmt;
		String sql = "select count(id) from list where id=?";
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, index);
			ResultSet rs = pstmt.executeQuery();
			
			rs.next();
			
			return rs.getInt("count(id)") > 0;
		}
		catch (SQLException e) { e.printStackTrace(); }
		
		return false;
	}
	
	public static void closeConnection() {
		DbConnect.closeConnection();
	}
}
