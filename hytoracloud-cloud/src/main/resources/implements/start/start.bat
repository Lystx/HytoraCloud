java -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -XX:MaxPermSize=256M -XX:+UnlockExperimentalVMOptions -XX:+UseCompressedOops -XX:-UseAdaptiveSizePolicy -XX:CompileThreshold=100 -Dfile.encoding=UTF-8 -Xmx456M -Xms256m -jar hytoracloud-cloud.jar
PAUSE
