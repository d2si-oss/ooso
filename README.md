# Serverless MapReduce 

### The library [serverless-mapreduce](serverless-mapreduce)
#### 1. Purpose of the library

This is a serverless implementation of the MapReduce algorithm. 
It is based on managed cloud services, Amazon Simple Storage Service and AWS Lambda and is mainly an alternative to standard ad-hoc querying and batch processing tools.

This library has many advantages :
- No need to learn complex technologies
- No server management
- Pay for what you consume

#### 2. Architecture
![alt text](images/MyArchitecture.png "Architecture")
The workflow of the library is as follows:
1. A driver function lists data splits from the input bucket
2. It then creates a thread pool of synchronized mapper functions
3. Each mapper processes a batch of the input splits and puts the result in an intermediary bucket
4. Once all the mappers finished, the driver launches the first reducers coordinator
5. The coordinator is responsible of recursively launching the reducers until there is only one output file

## [Usage example](example-project)

#### 1. Projet structure

The [example-project](example-project) directory contains basic mandatory structure for any project using the framework.
The structure is as follows :

![alt text](images/directory_tree.png "Project structure")

##### Classes to implement

Below is a description of the classes you need to implement in order to run a MapReduce job

- The class [Mapper](example-project/src/main/java/mapper/Mapper.java) is the implementation of your mappers. It must extend the `mapper.MapperAbstract` class which looks like the following:

```java
public abstract class MapperAbstract {
    public abstract String map(BufferedReader objectBufferedReader);
}
```

The `map` method receives a `BufferedReader` as a parameter which is a reader of the batch part that the mapper lambda processes. The Reader closing is done internally for you.

- The class [Reducer](example-project/src/main/java/reducer/Reducer.java) is the implementation of your reducers. It must extend the `reducer.ReducerAbstract` class which looks like the following:
  
```java
public abstract class ReducerAbstract {
    public abstract String reduce(List<ObjectInfoSimple> batch);
}
```

The `reduce` method receives a list of `ObjectInfoSimple` instances, which encapsulate information about the objects to be reduced such as the s3 source bucket and key.
The `ObjectInfoSimple` looks like this:

```java
public class ObjectInfoSimple {
    private String bucket;
    private String key;
    ...
}

```

You can use the utility method `Commons.getReaderFromObjectInfo(ObjectInfoSimple info)` to open a reader of the object passed as a parameter.
**For the reducer, you are responsible of closing the opened readers.**

##### Configuration file 
- The `jsonInfo.json` file located at src/main/resources holds various configuration options of the job.

```json
{
  "jobId": "",
  "jobInputBucket": "big-dataset",
  "mapperOutputBucket": "big-dataset-map",
  "reducerOutputBucket": "big-dataset-reduce",
  "reducerFunctionName": "reducer",
  "mapperFunctionName": "mapper",
  "reducerMemory": "1536",
  "mapperMemory": "1536",
  "mapperForceBatchSize": "-1",
  "reducerForceBatchSize": "-1"
}
```

`jobId` is automatically instantiated.

`jobInputBucket` contains the batch parts that each lambda mapper will process.

`mapperOutputBucket` is the bucket where the mappers will put their results.

`reducerOutputBucket` is the bucket where the reducers will put their results.

`reducerMemory` and `mapperMemory` are the amount of memory(and other ressources) allocated to the lambda functions. They are used by the framework to compute the batch size that each mapper/reducer will process.

`mapperForceBatchSize` and `reducerForceBatchSize` are used to force the framework to use the specified batch size instead of automatically computing it. **`reducerForceBatchSize` must be greater or equal than 2**.
A less than 0 value means that the values will be automatically computed.

##### Serverless-mapreduce as a Maven dependency
- The `pom.xml` must contain the framework dependency

```xml
    <dependencies>
    ...
        <dependency>
            <groupId>fr.d2-si</groupId>
            <artifactId>serverless-mapreduce</artifactId>
            <version>1</version>
        </dependency>
    ...
    </dependencies>
```

The dependency should ideally be in the maven repository, but for now we need it to be in our local repo.
This command should be able to do just that:

```commandline
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file
            -Dfile=example-project/jars/serverless-mapreduce.jar 
            -DgroupId=fr.d2-si
            -DartifactId=serverless-mapreduce 
            -Dversion=1 
            -Dpackaging=jar 
            -DlocalRepositoryPath=~/.m2/repository/
```

You may add any dependency that your mapper and reducer rely on.

##### Deployment

- The `deploy.sh` script is responsible of generating a new jobId and deploying necessary lambda functions and other infrastructure components

#### 2. Running the job

To run the job, enter the following command:
```commandline
  aws lambda invoke --function-name driver /dev/null
```

To successfully run the command, you need to install the aws cli: [Install AWS Cli](http://docs.aws.amazon.com/cli/latest/userguide/installing.html)

You also need to configure the cli: [Configure AWS Cli](http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html)