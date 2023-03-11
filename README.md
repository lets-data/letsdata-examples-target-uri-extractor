# letsdata-examples-target-uri-extractor
#Let's Data S3 Single File Reader example that implements the parser for the WARC files to extract the crawled web URIs.

This is an example of the S3 Read Connector for reader type 'Single File Reader'. 

* Check the reader docs at www.letsdata.io/docs#readconnectors
* This implements the simple 'SingleFileParser' interface 
  * Interface definition: [Link](https://github.com/lets-data/letsdata-data-interface/blob/main/src/main/java/com/resonance/letsdata/data/readers/interfaces/parsers/SingleFileParser.java)
  * Interface documentation: [Docs](https://82x9zh5ijl.execute-api.us-east-1.amazonaws.com/Test/docs#sdkinterface)
* WARC files are of the following format: 
```
...
WARC/1.0
WARC-Type: request
WARC-Date: 2022-01-16T09:37:04Z
WARC-Record-ID: <urn:uuid:a565d4a1-daf1-4697-abfb-6c155d7ed2e6>
Content-Length: 316
Content-Type: application/http; msgtype=request
WARC-Warcinfo-ID: <urn:uuid:6badabfb-3fa9-47c0-b0ef-ac4a71fd1456>
WARC-IP-Address: 173.161.93.241
WARC-Target-URI: http://01.deluxecleaning-services.com/

GET / HTTP/1.1
User-Agent: CCBot/2.0 (https://commoncrawl.org/faq/)
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: en-US,en;q=0.5
If-Modified-Since: Thu, 21 Oct 2021 04:21:20 GMT
Accept-Encoding: br,gzip
Host: 01.deluxecleaning-services.com
Connection: Keep-Alive



WARC/1.0
WARC-Type: response
WARC-Date: 2022-01-16T09:37:04Z
WARC-Record-ID: <urn:uuid:a2567d44-2052-4fbd-83af-dea76004f6dc>
Content-Length: 11581
Content-Type: application/http; msgtype=response
WARC-Warcinfo-ID: <urn:uuid:6badabfb-3fa9-47c0-b0ef-ac4a71fd1456>
WARC-Concurrent-To: <urn:uuid:a565d4a1-daf1-4697-abfb-6c155d7ed2e6>
WARC-IP-Address: 173.161.93.241
WARC-Target-URI: http://01.deluxecleaning-services.com/
WARC-Payload-Digest: sha1:LLHZL7RSYU3WB32BJNRJSNGZATJW5JLY
WARC-Block-Digest: sha1:Y3ZBLVGEXFKYBZIJPP7KJJMGF5N6OC5L
WARC-Identified-Payload-Type: text/html

HTTP/1.1 200 OK
Server: nginx/1.10.3 (Ubuntu)
Date: Sun, 16 Jan 2022 09:37:04 GMT
...
```
* The code instructs #Let's Data to extract the WARC-Type request records by:
  * Specifying the record start text `WARC-Type: request` in the `getRecordStartPattern` function 
  * and the record end text `WARC-Type: response` in the `getRecordEndPattern` function

* With the above start and end patterns, the extracted record given to the `parseDocument` function would be 
```
WARC-Type: request
WARC-Date: 2022-01-16T09:37:04Z
WARC-Record-ID: <urn:uuid:a565d4a1-daf1-4697-abfb-6c155d7ed2e6>
Content-Length: 316
Content-Type: application/http; msgtype=request
WARC-Warcinfo-ID: <urn:uuid:6badabfb-3fa9-47c0-b0ef-ac4a71fd1456>
WARC-IP-Address: 173.161.93.241
WARC-Target-URI: http://01.deluxecleaning-services.com/

GET / HTTP/1.1
User-Agent: CCBot/2.0 (https://commoncrawl.org/faq/)
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: en-US,en;q=0.5
If-Modified-Since: Thu, 21 Oct 2021 04:21:20 GMT
Accept-Encoding: br,gzip
Host: 01.deluxecleaning-services.com
Connection: Keep-Alive



WARC/1.0
WARC-Type: response
```

* Code reads all the lines and finds the  `WARC-Target-URI: <uri>` line and extracts the URI and returns it as a document. It returns error doc if a URI is not found.

* Here is an example dataset configuration json that was used to run this example - replace the `<placeholder>` with actual values:
```
{
  "datasetName": "<datasetName placeholder>",
  "accessGrantRoleArn": "arn:aws:iam::<aws account with IAM role>:role/LetsData_AccessRole_TargetUriExtractor",
  "customerAccountForAccess": "<aws account that should be given access>",
  "readConnector": {
        "artifactImplementationLanguage": "Java",      
        "artifactFileS3LinkResourceLocation": "Customer",
        "artifactFileS3Link": "s3://<s3Bucket that hosts the jar>/target-uri-extractor-1.0-SNAPSHOT-jar-with-dependencies.jar",     
        "connectorDestination": "S3",
        "readerType": "Single File Reader",
        "bucketName": "commoncrawl",
        "bucketResourceLocation": "LetsData",
        "singleFileParserImplementationClassName": "com.letsdata.example.TargetUriExtractor"
  },
  "writeConnector": {
        "connectorDestination": "Kinesis",
        "resourceLocation": "letsdata",
        "kinesisShardCount": 15
  },
  "errorConnector": {
        "connectorDestination": "S3",
        "resourceLocation": "letsdata"
  },
  "computeEngine": {
      "computeEngineType": "Lambda",
      "concurrency": 15
  },
  "manifestFile": {
      "fileContents": "crawl-data/CC-MAIN-2022-21/segments/1652662509990.19/warc/CC-MAIN-20220516041337-20220516071337-00000.warc.gz\ncrawl-data/CC-MAIN-2022-21/segments/1652662509990.19/warc/CC-MAIN-20220516041337-20220516071337-00001.warc.gz\ncrawl-data/CC-MAIN-2022-21/segments/1652662509990.19/warc/CC-MAIN-20220516041337-20220516071337-00002.warc.gz\ncrawl-data/CC-MAIN-2022-21/segments/1652662509990.19/warc/CC-MAIN-20220516041337-20220516071337-00003.warc.gz\ncrawl-data/CC-MAIN-2022-21/segments/1652662509990.19/warc/CC-MAIN-20220516041337-20220516071337-00004.warc.gz\ncrawl-data/CC-MAIN-2022-21/segments/1652662509990.19/warc/CC-MAIN-20220516041337-20220516071337-00005.warc.gz\ncrawl-data/CC-MAIN-2022-21/segments/1652662509990.19/warc/CC-MAIN-20220516041337-20220516071337-00006.warc.gz\ncrawl-data/CC-MAIN-2022-21/segments/1652662509990.19/warc/CC-MAIN-20220516041337-20220516071337-00007.warc.gz\ncrawl-data/CC-MAIN-2022-21/segments/1652662509990.19/warc/CC-MAIN-20220516041337-20220516071337-00008.warc.gz\ncrawl-data/CC-MAIN-2022-21/segments/1652662509990.19/warc/CC-MAIN-20220516041337-20220516071337-00009.warc.gz",
      "manifestType": "S3ReaderTextManifestFile",    
      "readerType": "SINGLEFILEREADER"
  }
}
```
* Here is the example IAM Policy that is used in the `accessGrantRoleArn` IAM Role. The commoncrawl bucket and the files specified in the manifest are public - so we do not need to add access to these.
```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::<s3Bucket that hosts the jar>/target-uri-extractor-1.0-SNAPSHOT-jar-with-dependencies.jar"
            ]
        }
    ]
}
```
* The CLI commands that were used to run this dataset are: (CLI can be downloaded from the [downloads](www.letsdata.io/#downloads) link)
```
# create the dataset
$ > ./letsdata datasets create --configFile dataset.json --prettyPrint

# view the dataset, monitor its creation 
$ > ./letsdata datasets view --datasetName <datasetName> --prettyPrint

# list the datset's tasks
$ > ./letsdata tasks list --datasetName <datasetName> --prettyPrint 
```
* The records that are extracted and written to Kinesis stream can be accessed using the IAM role that is created by #Let's Data. See [Granting Customer Access to #Let's Data Resources](www.letsdata.io/docs#accessgrants) section in the docs. There is a sample implementation code and CLI program that can be used to retrieve the records [letsdata-writeconnector-reader](https://github.com/lets-data/letsdata-writeconnector-reader)

* Details about tasks, execution logs, errors and dataset metrics can be learned at: [www.letsdata.io/docs](www.letsdata.io/docs)