
/******************************************************************************************************************
* File:REST.js
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2018 Carnegie Mellon University
* Versions:
*   1.0 February 2018 - Initial write of assignment 3 for 2018 architectures course(ajl).
*
* Description: This module provides the restful webservices for the Server.js Node server. This module contains GET,
* and POST services.  
*
* Parameters: 
*   router - this is the URL from the client
*   connection - this is the connection to the database
*   md5 - This is the md5 hashing/parser... included by convention, but not really used 
*
* Internal Methods: 
*   router.get("/"... - returns the system version information
*   router.get("/orders"... - returns a listing of everything in the ws_orderinfo database
*   router.get("/orders/:order_id"... - returns the data associated with order_id
*   router.post("/order?"... - adds the new customer data into the ws_orderinfo database
*
* External Dependencies: mysql
*
******************************************************************************************************************/

require("./ServiceLogger");
var mysql   = require("mysql");     //Database
var login = require("./Login.js");  //Login
var serviceEventBus = require("./ServiceEventBus"); //Event-bus

function REST_ROUTER(router,connection) {
    var self = this;
    self.handleRoutes(router,connection);
}

// Here is where we define the routes. Essentially a route is a path taken through the code dependent upon the 
// contents of the URL

REST_ROUTER.prototype.handleRoutes= function(router,connection) {

    // GET with no specifier - returns system version information
    // req paramdter is the request object
    // res parameter is the response object

    router.get("/",function(req,res){
        serviceEventBus.emit("log", "GET / requested", "INFO", "REST API", req.ip);
        res.json({"Message":"Orders Webservices Server Version 1.0"});
    });
    

    // Public endpoints for sign-up and login (do not require token)
    router.post("/signup", login.signup);
    router.post("/login", login.login);

    // Apply token verification to all routes defined after this point
    router.use(function(req, res, next) {
        login.tokenVerifier(req, res, next);
    });

    // GET for /orders specifier - returns all orders currently stored in the database
    // req paramdter is the request object
    // res parameter is the response object
  
    router.get("/orders",function(req,res){
        serviceEventBus.emit("log", "GET /orders requested", "INFO", "REST API", req.ip);
        console.log("Getting all database entries..." );
        var query = "SELECT * FROM ??";
        var table = ["orders"];
        query = mysql.format(query,table);
        connection.query(query,function(err,rows){
            if(err) {
                serviceEventBus.emit("log", "Error fetching orders", "ERROR", "REST API", req.ip);
                res.json({"Error" : true, "Message" : "Error executing MySQL query"});
            } else {
                serviceEventBus.emit("log", `Successfully retrieved ${rows.length} orders`, "SUCCESS", "REST API", req.ip);
                res.json({"Error" : false, "Message" : "Success", "Orders" : rows});
            }
        });
    });

    // GET for /orders/order id specifier - returns the order for the provided order ID
    // req paramdter is the request object
    // res parameter is the response object
     
    router.get("/orders/:order_id",function(req,res){
        const orderId = req.params.order_id;
        serviceEventBus.emit("log", `GET /orders/${orderId} requested`, "INFO", "REST API", req.ip);
        console.log("Getting order ID: ", orderId );
        var query = "SELECT * FROM ?? WHERE ??=?";
        var table = ["orders","order_id",orderId];
        query = mysql.format(query,table);
        connection.query(query,function(err,rows){
            if(err) {
                serviceEventBus.emit("log", `Error fetching order with order id: ${orderId}`, "ERROR", "REST API", req.ip);
                res.json({"Error" : true, "Message" : "Error executing MySQL query"});
            } else {
                serviceEventBus.emit("log", `Successfully retrieved order with order id: ${orderId}`, "SUCCESS", "REST API", req.ip);
                res.json({"Error" : false, "Message" : "Success", "Users" : rows});
            }
        });
    });

    // POST for /orders?order_date&first_name&last_name&address&phone - adds order
    // req paramdter is the request object - note to get parameters (eg. stuff afer the '?') you must use req.body.param
    // res parameter is the response object 
  
    router.post("/orders",function(req,res){
        //console.log("url:", req.url);
        //console.log("body:", req.body);
        serviceEventBus.emit("log", "POST /orders requested with data: " + JSON.stringify(req.body), "INFO", "REST API", req.ip);
        console.log("Adding to orders table ", req.body.order_date,",",req.body.first_name,",",req.body.last_name,",",req.body.address,",",req.body.phone);
        var query = "INSERT INTO ??(??,??,??,??,??) VALUES (?,?,?,?,?)";
        var table = ["orders","order_date","first_name","last_name","address","phone",req.body.order_date,req.body.first_name,req.body.last_name,req.body.address,req.body.phone];
        query = mysql.format(query,table);
        connection.query(query,function(err,rows){
            if(err) {
                serviceEventBus.emit("log", "Error creating order", "ERROR", "REST API", req.ip)
                res.json({"Error" : true, "Message" : "Error executing MySQL query"});
            } else {
                serviceEventBus.emit("log", `Successfully created order with order id: ${rows.insertId}`, "SUCCESS", "REST API", req.ip);
                res.json({"Error" : false, "Message" : "User Added !"});
            }
        });
    });

    // DELETE for /orders/delete/order id specifier - returns the order for the provided order ID
    // req parameter is the request object
    // res parameter is the response object
     
    router.delete("/orders/delete/:order_id",function(req,res){
        const orderId = req.params.order_id;
        serviceEventBus.emit("log", `DELETE /orders/${orderId} requested`, "INFO", "REST API", req.ip);
        console.log("Getting order ID: ", orderId );
        var query = "DELETE FROM ?? WHERE ??=?"
        var table = ["orders","order_id",orderId];
        query = mysql.format(query,table);
        connection.query(query,function(err,rows){
            if(err) {
                serviceEventBus.emit("log", `Error deleting order with order id: ${orderId}`, "ERROR", "REST API", req.ip)
                res.json({"Error" : true, "Message" : "Error executing MySQL query"});
            } else {
                serviceEventBus.emit("log", `Successfully deleted order with order id: ${orderId}`, "SUCCESS", "REST API", req.ip);
                res.json({"Error" : false, "Message" : "Order Deleted !"});
            }
        });
    });

}

// The next line just makes this module available... think of it as a kind package statement in Java

module.exports = REST_ROUTER;