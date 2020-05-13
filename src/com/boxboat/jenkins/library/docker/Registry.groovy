package com.boxboat.jenkins.library.docker

import com.boxboat.jenkins.library.config.BaseConfig
import com.boxboat.jenkins.library.config.Config
import com.boxboat.jenkins.library.credentials.ICredential
import com.boxboat.jenkins.library.credentials.vault.VaultUsernamePasswordCredential
import com.boxboat.jenkins.library.Utils

class Registry extends BaseConfig<Registry> implements Serializable {

    String scheme

    String host

    String namespace

    Object credential

    String imageUrlReplace

    def getRegistryImageUrl(String path, String tag = "latest") {
        if (!imageUrlReplace || !path) {
            return ""
        }
        return imageUrlReplace.replaceFirst(/(?i)\{\{\s+path\s+\}\}/, path).replaceFirst(/(?i)\{\{\s+tag\s+\}\}/, tag)
    }

    def getRegistryUrl() {
        return "${scheme}://${host}"
    }

    def makeDockerConfig(def username, def password, def dir="~/.docker"){
        def fileName = "${dir}/config.json"
        def reg = ["auths" :[
                "${getRegistryUrl()}": [
                    "auth": "${username}:${password}".bytes.encodeBase64().toString()
        ]]]

        if( Config.pipeline.fileExists(fileName) ){
            def data = Config.pipeline.readJSON(file: fileName)
            reg['auths'] = data['auths'] + reg['auths']
        }
        Config.pipeline.writeJSON(file: fileName, json: reg)
    }

    def withCredentials(Map configMap = [:], Closure closure) {
         // Sets value if unset
        def usernameVariable = configMap.get('usernameVariable', 'REGISTRY_USERNAME')
        def passwordVariable = configMap.get('passwordVariable', 'REGISTRY_PASSWORD')

        def credClosure = {
            Utils.withTmpDir(['variable': 'DOCKER_CONFIG']){
                def username = Config.pipeline.env[usernameVariable]
                def password = Config.pipeline.env[passwordVariable]

                if(Utils.hasCmd('docker')){
                    Config.pipeline.sh """
                        echo -n "\${REGISTRY_PASSWORD}" | docker login -u "\${REGISTRY_USERNAME}" --password-stdin "${getRegistryUrl()}"
                    """
                }else{
                    makeDockerConfig(username, password, Config.pipeline.env['DOCKER_CONFIG'])
                }
                closure()
            }
        }

        if (credential instanceof VaultUsernamePasswordCredential) {
            credential.withCredentials(configMap) {
                credClosure()
            }
        } else {
            Config.pipeline.withCredentials([Config.pipeline.usernamePassword(
                    credentialsId: credential,
                    usernameVariable: usernameVariable,
                    passwordVariable: passwordVariable,
            )]) {
                credClosure()
            }
        }
    }

}
