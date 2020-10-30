==============================================
How To Setup COI Push Service with Kubernetes
==============================================

This guide will help you set up a deployment of coi push service (cps) server in a kubernetes environment.
Requirements for a functional cps deployment are: a mariadb (or similar) server/cluster and a working firebase application.

For instructions on how to setup a firebase application take a look at the `firebase homepage <https://firebase.google.com/>`_.

After you created your firebase application you can then start to build our cps environment by setting up a mariadb database. See `link <https://mariadb.org/>`_ for more information.
Please note that we strongly suggest to deploy the database on different nodes than the cps servers. This way you can add additional cps server nodes in case you want to scale out. 
With a database up and running you can now start to deploy the cps deployment itself. 

The cps provides kubernetes files ready for setting up a deployment of multiple cps nodes including a load balancer.

-----------------
1. Configuration 
-----------------

~~~~~~~~~~~~~~~~~~~~~~~
Basic configuration
~~~~~~~~~~~~~~~~~~~~~~~

Most configuration options needs to be placed in a kubernetes *configmap*  called ``coi-services-config``.
You can use the ``kubernetes_config.yml`` file as template and edit it to meet your needs. You can apply the configuration 
with the following *kubectl* command:

::

  $ kubectl apply -f kubernetes_configmap.yml 

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Additional configuration files
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Besides the basic configuration options, various components of the cps come with their own 
configuration file(s) (for example the logging framework with it's ``logback.xml`` configuration file).
The cps provides default values for some of them, but you might want to change their behaviors to 
meet your needs.


You can mount a local directory containing additional configuration files, such as ``logback.xml``,
into the kubernetes pods by using a *configmap* called ``coi-services-config-dir``.

::

 $ mkdir config
 $ kubectl create configmap coi-services-config-dir --from-file=./config


By default the content of this directory is mounted to */opt/coi-push-service/config* inside the cps pods.

^^^^^^^^^^^^^^^^^^^^^^^
Firebase configuration
^^^^^^^^^^^^^^^^^^^^^^^

In order to send push messages through *firebase*, the cps needs to know a private key providing access to the firebase account.

You can obtain a private key file from the firebase console:

*Project Settings -> Service accounts -> Firebase Admin SDK -> Generate new private key*.

Place the private key file in the mounted *configmap* folder and set the corresponding
property ``com.openexchange.coi.services.push.firebase.privatekey`` to point to this file. 

For example:

    com.openexchange.coi.services.push.firebase.privatekey: "/opt/coi-push-service/config/firebase-private-key.json"

^^^^^^^^^
Logback
^^^^^^^^^

In order to provide a custom logging configuration, you need to place your ``logback.xml`` file into the mounted *configmap* directory. 

::
  
  $ cp /my/custom/logback.xml ./config/logback.xml

After that you need to activate the new logging configuration by setting the property ``logging.config`` to point to your custom configuration file:

::

  logging.config: "/opt/coi-push-service/config/logback.xml"

^^^^^^^^^
Invite templates
^^^^^^^^^

The invitation service comes with a default html template, but if you like, you can 
mount a custom template via a *configmap* called ``coi-services-tmpl-dir``.

::

  $ kubectl create configmap coi-services-tmpl-dir --from-file=/my/custom/templatefolder

--------------------------
2. Image registry access 
--------------------------

In case your cps docker image is in a non public registry, you need to configure read access to this registry for kubernets.
Create a kubernets secret for this with the following command: 

::

  $ kubectl create secret docker-registry regcred --docker-server=YOUR_REGISTRY_URL --docker-username=YOUR_USERNAME --docker-password=YOUR_PASSWORD

This creates a secret called ``regcred``. If you want to give your secret a different name, ensure that 
you also reflect this new name in the kubernetes.yml file under the ``imagePullSecrets`` section.

--------------------------
3. Creating the deployment
--------------------------

You can create the deployment by applying the kubernetes.yml file. This will deploy a specific amount of cps nodes including a load balancer in front of them. 
You can optionally change the number of cps nodes (replicas) and the default port of the published service 
within the kubernets.yml file before applying it.

::
  
  $ kubectl apply -f kubernetes.yml

Verify that everything was started: 

::

  $ kubectl get deployment
  $ kubectl get pods
  $ kubectl get services