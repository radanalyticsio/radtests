# Radanalytics Tests

In this repo you will find tests for the various tutorial projects listed on the [radanalytics.io](https://radanalytics.io/tutorials)
 page. 

### Usage 

Clone the repo: 

```bash
git clone git@github.com:radanalyticsio/radtests.git`

cd radtests
```

Ensure that google-chrome and chromium driver is installed. We offer a script to do this for you on Fedora 25/26/27:

```bash
sudo ./install-env.sh
```

Before running the tests you will need to export some environment variables that outline your openshift cluster information: 

```bash
export OPENSHIFT_URL="https://127.0.0.1:8443"
export OPENSHIFT_HOST_IP="127.0.0.1"
export OPENSHIFT_USERNAME="admin"
export OPENSHIFT_PASSWORD="admin"
export OPENSHIFT_NAMESPACE="test-s3source"

# For test-pysparkhdfs module, a hadoop instance is required with a textfile, 
# the content of the text is irrelevant

export HADOOP_HOST="127.0.0.1"  
export HADOOP_PORT="9000"
export HADOOP_PATH="/integration/README.txt"
```


Now, simply run the maven clean tests commands on one of the profiles using the format: 

`mvn clean test -P<profile-name>`

Example:

```
mvn clean test -Ptest-s3source
```

If you have a kafka host and would like to stream logs to it, you can enter system properties specifying the host/port for your kafka instance: 

`mvn clean test -P<profile-name> -Dkafka.host=<your-kafka-host> -Dkafka.port=<your-kafka-port>`

Example:

```
mvn clean test -Ptest-s3source -Dkafka.host=0.0.0.0 -Dkafka.port=9092
```
## Documentation
For further documentation on contributing and/or customisation options see the documentation [here](https://humairak.gitbooks.io/radtest/content/). 
