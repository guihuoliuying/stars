package com.stars.core.module;

/**
 * module
 * 1. life cycle of a player, a service, a module, or a component
 * 2. packet handler
 * 3. event handler
 * 4. a interface to other module, service, component
 * 5. config data(load, init, reload), more independent
 * 6. extension of other module(e.g. condition, item function, target)
 * ----
 * service
 * 1. life cycle(config data, start, close, save data)
 * 2. packet handler(cross server, route from connector directory)
 * 3. event handler(divide the service according to business)
 * 4. a data component for persist
 * 5. service should be easily switched on player process or an independent process
 * ----
 * the directory of module
 * xxx(module)
 *  |---XxxModule(user data, life cycle, basic function for packet handler, event handler, facade, service)
 *  |---XxxPacketHandler(handle the client request)
 *  |---XxxEventHandler(handler the event from other module)
 *  |---XxxFacade(for module in self player)
 *  |---XxxService(for module in other player, or other service)
 *  |---events
 *       |---Xxx1Event
 *       |---Xxx2Event
 *  |---extensions
 *       |---condition
 *            |---XxxCondition
 *
 *  yyy(service)
 *   |---
 */