package com.todo.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnect {
	private static Connection conn = null;
	
	public static void closeConnection() {
		if (conn != null) {
			try { conn.close(); }
			catch (SQLException e) { e.printStackTrace(); }
		}
	}
	
	public static Connection getConnection() {
		if (conn == null) {
			try {
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection("jdbc:sqlite:" + "todolist.db");
			}
			catch (Exception e) { e.printStackTrace(); }
		}
		
		return conn;
	}
}
