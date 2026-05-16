sudo addgroup hadoop # create group hadoop
sudo adduser --ingroup hadoop hduser # create user hduser insidethehadoop group

cd ~/Downloads # change directory
wget https://downloads.apache.org/hadoop/common/stable/hadoop-3.4.1.tar.gz # download hadoop
tar -xvf hadoop-3.4.1.tar.gz # extract from archive
sudo mv hadoop-3.4.1 hadoop # rename to hadoop
sudo mv hadoop /opt # move hadoop 
sudo chown -R hduser:hadoop /opt/hadoop # change owner and grouptohduser and hadoop