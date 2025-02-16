const fs = require("fs");
const path = require("path");
const serviceEventBus = require("./ServiceEventBus");

const LOG_FILE_PATH = path.join(__dirname, "ws_service_logs.txt");

function writeServiceLog(message) {
    const timestamp = new Date().toISOString();
    const logMessage = `[${timestamp}] ${message}\n`;
    fs.appendFile(LOG_FILE_PATH, logMessage, (err) => {
        if (err) {
            console.error("Failed to write service log:", err);
        }
    });
}

// Listen for log events from the service event bus.
serviceEventBus.on("log", (message, level, service, user) => {
    console.log(`Service log event: ${message}, ${level}, ${service}, ${user}`);
    // Use setImmediate to schedule asynchronous writing and non-blocking calls.
    setImmediate(() => {
        writeServiceLog(`${service} | ${level} | ${message} | User/IP: ${user}`);
    });
});

module.exports = writeServiceLog;