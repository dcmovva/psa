import AssemblyKeys._

assemblySettings

name := "TwitterProcessor"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
		"org.apache.hadoop" % "hadoop-client" % "2.4.0",
        "org.apache.spark" % "spark-core_2.10" % "1.1.0" exclude("org.apache.hadoop", "hadoop-yarn-common") exclude("org.eclipse.jetty.orbit", "javax.servlet") exclude("org.eclipse.jetty.orbit", "javax.transaction") exclude("org.eclipse.jetty.orbit", "javax.activation") exclude("org.eclipse.jetty.orbit", "javax.mail.glassfish") exclude("commons-beanutils", "commons-beanutils-core") exclude("commons-collections", "commons-collections") exclude("commons-logging", "commons-logging") exclude("com.esotericsoftware.minlog", "minlog") exclude("org.mortbay.jetty", "servlet-api")  exclude("org.mortbay.jetty", "jsp-api") exclude("org.mortbay.jetty", "jsp-api-2.1") exclude("org.mortbay.jetty", "jsp-2.1"),
        "org.apache.spark" %% "spark-streaming-twitter" % "1.1.0" % "provided" exclude("org.apache.hadoop", "hadoop-client"),
        "org.apache.hbase" % "hbase" % "0.98.6-hadoop2" ,
        "org.apache.hbase" % "hbase-client" % "0.98.6-hadoop2" ,
        "org.apache.hbase" % "hbase-common" % "0.98.6-hadoop2" ,
        "org.apache.hbase" % "hbase-server" % "0.98.6-hadoop2",
        "com.google.code.gson" % "gson" % "2.2",
        "org.twitter4j" % "twitter4j-core" % "3.0.3",
        "edu.stanford.nlp" % "stanford-corenlp" % "3.4.1" artifacts (Artifact("stanford-corenlp", "models"), Artifact("stanford-corenlp")))
        
        

lazy val app = Project("approxstrmatch", file("approxstrmatch"),
    settings = assemblySettings ++ Seq(
    mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
    {
    	case PathList("META-INF", "maven","jline","jline", "pom.properties" ) => MergeStrategy.discard
    	case PathList("META-INF", "maven","jline","jline", "pom.xml" ) => MergeStrategy.discard
    	case PathList("org", "mortbay", "jsp-api", xs @ _*) => MergeStrategy.discard
        case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.last
        case PathList("javax", "transaction", xs @ _*)     => MergeStrategy.first
        case PathList("javax", "mail", xs @ _*)     => MergeStrategy.first
        case PathList("javax", "activation", xs @ _*)     => MergeStrategy.first
        case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
        case "application.conf" => MergeStrategy.concat
        case "unwanted.txt"     => MergeStrategy.discard
        case "META-INF/jboss-beans.xml" => MergeStrategy.first
         case "META-INF/MANIFEST.MF" => MergeStrategy.first
        case _ => MergeStrategy.deduplicate
        }
    })
  )
  
  ivyXML :=
  <dependencies>
    <exclude org="org.slf4j" module="slf4j-simple"/>
    <exclude org="org.slf4j"  module="log4j-over-slf4j"/>
    <exclude org="org.apache.hadoop"  module="hadoop-yarn-common"/>
    <exclude org="org.apache.hadoop"  module="hadoop-yarn-api"/>
    <exclude org="commons-logging" module="commons-logging"/>
    <exclude org="ch.qos.logback" module="logback-classic"/>
    <override org="commons-daemon" module="commons-daemon" rev="1.0.15"/>
    <exclude org="commons-beanutils" module="commons-beanutils"/>
    <exclude org="commons-beanutils" module="commons-beanutils-core"/>
    <override org="joda-time" module="joda-time" rev="2.3"/>
    <override org="org.joda" module="joda-convert" rev="1.4"/>
    <exclude org="org.jruby" module="jruby-complete"/>
    <exclude org="org.eclipse.jetty.orbit" module="javax.servlet"/>
    <exclude org="org.mortbay.jetty" module="servlet-api-2.5"/>
    <exclude org="org.mortbay.jetty" module="jsp-api-2.1"/>
    <exclude org="org.jboss.netty" module="netty"/>
    <exclude org="tomcat" module="jasper-compiler"/>
    <exclude org="tomcat" module="jasper-runtime"/>
    <exclude org="asm" module="asm"/>
     <exclude org="com.sun.jersey" module="jersey-core"/>
     <exclude org="com.sun.jersey" module="jersey-json"/>
     <exclude org="com.sun.jersey" module="jersey-server"/>
    <exclude org="com.esotericsoftware.minlog" module="minlog"/>
    <exclude org="org.sonatype.sisu.inject" module="cglib"/>
  </dependencies>


