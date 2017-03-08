# AWS lambda hands-on

This lambda function is triggered by an S3 object creation event. More specifically, when a bzip2 file is uploaded to a source bucket, the lambda uses the Apache Compression Commons library to decompress it and store the result in another bucket.

## Deploying the example lambda function

1. Install maven

```
sudo apt install maven
```


2. [Install terraform](https://www.terraform.io/intro/getting-started/install.html)


3. Deploy lambda

```
cd terraform
./deploy.sh
```