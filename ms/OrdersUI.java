import java.lang.Exception;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.Console;

public class OrdersUI
{
	public static void main(String args[]) throws Exception
	{
		boolean done = false;						// main loop flag
		boolean error = false;						// error flag
		char    option;								// Menu choice from user
		Console c = System.console();				// Press any key
		String  date = null;						// order date
		String  first = null;						// customer first name
		String  last = null;						// customer last name
		String  address = null;						// customer address
		String  phone = null;						// customer phone number
		String  orderid = null;						// order ID
		String  response = null;					// response string from REST 
		Scanner keyboard = new Scanner(System.in);	// keyboard scanner object for user input
		DateTimeFormatter dtf = null;				// Date object formatter
		LocalDate localDate = null;					// Date object
		MSClientAPI api = new MSClientAPI();		// RESTful API object

		/////////////////////////////////////////////////////////////////////////////////
		// Main UI loop
		/////////////////////////////////////////////////////////////////////////////////

		while (!done)
		{	
			// Print the main menu options
			System.out.println( "\n\n\n\n" );
			System.out.println( "Orders Database User Interface: \n" );
			System.out.println( "Select an Option: \n" );
			System.out.println( "1: Retrieve all orders in the order database." );
			System.out.println( "2: Retrieve an order by ID." );
			System.out.println( "3: Add a new order to the order database." );				
			System.out.println( "4: Delete an order from the database." );  // ✅ Moved Delete Option Here
			System.out.println( "X: Exit\n" );
			System.out.print( "\n>>>> " );
			option = keyboard.next().charAt(0);	
			keyboard.nextLine();	// Removes data from keyboard buffer

			//////////// option 1 ////////////
			if ( option == '1' )
			{
				System.out.println( "\nRetrieving All Orders::" );
				try
				{
					response = api.retrieveOrders();
					System.out.println(response);
				} 
				catch (Exception e) {
					System.out.println("Request failed:: " + e);
				}
				System.out.println("\nPress enter to continue..." );
				c.readLine();
			}

			//////////// option 2 ////////////
			else if ( option == '2' )
			{
				error = true;
				while (error)
				{
					System.out.print( "\nEnter the order ID: " );
					orderid = keyboard.nextLine();
					try
					{
						Integer.parseInt(orderid);
						error = false;
					} 
					catch (NumberFormatException e) {
						System.out.println( "Not a number, please try again..." );
					}
				}

				try
				{
					response = api.retrieveOrders(orderid);
					System.out.println(response);
				} 
				catch (Exception e) {
					System.out.println("Request failed:: " + e);
				}
				System.out.println("\nPress enter to continue..." );
				c.readLine();
			}

			//////////// option 3 ////////////
			else if ( option == '3' )
			{
				dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				localDate = LocalDate.now();
				date = localDate.format(dtf);

				System.out.println("Enter first name:");
				first = keyboard.nextLine();
				System.out.println("Enter last name:");
				last = keyboard.nextLine();
				System.out.println("Enter address:");
				address = keyboard.nextLine();
				System.out.println("Enter phone:");
				phone = keyboard.nextLine();

				System.out.println("Creating the following order:");
				System.out.println("==============================");
				System.out.println(" Date:" + date);		
				System.out.println(" First name:" + first);
				System.out.println(" Last name:" + last);
				System.out.println(" Address:" + address);
				System.out.println(" Phone:" + phone);
				System.out.println("==============================");					
				System.out.println("\nPress 'y' to create this order:");
				option = keyboard.next().charAt(0);

				if (( option == 'y') || (option == 'Y'))
				{
					try
					{
						System.out.println("\nCreating order...");
						response = api.newOrder(date, first, last, address, phone);
						System.out.println(response);
					} 
					catch(Exception e) {
						System.out.println("Request failed:: " + e);
					}
				} 
				else {
					System.out.println("\nOrder not created...");
				}

				System.out.println("\nPress enter to continue..." );
				c.readLine();
			} 

			//////////// option 4: DELETE ORDER ////////////
			else if ( option == '4' )  // ✅ Fix: Ensuring delete logic is within the menu selection
			{
				error = true;
				while (error)
				{
					System.out.print( "\nEnter the order ID to delete: " );
					orderid = keyboard.nextLine();
					try
					{
						Integer.parseInt(orderid);
						error = false;
					} 
					catch (NumberFormatException e) {
						System.out.println( "Not a number, please try again..." );
					}
				}

				try
				{
					response = api.deleteOrder(orderid);
					System.out.println(response);
				} 
				catch (Exception e) {
					System.out.println("Request failed:: " + e);
				}

				System.out.println("\nPress enter to continue..." );
				c.readLine();
			}

			//////////// option X ////////////
			else if ( ( option == 'X' ) || ( option == 'x' ))
			{
				done = true;
				System.out.println( "\nDone...\n\n" );
			}
			else
			{
				System.out.println("\nInvalid option, please try again.");
			}
		} 
  	} 
} 
