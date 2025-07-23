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