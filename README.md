# Run UT 
# export PATH=$PATH:/opt/gradle/gradle-7.6.1/bin
# gradle -v
# gradle wrapper
# ./gradlew test



# Run Java Main Testing
# javac -cp src -d out src/MainTestingFunction.java src/model/Goal.java src/model/GoalType.java src/model/Task.java
# java -cp out MainTestingFunction


./gradlew run



javac -cp "src/main/java" -d out src/main/java/Main.java src/main/java/model/Goal.java src/main/java/model/GoalType.java src/main/java/model/Task.java
java -cp out Main





export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
export PATH=$JAVA_HOME/bin:$PATH
java -version


export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
java -version
