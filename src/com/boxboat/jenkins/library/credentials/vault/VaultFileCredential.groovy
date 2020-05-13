package com.boxboat.jenkins.library.credentials.vault

import com.boxboat.jenkins.library.config.Config
import com.boxboat.jenkins.library.Utils

public class VaultFileCredential extends VaultBaseCredential {

    public String fileKey

    @Override
    def withCredentials(Map configMap=[:], Closure closure) {
        def fileVariable = configMap.get('variable', 'FILE')

        Utils.withTmpFile(configMap){
            def fileContent = getSecret(fileKey)
            def file = Config.pipeline.env[fileVariable]
            Config.pipeline.writeFile(file: file, text: fileContent, encoding: "Utf8")

            def unmaskedEnvList = [
                "${fileVariable}=${file}"
            ]

            Config.pipeline.withEnv(unmaskedEnvList) {
                closure()
            }

        }
    }
}
