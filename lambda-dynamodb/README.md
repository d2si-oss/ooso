# AWS lambda hands-on
   
- This lambda function is triggered when a new record is added to a DynamoDB stream.
- The creation of the source DynamoDB table, the DynamoDB stream and the appropriate roles and policies and policies attachements is automatically created using terraform.
- The lambda function simply logs the new added record.
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