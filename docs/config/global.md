# Global Config

All keys from the [Common Config](common.md) are valid in addition to the keys documented here.

Note that to make use of the Vault credentials (ie `credential: !!com.boxboat.jenkins.library.credentials.vault.*`), [dockcmd](https://github.com/boxboat/dockcmd) must be installed on the jenkins agents. This functionality also requires the mask-passwords plugin for Jenkins to be installed.

## deployTargetMap

Map of deployment targets.  Supported deployment target types are:

- `com.boxboat.jenkins.library.deployTarget.KubernetesDeployTarget`
- `com.boxboat.jenkins.library.gcloud.GCloudGKEDeployTarget`

```yaml
deployTargetMap:
  dev01: !!com.boxboat.jenkins.library.deployTarget.KubernetesDeployTarget
    # kubernetes context to use
    contextName: boxboat
    # credential ID with kube config
    credential: kubeconfig-dev
  dev02: !!com.boxboat.jenkins.library.deployTarget.KubernetesDeployTarget
    # kubernetes context to use
    contextName: boxboat
    # Vault credential info (expects a kubeconfig file at the location specified)
    credential: !!com.boxboat.jenkins.library.credentials.vault.VaultFileCredential
      # The vault in the vaultMap to read this credential from
      vault: default
      # The path in vault where this credential is stored
      path: kv/kubeconfig
      # The key at the path that contains the kubernetes config contents
      fileKey: prod03
  gke: !!com.boxboat.jenkins.library.gcloud.GCloudGKEDeployTarget
    # gCloudAccountMap key to reference
    gCloudAccountKey: default
    # GKE cluster name
    name: kube-cluster-name
    # Google Cloud project name
    project: gcloud-project
    # specify either region or zone, not both; use region for regional clusters
    region: us-central1
    # specify either region or zone, not both; use zone for zonal clusters
    zone: us-central1-a
```

## environmentMap

Map of environments.  Environments reference a deployment target.  This way, underlying deployment targets can be switched out.

```yaml
environmentMap:
  dev:
    # deployTargetMap key to reference
    deployTargetKey: dev01
```

## gCloudAccountMap

Map of Google Cloud accounts for use with `GCloudGKEDeployTarget` and `GCloudRegistry`

```yaml
gCloudAccountMap:
  default:
    # name of the service account
    account: service-account@gcloud-project.iam.gserviceaccount.com
    # secret file credential with the service account JSON key
    keyFileCredential: gcloud-key-file-credential
```

## git

Stores the git configuration.  To select an alternate config, set a repo-specific [gitAlternateKey](common.md#gitAlternateKey)

```yaml
git:
  # repository where build versions are written to
  buildVersionsUrl: git@github.com:boxboat/build-versions.git
  # SSH key credential for git service account
  credential: git
  # email that Jenkins commits as
  email: jenkins@boxboat.com
  # regex to convert a git remote to friendly path
  # first capture group is the friendly path
  remotePathRegex: github\.com[:\/]boxboat\/(.*)\.git$
  # string to convert a friendly path to a git remote
  # {{ path }} is replaced with the git path
  remoteUrlReplace: git@github.com:boxboat/{{ path }}.git
  # string to display a Git branch URL
  # {{ path }} is replaced with the git path
  # {{ branch }} is replaced with the git branch
  branchUrlReplace: https://github.com/boxboat/{{ path }}/tree/{{ branch }}
  # string to display a Git commit URL
  # {{ path }} is replaced with the git path
  # {{ hash }} is replaced with the git commit hash
  commitUrlReplace: https://github.com/boxboat/{{ path }}/commit/{{ hash }}
  # alternate git configurations
  # buildVersionsUrl, credential, and email are not allowed
  # in alternate git configurations
  gitAlternateMap:
    gitlab:
      remotePathRegex: gitlab\.com[:\/]boxboat\/(.*)\.git$
      remoteUrlReplace: git@gitlab.com:boxboat/{{ path }}.git
      branchUrlReplace: https://gitlab.com/boxboat/{{ path }}/tree/{{ branch }}
      commitUrlReplace: https://gitlab.com/boxboat/{{ path }}/commit/{{ hash }}
```

## notifyTargetMap

Map of notification targets.  Supported notification target types are:

- `com.boxboat.jenkins.library.notify.SlackWebHookNotifyTarget`
  - For use with the [Slack Incoming Webhooks App](https://boxboat.slack.com/apps/A0F7XDUAZ-incoming-webhooks?next_id=0)
  - Jenkins Credential referenced in `credential` is a Secret Text credential with the full webhook URL
- `com.boxboat.jenkins.library.notify.SlackJenkinsAppNotifyTarget`
  - For use with the [Slack Jenkns CI App](https://boxboat.slack.com/apps/A0F7VRFKN-jenkins-ci?next_id=0)
  - Use `channel` to override channel
```yaml
notifyTargetMap:
  default: !!com.boxboat.jenkins.library.notify.SlackWebHookNotifyTarget
    credential: slack-webhook-url
  prod: !!com.boxboat.jenkins.library.notify.SlackWebHookNotifyTarget
     # Vault credential info (expects a slack webhook url at the location specified)
    credential: !!com.boxboat.jenkins.library.credentials.vault.VaultStringCredential
      # The vault in the vaultMap to read this credential from
      vault: default
      # The path in vault where this credential is stored
      kv/slack-prod-credentials
      # The key at the path that contains the slack webhook url
      stringKey: webhook-url
  jenkins-success: !!com.boxboat.jenkins.library.notify.SlackJenkinsAppNotifyTarget
    channel: "#jenkins-success"
  jenkins-failure: !!com.boxboat.jenkins.library.notify.SlackJenkinsAppNotifyTarget
    channel: "#jenkins-failure"
```

## registryMap

Map of Docker registries.  Supported registry types are:

Supported deployment target types are:

- `com.boxboat.jenkins.library.docker.Registry` (the default)
- `com.boxboat.jenkins.library.gcloud.GCloudRegistry`

```yaml
registryMap:
  default:
    scheme: https
    host: dtr.boxboat.com
    credential: registry
    # string to display a registry image URL
    # {{ path }} is replaced with the image path
    # {{ tag }} is replaced with the image tag
    imageUrlReplace: https://dtr.boxboat.com/repositories/{{ path }}/{{ tag }}/linux/amd64/layers
  dev:
    scheme: https
    host: harbor.boxboat.com
    # Vault credential info (expects username and password keys at the location specified)
    credential: !!com.boxboat.jenkins.library.credentials.vault.VaultUsernamePasswordCredential
      # The vault in the vaultMap to read this credential from
      vault: default
      # The path in vault where this credential is stored
      path: kv/harbor-dev-credentials
      # The key at the path in vault that stores the username for this registry
      usernameKey: username
      # The key at the path in vault that stores the password for this registry
      passwordKey: password
    imageUrlReplace: https://harbor.boxboat.com/harbor/projects/1/repositories/{{ path }}/tags/{{ tag }}
  gcr: !!com.boxboat.jenkins.library.gcloud.GCloudRegistry
    scheme: https
    host: gcr.io
    # Google Cloud project
    namespace: gcloud-project
    # gCloudAccountMap key to reference
    gCloudAccountKey: default
```

## vaultMap

Map of Hashicorp Vault endpoints.  Either (`roleIdCredential` and `secretIdCredential`) or (`tokenCredential`) are required.

```yaml
vaultMap:
  default:
    # vault KV version
    kvVersion: 1
    # secret text credential ID with roleId
    roleIdCredential: vault-role-id
    # secret text credential ID with secretId
    secretIdCredential: vault-secret-id
    # secret text credential ID
    tokenCredential: vault-token
    # full URL to vault
    url: http://localhost:8200
```

## repo

Repository configurations that are applied globally to all repositories.

- `repo.common`: [Common Configuration](common.md)
- `repo.build`: [Build Configuration](build.md)
- `repo.promote`: [Promote Configuration](promote.md)
- `repo.deploy`: [Deploy Configuration](deploy.md)

```yaml
repo:
  common:
    vaultKey: default
  promote:
    promotionMap:
      prod:
        event: commit/master
        promoteToEvent: tag/release
  deploy:
    deploymentMap:
      dev:
        environmentKey: dev
        event: commit/master
        trigger: true
```
