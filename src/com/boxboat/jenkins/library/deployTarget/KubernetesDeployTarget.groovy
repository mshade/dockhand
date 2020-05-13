package com.boxboat.jenkins.library.deployTarget

import com.boxboat.jenkins.library.config.BaseConfig
import com.boxboat.jenkins.library.config.Config
import com.boxboat.jenkins.library.credentials.vault.VaultFileCredential
import com.boxboat.jenkins.library.Utils

class KubernetesDeployTarget extends BaseConfig<KubernetesDeployTarget> implements IDeployTarget, Serializable {

    String caCertificate

    String contextName

    Object credential

    String serverUrl

    @Override
    void withCredentials(Map configMap = [:], Closure closure) {
        if (credential instanceof VaultFileCredential) {
            credential.withCredentials(['variable': 'KUBECONFIG']){
                if( contextName && Utils.hasCmd('kubectl') ){
                    Config.pipeline.sh """
                        kubectl config use-context ${contextName}
                    """
                }
                closure()
            }
        }else{
            Config.pipeline.withKubeConfig(
                    credentialsId: credential,
                    caCertificate: caCertificate,
                    serverUrl: serverUrl,
                    contextName: contextName
            ) {
                closure()
            }
        }
    }

}
