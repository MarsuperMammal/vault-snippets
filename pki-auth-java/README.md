# pki-auth-java

### Setup

Sample standalone Java & TLS cert auth demo.

1. Run `vagrant up vault`
2. [Init Vault](https://www.vaultproject.io/intro/getting-started/deploy.html#initializing-the-vault)
3. Run the [Vault script](vault.sh)
4. Run `vagrant up spring`
5. Validate below

### Validation

- Curl
```
[vagrant@spring ~]$ curl  --silent   --request POST     --cert /vagrant/certs/spring.crt     --key /vagrant/certs/spring.key     https://vault.hashicorp.com:8200/v1/auth/cert/login | jq
{
  "request_id": "0273d459-68c3-b42a-eb90-d246519be1c8",
  "lease_id": "",
  "renewable": false,
  "lease_duration": 0,
  "data": null,
  "wrap_info": null,
  "warnings": null,
  "auth": {
    "client_token": "9d68a489-0488-7519-a23b-ce740856e5f0",
    "accessor": "0cf7ea3e-cb43-6ec1-2faf-207d960f1141",
    "policies": [
      "default",
      "spring"
    ],
    "token_policies": [
      "default",
      "spring"
    ],
    "metadata": {
      "authority_key_id": "",
      "cert_name": "spring",
      "common_name": "spring.hashicorp.com",
      "serial_number": "14503497771562791000",
      "subject_key_id": ""
    },
    "lease_duration": 3600,
    "renewable": true,
    "entity_id": "a548e630-f066-05b3-9281-739b1671e90e"
  }
}
```

- Spring
```
[vagrant@spring ~]$ sudo journalctl -u spring | grep Token
Sep 28 16:45:18 spring.hashicorp.com vault-pki-auth-1.0.jar[4257]: 2018-09-28 16:45:18.941  INFO 4274 --- [           main] c.h.lance.SpringPKIAuthApplication       : Got Vault Token: 5e88b521-a234-e0e4-ea30-2d7bc8ca78c9
```
