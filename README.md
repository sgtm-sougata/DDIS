# chavi-ddis-ubuntu
https://github.com/jmGithub2021/CHAVI3DS/tree/main

## mysql install
Before install mysql check already install or not ?
```bash
mysql -u your_username -p    # or
sudo mysql -u your_username -p   
```
in the terminal output **Command 'mysql' not found, but can be installed with:** then mysql presently not install.
https://www.digitalocean.com/community/tutorials/how-to-install-mysql-on-ubuntu-20-04
```bash
sudo apt install mysql-server
sudo mysql
```
```mysql
# in mysql cell
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '1234';
exit;
```
```bash
mysql -u root -p
```
In the terminal output 
**Enter password: 
Welcome to the MySQL monitor.**
Sucessfully install mysql also login with the root user

## dcm4che install
https://sourceforge.net/projects/dcm4che/files/dcm4che3/
Download the latest version of dcm4che.After download go to the dcm4che .deb file location and write click to your mouse and open terminal write
```bash
sudo dpkg -i <download dcm4che file name>
```
if facing issue instll dpkg
https://www.digitalocean.com/community/tutorials/dpkg-command-in-linux
Sucessfully install search the application in your application search bar weasis if showing icon then go to next step.

## insatll netbeens
https://www.apache.org/dyn/closer.lua/netbeans/netbeans-installers/22/apache-netbeans_22-1_all.deb

## import all the sql table in the chaviro database
Go to the git clone CHAVI3DS directory inside it have sub directory SQL inside it have three sql file which need to be run in mysql bash shell
open a new terminal and in terminal write mysql -u root -p then enter the password, login mysql shell copy paste one by one line
```mysql
create database chaviro;
show databases;
exit;
```
At first create a new database **chaviro** and also show in the database list.
```
mysql -u root -p chaviro < chavid3s_schema.sql
mysql -u root -p chaviro < Anatomic_Site.sql
mysql -u root -p chaviro < Administrator_loginData.sql
```
## modify config.json globalpath and tempdirectory
go to the CHAVI3DS directory and inside have config.json open and modify two path 
**globalPath**  <"in your CHAVI3DS directory location">
**tempDirectory** create a new folder temp inside CHAVI3DS directory and <"add path">

last part of the installation is the java and javafx in the same terminal write the below code.
## java download
```bash
sudo apt install openjdk-17-jdk
sudo apt install openjdk-17-jre

wget https://download2.gluonhq.com/openjfx/21.0.1/openjfx-21.0.1_linux-x64_bin-sdk.zip
unzip openjfx-21.0.1_linux-x64_bin-sdk.zip
```

## run jarfile 
go to the CHAVI3DS directory and inside have a sub directory dist go to dist dir and open terminal and run
```bash
java --module-path "<javafx-sdk lib path> --add-modules javafx.controls,javafx.fxml -jar DicomODIS.jar

# example
java --module-path "/media/sougata/New Volume/D drive/sougata/CHAVI3DS/javafx-sdk-21.0.1/lib" --add-modules javafx.controls,javafx.fxml -jar DicomODIS.jar
```

Boom !! it open the login page
![image](https://github.com/user-attachments/assets/b4a3af9d-ea30-4312-a648-70717d09fda5)


username: admin
password: 1234
open the System administrator page enter the name, email and the other information as well as user type **User**



# DDIS
