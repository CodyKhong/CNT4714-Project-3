import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.*;

//Name: Cody Khong
//Course: CNT 4714 – Summer 2021 – Project Three
//Assignment title: A Three-Tier Distributed Web-Based Application
//Date: Sunday August 1, 2021

public class Project3Servlet extends HttpServlet {
	
	public class Table {
		private String snum;
		private String pnum;
		private String jnum;
		private String quantity;
		
		public Table(String snum, String pnum, String jnum, String quantity) {
			this.snum = snum;
			this.pnum = pnum;
			this.jnum = jnum;
			this.quantity = quantity;
		}
		
		public String getSnum() {
			return this.snum;
		}
		
		public String getQuantity() {
			return this.quantity;
		}
	}
	
	private Connection connection = null;
	private Statement statement = null;
		
	String message = "";
	String[] Columns = null;
	Integer Order = null;
	
	private static String bonusPreLoad = 	 "create table beforeShipments like shipments; " +  "insert into beforeShipments select * from shipments; ";
	private static String bonusPostLoad = "update suppliers " + "set status = status + 5 " + "where snum in ( " + "select snum " + "from shipments " + "where quantity > 100) ";

	@Override
	public void init (ServletConfig config) throws ServletException {
		super.init(config);
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/project3", "root", "");
			statement = connection.createStatement();
		}
		catch(SQLException | ClassNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sqlStatement = request.getParameter("sqlStatement");
		
		HttpSession session = request.getSession();
		
		try {
			char firstChar = sqlStatement.charAt(0);
			if(firstChar == 's' || firstChar == 'S') {
				Select(sqlStatement, statement);
			}
			else {
				Update(sqlStatement, statement);
			}
		}
		catch (SQLException e) {
			this.message += "<p style='background-color:red; border:3px;" + "border-style:solid; border-color:black; text-align:center'>Error executing command:<br>";
			this.message += e.getMessage() + "</p>";
		}
		
		session.setAttribute("sqlStatement", sqlStatement);
		if(this.message != null) {
			session.setAttribute("message", message);
			this.message = null;
			this.message = "";
		}
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
		dispatcher.forward(request, response);
	}
	
	protected void Select(String sql, Statement statement) throws SQLException {
		sql.split(";");
		ResultSet rs = statement.executeQuery(sql);
		ResultSetMetaData rsmd = rs.getMetaData();
		
		int numberOfColumns = rsmd.getColumnCount();
		this.Columns = new String[numberOfColumns];
		for(int i=0; i<numberOfColumns; i++) {
			this.Columns[i] = rsmd.getColumnName(i+1);
		}
		
		String tabletag = "";
		String top = "<thead><tr>";
		for(String columnName : Columns) {
			top += "<th>" + columnName + "</th>";
		}
		top += "</tr></thead>";
		String main = "<tbody><tr>";
		String[] colour = new String[] {"lightgrey", "white"};
		while(rs.next()) {
			String toggle = "yellow";
			int i = 0;
			if(i % 2 == 0) {
				toggle = colour[0];
				i++;
			}
			else {
				toggle = colour[1];
				i++;
			}
			main += "<tr background-color=" + toggle + ">";
			for(int j=1; j<= numberOfColumns; j++ ) {
				main += "<td>" + rs.getString(j) + "</td>";
			}
			main += "</tr>";
		}
		main += "</tbody>";
		tabletag += (top + main);
		this.message += tabletag;
		if(rs != null) {
			rs.close();
		}

	}
	
	protected void Update(String sql, Statement statement) throws SQLException {
		boolean triggerLoad = false;
		ResultSet bonus = null;

		bonus = statement.executeQuery("select * from shipments;");
		List<Table> preShipments = new ArrayList<Table>();
		while(bonus.next()) {
			String supplier = bonus.getString("snum");
			String part = bonus.getString("pnum");
			String job = bonus.getString("jnum");
			String quantities = bonus.getString("quantity");
			Table nextShip = new Table(supplier, part, job, quantities);
			preShipments.add(nextShip);
		}
		bonus.close();
		
		int numRowsAffected = statement.executeUpdate(sql);
		
		bonus = statement.executeQuery("select * from shipments;");
		List<Table> postShipments = new ArrayList<Table>();
		while(bonus.next()) {
			String supplier = bonus.getString("snum");
			String part = bonus.getString("pnum");
			String job = bonus.getString("jnum");
			String quantities = bonus.getString("quantity");
			Table updatedShipment = new Table(supplier, part, job, quantities);
			postShipments.add(updatedShipment);
		}
		bonus.close();
		
		for(Table unaffected : preShipments) {
			postShipments.remove(unaffected);
		}
		
		for(int i=0; i<postShipments.size(); i++) {
			Table st = postShipments.get(i);
			if(Integer.parseInt(st.getQuantity()) > 100)
				triggerLoad = true;
		}
		
		int numStatusRowsAffected = 0;
			String nonbonusLoad = "update suppliers set status = status + 5 " + "where snum in (select snum from shipments " + "where quantity >= 100)";
		numStatusRowsAffected = statement.executeUpdate(nonbonusLoad);
		
		
		if(triggerLoad) {
			this.message += "<p style='background-color:green; border:3px; " + "border-style:solid; border-color:black; text-align:center'>"  + "The statement executed succesfully.<br>" + numRowsAffected  + " row(s) affected.<br>" + "Business Logic Detected! - Updating Supplier Status<br>" +  "Business Logic updated " + numStatusRowsAffected + " supplier status marks.</p>";
			bonus.close();
			triggerLoad = false;
		}
		else {
			this.message += "<p style='background-color:green; border:3px; " + "border-style:solid; border-color:black; text-align:center'>" + "The statement executed succesfully.<br>" + numRowsAffected + " row(s) affected.</p>";
		}
	}
	
	public void destroy() {
		try {
			statement.close();
			connection.close();
		}
		catch(SQLException sqlException) {
			sqlException.printStackTrace();
		}
	}

}
