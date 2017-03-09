# AWS lambda hands-on
   
- This lambda function is triggered when a batch of kinesis stream records is added, the lambda simply logs the records data in the CloudWatch logs
- The creation and deployment of the stream, the lambda and the appropriate roles is done using terraform
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