# The Components API
The `Components` class is the only class you need to directly use when using components (buttons / selection menus), it provides a builder for every component in order to not introduce boilerplate such as `#!java Button.primary(getId(...), "Test button");`, it would instead be `#!java Components.primaryButton(...).build("Test button");`

## Prerequisites
You will need to set a `ComponentManager` in `CommandsBuilder#setComponentManager`, I strongly recommend that you use the `DefaultComponentManager`, unless you want to reimplement the interface.

For the default component manager, you will need to use PostgreSQL to be able to use the `DefaultComponentManager`, as it is what I tested the framework with.

*The database does not need to be populated with anything*, the tables are created on startup, you will only need to provide a `Connection` supplier.

**I also highly recommend you use a library capable of pooling SQL connections** such as [HikariCP](https://github.com/brettwooldridge/HikariCP), which will greatly reduce the time to process interactions / components

## Discord components features
There are two types of components:

* Persistent components: These are used when you may need to run a method even after your bot is restarted
* Lambda components: These are used when you need a command's context (such as captured variables) using lambdas, **however this does not survive bot restarts**

Both types supports properties such as:

* One-use-ness (Component is deleted from the component manager when all conditions are met and the linked code is executed)
* Interaction constraints (Component is only usable by users which meets *any* of the predefined filters)
* Timeouts (Component is deleted from the component manager after a period of time)
