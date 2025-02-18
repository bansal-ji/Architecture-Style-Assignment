/******************************************************************************************************************
* File:OrdersUI.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2018 Carnegie Mellon University
* Versions:
*	1.0 February 2018 - Initial write of assignment 3 (ajl).
*
* Description: This class is the console for the an orders database. This interface uses a webservices or microservice
* client class to update the ms_orderinfo MySQL database. 
*
* Parameters: None
*
* Internal Methods: None
*
* External Dependencies (one of the following):
*	- MSlientAPI - this class provides an interface to a set of microservices
*	- RetrieveServices - this is the server-side micro service for retrieving info from the ms_orders database
*	- CreateServices - this is the server-side micro service for creating new orders in the ms_orders database
*
******************************************************************************************************************/

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
		MSClientAPI api = new MSClientAPI();	// RESTful api object
		String token = ""; // Placeholder for authentication token


		// -------------- Authentication Loop -----------------
        boolean authenticated = false;
        while (!authenticated) {
            System.out.println("\n\nWelcome to the Orders Database!");
            System.out.println("Please select an option:");
            System.out.println("0: Sign up");
            System.out.println("1: Log in");
            System.out.print(">>>> ");
            int authChoice = keyboard.nextInt();
            keyboard.nextLine(); // Clear newline

            System.out.print("Enter username: ");
            String username = keyboard.nextLine();
            System.out.print("Enter password: ");
            String password = keyboard.nextLine();

            if (authChoice == 0) {
                // Signup process
                response = api.signup(username, password);
                System.out.println(response);
				Logger.info("New user signed up!");
                // After signup, we typically require the user to log in.
            } 
            if (authChoice == 1) {
                // Login process
                token = api.login(username, password);
                // In our design, a valid token is returned if credentials are valid.
                if (token != null && !token.startsWith("Invalid") && !token.startsWith("Login failed")) {
                    System.out.println("Login successful! Your token: " + token);
                    authenticated = true;
					Logger.info("User has logged in!");
                } else {
                    System.out.println("Login failed. Please try again.");
					Logger.error("Login failed for user!");
                }
            }
        }

		/////////////////////////////////////////////////////////////////////////////////
		// Main UI loop
		/////////////////////////////////////////////////////////////////////////////////

		while (!done)
		{	
			// Here, is the main menu set of choices
			System.out.println( "\n\n\n\n" );
			System.out.println( "Orders Database User Interface: \n" );
			System.out.println( "Select an Option: \n" );
			System.out.println( "1: Retrieve all orders in the order database." );
			System.out.println( "2: Retrieve an order by ID." );
			System.out.println( "3: Add a new order to the order database." );				
			System.out.println( "4: Delete an order from the database." );  
			System.out.println( "X: Exit\n" );
			System.out.print( "\n>>>> " );
			option = keyboard.next().charAt(0);	
			keyboard.nextLine();	// Removes data from keyboard buffer. If you don't clear the buffer, you blow 
									// through the next call to nextLine()

			//////////// option 1 ////////////
			if ( option == '1' )
			{
				// Here we retrieve all the orders in the ms_orderinfo database
				System.out.println( "\nRetrieving All Orders::" );
				System.out.println( token );
				try
				{
					response = api.retrieveOrders(token);
					System.out.println(response);
					Logger.info("Retrieved all orders: " + response);
				} 
				catch (Exception e) {
					System.out.println("Request failed:: " + e);
					Logger.error("Error retrieving all orders: " + e);
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
					response = api.retrieveOrders(orderid, token);
					System.out.println(response);
					Logger.info("Retrieved order with order id = " + orderid + ": " + response);
				} 
				catch (Exception e) {
					System.out.println("Request failed:: " + e);
					Logger.error("Failed to retrieve order with order id = " + orderid);
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
						response = api.newOrder(date, first, last, address, phone, token);
						System.out.println(response);
						Logger.info("Created new order!");
					} 
					catch(Exception e) {
						System.out.println("Request failed:: " + e);
						Logger.error("Failed to create new order!");
					}
				} 
				else {
					System.out.println("\nOrder not created...");
					Logger.info("User cancelled to create the order!");
				}

				System.out.println("\nPress enter to continue..." );
				c.readLine();
			} 

			//////////// option 4: DELETE ORDER ////////////
			else if ( option == '4' )  
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
					Logger.info("Order deleted: " + orderid);
				} 
				catch (Exception e) {
					System.out.println("Request failed:: " + e);
					Logger.error("Failed to delete order!");
				}

				System.out.println("\nPress enter to continue..." );
				c.readLine();
			}

			//////////// option X ////////////
			else if ( ( option == 'X' ) || ( option == 'x' ))
			{
				done = true;
				System.out.println( "\nDone...\n\n" );
				Logger.info("User logged out!");
			}
			else
			{
				System.out.println("\nInvalid option, please try again.");
			}
		} 
  	} 
} 
