HOW TO: coi services with vault
=====

In order to provide secrets to the coi services it is advisable to use a vault to store those secrets. This guid explains how to configure the service.
Note that the vault should run in an own kubernetes cluster or on premise. This is recommended to improve the security. That said, this guide doesn't cover how to configure said k8s cluster. Please refer to the kubernetes documentation for this.

First you need to setup the vault service and made it available via an api. It is recommended to run the cluster in an high availability mode and always acces the vault via an loadbalancer. 
For example you can start a local server (see `Starting the Server <https://learn.hashicorp.com/vault/getting-started/dev-server>`_) with the following command:
::
	vault server -dev -config=my.hcl

where ``my.hcl`` contains the following config:
::
	listener "tcp" {
  		address     = "<yourip>:8200"
  		tls_disable = 1
	}

Now you only need to configure your vault environment variables so you can access the vault. You can test it with this command:
::
	vault status
And you should see a response like this:
::
	Key             Value
	---             -----
	Seal Type       shamir
	Initialized     true
	Sealed          false
	Total Shares    1
	Threshold       1
	Version         1.2.2
	Cluster Name    vault-cluster-7173ae7a
	Cluster ID      b073d4c8-d599-23da-72c6-129218c5ef30
	HA Enabled      false

You can add the proprties and add a policy which will be used. E.g.:
::
	vault kv put secret/db spring.datasource.username=<dbusername> spring.datasource.password=<dbpassword>

	vault policy write mypolicy policy.hcl

Whereby the policy could look like this:
::
	path "secret/data/*" {
    		capabilities = ["read", "list"]
	}

Now you need to configure your kubernetes service role by applying the following cluster role binding:
::
	---
	apiVersion: rbac.authorization.k8s.io/v1beta1
	kind: ClusterRoleBinding
	metadata:
	  name: role-tokenreview-binding
	  namespace: default
	roleRef:
	  apiGroup: rbac.authorization.k8s.io
	  kind: ClusterRole
	  name: system:auth-delegator
	subjects:
	- kind: ServiceAccount
	  name: coi-service-account
	  namespace: default

This binds the ``system:auth-delegator`` permission to the ``coi-service-account`` service account. If you have no ``coi-service-account`` yet, you must create one beforehand.
Once created you can access the secrets kubernetes created for this account. This is needed to configure the vault kubernetes authentication mechanism.

Go to your kubernetes dashboard -> Secrets and choose the secret of the ``coi-service-account``. It is normally named like ``coi-service-account-token-xxx``.
You need to copy both, the ca.crt file and the jwt token. Store the certificate to a ca.crt file on your disk.

Now you can enable the vault kubernetes auth mechanism with the follwing command:
::
	vault auth enable kubernetes

And then configure it:
::
	vault write auth/kubernetes/config token_reviewer_jwt="<jwt_token>" kubernetes_host="https://<ip_of_your_kubernetes_cluster>:<port_of_the_kubernetes_api>" kubernetes_ca_cert=@Path/to/your/ca.crt

At last configure a vault role for this (e.g. named example):
::
	vault write auth/kubernetes/role/example bound_service_account_names=coi-service-account bound_service_account_namespaces=default policies=mypolicy


The vault should now be ready. All that remains is to configure the coi service and restart the deployment. Add the following properties to your configmap:
::
  com.openexchange.coi.services.vault.role: example
  com.openexchange.coi.services.vault.endpoint: <the_vault_endpoint>
  com.openexchange.coi.services.vault.path: /secret/data/db

And add the ``k8sauth`` profile to your list of active profiles. Also remove the existing database credentials.
Then apply those changes and restart:
::
	kubectl apply -f myconfig.yml
	kubectl delete deployment coi-services-deployment
	kubectl apply -f kubernetes.yml
