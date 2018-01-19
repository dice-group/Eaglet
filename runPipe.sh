mvn clean install -DskipTests
mvn -e exec:java -Dexec.mainClass="org.aksw.dice.eaglet.main.RunPipeLine" -Dexec.args="-n testdata -f /Users/Kunal/workspace/Eaglet/eaglet_data/abc.ttl"  >

