####################################################################################################
                                        Changelog
####################################################################################################

All notable changes to the **COI Services** project will be documented in this file.

The format is based on `Keep a Changelog <https://keepachangelog.com/en/1.0.0>`_,
and this project adheres to `Semantic Versioning <https://semver.org/spec/v2.0.0.html>`_.

1.0.0
================

Added
---------------
* | **Basic functionality of the COI Push Service**
  | This service allows the coi clients to receive messages from the coi server via push messages.
    For this purpose the service uses googles firebase to transport the encrypted messages to both android and ios devices.
    At its core it's a rest interface, which allows the clients to register their push tokens and allows the coi servers to use those token to send the messages.
  |

* | **Basic functionality of the COI Invitation Service**
  | This service allows coi users to invite their friends/family/colleagues/etc. to use the coi messenger. This is also helpful to exchange mail addresses in case only a phone number is known.
  |

* | **Service independent features**
  | Some features are shared between the different coi services and are therefore valid for all of them:

  * The storage layer uses a mysql compliant database (e.g. mariadb) and manages it via liquibase
  * The service uses the spring boots own configuration system which can be configured in any way you want: via environment variables (recommended), config files, system properties and others. See the spring boot documentation for more informations.
  * All services and most sub modules support spring profiles to activate or deactivate them. This way it's for example easily possible to disable the invite service or to disable cleaning tasks for any given node.
  * The rest api supports rate limiting by a simple in memory ip based rate limiter. So there is theoretically no need for the load balancers to do rate limiting.
  |

* | **Support for operating the COI Services in Docker and/or Kubernetes**
  | With the help of gradle is is possible to create docker images which then can be deployed to a kubernetes cluster. 
    See the deployment documentation for more informations.
  | This includes the following features:

  * Native java container support thanks to using java 11
  * Create the docker images yourself via gradle
  * Support for vault as a database credential source
  * Health rest endpoints which can be used by k8s health probes
