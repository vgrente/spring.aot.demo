# Vault policy for spring-aot-demo application
# This policy grants read access to GHCR credentials

path "secret/data/ghcr" {
  capabilities = ["read"]
}