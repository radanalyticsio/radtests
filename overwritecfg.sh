cat << EOF > radtests.properties 

# OPENSHIFT Version
com.redhat.xpaas.openshift.version=3.7

# OPENSHIFT server domain
com.redhat.xpaas.config.master.url=https://10.15.17.78:8443

# OPENSHIFT username/password
com.redhat.xpaas.config.master.username=developer
com.redhat.xpaas.config.master.password=developer

# OPENSHIFT project/namespace to run the testsuite
com.redhat.xpaas.config.master.namespace=radtests-one
com.redhat.xpaas.config.login.maxattempts=20

# General
# TODO: Change to a reasonable timeout, currently set to high for test dev, 1000=1sec
com.redhat.xpaasqe.rad.config.timeout=500000000
com.redhat.xpaasqe.rad.config.httptimeout=5000000
com.redhat.xpaasqe.rad.config.webdriver=webdriver.chrome.driver
com.redhat.xpaasqe.rad.config.webdriver.path=/usr/bin/chromedriver
com.redhat.xpaasqe.rad.config.host.ip=10.15.17.78
com.redhat.xpaasqe.rad.config.route.suffix=.nip.io
com.redhat.xpaasqe.rad.config.use.headless.tests=true

# If running locally set to false
com.redhat.xpaasqe.utilities.config.openshift.use.token=true
com.redhat.xpaasqe.utilities.config.auth.token=J7JqMKgZvD1IujvzGQrflBZMQIlyrGmDuecfNBLXHRU

# Ophicleide configs
com.redhat.xpaasqe.rad.ophicleide.config.ophicleide.app.name=ophicleide
com.redhat.xpaasqe.rad.ophicleide.config.model.name=Linconl's Plan of Reconstruction
com.redhat.xpaasqe.rad.ophicleide.config.model.url=https://www.gutenberg.org/files/56039/56039-0.txt
com.redhat.xpaasqe.rad.ophicleide.config.model.queryWord=james

# Oshinko config
com.redhat.xpaasqe.rad.oshinko.config.app.name=oshinko-web
com.redhat.xpaasqe.rad.oshinko.config.service.account=oshinko
com.redhat.xpaasqe.rad.oshinko.config.use.headless=true
com.redhat.xpaasqe.rad.oshinko.config.sparkcluster.name=sparkc
com.redhat.xpaasqe.rad.oshinko.config.oshinko.initialworkers=2
com.redhat.xpaasqe.rad.oshinko.config.oshinko.masterurl=spark://sparkc:7077

# MongoDB
com.redhat.xpaasqe.rad.mongoDB.config.mongodb.name=ophicleide
com.redhat.xpaasqe.rad.mongoDB.config.mongodb.app.name=mongodb

# Hadoop info
com.redhat.xpaasqe.rad.hadoop.host=et10.et.eng.bos.redhat.com
com.redhat.xpaasqe.rad.hadoop.port=9000
com.redhat.xpaasqe.rad.hadoop.path=/integration/README.txt


EOF

