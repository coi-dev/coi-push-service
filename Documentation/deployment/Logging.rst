=======
Logging
=======

The COI-services are able to log various messages for diagnostic purpose.
`Logback <https://logback.qos.ch>`_ is used as logging framework.

--------------
Configuration
--------------

The COI services come with a default logging configuration which logs all messages to the console.
The configuration can be enhanced to support various real life scenarios. 

In order to apply a custom logging configuration, you need to set the ``logging.config`` configuration property to the full path of your custom logback configuration file. 

For example:
::
  logging.config: "/opt/coi-push-service/config/custom_logback.xml"

--------------
File logging
--------------

In order to log information to the file system you can configure a File-Appender.
See `RollingFileAppender <https://logback.qos.ch/manual/appenders.html#RollingFileAppender>`_ for more information.

--------------
Logstash
--------------

The COI-services also support appending log messages to *logstash*. For this you need to add one of the *logstash* appender to your *logback* configuration (for example a ``net.logstash.logback.appender.LogstashTcpSocketAppender``)

The logback configuration could look the following:

.. code-block:: xml

    <appender name="stash" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>mylogbackhost:1337</destination>

        <!-- encoder is required -->
        <encoder class="net.logstash.logback.encoder.LogstashEncoder" />
    </appender>


The corresponding *logstah* configuration could look like the following snippet, where all incoming messages are forwarded to `elasticsearch <https://www.elastic.co/de/products/elasticsearch>`_:

:: 

  input {
    tcp{
     port => 1337
    }
  }

  filter {
    ### getting all fields that where extracted from logstash-logback-plugin
    json {
      source => "message"
    }
    ### filter out keep-alive-messages, that have no valid JSON-format and produce _jsonparsefailure in tags
    if "_jsonparsefailure" in [tags] {
        drop { }
    }
  }

  output {
    elasticsearch {
        hosts => "elasticsearchhost:9200"
    }
  }

See the `documentation <https://github.com/logstash/logstash-logback-encoder>`_ of the 
*logstash-appender-encoder* project for more information.
