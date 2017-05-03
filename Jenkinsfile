#! groovy
@Library('pipeline-build') _

node('linux && ant && eclipse && jdk && vncserver') {
	try {
		stage('Checkout') {
			checkout scm
		}

		def studio3Repo = "file://${env.WORKSPACE}/studio3-core/dist/"
		def studio3TestRepo = "file://${env.WORKSPACE}/studio3-core/dist-tests/"
		def rubyRepo = "file://${env.WORKSPACE}/dist/"

		buildPlugin {
			dependencies = ['studio3-core': '../studio3']
			builder = 'com.aptana.radrails.build'
			properties = ['studio3.p2.repo': studio3Repo]
		}

		testPlugin {
			builder = 'com.aptana.radrails.tests.build'
			properties = [
				'studio3.p2.repo': studio3Repo,
				'studio3.test.p2.repo': studio3TestRepo,
				'radrails.p2.repo': rubyRepo
			]
			classPattern = 'eclipse/plugins'
			inclusionPattern = 'com.aptana.deploy.capistrano_*.jar, com.aptana.deploy.engineyard_*.jar, com.aptana.deploy.heroku_*.jar, com.aptana.editor.erb_*.jar, com.aptana.editor.haml_*.jar, com.aptana.editor.ruby*.jar, com.aptana.editor.sass_*.jar, com.aptana.ruby.*.jar, org.radrails.rails.*.jar'
			exclusionPattern = '**/tests/**/*.class,**/*Test*.class,**/Messages.class,com.aptana.*.tests*.jar'
		}

		// If not a PR, trigger downstream builds for same branch
		if (!env.BRANCH_NAME.startsWith('PR-')) {
			build job: "../studio3-rcp/${env.BRANCH_NAME}", wait: false
		}
	} catch (e) {
		// if any exception occurs, mark the build as failed
		currentBuild.result = 'FAILURE'
		throw e
	} finally {
		step([$class: 'WsCleanup', notFailBuild: true])
	}
} // end node
