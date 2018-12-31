# BookingProvider

## A Care Connect Booking Provider demonstrator

This is able to act as a Provider system, offering a set of Slots that can be booked, and allowing Appointments to be POSTed in, thereby booking one of those slots.

## Basis
Based on the [HAPI FHIR RESTful server](http://hapifhir.io/doc_rest_server.html) code.

Uses Auth0 libraries to validate the supplied JWT.

## Usage
Build as a WAR and run (e.g. with a Context Root of /poc) and then:
- Browse to [http://localhost:/poc/index](http://localhost:/poc/index) to see management interface.

See Dockerfile for more details of running as a self contained docker container using Jetty.

To run locally in Docker, use the following commands:
```bash
docker build -t a2sibookingprovider .
docker run -d -p 8080:8080 --name bookingprovider a2sibookingprovider
```

[Repo and code](https://bitbucket.org/TCoates/bookingprovider/src)


## Deployment
Runs a in a Docker container in ECS, at http://appointments.directoryofservices.nhs.uk:8080/poc/index

Uses BitBucket pipeline (see bitbucket-pipelines.yml) to deploy whenever a successful (i.e. unit tests passing) push is made to the Master branch:

$(aws ecr get-login --no-include-email --region eu-west-2 --profile domainb)
docker build -t a2sibookingpoc .
docker tag -f a2sibookingpoc:latest 410123189863.dkr.ecr.eu-west-2.amazonaws.com/a2sibookingpoc:latest
docker push ${AWS_REGISTRY_URL}:latest

## JWT details
In order for requests to be accespted, they need to include an access_token in an Authorization header, for example:

curl -X GET \
  http://localhost:8080/poc/Slot \
  -H 'Accept: application/json' \
  -H 'Authorization: Bearer:  eyJ0eX----wCXBzcEVg'

In order to get an access_token, make a call as follows:

curl -X POST \
  https://login.microsoftonline.com/e52111c7-4048-4f34-aea9-6326afa44a8d/oauth2/v2.0/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'client_id=0f7bc08b-3395-4b4b-b23b-f790fc62bf91&client_secret=mVBtuzR-------XreUpnBVb8%3D&grant_type=client_credentials&scope=http%3A%2F%2Fappointments.directoryofservices.nhs.uk%3A8080%2Fpoc%2F.default'

Where scope is the root of the FHIR Server being teragetted.