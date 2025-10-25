# Vault Setup for Kubernetes Secrets Management

This directory contains configuration for HashiCorp Vault and the Vault Secrets Operator to manage Kubernetes secrets.

## Prerequisites

- Kubernetes cluster (minikube)
- Helm 3.x
- kubectl

## Installation Steps

### 1. Install Vault Secrets Operator

```bash
# Add HashiCorp Helm repository
helm repo add hashicorp https://helm.releases.hashicorp.com
helm repo update

# Install Vault Secrets Operator (latest version)
helm install vault-secrets-operator hashicorp/vault-secrets-operator \
  --version 0.10.1 \
  --namespace vault-secrets-operator-system \
  --create-namespace
```

### 2. Deploy Vault (Dev Mode)

**⚠️ WARNING: This is a dev-mode Vault - DO NOT use in production!**

```bash
# Create namespace
kubectl apply -f k8s/vault/00-namespace.yaml

# Create Vault ServiceAccount with necessary RBAC permissions
kubectl apply -f k8s/vault/05-vault-rbac.yaml

# Deploy Vault
kubectl apply -f k8s/vault/01-vault-dev.yaml

# Wait for Vault to be ready
kubectl wait --for=condition=ready pod -l app=vault -n vault --timeout=60s

# Verify Vault is running
kubectl get pods -n vault
```

### 3. Configure Vault for Kubernetes Authentication

```bash
# Port-forward to access Vault (keep this running in background)
kubectl port-forward -n vault svc/vault 8200:8200 &

# Set Vault environment variables (for local CLI access)
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='root'

# Enable Kubernetes authentication
vault auth enable kubernetes

# Configure Kubernetes auth method from inside the Vault pod
# This ensures Vault uses the correct ServiceAccount with auth-delegator permissions
kubectl exec -n vault $(kubectl get pod -n vault -l app=vault -o jsonpath='{.items[0].metadata.name}') -- sh -c '
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=root

# Configure with the vault ServiceAccount token
vault write auth/kubernetes/config \
    kubernetes_host=https://kubernetes.default.svc:443 \
    kubernetes_ca_cert=@/var/run/secrets/kubernetes.io/serviceaccount/ca.crt \
    token_reviewer_jwt=@/var/run/secrets/kubernetes.io/serviceaccount/token
'

# Create a policy for the spring-aot-demo app
# Recommended: Use the provided policy file
vault policy write spring-aot-demo k8s/vault/policy.hcl

# Alternative Option 1: Using a temporary file
# cat > /tmp/vault-policy.hcl <<'POLICY'
# path "secret/data/ghcr" {
#   capabilities = ["read"]
# }
# POLICY
# vault policy write spring-aot-demo /tmp/vault-policy.hcl
# rm /tmp/vault-policy.hcl

# Alternative Option 2: Using echo (single line)
# echo 'path "secret/data/ghcr" { capabilities = ["read"] }' | vault policy write spring-aot-demo -

# Create a Kubernetes auth role
# This role allows the vault-auth ServiceAccount to authenticate
vault write auth/kubernetes/role/spring-aot-demo \
    bound_service_account_names=vault-auth \
    bound_service_account_namespaces=default \
    policies=spring-aot-demo \
    ttl=24h
```

### 4. Store GitHub Container Registry Credentials in Vault

```bash
# Enable KV v2 secrets engine (if not already enabled)
vault secrets enable -path=secret kv-v2

# Store GHCR credentials
vault kv put secret/ghcr \
    username=vgrente \
    password=<YOUR_GITHUB_TOKEN>

# Verify the secret
vault kv get secret/ghcr
```

### 5. Deploy Vault Integration Resources

```bash
# Create ServiceAccount for the application to authenticate with Vault
kubectl apply -f k8s/vault/06-vault-auth-rbac.yaml

# Deploy VaultConnection, VaultAuth, and VaultStaticSecret
kubectl apply -f k8s/vault/02-vault-connection.yaml
kubectl apply -f k8s/vault/03-vault-auth.yaml
kubectl apply -f k8s/vault/04-ghcr-secret.yaml

# Wait a moment for the operator to sync
sleep 10

# Verify the secret was created
kubectl get secret ghcr-secret

# Optional: View the secret data
kubectl get secret ghcr-secret -o jsonpath='{.data._raw}' | base64 -d
```

### 6. Update Deployment to Use Image Pull Secret

Update the `k8s/deployment.yaml` to reference the secret:

```yaml
spec:
  template:
    spec:
      imagePullSecrets:
      - name: ghcr-secret
      containers:
      - name: spring-aot-demo
        # ... rest of container spec
```

Or patch the existing deployment:

```bash
kubectl patch deployment spring-aot-demo -p '{"spec":{"template":{"spec":{"imagePullSecrets":[{"name":"ghcr-secret"}]}}}}'
```

## Verification

