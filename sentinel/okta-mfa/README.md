Prerequisites
1. Configure Okta 2FA for a valid Okta user.
2. Add your user to an Okta group called "Okta" from your Okta tenant.

Instructions for use

1. Place the vault binary in this folder. I.E - vault
2. Open the Vagrantfile and configure the $okta section with the variables for your env. Example below.
```
#************** EDIT THESE FOR YOUR ENV **************
OKTA_USERNAME=testuser
OKTA_DOMAIN=hashicorp.com
OKTA_ORG=dev-org
OKTA_API_TOKEN=00cRCGb4v04wp-sl2bjUz-HnoRlFkV2RlqMLJp3aDp
#************** EDIT THESE FOR YOUR ENV **************
```
3. SSH to the vault server with `vagrant ssh`.
4. Authenticate with Okta. You will be asked to 2FA on your device. `vault auth -method=okta username=<your_user>@hashicorp.com`
5. Additionally, you can test MFA on an ACL path. `vault read secret/me`
