

# What is Home Measure?

HM is a small system implemented in Java (Spring Boot) to gather measures like e.g. temperature from a sensor, amount of produced power from a PV DC-AC converter or the amount of electricity currently being consumed.
For further processing purposes measures can optionally be sent to a MQTT broker, stored in a time series or a relational database system. 

The system is written in Java and comes as a stand-alone Spring Boot application that can directly be started.

# Data Model

## Device

A device represents a physical device like e.g. 

 - temperature sensor
 - PV DC/ AC converter
 - Electricity meter

## Measure

A device provides measures like e.g.

 - current temperature (°C, °F, etc.)
 - amount of electricity (KWh)
 - produced power (W)

# Services
Services provide the possibility to push measures and their values to other system e.g. for futher processing.

**Included Measures**
The following setting can be used to define which measures to include per service (default is always all):

    service.SERVICE_ID.included-measures=FILTER
Where *FILTER* can be one of the following:

 - *DEVICE_ID.**
 - *DEVICE_ID.MEASURE_ID*
 - *DEVICE1.*, DEVICE2.MEASURE2*
 - *DEVICE1.* , DEVICE2.**

## MQTT
Pass measure values to a home automation central like e.g.  Node Red, OpenHAB, etc.

**Configuration**

Enable service (default is *false*):

    service.mqtt.enabled=true
Server URL (this is the default):

    service.mqtt.broker-url=tcp://localhost:1883

Optionally provide username and password (default is empty String):

    service.mqtt.broker-user=MYUSER
    service.mqtt.broker-password=MYSECRETPASSWORD

Define custom topic root (this is the default):

    service.mqtt.topic-root=homemeasure/

## InfluxDb
Create custom statistics concerning the power consumption over time.

**Configuration**

Enable service (default is *false*):

    service.influxdb.enabled=true
    
Define which devices/ measures should be included (default is all):

    service.influxdb.included-measures=FILTER
Where *FILTER* can be one of the following:

 - *DEVICE_ID.**
 - *DEVICE_ID.MEASURE_ID*
 - *DEVICE1.*, DEVICE2.MEASURE2*
 - *DEVICE1.* , DEVICE2.**

Database name (this is the default):

    service.influxdb.dbname=HMServer
Url of the database (this is the default):

    service.influxdb.url=tcp://localhost:1883
User and password (both are default):

    service.influxdb.user=root
    service.influxdb.password=root
Time in milli seconds after which a flush will be executed (this is the default):

    service.influxdb.flush-duration=60000

## JDBC
Storage of measures in relational databases. 

Enable service (default is *false*):

    service.jdbc.enabled=true

name of the database table (this is the default):

    service.jdbc.tablename=hmdata
columns names (all default) :

    service.jdbc.column_name_data=measure_id
    service.jdbc.column_name_device=device_id
    service.jdbc.column_name_timestamp=measure_timestamp
    service.jdbc.column_name_value=measure_value
    
# Grab measure data

## Rest API Endpoint
**Configuration**
enable service (default is *false*):

    service.rest.enabled=true

**Grab Data**
Once HMServer is started you can grab measure data by pointing your browser e.g. to the following URL:
http://localhost:8080/measures
You will get data as JSON by default:

    {
      "demo":
        {
          "MEASURE_1":	"663",
          "MEASURE_2":	"25",
          "MEASURE_3":	"533",
          "MEASURE_4":	"622",
          "MEASURE_5":	"123"
        }
    }

**Serializer**
The responsibility of a serializer is to render measures and their values in a readable form.
Serializer for JSON and XML are supported.
 You can choose the type of the serializer to use by adding a parameter to the request:
http://localhost:8080/measures?serializer=xml
http://localhost:8080/measures?serializer=json

Custom serializers can easily be implemented by providing a class implementing the interface *ginious.home.measure.model.MeasurementsSerializer*. In order to be found the serializer implementation must be  in the classpath and must reside in package *ginious.home.measure* or  a any sub package.

 
