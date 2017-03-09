# AWS lambda hands-on

- A kinesis producer that generates random integers and puts them in the source stream of the lambda function


## Deploying the example lambda function
   
1. Install maven

```
sudo apt install maven
```

2. Create the jar


```
mvn package
```

3. Launch the producer


```
java -jar kinesis-producer <stream_name> <region>
```
