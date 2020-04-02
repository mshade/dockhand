package com.boxboat.jenkins.library.config

import com.boxboat.jenkins.library.docker.Image
import com.boxboat.jenkins.library.docker.Registry
import com.boxboat.jenkins.library.event.EventRegistryKey
import com.boxboat.jenkins.library.notify.NotifyConfig

class CommonConfigBase<T> extends BaseConfig<T> {

    String defaultBranch

    List<EventRegistryKey> eventRegistryKeys

    String gitAlternateKey

    List<Image> images

    NotifyConfig notify

    String vaultKey

    List<Registry> getEventRegistries(String event) {
        List<Registry> registries = []
        eventRegistryKeys?.each { EventRegistryKey eventRegistryKey ->
            if (eventRegistryKey.event) {
                if (event == eventRegistryKey.event) {
                    registries.add(Config.global.getRegistry(eventRegistryKey.registryKey))
                }
            } else if (eventRegistryKey.eventRegex) {
                def matcher = event =~ eventRegistryKey.eventRegex
                if (matcher.matches()) {
                    registries.add(Config.global.getRegistry(eventRegistryKey.registryKey))
                }
            }
        }
        return registries
    }

}
