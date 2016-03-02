# Picasa Web Crawler

Crawls your Picasa web albums to download the photos & videos.

## System requirements

- Java 8
- Firefox

## How to use it

### Install

Download the latest version from https://github.com/adelolmo/picasa-web-crawler/releases/latest

    $ cd $HOME
    $ wget https://github.com/adelolmo/picasa-web-crawler/releases/download/v1.1/picasa-crawler-1.1.jar
    $ wget https://raw.githubusercontent.com/adelolmo/picasa-web-crawler/master/picasa
    $ chmod 700 picasa
    
Add the following environment variables
    - GOOGLE_ACCOUNT with your email address.
    - GOOGLE_PASSWORD with your password.

You can do it like this:
    
    $ vim ~/.bashrc

And append the values at the end.
    
```
export GOOGLE_ACCOUNT=me@gmail.com
export GOOGLE_PASSWORD=mypassword
``` 

    $ source ~/.bashrc   

### Run

The application supports two parameters, "a" to download an specific album and "v" to set the verification code in case that two factor authentication is enable.
You can skip the "v" parameter if two factor authentication is not enable in your account.
You can skip the "a" parameter If you want to download all you albums.

    $ ./picasa -a "My dog" -v 123456
    
The albums are located under /tmp/picasa

## How to build it

    $ mvn clean install