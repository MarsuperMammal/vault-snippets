#!/bin/bash

#Set up policy
echo '
path "secret/*" {
    capabilities = ["create", "read", "update", "delete", "list"]
}
path "sys/renew/*" {
  capabilities = ["update"]
}' | vault policy write spring -

#Set up cert roles
vault auth enable cert
vault write auth/cert/certs/spring \
    display_name=spring \
    allowed_common_names=spring.hashicorp.com \
    policies=spring \
    certificate=@/vagrant/certs/rootCA.pem \
    period=30m
