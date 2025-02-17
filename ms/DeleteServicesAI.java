import java.rmi.*;

public interface DeleteServicesAI extends Remote {
    /*******************************************************
    * Deletes an order corresponding to the given order id.
    * Returns true if the deletion was successful, false otherwise.
    *******************************************************/
    boolean deleteOrder(int orderId) throws RemoteException;
}
