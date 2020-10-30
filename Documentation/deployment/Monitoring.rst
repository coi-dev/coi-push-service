==============================
System information and metrics
==============================

The COI-services are able to provide various runtime information and metrics for diagnostic purpose. These information can be pulled from the services via a HTTP or JMX. In addition system metrics can also be pushed to an external *registry*.

-----------------
Pulling metrics
-----------------

~~~~~~~~~~~~~~~~~~~~~
Enabling or disabling
~~~~~~~~~~~~~~~~~~~~~

The services provide various endpoints to gather system specific information from. 
By default, most of the endpoints are enabled. See the `Spring Boot documentation <https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html#production-ready-endpoints-enabling-endpoints>`_ for more details. 

In order to disable all endpoints by default, you can set the configuration option ``management.endpoints.enabled-by-default`` to ``false``:

.. code-block:: ini

 management.endpoints.enabled-by-default=false


To enable or disable an specific endpoint you can simply set it's ``enabled`` property:
``management.endpoint.<endpoint-id>.enabled`` to ``true`` or ``false``. 
See the `Spring Boot documentation <https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html#production-ready-endpoints>`_ 
for their specific names. Example configuration for disabling the ``health`` endpoint: 

.. code-block:: ini

  management.endpoint.health.enabled=false

~~~~~~~~~~~~~~~~~~
Exposing Endpoints
~~~~~~~~~~~~~~~~~~

You can specify how an endpoint is exposed (Web or JMX) by setting the ``inlcude`` and/or ``exclude`` properties of the technology. Use a comma separated list of endpoint names or an *asteriks* character as a placeholder for all endpoints. 

The default values are shown in the following table:

+------------------------------------------+---------------+
| Property                                 | Default       |
+==========================================+===============+
|management.endpoints.jmx.exposure.exclude |               |
+------------------------------------------+---------------+
|management.endpoints.jmx.exposure.include | \*            |
+------------------------------------------+---------------+
|management.endpoints.web.exposure.exclude |               |
+------------------------------------------+---------------+
|management.endpoints.web.exposure.include |  info, health |
+------------------------------------------+---------------+


That means that by default, all enabled endpoints are exposed via *JMX* but only the *info* and 
*health* endpoints are exposed via *web*.

-----------------
Pushing metrics
-----------------

The COI-services are able to push various system metrics to an extern system called a *registry*.
Currenlty only *StatsD* is supported.

~~~~~~~~~~
StatsD
~~~~~~~~~~

The COI-services push various metrics to *StatsD* on *localhost* by default. 

Set the following configuration option to ``false`` in order to disable pushing metrics to *StatsD*:

.. code-block:: ini

 management.metrics.export.statsd.enabled=false

The *StatsD* host can be specified by using the following property.

.. code-block:: ini

  management.metrics.export.statsd.host=statsd.example.com

The default value is ``localhost``.  See the `Spring Boot documentation <https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready-metrics-export-statsd>`_
For more *StatsD* related configuration options.


