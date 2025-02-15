/******************************************************************************************************************
* File: LoginServices.java
* Course: 17633
* Project: Assignment A3 - Login Microservice
* Copyright:
* Versions:
*    1.0 April 2025 - Initial implementation of LoginServices (adapted by eParts team)
*
* Description: This class provides the concrete implementation of the login microservice.
* It connects to the MySQL database and validates user credentials against the 'users' table.
* This service is exposed via RMI and follows the same microservice style as the Create and Retrieve services.
*
* Parameters: None
*
* Internal Methods:
*    String login(String username, String password) - authenticates the user based on credentials
*
* External Dependencies:
*    - RMI Registry must be running to start this server.
*    - MySQL database with a 'users' table containing 'username' and 'password' columns.
******************************************************************************************************************/
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.sql.*;

public class LoginServices extends UnicastRemoteObject implements LoginServicesAI {

    // Set up the JDBC driver name and database URL (using the same configuration as the other services)
    static final String JDBC_CONNECTOR = "com.mysql.jdbc.Driver";
    static final String DB_URL = Configuration.getJDBCConnection();

    // Set up the database credentials
    static final String USER = "root";
    static final String PASS = Configuration.MYSQL_PASSWORD;

    // Default constructor
    public LoginServices() throws RemoteException {
        super();
    }

    /**
     * Validates the provided username and password against the 'users' table.
     * @param username the username provided
     * @param password the password provided
     * @return "Login Successful" if credentials are valid, or an error message if not
     * @throws RemoteException if a remote invocation error occurs
     */
    @Override
    public String login(String username, String password) throws RemoteException {
        Connection conn = null;
        PreparedStatement stmt = null;
        String result = "Login Failed";

        try {
            // Load the JDBC driver
            Class.forName(JDBC_CONNECTOR);

            // Establish a connection to the database
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Prepare SQL query to validate user credentials
            // Assumes a 'users' table with columns 'username' and 'password'
            String sql = "SELECT COUNT(*) AS user_count FROM users WHERE username = ? AND password = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);

            // Execute the query and process the result
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("user_count");
                if (count > 0) {
                    result = "Login Successful";
                } else {
                    result = "Invalid Credentials";
                }
            }

            // Clean up JDBC objects
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            // In case of error, return the exception details
            result = "Error: " + e.toString();
        }
        return result;
    }

    /**
     * Main method to register the LoginServices with the RMI registry.
     * This service binds itself to the registry under the name "LoginServices".
     */
    public static void main(String[] args) {
        try {
            // Create an instance of the login service
            LoginServices loginService = new LoginServices();

            // Create or get the RMI registry
            Registry registry = Configuration.createRegistry();

            // Bind the login service to the registry with the name "LoginServices"
            registry.bind("LoginServices", loginService);
            System.out.println("LoginServices registered in RMI registry.");
        } catch (Exception e) {
            System.out.println("LoginServices binding error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
