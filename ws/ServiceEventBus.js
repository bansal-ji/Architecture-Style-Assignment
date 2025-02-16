const EventEmitter = require('events');

class ServiceEventBus extends EventEmitter {}

const serviceEventBus = new ServiceEventBus();
module.exports = serviceEventBus;