resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

//https://github.com/scopt/scopt
libraryDependencies += "com.github.scopt" %% "scopt" % "2.1.0"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.2.0-SNAPSHOT")