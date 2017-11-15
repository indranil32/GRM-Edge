FROM anapsix/alpine-java:latest
VOLUME /tmp
RUN apk -U add curl
ENV APPDYNAMICS_AGENT_TIER_NAME=grm-edge-service
ADD grm-edge-service-1.1.18.jar /app.jar
ADD startService.sh /startService.sh
ADD shutdownhook.sh /shutdownhook.sh
ADD healthCheck.sh /healthCheck.sh
RUN chmod 700 /startService.sh
RUN chmod 700 /shutdownhook.sh
RUN chmod 700 /healthCheck.sh
RUN dos2unix /shutdownhook.sh
RUN dos2unix /healthCheck.sh
ENTRYPOINT ./startService.sh