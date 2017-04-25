<p align="center">
  <img src="images/library-logo.png" width="20%"/>
</p>


This is a serverless implementation of the MapReduce algorithm.
It is based on managed cloud services, [Amazon Simple Storage Service](https://aws.amazon.com/s3/) and [AWS Lambda](https://aws.amazon.com/lambda/) and is mainly an alternative to standard ad-hoc querying and batch processing tools.

## Table of contents

  * [Architecture and workflow](#architecture-and-workflow)
  * [How to use](#how-to-use)
    * [Library dependency](#library-dependency)
    * [Classes to implement](#classes-to-implement)
    * [Configuration file](#configuration-file)
    * [Project packaging](#project-packaging)
  * [Deployment](#deployment)
    * [S3 Buckets](#s3-buckets)
    * [IAM Roles and policies](#iam-roles-and-policies)
    * [Lambda functions](#lambda-functions)
    * [Deployment methods](#deployment-methods)
  * [Running the job](#running-the-job)

___

## Architecture and workflow
<p align="center">
  <img src="images/MyArchitecture.png"/>
</p>

The library workflow is as follows:

1. The workflow begins by invoking the `Mappers Driver` lambda function
2. The `Mappers Driver` does two things:
    1. It compute sbatches of data splits and assigns each batch to a `Mapper`
    2. It invokes a `Mappers Listener` lambda function which is responsible of detecting the end of the map phase
3. Once the `Mappers Listener` detects the end of the map phase, it invokes a first instance of the `Reducers Driver` function
4. The `Reducers Driver` is somewhat similar to the `Mappers Driver`:
    1. It computes batches from either the `Map Output Bucket` if we are in the first step of the reduce phase, or from previous reducers outputs located in the `Reduce Output Bucket`. It then assigns each batch to a `Reducer`
    2. It also invokes a `Reducers Listener` for each step of the reduce phase.
5. Once the `Reducers Listener` detects the end of a reduce step, it decides whether to invoke the next `Reducers Driver` if the previous reduce step produced more than one file. Otherwise, there is no need to invoke a `Reducers Driver`, because the previous step would have produced one single file which is the result of the job

___

## How to use
The [example-project](example-project) directory contains basic mandatory structure for any project using the library.
The structure is as follows :

<p align="center">
  <img src="images/directory_tree.png"/>
</p>

#### Library dependency
`pom.xml` should contain the library dependency

```xml
    <dependencies>
    ...
        <dependency>
            <groupId>fr.d2si</groupId>
            <artifactId>serverless_mapreduce</artifactId>
            <version>1</version>
        </dependency>
    ...
    </dependencies>
```

The dependency should ideally be in the maven repository, but for now we need it to be in our local repo.
This command should be able to do just that:

```commandline
mvn -e org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file
    -Dfile=example-project/jars/serverless_mapreduce.jar
    -DgroupId=fr.d2si
    -DartifactId=serverless_mapreduce
    -Dversion=0.0.1
    -Dpackaging=jar
    -DlocalRepositoryPath=~/.m2/repository/
```

You may add any dependency needed in subsequent steps.

#### Classes to implement
Below is a description of the classes you need to implement in order to run a MapReduce job

- The class [Mapper](example-project/src/main/java/mapper/Mapper.java) is the implementation of your mappers. It must extend the `fr.d2si.serverless_mapreduce.mapper.MapperAbstract` class which looks like the following:
    ```java
    public abstract class MapperAbstract {
        public abstract String map(BufferedReader objectBufferedReader);
    }
    ```
The `map` method receives a `BufferedReader` as a parameter which is a reader of the batch part that the mapper lambda processes. The Reader closing is done internally for you.

- The class [Reducer](example-project/src/main/java/reducer/Reducer.java) is the implementation of your reducers. It must extend the `fr.d2si.serverless_mapreduce.reducer.ReducerAbstract` class which looks like the following:
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


#### Configuration file
The `jsonInfo.json` file located at src/main/resources holds various configuration options of the job.
```json
{
    "jobId": "",
    "jobInputBucket": "dataset",
    "mapperOutputBucket": "map-output",
    "reducerOutputBucket": "reduce-output",
    "reducerFunctionName": "reducer",
    "mapperFunctionName": "mapper",
    "reducerMemory": "1536",
    "mapperMemory": "1536",
    "mapperForceBatchSize": "-1",
    "reducerForceBatchSize": "-1"
    "disableReducer": "false"
}
```

`jobId` is automatically set.

`jobInputBucket` contains the dataset splits that each `Mapper` will process.

`mapperOutputBucket` is the bucket where the mappers will put their results.

`reducerOutputBucket` is the bucket where the reducers will put their results.

`reducerMemory` and `mapperMemory` are the amount of memory(and therefore other ressources) allocated to the lambda functions. They are used internally by the library to compute the batch size that each mapper/reducer will process.

`mapperForceBatchSize` and `reducerForceBatchSize` are used to force the library to use the specified batch size instead of automatically computing it. **`reducerForceBatchSize` must be greater or equal than 2**.
A less than 0 value means that the values will be automatically computed.

`disableReducer`: if set to "true", disables the reducer if your job dosen't need it.
#### Project packaging
In order to generate the [jar](https://en.wikipedia.org/wiki/JAR_(file_format)) file used during the [deployment](#deployment) of the lambda, you need to install [maven](https://en.wikipedia.org/wiki/Apache_Maven). You may use the following command:
```
sudo apt-get install maven
```
Then, run the following command in the root of your project directory in order to generate the jar file:
```
mvn package
```

You are now ready to proceed to the deployment of the necessary components.
___

## Deployment
In order to be able to use the library, you need to deploy the following resources.
### S3 Buckets
S3 buckets are the containers that the mappers and reducers will use to fetch the files to process and to put the results of their processing.

You need three buckets, one containing your dataset splits and two others for the mappers and reducers.

You must use the same bucket names used in the configuration step above.

[Create S3 Buckets using the console](http://docs.aws.amazon.com/AmazonS3/latest/user-guide/create-bucket.html)

[Create S3 Buckets using the commandline](http://docs.aws.amazon.com/cli/latest/reference/s3api/create-bucket.html)

### IAM Roles and policies
Our lambda functions need to have a role with specific policies attached to it to be authorized to access the various services used by the library.

1. You first need to create the role with the following trust policy:

    ```json
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Principal": {
                    "Service": "lambda.amazonaws.com"
                },
                "Action": "sts:AssumeRole"
            }
        ]
    }
    ```
    [Create a IAM role using the console](http://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_create_for-service.html#roles-creatingrole-service-console)

    [Create a IAM role using the commandline](http://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_create_for-service.html#roles-creatingrole-service-cli)

2. You must then attach policies with the following arn's to your newly created role:
    - `arn:aws:iam::aws:policy/AWSLambdaFullAccess`
    - `arn:aws:iam::aws:policy/AmazonS3FullAccess`

    Note that these policies are too broad. You may use more fine-grained policies/roles for each lambda.

    [Attach a policy to a role using the console](http://docs.aws.amazon.com/IAM/latest/UserGuide/access_policies_managed-using.html)

    [Attach a policy to a role using the commandline](http://docs.aws.amazon.com/cli/latest/reference/iam/attach-role-policy.html)


### Lambda functions
There are six lambda functions to deploy. We assume that the project jar is located at `example-project/target/job.jar`. Following are the deployment details:

| Lambda Name   | Handler       |Memory|Function package|Runtime|
|:-------------:|:-------------:|:----:|:----:|:----:|
| mappers_driver| fr.d2si.serverless_mapreduce.mappers_driver.MappersDriver | anything you want|example-project/target/job.jar|java8|
| mappers_listener| fr.d2si.serverless_mapreduce.mappers_listener.MappersListener | anything you want|example-project/target/job.jar|java8|
| mapper     | fr.d2si.serverless_mapreduce.mapper_wrapper.MapperWrapper | same used in the [configuration file](#configuration-file)  |example-project/target/job.jar|java8|
| reducers_driver| fr.d2si.serverless_mapreduce.reducers_driver.ReducersDriver | anything you want|example-project/target/job.jar|java8|
| reducers_listener| fr.d2si.serverless_mapreduce.reducers_listener.ReducersListener | anything you want|example-project/target/job.jar|java8|
| reducer     | fr.d2si.serverless_mapreduce.reducer_wrapper.ReducerWrapper | same used in the [configuration file](#configuration-file)  |example-project/target/job.jar|java8|

[Create a lambda function using the console](http://docs.aws.amazon.com/lambda/latest/dg/getting-started-create-function.html)

[Create a lambda function using the commandline](http://docs.aws.amazon.com/cli/latest/reference/lambda/create-function.html)

### Deployment methods
You may use any deployment method you are familiar with. We recommend using an Infrastructure-As-Code (IAC) tool such as [Terraform](https://www.terraform.io/) or [CloudFormation](https://aws.amazon.com/cloudformation/) . These tools make it straightforward to deploy infrastructure using template files, which enables infrastructure specs sharing and versioning.

We already provided a Terraform [template file](./example-project/terraform) to deploy the various resources of the architecture. You only need to [install Terraform](https://www.terraform.io/intro/getting-started/install.html), change the current directory to where the template files are located and launch the following command:
 ```
 terraform apply
 ```
For more info about Terraform, check [Terraform documentation](https://www.terraform.io/docs/) .

___

## Running the job
In order to run the job, we only need to invoke the mappers_driver function. You may use the following command:
 ```
 aws lambda invoke mappers_driver /dev/null &
 ```