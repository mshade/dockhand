package com.boxboat.jenkins.test.pipeline

import com.lesfurets.jenkins.unit.BasePipelineTest
import static com.lesfurets.jenkins.unit.global.lib.LibraryConfiguration.library
import static com.lesfurets.jenkins.unit.global.lib.LocalSource.localSource

abstract class PipelineBase extends BasePipelineTest {

    final String scriptBase = 'test-resources/com/boxboat/jenkins/test/pipeline/'

    void setUp() throws Exception {
        super.setUp()
        helper.registerAllowedMethod('file', [Map.class], null)
        helper.registerAllowedMethod('fileExists', [String.class], { fileName ->
            return false
        })
        helper.registerAllowedMethod('pwd', [], {
            return System.getProperty('java.io.tmpdir')
        })
        helper.registerAllowedMethod('error', [String.class], { error ->
            throw new Exception(error)
        })
        helper.registerAllowedMethod('libraryResource', [String.class], { fileName ->
            return new File("resources/${fileName}").getText('Utf8')
        })
        helper.registerAllowedMethod('libraryResource', [Map.class], { config ->
            return config.resource
        })
        helper.registerAllowedMethod('build', [Map.class], null)
        helper.registerAllowedMethod('httpRequest', [Map.class], null)
        helper.registerAllowedMethod('writeFile', [Map.class], null)
        helper.registerAllowedMethod('sshUserPrivateKey', [Map.class], null)
        helper.registerAllowedMethod('throttle', [List.class, Closure.class], {
            category, next -> next()
        })
        helper.registerAllowedMethod('usernamePassword', [Map.class], null)
        helper.registerAllowedMethod('withKubeConfig', [Map.class, Closure.class], {
            config, next -> next()
        })
        helper.registerAllowedMethod('withEnv', [List.class, Closure.class], {
            env, next -> next()
        })
        helper.registerAllowedMethod('slackSend', [Map.class], null)

        binding.setVariable('env', [
                'sshKey'         : 'sshKey',
                'username'       : 'username',
                'BRANCH_NAME'    : 'master',
                'JOB_NAME'       : 'test/master',
                'WORKSPACE'      : System.getProperty('java.io.tmpdir'),
                'GRADLE_TEST_ENV': 'true',
        ])
        binding.setVariable('scm', [:])
        binding.setVariable('params', [:])

        def sharedLib = 'dist'
        def library = library()
                .name('jenkins-shared-library')
                .retriever(localSource(sharedLib))
                .targetPath(sharedLib)
                .defaultVersion('jenkins')
                .allowOverride(true)
                .implicit(false)
                .build()
        helper.registerSharedLibrary(library)
    }

}
