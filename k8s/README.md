# Kubernetes Deployment

This directory contains Kubernetes manifests for deploying the Spring Boot AOT demo application.

## Prerequisites

- Kubernetes cluster (minikube, kind, or cloud provider)
- kubectl configured to access your cluster
- NGINX Ingress Controller (for ingress)

## Manifests

- `deployment.yaml` - Deployment with 2 replicas, health probes, and resource limits
- `service.yaml` - ClusterIP service exposing port 8080
- `configmap.yaml` - Configuration for application properties
- `ingress.yaml` - Ingress for external access (optional)

## Quick Start

### Deploy All Resources

```bash
kubectl apply -f k8s/
```

### Deploy Individual Resources

```bash
# Apply in order
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml  # Optional
```

## Verify Deployment

```bash
# Check deployment status
kubectl get deployments
kubectl get pods

# Check service
kubectl get services

# View logs
kubectl logs -l app=spring-aot-demo

# Describe deployment
kubectl describe deployment spring-aot-demo
```

## Access the Application

### Option 1: Using Port Forward (Recommended for Local Development)

The simplest and most reliable way to access the application locally:

```bash
kubectl port-forward service/spring-aot-demo 8080:8080
```

Then access at: `http://localhost:8080`

This method works immediately without any additional configuration.

### Option 2: Using Ingress with Minikube

The ingress configuration requires NGINX Ingress Controller and `minikube tunnel` to work properly.

#### Prerequisites

1. **Install NGINX Ingress Controller** (if not already installed):

   ```bash
   kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.1/deploy/static/provider/cloud/deploy.yaml
   ```
2. **Add hostname to `/etc/hosts`**:

   ```bash
   echo "127.0.0.1 spring-aot-demo.local" | sudo tee -a /etc/hosts
   ```

#### Start Minikube Tunnel

The ingress requires `minikube tunnel` to expose the LoadBalancer service. Run this in a **separate terminal window**:

```bash
minikube tunnel
```

**Important Notes:**
- You will be prompted for your sudo password
- Keep this terminal window open - the tunnel must stay running
- The tunnel runs in the foreground and requires interactive input
- You should see output like: `Tunnel successfully started`

#### Access the Application

Once the tunnel is running:

```bash
# Test the ingress
curl http://spring-aot-demo.local/actuator/health

# Or access in browser
open http://spring-aot-demo.local
```

#### Troubleshooting Ingress

If ingress doesn't work:

```bash
# Check ingress status
kubectl get ingress spring-aot-demo

# Check if LoadBalancer has external IP
kubectl get svc -n ingress-nginx ingress-nginx-controller

# Verify tunnel is running and bound to port 80
sudo lsof -i :80

# Check ingress controller logs
kubectl logs -n ingress-nginx -l app.kubernetes.io/component=controller --tail=50
```

If still not working, use **Option 1 (Port Forward)** instead.

## Test Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness

# Products API
curl http://localhost:8080/api/products

# Metrics
curl http://localhost:8080/actuator/metrics
```

## Scaling

```bash
# Scale to 3 replicas
kubectl scale deployment spring-aot-demo --replicas=3

# Auto-scale based on CPU
kubectl autoscale deployment spring-aot-demo --min=2 --max=5 --cpu-percent=80
```

## Update Deployment

```bash
# Update to new image version
kubectl set image deployment/spring-aot-demo \
  spring-aot-demo=ghcr.io/vgrente/spring-aot-demo:new-version

# Or edit deployment
kubectl edit deployment spring-aot-demo

# Restart deployment
kubectl rollout restart deployment spring-aot-demo
```

## Rollback

```bash
# View rollout history
kubectl rollout history deployment spring-aot-demo

# Rollback to previous version
kubectl rollout undo deployment spring-aot-demo

# Rollback to specific revision
kubectl rollout undo deployment spring-aot-demo --to-revision=2
```

## Monitoring

```bash
# Watch pod status
kubectl get pods -l app=spring-aot-demo -w

# View events
kubectl get events --sort-by='.lastTimestamp'

# Check resource usage
kubectl top pods -l app=spring-aot-demo
```

## Clean Up

```bash
# Delete all resources
kubectl delete -f k8s/

# Or delete individually
kubectl delete deployment spring-aot-demo
kubectl delete service spring-aot-demo
kubectl delete configmap spring-aot-demo-config
kubectl delete ingress spring-aot-demo
```

## Configuration

### Resource Limits

The deployment is configured with:
- **Requests**: 256Mi memory, 250m CPU
- **Limits**: 512Mi memory, 500m CPU

Adjust in `deployment.yaml` based on your needs.

### Health Probes

- **Startup Probe**: Checks if app has started (max 60 seconds)
- **Liveness Probe**: Restarts pod if app becomes unresponsive
- **Readiness Probe**: Removes pod from service if not ready

### Environment Variables

Set in `deployment.yaml`:
- `SPRING_PROFILES_ACTIVE`: Active Spring profile
- `JAVA_TOOL_OPTIONS`: JVM options

## Troubleshooting

### Pod not starting

```bash
# Check pod events
kubectl describe pod <pod-name>

# View logs
kubectl logs <pod-name>

# Get previous logs if crashed
kubectl logs <pod-name> --previous
```

### Image pull errors

Ensure you have access to the GitHub Container Registry:

```bash
# Create image pull secret
kubectl create secret docker-registry ghcr-secret \
  --docker-server=ghcr.io \
  --docker-username=<github-username> \
  --docker-password=<github-token>

# Reference in deployment.yaml
spec:
  imagePullSecrets:
  - name: ghcr-secret
```

### Service not accessible

```bash
# Check endpoints
kubectl get endpoints spring-aot-demo

# Test from within cluster
kubectl run -it --rm debug --image=curlimages/curl --restart=Never -- \
  curl http://spring-aot-demo:8080/actuator/health
```

## Production Considerations

1. **Database**: Replace H2 with production database (PostgreSQL, MySQL)
2. **Secrets**: Use Kubernetes Secrets for sensitive data
3. **Resource Limits**: Adjust based on actual usage
4. **Monitoring**: Integrate with Prometheus/Grafana
5. **Logging**: Configure centralized logging (ELK, Loki)
6. **Network Policies**: Restrict pod-to-pod communication
7. **Pod Disruption Budget**: Ensure high availability during updates
8. **Affinity Rules**: Control pod placement across nodes

