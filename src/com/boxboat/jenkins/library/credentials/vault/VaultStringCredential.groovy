package com.boxboat.jenkins.library.credentials.vault

import com.boxboat.jenkins.library.config.Config

public class VaultStringCredential extends VaultBaseCredential {

    public String stringKey

    @Override
    def withCredentials(Map configMap=[:], Closure closure) {
        def stringVariable = configMap.get('variable', 'STRING')
        def string = getSecret(fileKey)

        def maskedPairs = [
                [
                        var     : stringVariable,
                        password: string,
                ],
        ]
        def maskedEnvList = maskedPairs.collect { "${it.var}=${it.password}" }
        Config.pipeline.wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: maskedPairs]) {
            Config.pipeline.withEnv(maskedEnvList) {
                closure()
            }
        }
    }
}
