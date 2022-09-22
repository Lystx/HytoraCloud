

#if enabled "Lystx" has all permisisons
var cloud.hytora.launcher.devmode = false

if ($cloud.hytora.launcher.devmode$) -> {
    print "DevMode is activated!"
} else -> {
    print "DevMode is not activated!"
}

#first including repos because dependencies need a valid repo
run includeRepositories();

#then including dependencies
run includeDependencies();


#Including repositories
Task includeRepositories() -> {
    repo ossrh https://oss.sonatype.org/content/repositories/snapshots
    repo mvn https://repo1.maven.org/maven2

} : Void

#Including dependencies
Task includeDependencies() -> {
    dependency org.fusesource.jansi jansi 2.4.0
    dependency me.tongfei progressbar 0.5.5
    dependency mysql mysql-connector-java 8.0.27
    dependency org.mongodb mongodb-driver-core 4.2.0-beta1
    dependency org.mongodb mongodb-driver-sync 4.2.0-beta1
    dependency org.fusesource.jansi jansi 2.4.0
    dependency org.zeroturnaround zt-exec 1.12
    dependency dnsjava dnsjava 3.4.0
    dependency commons-io commons-io 2.11.0
    dependency org.reflections reflections 0.10.2
    dependency com.google.code.gson gson 2.8.9
    dependency com.google.guava guava 31.0.1-jre
    dependency io.netty netty-all 4.1.72.Final
    dependency io.netty netty-transport 4.1.72.Final
    dependency io.netty netty-transport-classes-epoll 4.1.72.Final
    dependency io.netty netty-codec 4.1.72.Final
    dependency org.jline jline 3.20.0

} : Void


