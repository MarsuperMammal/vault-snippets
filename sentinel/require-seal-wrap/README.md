# Require Seal Wrap on Secret Mounts in Vault

## Introduction
Introduced in Vault Enterprise Premium v0.9.0, Vault's Seal Wrap functionality will be able to utilize any of the supported seal types (HSM, AWS KMS, Google Cloud KMS) to add an additional layer of encryption to Values in Vault.

Vault's Seal Wrap feature has been evaluated for compliance with FIPS 140-2 requirements. When used with a FIPS 140-2-compliant HSM, Vault will store Critical Security Parameters (CSPs) in a manner that is compliant with KeyStorage and KeyTransit requirements. You can read more about this here: https://github.com/hashicorp/vault-enterprise/blob/master/website/source/docs/enterprise/sealwrap/index.html.md

Seal Wrap is on by default for many parts of Vault and opt-in for each individual mount. However, in strict compliance environments it is also best-practice to enforce policy to ensure that required secret mounts be configured to use the Seal Wrap functionality.

## The Policy
The following is an example Sentinel policy that will require the `seal_wrap` flag to be set to `true` when attempting to mount specific secret backends.

```
import "strings"

require_seal_wrap = rule {
    request.data.config.seal_wrap is true
}

precond = rule {

    # Only apply the rule when creating/updating a secret mount
    request.operation in ["create", "update"] and
    
    # Only apply the rule to secret backend types that we want to require seal-wrap on
    request.data.type in ["pki", "transit"]
}

main = rule when precond {
    require_seal_wrap
}
```

## A Note About the `root` Token
It's important to remember that Vault does not evaluate any types of policies (ACL, RGP, EGP) against the `root` token (I learned this the hard way...) so make sure to switch to a different user for testing.

For this example, I'll quickly create a Vault admin user that has, essentially, the same privileges as the `root` token. You can skip this if you already have a user you'd like to test with.

```
# Write our admin policy file
tee policy-admin.hcl <<EOF
path "*" {
  capabilities = ["sudo", "create", "read", "update", "delete", "list"]
}
EOF

# Write the admin policy to Vault
vault policy-write admin policy-admin.hcl

# Enable the userpass auth backend in Vault
vault auth-enable userpass

# Create the localadmin Vault user and associate the admin policy
vault write auth/userpass/users/localadmin \
    password=testadmin \
    policies=admin

# Authenticate
vault auth -method=userpass \
    username=localadmin \
    password=testadmin
```

Remember to export the returned token to the `VAULT_TOKEN` environment variable for the API calls later in this document.

## Write the Policy to Vault

The first step we'll take is to write our policy to a file. I've added additional debug functionality to the above policy:

```
tee egp-policy-require-seal-wrap-secret-mounts.sentinel <<EOF
import "strings"
debug = func() {
    print("request.connection:", request.connection)
    print("request.data:", request.data)
    print("request.operation:", request.operation)
    print("request.path:", request.path)
    print("request.policy_override:", request.policy_override)
    print("request.unauthenticated:", request.unauthenticated)
    print("request.wrapping:", request.wrapping)
    print("identity.entity:", identity.entity)
    print("identity.groups:", identity.groups)
    print("mfa.methods:", mfa.methods)

    return true
}

# Require that seal_wrap is set to true
require_seal_wrap = rule {
    request.data.config.seal_wrap is true
}

precond = rule {

    # Only apply the rule when creating/updating a secret mount
    request.operation in ["create", "update"] and
    
    # List out the secret backend types that we want to require seal-wrap on
    request.data.type else "" in ["pki", "transit"]
}

main = rule when precond {
    debug() and
    require_seal_wrap
}
EOF
```

Since this policy isn't tied to a specific indentity or token, but rather a mount path, we'll create this as an Endpoint Governing Policy (EGP) and tie it to the `sys/mounts` path. Also note that I am base64-encoding the policy to avoid having to worry about string escaping:

```
tee payload.json <<EOF
{
  "policy": "$(cat egp-policy-require-seal-wrap-secret-mounts.sentinel | base64)",
  "paths": [ "sys/mounts/*" ],
  "enforcement_level": "soft-mandatory"
}
EOF

curl \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    --request PUT \
    --data @payload.json \
    $VAULT_ADDR/v1/sys/policies/egp/require-seal-wrap-secret-mounts | jq
```

## Test the Policy
Now let's try to mount a secret backend that we've set Seal Wrap policy against:

```
tee payload.json <<EOF
{
  "type": "transit",
  "config": {
    "seal_wrap": false
  }
}
EOF

curl \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    --request POST \
    --data @payload.json \
    $VAULT_ADDR/v1/sys/mounts/test-transit | jq
```

You should recieve an error similar to the following:

```
{
  "errors": [
    "1 error occurred:\n\n* egp policy \"require-seal-wrap-secret-mounts\" evaluation resulted in denial.\n\nThe specific error was:\n<nil>\n\nA trace of the execution for policy \"require-seal-wrap-secret-mounts\" is available:\n\nResult: false\n\nDescription: <none>\n\nprint() output:\n\nrequest.connection: [{\"remote_addr\" \"127.0.0.1\"}]\nrequest.data: [{\"type\" \"transit\"} {\"config\" [{\"seal_wrap\" false}]}]\nrequest.operation: update\nrequest.path: sys/mounts/test-transit\nrequest.policy_override: false\nrequest.unauthenticated: false\nrequest.wrapping: []\nidentity.entity: []\nidentity.groups: undefined\nmfa.methods: []\n\n\nRule \"main\" (byte offset 922) = false\n  true (offset 953): debug()\n  false (offset 969): require_seal_wrap\n    false (offset 620): request.data.config.seal_wrap is true\n\nRule \"precond\" (byte offset 661) = true\n  true (offset 747): request.operation in [\"create\", \"update\"]\n  true (offset 879): request.data.type in [\"pki\", \"transit\"]\n\nRule \"require_seal_wrap\" (byte offset 589) = false\n"
  ]
}
```

Reading through this error we see all our `debug()` print statements and also the results of our rules. The debugging print statements are helpful when developing and testing rules and once you're comfortable with the rule providing the correct checks, you can simply comment out the `debug()` line in your policy.

Now let's try to add the same backend, with `seal_wrap` set to `true`:

```
tee payload.json <<EOF
{
  "type": "transit",
  "config": {
    "seal_wrap": true
  }
}
EOF

curl \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    --request POST \
    --data @payload.json \
    $VAULT_ADDR/v1/sys/mounts/test-transit | jq
```

This time, the API call should not return any errors. You can check to see that the mount was created with the `seal_wrap` flag set to `true`:

```
curl -s \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    $VAULT_ADDR/v1/sys/mounts | jq '.["test-transit/"]'

{
  "accessor": "transit_57f103e3",
  "config": {
    "default_lease_ttl": 0,
    "force_no_cache": false,
    "max_lease_ttl": 0,
    "plugin_name": "",
    "seal_wrap": true
  },
  "description": "",
  "local": false,
  "type": "transit"
}
```