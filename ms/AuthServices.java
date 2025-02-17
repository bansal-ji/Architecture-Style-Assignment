import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.sql.*;

public class AuthServices extends UnicastRemoteObject implements AuthServicesAI {

    // JDBC and database details (reuse your Configuration settings)
    static final String JDBC_CONNECTOR = "com.mysql.jdbc.Driver";  
    static final String DB_URL = Configuration.getJDBCConnection();
    static final String USER = "root";
    static final String PASS = Configuration.MYSQL_PASSWORD;
    
    // Do-nothing constructor
    public AuthServices() throws RemoteException { }
    
    // Signup method: Inserts a new user into the "users" table.
    public String signup(String username, String password) throws RemoteException {
        Connection conn = null;
        Statement stmt = null;
        String result = "Signup successful";
        try {
            System.out.println("Signup attempt for user: " + username);
            Class.forName(JDBC_CONNECTOR);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String sql = "INSERT INTO users(username, password) VALUES ('" 
                          + username + "', '" + password + "')";
            stmt.executeUpdate(sql);
            stmt.close();
            conn.close();
        } catch(Exception e) {
            result = "Signup failed: " + e.toString();
        }
        return result;
    }
    
    // Login method: Verifies credentials and returns a self-contained token if valid.
    public String login(String username, String password) throws RemoteException {
        Connection conn = null;
        Statement stmt = null;
        String token = "";
        try {
            Class.forName(JDBC_CONNECTOR);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String sql = "SELECT * FROM users WHERE username = '" + username 
                         + "' AND password = '" + password + "'";
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                // Credentials are valid: generate a token valid for one hour (3600000 ms)
                token = SimpleTokenUtil.generateToken(username, 3600000);
            } else {
                token = "Invalid credentials";
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch(Exception e) {
            token = "Login failed: " + e.toString();
        }
        return token;
    }
    
    // Main method to bind AuthServices to the RMI registry.
    public static void main(String args[]) {
        try {
            AuthServices authObj = new AuthServices();
            Registry registry = Configuration.createRegistry();
            registry.bind("AuthServices", authObj);
            
            String[] boundNames = registry.list();
            System.out.println("Registered AuthServices:");
            for (String name : boundNames) {
                System.out.println("\t" + name);
            }
        } catch(Exception e) {
            System.out.println("AuthServices binding error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
