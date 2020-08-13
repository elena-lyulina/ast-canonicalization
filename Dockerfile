#Download base image ubuntu 20.04
FROM ubuntu:20.04

# Disable Prompt During Packages Installation
ARG DEBIAN_FRONTEND=noninteractive

# Update Ubuntu Software repository
RUN apt update

# Install JRE
RUN apt-get install -y default-jre
# Install JDK
RUN apt-get install -y default-jdk
# Install Python
RUN apt-get install -y python3.8
# Install PIP
RUN apt-get install -y python3-pip && pip3 install pip --upgrade

# Copying
COPY requirements.txt /requirements.txt
RUN pip3 install -r requirements.txt