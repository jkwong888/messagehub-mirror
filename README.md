# IBM Message Hub Mirror application
This Java console application demonstrates how to connect to two [IBM Message Hub](https://console.ng.bluemix.net/docs/services/MessageHub/index.html) instances, copying json-based messages from one to the other using the [Kafka](https://kafka.apache.org) Java API. It also shows how to create and list topics using the Message Hub Admin REST API.

It can be run locally on your machine or deployed into [IBM Bluemix](https://console.ng.bluemix.net/).


## Global Prerequisites
To build and run the sample, you must have the following installed:
* [git](https://git-scm.com/)
* Docker
* 2X [Message Hub Service Instance](https://console.ng.bluemix.net/catalog/services/message-hub/) provisioned in [IBM Bluemix](https://console.ng.bluemix.net/) (presumably in two different regions)

## Build the Docker Container
Run the following commands on your local machine, after the prerequisites for your environment have been completed:
```shell
docker build -t messagehub-mirror .
 ```
 
## Running the Sample (Local)
Once built, to run the sample, execute the following command:
```shell
export SRC_MESSAGE_HUB=<json credentials of source>
export DST_MESSAGE_HUB=<json credentials of destination>
docker run -it -e SRC_MESSAGE_HUB=${SRC_MESSAGE_HUB} -e DST_MESSAGE_HUB=${DST_MESSAGE_HUB} -e TOPIC=<name of topic> --name messagehub-mirror messagehub-mirror
```

To find the values for `<json credentials of source>` and `<json credentials of destination>`, access your Message Hub instances in Bluemix, go to the `Service Credentials` tab and select the `Credentials` you want to use.  Typically the usage is to have a message hub instance in two regions and have the an instance of the application in both regions copying messages to the other one.  The topic must exist in both MessageHub instances.



