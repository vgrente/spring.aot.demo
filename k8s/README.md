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

### Using Port Forward

```bash
kubectl port-forward service/spring-aot-demo 8080:8080
```

Then access at: `http://localhost:8080`

### Using Ingress (if configured)

Add to `/etc/hosts`:
```
<INGRESS_IP> spring-aot-demo.local
```

Access at: `http://spring-aot-demo.local`

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