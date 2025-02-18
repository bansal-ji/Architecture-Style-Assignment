/******************************************************************************************************************
* File: Login.js
* Course: 17633
* Project: Assignment A3 - Web-Services Login
* Copyright:
* Versions:
*    16 February 2025 - Initial implementation of token-based authentication for the web-services system.
*
* Description: This module provides functions for user sign-up, login, and token verification for the web-services system.
* It includes:
*   - generateToken(username, validityMillis): Creates a token containing the username and an expiration timestamp,
*       encrypted using a simple XOR-based algorithm.
*   - verifyToken(token): Decodes and decrypts a token and checks whether it is well-formed and not expired.
*   - signup(req, res): Express route handler that inserts a new user into the "users" table.
*   - login(req, res): Express route handler that verifies credentials against the MySQL "users" table and
*       returns a token if successful.
*   - tokenVerifier(req, res, next): An Express function that checks for a token in the request and
*       rejects the request if the token is missing or invalid.
*
* External Dependencies:
*    - mysql: for connecting to the MySQL database.
*    - process.env.SECRET_KEY: the secret key used for token encryption/verification.
*    - express: to be used as vefrifier step.
******************************************************************************************************************/

require("./ServiceLogger");
var mysql = require('mysql');
var config = require('./config/mysql.config.json');
var serviceEventBus = require("./ServiceEventBus");
var SECRET_KEY = process.env.SECRET_KEY || 'defaultSecret';

/**
 * A simple XOR-based encryption/decryption function.
 * Since XOR is symmetric, applying it twice with the same key recovers the original data.
 * @param {string} data The input string.
 * @param {string} key The secret key.
 * @returns {string} The XOR-encrypted string.
 */
function xorEncrypt(data, key) {
    let result = '';
    for (let i = 0; i < data.length; i++) {
        result += String.fromCharCode(data.charCodeAt(i) ^ key.charCodeAt(i % key.length));
    }
    return result;
}

/**
 * Generates a token for the given username that is valid for validityMillis milliseconds.
 * The token contains the username and an expiration timestamp, encrypted and encoded in Base64.
 * @param {string} username The username.
 * @param {number} validityMillis Token validity duration in milliseconds.
 * @returns {string} The generated token.
 */
function generateToken(username, validityMillis) {
    let expiration = Date.now() + validityMillis;
    let payload = username + ":" + expiration;
    let encrypted = xorEncrypt(payload, SECRET_KEY);
    return Buffer.from(encrypted, 'utf-8').toString('base64');
}

/**
 * Verifies the token by decoding, decrypting, and checking the expiration.
 * @param {string} token The token to verify.
 * @returns {boolean} True if the token is valid, false otherwise.
 */
function verifyToken(token) {
    try {
        let encrypted = Buffer.from(token, 'base64').toString('utf-8');
        let payload = xorEncrypt(encrypted, SECRET_KEY);
        let parts = payload.split(':');
        if (parts.length !== 2) {
            return false;
        }
        let expiration = parseInt(parts[1], 10);
        return Date.now() < expiration;
    } catch (err) {
        return false;
    }
}

/**
 * Express route handler for user sign-up.
 * Expects req.body.username and req.body.password.
 * Inserts a new record into the "users" table.
 */
exports.signup = function(req, res) {
    let username = req.body.username;
    let password = req.body.password;
    serviceEventBus.emit("log", `Signup attempt for username: ${username}`, "INFO", "Auth API", req.ip);
    if (!username || !password) {
        serviceEventBus.emit("log", "Signup failed: Missing username or password", "ERROR", "Auth API", req.ip);
        return res.status(400).json({Error: true, Message: "Username and password required"});
    }
    let connection = mysql.createConnection(config);
    connection.connect(function(err) {
        if (err) {
            serviceEventBus.emit("log", "Signup failed: Database connection error", "ERROR", "Auth API", req.ip);
            return res.status(500).json({Error: true, Message: "Database connection error"});
        }
        let query = "INSERT INTO users(username, password) VALUES (?, ?)";
        connection.query(query, [username, password], function(err, results) {
            if (err) {
                connection.end();
                serviceEventBus.emit("log", `Signup failed for username: ${username} - ${err}`, "ERROR", "Auth API", req.ip);
                return res.status(500).json({Error: true, Message: "Sign-up failed: " + err});
            }
            connection.end();
            serviceEventBus.emit("log", `Signup successful for username: ${username}`, "SUCCESS", "Auth API", req.ip);
            return res.json({Error: false, Message: "Sign-up successful"});
        });
    });
};

/**
 * Express route handler for login.
 * Expects req.body.username and req.body.password.
 * Queries the "users" table in the MySQL database to verify credentials.
 * If valid, returns a token valid for one hour.
 */
exports.login = function(req, res) {
    let username = req.body.username;
    let password = req.body.password;
    serviceEventBus.emit("log", `Login attempt for username: ${username}`, "INFO", "Auth API", req.ip);
    if (!username || !password) {
        serviceEventBus.emit("log", "Login failed: Missing username or password", "ERROR", "Auth API", req.ip);
        return res.status(400).json({Error: true, Message: "Username and password required"});
    }
    
    let connection = mysql.createConnection(config);
    connection.connect(function(err) {
        if (err) {
            serviceEventBus.emit("log", "Login failed: Database connection error", "ERROR", "Auth API", req.ip);
            return res.status(500).json({Error: true, Message: "Database connection error"});
        }
        let query = "SELECT * FROM users WHERE username = ? AND password = ?";
        connection.query(query, [username, password], function(err, results) {
            if (err) {
                connection.end();
                serviceEventBus.emit("log", "Login failed: MySQL query error", "ERROR", "Auth API", req.ip);
                return res.status(500).json({Error: true, Message: "MySQL query error"});
            }
            if (results.length > 0) {
                let token = generateToken(username, 3600000);
                connection.end();
                serviceEventBus.emit("log", `Login successful for username: ${username}. Token issued.`, "SUCCESS", "Auth API", req.ip);
                return res.json({Error: false, Message: "Login successful", token: token});
            } else {
                connection.end();
                serviceEventBus.emit("log", `Login failed for username: ${username} - Invalid credentials`, "ERROR", "Auth API", req.ip);
                return res.status(401).json({Error: true, Message: "Invalid credentials"});
            }
        });
    });
};

/**
 * Express function that verifies the token.
 * Checks for the token in the 'x-access-token' header, query string, or request body.
 */
exports.tokenVerifier = function(req, res, next) {
    serviceEventBus.emit("log", "Token verification initiated", "INFO", "Auth API", req.ip);
    console.log("Verifying token");
    let token = req.headers['x-access-token'] || req.query.token || req.body.token;
    if (!token) {
        serviceEventBus.emit("log", "Token verification failed: No token provided", "ERROR", "Auth API", req.ip);
        return res.status(403).json({Error: true, Message: "No token provided"});
    }
    if (verifyToken(token)) {
        serviceEventBus.emit("log", "Token verified successfully", "SUCCESS", "Auth API", req.ip);
        next();
    } else {
        serviceEventBus.emit("log", "Token verification failed: Invalid or expired token", "ERROR", "Auth API", req.ip);
        return res.status(401).json({Error: true, Message: "Failed to authenticate token"});
    }
};

exports.verifyToken = verifyToken;
