import java.rmi.RemoteException; 
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.sql.*;

public class DeleteServices extends UnicastRemoteObject implements DeleteServicesAI
{ 
    // Set up the JDBC driver and database URL
    static final String JDBC_CONNECTOR = "com.mysql.jdbc.Driver";  
    static final String DB_URL = Configuration.getJDBCConnection();

    // Database credentials
    static final String USER = "root";
    static final String PASS = Configuration.MYSQL_PASSWORD;

    public DeleteServices() throws RemoteException {}

    // Main service loop
    public static void main(String args[]) 
    { 	
        try 
        { 
            DeleteServices obj = new DeleteServices();

            Registry registry = Configuration.createRegistry();
            registry.bind("DeleteServices", obj);

            String[] boundNames = registry.list();
            System.out.println("Registered services:");
            for (String name : boundNames) {
                System.out.println("\t" + name);
            }

        } catch (Exception e) {
            System.out.println("DeleteServices binding err: " + e.getMessage()); 
            e.printStackTrace();
        } 
    }

    // Method to delete an order from the database
    public boolean deleteOrder(int orderId) throws RemoteException
    {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;

        try
        {
            // Load JDBC driver
            Class.forName(JDBC_CONNECTOR);

            // Connect to the database
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // SQL query to delete an order by ID
            String sql = "DELETE FROM orders WHERE order_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, orderId);

            int rowsAffected = stmt.executeUpdate();
            success = (rowsAffected > 0);  // Return true if at least one row was deleted

            // Clean-up
            stmt.close();
            conn.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return success;
    }
}
