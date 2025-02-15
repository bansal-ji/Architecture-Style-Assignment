/******************************************************************************************************************
* File: LoginServicesAI.java
* Course: 17633
* Project: Assignment A3 - Login Microservice
* Copyright:
* Versions:
*    15 February 2025 - Initial creation of login service interface (adapted by eParts Team)
*
* Description: This interface defines the remote method for user login. Implementations of this interface
* provide a login service that authenticates a user against the MySQL database.
*
* Parameters: None
*
* Internal Methods:
*    String login(String username, String password) - validates the provided credentials
*
* External Dependencies: None
******************************************************************************************************************/
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LoginServicesAI extends Remote {
    /**
     * Attempts to log in with the given username and password.
     * @param username the username provided by the user
     * @param password the password provided by the user
     * @return a String message indicating whether the login was successful or if it failed
     * @throws RemoteException if an RMI error occurs during the login operation
     */
    String login(String username, String password) throws RemoteException;
}
