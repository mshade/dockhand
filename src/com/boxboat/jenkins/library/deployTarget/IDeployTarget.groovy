package com.boxboat.jenkins.library.deployTarget

interface IDeployTarget {

    void withCredentials(Map configMap, Closure closure)

}
