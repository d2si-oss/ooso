variable "access_key" {
}

variable "secret_key" {
}

variable "region" {
  default = "eu-west-1"
}

variable "mapperMemory" {
  default = 1536
}

variable "reducerMemory" {
  default = 1536
}

variable "jobInputBucket" {
  default = "big-dataset"
}

variable "mapperOutputBucket" {
  default = "big-dataset-map"
}

variable "reducerOutputBucket" {
  default = "big-dataset-reduce"
}

variable "reducerFunctionName" {
  default = "reducer"
}

variable "mapperFunctionName" {
  default = "mapper"
}