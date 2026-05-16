#!/bin/bash
wget https://download.java.net/openjdk/jdk8u44/ri/openjdk-8u44-linux-x64.tar.gz

tar xvfz openjdk-8u44-linux-x64.tar.gz

sudo mkdip -p /usr/lib/jvm/

sudo mv java-se-8u44-ri /usr/lib/jvm/

sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-se-8u44-ri/bin/java 1102