```bash
# Check Vault Secrets Operator is running
kubectl get pods -n vault-secrets-operator-system

# Check Vault is running
kubectl get pods -n vault

# Check VaultConnection status
kubectl get vaultconnection vault-connection
kubectl describe vaultconnection vault-connection

# Check VaultAuth status
kubectl get vaultauth vault-auth
kubectl describe vaultauth vault-auth

# Check VaultStaticSecret status
kubectl get vaultstaticsecret ghcr-vault-secret
kubectl describe vaultstaticsecret ghcr-vault-secret

# Verify the Kubernetes secret was created
kubectl get secret ghcr-secret
kubectl describe secret ghcr-secret

# Test pulling the image
kubectl delete pod -l app=spring-aot-demo
kubectl get pods -w
```

## Troubleshooting

### Vault pod not starting

```bash
kubectl logs -n vault -l app=vault
kubectl describe pod -n vault -l app=vault
```

### VaultStaticSecret not syncing

```bash
# Check operator logs
kubectl logs -n vault-secrets-operator-system -l app.kubernetes.io/name=vault-secrets-operator

# Check VaultStaticSecret status
kubectl describe vaultstaticsecret ghcr-vault-secret

# Common issues:
# 1. Vault not reachable - check VaultConnection
# 2. Auth failed - check VaultAuth and Vault role configuration
# 3. Secret path wrong - verify path in Vault
# 4. ServiceAccount not found - ensure vault-auth ServiceAccount exists (06-vault-auth-rbac.yaml)
```

### "Permission denied" during authentication

If you see "permission denied" when the VSO tries to authenticate:

```bash
# Test authentication manually
export VAULT_ADDR='http://127.0.0.1:8200'
curl -s -X POST $VAULT_ADDR/v1/auth/kubernetes/login \
  -d '{"role": "spring-aot-demo", "jwt": "'"$(kubectl create token vault-auth -n default --duration=1h)"'"}'

# If you get permission denied, reconfigure Vault kubernetes auth:
kubectl exec -n vault $(kubectl get pod -n vault -l app=vault -o jsonpath='{.items[0].metadata.name}') -- sh -c '
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN=root
vault write auth/kubernetes/config \
    kubernetes_host=https://kubernetes.default.svc:443 \
    kubernetes_ca_cert=@/var/run/secrets/kubernetes.io/serviceaccount/ca.crt \
    token_reviewer_jwt=@/var/run/secrets/kubernetes.io/serviceaccount/token
'

# Recreate the VaultStaticSecret to trigger retry
kubectl delete vaultstaticsecret ghcr-vault-secret
kubectl apply -f k8s/vault/04-ghcr-secret.yaml
```

### Image still not pulling

```bash
# Check if secret was created
kubectl get secret ghcr-secret

# Verify secret content
kubectl get secret ghcr-secret -o yaml

# Check deployment references the secret
kubectl get deployment spring-aot-demo -o yaml | grep -A 2 imagePullSecrets

# Force pod recreation
kubectl rollout restart deployment spring-aot-demo
```

### Access Vault UI (Dev Mode)

```bash
# Port-forward
kubectl port-forward -n vault svc/vault 8200:8200

# Access at http://localhost:8200
# Token: root
```

## Production Considerations

**This setup uses dev-mode Vault which is NOT suitable for production!**

For production:

1. **Use Vault in HA mode** with persistent storage
2. **Enable TLS/SSL** for Vault communication
3. **Use auto-unseal** (AWS KMS, Azure Key Vault, etc.)
4. **Configure proper backup** and disaster recovery
5. **Use separate namespaces** for isolation
6. **Enable audit logging**
7. **Rotate credentials** regularly
8. **Use least-privilege policies**
9. **Monitor Vault metrics** with Prometheus
10. **Consider Vault Enterprise** for advanced features

### Production Vault Installation

```bash
# Install production Vault with Helm
helm install vault hashicorp/vault \
  --namespace vault \
  --create-namespace \
  --set server.ha.enabled=true \
  --set server.ha.replicas=3 \
  --set server.dataStorage.enabled=true \
  --set server.dataStorage.size=10Gi
```

## Clean Up

```bash
# Stop port-forward
pkill -f "port-forward.*vault"

# Delete application resources
kubectl delete -f k8s/vault/06-vault-auth-rbac.yaml
kubectl delete -f k8s/vault/04-ghcr-secret.yaml
kubectl delete -f k8s/vault/03-vault-auth.yaml
kubectl delete -f k8s/vault/02-vault-connection.yaml

# Delete Vault and RBAC
kubectl delete -f k8s/vault/05-vault-rbac.yaml
kubectl delete -f k8s/vault/01-vault-dev.yaml
kubectl delete -f k8s/vault/00-namespace.yaml

# Uninstall Vault Secrets Operator
helm uninstall vault-secrets-operator -n vault-secrets-operator-system
kubectl delete namespace vault-secrets-operator-system
```

## Resources

- [Vault Secrets Operator Documentation](https://developer.hashicorp.com/vault/docs/platform/k8s/vso)
- [Vault Kubernetes Auth](https://developer.hashicorp.com/vault/docs/auth/kubernetes)
- [Vault KV Secrets Engine](https://developer.hashicorp.com/vault/docs/secrets/kv)