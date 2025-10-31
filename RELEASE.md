# Release Process

This document describes the release workflow for creating versioned native image releases.

## Overview

The project uses semantic versioning (MAJOR.MINOR.PATCH) for releases:
- **MAJOR** version for incompatible API changes
- **MINOR** version for new features (backwards compatible)
- **PATCH** version for bug fixes (backwards compatible)

## Workflows

### 1. Development Builds (Automatic)

**Workflow**: `.github/workflows/build-native.yml`
**Trigger**: Push to `main` branch
**Output**:
- `ghcr.io/YOUR_ORG/spring-aot-demo:0.0.1-SNAPSHOT`
- `ghcr.io/YOUR_ORG/spring-aot-demo:dev`

This workflow runs automatically on every push to main and builds SNAPSHOT versions for testing.

### 2. Release Builds (Tag-triggered)

**Workflow**: `.github/workflows/release.yml`
**Trigger**: Push Git tag matching `v*.*.*` pattern
**Output**: Multiple versioned tags

## How to Create a Release

### Option 1: Using Git Tags (Recommended)

1. **Determine the version number** using semantic versioning:

   ```bash
   # For a bug fix (e.g., 1.0.0 -> 1.0.1)
   VERSION=1.0.1

   # For a new feature (e.g., 1.0.1 -> 1.1.0)
   VERSION=1.1.0

   # For breaking changes (e.g., 1.1.0 -> 2.0.0)
   VERSION=2.0.0
   ```
2. **Create and push the tag**:

   ```bash
   git tag -a v${VERSION} -m "Release version ${VERSION}"
   git push origin v${VERSION}
   ```
3. **Monitor the workflow**:
   - Go to Actions tab in GitHub
   - Watch the "Release Native Image" workflow
   - Wait for completion (~10-15 minutes)
4. **Verify the release**:
   - Check the Releases page for the new release
   - Verify Docker images are available:

     ```bash
     docker pull ghcr.io/YOUR_ORG/spring-aot-demo:${VERSION}
     docker pull ghcr.io/YOUR_ORG/spring-aot-demo:latest
     ```

### Option 2: Manual Workflow Dispatch

If you need to create a release without a tag:

1. Go to Actions tab in GitHub
2. Select "Release Native Image" workflow
3. Click "Run workflow"
4. Enter the version number (without 'v' prefix)
5. Click "Run workflow"

## Release Artifacts

Each release creates:

### Docker Images

For version `1.2.3`, the following images are created:

- `ghcr.io/YOUR_ORG/spring-aot-demo:1.2.3` - Full version
- `ghcr.io/YOUR_ORG/spring-aot-demo:1.2` - Minor version
- `ghcr.io/YOUR_ORG/spring-aot-demo:1` - Major version
- `ghcr.io/YOUR_ORG/spring-aot-demo:latest` - Latest stable

Each tag includes multi-arch support (amd64 and arm64).

### GitHub Release

- Release notes with categorized changelog (Features, Bug Fixes, Other Changes)
- Image metadata JSON files for both architectures
- Links to Docker images

## Commit Message Convention

For better changelogs, use conventional commit messages:

- `feat: Add user authentication` - New features
- `fix: Resolve startup crash on ARM` - Bug fixes
- `chore: Update dependencies` - Maintenance tasks
- `docs: Update API documentation` - Documentation
- `refactor: Simplify database layer` - Code refactoring
- `test: Add integration tests` - Tests
- `perf: Improve startup time` - Performance

Example:

```bash
git commit -m "feat: add product search endpoint"
git commit -m "fix: resolve memory leak in background job"
```

## Image Tagging Strategy

### Development

- `dev` - Always points to latest main branch build
- `0.0.1-SNAPSHOT` - Development snapshot version

### Production Releases

- `1.2.3` - Exact version (immutable, recommended for production)
- `1.2` - Latest patch version of 1.2.x (auto-updated)
- `1` - Latest minor version of 1.x.x (auto-updated)
- `latest` - Latest stable release (auto-updated)

## Example Release Workflow

### Releasing a Bug Fix (1.0.0 -> 1.0.1)

```bash
# Ensure you're on main and up to date
git checkout main
git pull origin main

# Create and push the tag
git tag -a v1.0.1 -m "Release version 1.0.1 - Fix database connection issue"
git push origin v1.0.1

# Wait for GitHub Actions to complete
# Check https://github.com/YOUR_ORG/YOUR_REPO/actions

# Verify the release
docker pull ghcr.io/YOUR_ORG/spring-aot-demo:1.0.1
```

### Releasing a New Feature (1.0.1 -> 1.1.0)

```bash
# Ensure you're on main and up to date
git checkout main
git pull origin main

# Create and push the tag
git tag -a v1.1.0 -m "Release version 1.1.0 - Add product export feature"
git push origin v1.1.0

# Wait for GitHub Actions to complete
# The release workflow will automatically create a GitHub release
```

## Troubleshooting

### Build Fails

- Check the Actions tab for error logs
- Common issues:
  - Test failures
  - Native image compilation errors
  - Memory issues during build

### Tag Already Exists

```bash
# Delete local tag
git tag -d v1.0.1

# Delete remote tag
git push origin :refs/tags/v1.0.1

# Recreate the tag
git tag -a v1.0.1 -m "Release version 1.0.1"
git push origin v1.0.1
```

### Image Not Published

- Check workflow logs for registry authentication errors
- Verify GITHUB_TOKEN has `packages:write` permission
- Ensure repository settings allow package publishing

## Best Practices

1. **Always test before releasing**: Ensure all tests pass on main before creating a release tag
2. **Use semantic versioning**: Follow semver principles for version numbers
3. **Write meaningful release notes**: The changelog is auto-generated from commits
4. **Pin exact versions in production**: Use `1.2.3` instead of `latest` in production
5. **Keep development and releases separate**: Development uses `dev` tag, releases use versioned tags
6. **Document breaking changes**: Include migration guide in commit messages for major versions

## Rollback

To rollback to a previous version:

```bash
# Deploy previous version
docker pull ghcr.io/YOUR_ORG/spring-aot-demo:1.0.0
docker run ghcr.io/YOUR_ORG/spring-aot-demo:1.0.0
```

Note: The `latest` tag will still point to the newest release. Use specific version tags for rollbacks.
