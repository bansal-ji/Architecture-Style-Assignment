import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AuthServicesAI extends Remote {
    /**
     * Signs up a new user by inserting the username and password into the database.
     * Returns a confirmation string or an error message.
     */
    String signup(String username, String password) throws RemoteException;

    /**
     * Logs in the user by checking the username and password.
     * If valid, returns a token; otherwise, returns an error message.
     */
    String login(String username, String password) throws RemoteException;
}
