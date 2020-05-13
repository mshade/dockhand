package com.boxboat.jenkins.library.credentials

interface ICredential {
    def withCredentials(Map configMap, Closure closure)
}
