package com.boxboat.jenkins.library.credentials.vault

import com.boxboat.jenkins.library.config.Config

public class VaultUsernamePasswordCredential extends VaultBaseCredential {

    public String usernameKey

    public String passwordKey

    @Override
    def withCredentials(Map configMap=[:], Closure closure) {
        def username = getSecret(usernameKey)
        def password = getSecret(passwordKey)

        def usernameVariable = configMap.get('usernameVariable', 'USERNAME')
        def passwordVariable = configMap.get('passwordVariable', 'PASSWORD')

        def unmaskedEnvList = [
                "${usernameVariable}=${username}"
        ]
        def maskedPairs = [
                [
                        var     : passwordVariable,
                        password: password,
                ],
        ]
        def maskedEnvList = maskedPairs.collect { "${it.var}=${it.password}" }
        Config.pipeline.wrap([$class: 'MaskPasswordsBuildWrapper', varPasswordPairs: maskedPairs]) {
            Config.pipeline.withEnv(unmaskedEnvList + maskedEnvList) {
                closure()
            }
        }
    }
}
