## Fetch Rewards Backend Coding Challenge
* https://fetch-hiring.s3.us-east-1.amazonaws.com/points.pdf
---
## Requirements:
Java 8 - SdkMan can be used to easily install and switch between JDKs - https://sdkman.io/jdks

## Technical Notes:
* Written in Kotlin (JVM target 1.8)
* Spring Boot version `2.7.5`
* Built with Gradle version `7.5.1`

## How to Run:
**Use the included gradle wrapper for the following**
  * Starting the service - `./gradlew bootRun`
    * The app will run on port 8080 - http://localhost:8080
    * this will handle building the app too. If you would like to build the app before running it use - `./gradlew clean build`
  * Run unit tests - `./gradlew test`
    * Any failed tests will display in the terminal
    * If all tests passed, `BUILD SUCCESSFUL` will be displayed

---
## API Endpoints:
* see `FR.postman_collection.json` for a Postman collection with examples for the below requests
### Return all payer point balances 
#### GET `/api/points`
#### Parameters - None
#### Sample curl 
```
curl --location --request GET 'localhost:8080/api/points'
```
#### Sample Response
```
{
    "DANNON": 500,
    "MILLER COORS": 800,
    "UNILEVER": 1500
}
```

### Add Payer Transaction 
#### POST `/api/points` 
#### Parameters
* `payer` - **String** - _required_ 
* `points` - **Integer** - _required_
* `timestamp` - **Timestamp** - _required_ - format `yyyy-MM-dd'T'HH:mm:ss'Z'`
#### Example payload
```
{
    "payer": "UNILEVER",
    "points": 500,
    "timestamp": "2022-11-18T11:00:00Z"
}
```
#### Sample curl 
```
curl --location --request POST 'localhost:8080/api/points' \
--header 'Content-Type: application/json' \
--data-raw '{
    "payer": "UNILEVER",
    "points": 500,
    "timestamp": "2022-11-18T11:00:00Z"
}'
```
#### Sample Response
```
{
    "payer": "UNILEVER",
    "points": 500,
    "timestamp": "2022-11-18T11:00:00Z"
}
```

### Spend Points
#### POST `/api/spend`
#### Parameters
* `points` - **Integer** - _required_ - must be a positive integer
#### Example payload
```
{
    "points": 500
}
```
#### Sample curl
```
curl --location --request POST 'localhost:8080/api/spend' \
--header 'Content-Type: application/json' \
--data-raw '{
    "points": 700
}'
```
#### Sample Response
```
[
    {
        "payer": "MILLER COORS",
        "points": -200
    },
    {
        "payer": "UNILEVER",
        "points": -300
    },
    {
        "payer": "DANNON",
        "points": -200
    }
]
```
