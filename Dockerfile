# Dockerfile to run my WAR file
# This uses a base image from jetty, which can run war files...
#FROM jetty:9.3.12-jre8-alpine - lighter weight option, but doesn't include bash and curl for health checking
FROM jetty:latest

# Add our war into the image
ADD ./target/bookingprovider-2.0-SNAPSHOT.war /var/lib/jetty/webapps/ROOT.war

# Says that when it runs, it's port 8080 needs to be available
EXPOSE 8080

# To build the image using this, simply run:
# $docker build -t a2sibookingprovider .
# This creates an image called a2sibookingprovider based on this dockerfile
#
# To run a container based on this image use:
# $docker run -d -p 443:8080 --name bookingprovider a2sibookingprovider
# This creates a running (in the background) container called bookingprovider on port 443.
#
# Browse to http://localhost:443/poc
#
# To stop the running container use:
# $docker stop bookingprovider
#
# To remove the container and image:
# $docker rm bookingprovider
# $docker rmi a2sibookingprovider

