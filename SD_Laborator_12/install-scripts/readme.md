## Java 8

Pentru instalare java 8 avem script-ul [acesta](./install-java8.sh).

Ulterior rularii acestui script, ar trebui sa puteti rula `sudo update-alternatives --config java` unde veti alege versiunea de java instalata mai devreme.

## Hadoop

Similar, avem script-ul pentru [hadoop](./install-hadoop.sh). Cand il rulati acesta va crea user-ul `hduser` caruia va trebuia sa-i stabiliti o parola.

## Alte configurari

SSH il veti configura singuri urmand pasii din laborator, la fel si `/etc/hosts`. De asemenea trebuie sa configurati `.bashrc` pentru user-ul `hduser` si sa modificati `/opt/hadoop/etc/hadoop/hadoop-env.sh` asa cum e precizat in laborator. Daca ati folosit script-ul de instalat java 8 prezent aici atunci jdk-ul vostru se va afla la `/usr/lib/jvm/java-se-8u44-ri/` ci nu la `/usr/lib/jvm/jdk1.8.0_301` asa cum e in laborator. 

La partea de configurare a fisierelor xml si pus in `/opt/hadoop/etc/hadoop` avem un alt [script ajutator](./copy-etc-files.sh).

Pentru orice alte nelamuriri privind pasii de instalare consultati laboratorul.