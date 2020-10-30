=============
Configuration
=============

This page provides an overview over all custom coi service properties. Be aware that you can also all usual spring properties to configure the application.

--------------------------
General config properties
--------------------------

+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+
| Name                                                    | Type      | Default    | Description                                                                  |
+=========================================================+===========+============+==============================================================================+
| com.openexchange.coi.services.ratelimit.enabled         | boolean   | true       | Whether server side rate-limiting is enabled or not                          |
+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+
| com.openexchange.coi.services.ratelimit.limit           | int       | 300        | The number of allowed request in any given timeframe                         |
+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+
| com.openexchange.coi.services.ratelimit.period          | int       | 60         | The timeframe in seconds for the rate limiter                                |
+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+
| com.openexchange.coi.services.forcehttps                | boolean   | false      | Whether http requests to port 80 should be delegated to https on port 8443   |
+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+
| com.openexchange.coi.services.hostName                  | string    | loopback   | The hostname used to create endpoint urls.                                   |
|                                                         |           |            | E.g. for the push endpoint used by coi servers.                              |
+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+
| com.openexchange.coi.services.vault.endpoint            | string    | empty      | The uri to the vault endpoint                                                |
+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+
| com.openexchange.coi.services.vault.path                | string    | empty      | The path to the db secrets. E.g. /secret/data/db                             |
+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+
| com.openexchange.coi.services.vault.token               | string    | empty      | The token to use to authenticate against the vault endpoint.                 |
|                                                         |           |            | Note: This only works in case if you dont use the k8s authentication method. |
+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+
| com.openexchange.coi.services.vault.role                | string    | empty      | The vault role used for k8s authentication.                                  |
|                                                         |           |            | Be aware that the token login is disabled if this property is set.           |
+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+
| com.openexchange.coi.services.vault.truststore          | string    | empty      | The path to the JKS trustore which contains the vault certificate.           |
+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+
| com.openexchange.coi.services.vault.truststore-password | string    | empty      | The password of the JKS truststore.                                          |
+---------------------------------------------------------+-----------+------------+------------------------------------------------------------------------------+

-------------------------------
Push service config properties
-------------------------------

Here is a list of all custom push service config properties. We advice to set those properties as environment variables for the docker image.

+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| Name                                                    | Type    | Default   | Description                                                                                     |
+=========================================================+=========+===========+=================================================================================================+
| com.openexchange.coi.services.push.firebase.privatekey  | string  | empty     | The path to the firebase admin sdk private key.                                                 |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.push.firebase.enabled     | boolean | true      | Whether the firebase push transport is enabled or not                                           |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.push.ttl                  | long    | 345600000 | The time to live in milliseconds of a push resource. Defaults to 96 hours.                      |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.push.short_ttl            | long    | 600000    | The time to live in milliseconds of a push resource before it is validated. Defaults to 10 min. |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.push.vapid                | boolean | true      | Enables or disables vapid signature check                                                       |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.push.database.cleanup     | long    | 3600000   | The time in milliseconds between runs of the push resource cleaner task. Defaults to 1 hour.    |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.push.vapid.cache.enabled  | boolean | true      | Enables or disables vapid header caching                                                        |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.push.vapid.cache.max      | long    | 10000     | Maximum number of entries in the vapid header cache                                             |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.cache.enabled             | boolean | false     | Enables or disables push resource caching                                                       |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.cache.max                 | long    | 100000    | Maximum number of entries in the push resource cache                                            |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.cache.expire              | long    | 60        | The time in minutes after which push resource cache entries expire                              |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
---------------------------------
Invite service config properties
---------------------------------

Here is a list of all custom invite service config properties. We advice to set those properties as environment variables for the docker image.

+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| Name                                                    | Type    | Default   | Description                                                                                     |
+=========================================================+=========+===========+=================================================================================================+
| com.openexchange.coi.services.invite.ttl                | long    | 345600000 | The time to live in milliseconds of an invitation. Defaults to 96 hours.                        |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.invite.maxImageSize       | long    | 10485760  | The maximum allowed image size in bytes                                                         |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.invite.folder             | string  | empty     | The folder containing custom invitation templates.                                              |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.invite.appleLink          | string  | empty     | The link to the apple store app.                                                                |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.invite.googleLink         | string  | empty     | The link to the google store app.                                                               |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.invite.conversationPrefix | string  | empty     | The prefix for the start conversation URL.                                                      |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.invite.database.cleanup   | long    | 3600000   | The time in milliseconds between runs of the invitation cleaner task. Defaults to 1 hour.       |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+
| com.openexchange.coi.services.invite.maxMessageSize     | int     | 1024      | The maximal allowed message size in characters.                                                 |
+---------------------------------------------------------+---------+-----------+-------------------------------------------------------------------------------------------------+

----------------------
COI service profiles
----------------------

In order to activate or deactivate certain parts of the application you can define profiles by using the property `spring.profiles.active`.
For example the default profiles look like this:
::
 spring.profiles.active = default, push, invite, mysql, cleanup

Here is a list of all available profiles:

+---------------+----------------------------------------------------------------------------------------------------------+
| Name          | Description                                                                                              |
+===============+==========================================================================================================+
| push          | Activates the push service components                                                                    |
+---------------+----------------------------------------------------------------------------------------------------------+
| invite        | Activates the invite service components                                                                  |
+---------------+----------------------------------------------------------------------------------------------------------+
| mysql         | Activates the mysql storage components                                                                   |
+---------------+----------------------------------------------------------------------------------------------------------+
| cleanup       | Activates the cleanup service components. Only valid together with mysql.                                |
+---------------+----------------------------------------------------------------------------------------------------------+
| cleanup_only  | Activates the cleanup service components and all required mysql components. E.g. for a cleanup only node |
+---------------+----------------------------------------------------------------------------------------------------------+
| vault         | Activates the vault secret service                                                                       |
+---------------+----------------------------------------------------------------------------------------------------------+
| test          | Activates some test components like the test transport                                                   |
+---------------+----------------------------------------------------------------------------------------------------------+
