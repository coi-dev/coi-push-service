=========================================
How To Setup COI Push Service with Docker
=========================================

This guide will help you set up a single node coi push service (cps) server in a docker container.
Required for a functional cps are three things: the cps server itself, a mariadb (or similar) and a working firebase application.

For instructions on how to setup a firebase application take a look at the [firebase homepage](https://firebase.google.com/).

After you created your firebase application you can then start to build our cps environment by setting up a mariadb database. See [link](https://mariadb.org/) for more information.
Please note that we strongly suggest to deploy the database on a different node than the cps server itself. This way you can add additional cps server nodes in case you want to scale out.
With a database up and running you can now start to deploy the cps server itself.

The cps is packaged as a docker file and it is configured via environment variables. So in order to start the server a working docker environment is required.
In addition the host-system requires a few folders to be present: _/var/log/coi_ for log files and _/config_ for additional configuration files.
Optionally, if you want to use custom templates for the invitation service, you also need a folder for them: _/templates/_ for example.

Then you can download the docker image and start it with the following command:

::

 docker run -d -it -p 443:443 -v /var/log/coi:/opt/coi-push-service/logs -v /config:/opt/coi-push-service/config -v /templates:/opt/coi-push-service/templates --env-file=env_file_name gitlab.open-xchange.com:4567/coi-services/pushservice:latest


Lets tear this down:
1. The cps uses https and therefore runs on port 443. It is possible to change the port if required.
2. You must provide a folder for the cps logs and mount it to '/opt/coi-push-service/logs'
3. You should also provide a folder for other configuration files. E.g. for the firebase tokens and certificates
4. Optional: The invitation service can use custom a templates, which needs to be mounted into the container.
5. You must provide the required environment variables. The most easy way to do this is by providing a environment file ('--env-file=env_file_name')
6. The command uses the latest docker image. You can change that to a specific version if you like.

For a basic configuration you need the following properties:

.. code-block:: ini

    SPRING.CACHE.TYPE=NONE
    spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
    spring.datasource.password=<yourdbpassword>
    spring.datasource.url=jdbc:mariadb://<yourdbhost>:<dbport>/<dbdatabasename>
    spring.datasource.username=<dbuser>
    spring.cloud.kubernetes.enabled=false
    spring.cloud.kubernetes.config.enabled=false
    server.port=443
    server.ssl.key-store-type=PKCS12
    server.ssl.key-store=/opt/coi-push-service/config/keystore.p12
    server.ssl.key-store-password=<keystore password>
    server.ssl.key-alias=<keystorealias>
    spring.liquibase.change-log=classpath:master.xml
    management.endpoints.web.exposure.include=metrics
    com.openexchange.coi.services.hostName=<hostname to use> # e.g. localhost or the ip address of the host node for testing
    com.openexchange.coi.services.push.firebase.privatekey=/opt/coi-push-service/config/firebase.json
    com.openexchange.coi.services.push.firebase.enabled=true
    com.openexchange.coi.services.invite.folder=/opt/coi-push-service/templates
    com.openexchange.coi.services.invite.conversationPrefix=prefix
    com.openexchange.coi.services.invite.appleLink=http://apple.com
    com.openexchange.coi.services.invite.googleLink=http://google.com


You can get the firebase.json from the firebase console:
Project Settings -> Service accounts -> Firebase Admin SDK -> Generate new private key

## Logging

Logging by default is only sent to the console. You need to add a custom loback configuration if you want to log into the host's filesystem.
(See also logging documentation in Logging.md for advanced logging configurations like using _logstash_).

The following example shows a logback.xml configuration which sends log messages to the console and the filesystem.

.. code-block:: xml

  <?xml version="1.0" encoding="UTF-8"?>
    <configuration>

        <property name="LOGS" value="./logs" />

         <!-- use Spring default values -->
        <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

        <appender name="Console" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>${CONSOLE_LOG_PATTERN}</pattern>
                <charset>utf8</charset>
            </encoder>
        </appender>

        <appender name="RollingFile"
            class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>${LOGS}/coi-services.log</file>
            <encoder
                class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
                <Pattern>%d %p %C{1.} [%t] %m%n</Pattern>
            </encoder>

            <rollingPolicy
                class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <!-- roll over daily and when the file reaches 10 MegaBytes -->
                <fileNamePattern>${LOGS}/archived/coi-services-%d{yyyy-MM-dd}.%i.log
                </fileNamePattern>
                <timeBasedFileNamingAndTriggeringPolicy
                    class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                    <maxFileSize>10MB</maxFileSize>
                </timeBasedFileNamingAndTriggeringPolicy>
            </rollingPolicy>
        </appender>

        <!-- LOG everything at INFO level -->
        <root level="info">
            <appender-ref ref="RollingFile" />
            <appender-ref ref="Console" />
        </root>

        <!--<logger name="com.openexchange.coi" level="debug" /> -->
    </configuration>


Put this file into the mounted configuration folder and apply it by setting
the following configuration:

.. code-block:: ini

 logging.config=file:/opt/coi-push-service/etc/logback.xml


This will write the log files to the _logs_ subfolder, so make sure the container was started with a valid mount for the logs.
For example: _-v /var/log/coi:/opt/coi-push-service/logs_
