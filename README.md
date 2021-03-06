# Picasa Web Crawler

Crawls your Picasa web albums to download the photos & videos.

It works by accessing your picasa account via Firefox and reproduces user interaction to download the photos and videos from your albums.


## System requirements

- Java 8
- Firefox

## How to use it

### Install in Ubuntu/Debian

Download the latest version from https://github.com/adelolmo/picasa-web-crawler/releases/latest

    $ cd /tmp
    $ wget https://github.com/adelolmo/picasa-web-crawler/releases/download/v1.3/picasa-crawler-1.3.deb
    $ sudo dpkg -i picasa-crawler-1.3.deb
    
### Install in other Systems

Download the latest version from https://github.com/adelolmo/picasa-web-crawler/releases/latest

    $ cd $HOME
    $ wget https://github.com/adelolmo/picasa-web-crawler/releases/download/v1.3/picasa-crawler-1.3.jar
    $ wget https://raw.githubusercontent.com/adelolmo/picasa-web-crawler/master/picasa-crawler
    $ chmod 700 picasa-crawler
    
Add the following environment variables
+ GOOGLE_ACCOUNT with your email address.
+ GOOGLE_PASSWORD with your password.

You can do it like this:
    
    $ vim ~/.bashrc

And append the values at the end.
    
```
export GOOGLE_ACCOUNT=me@gmail.com
export GOOGLE_PASSWORD=mypassword
``` 

    $ source ~/.bashrc   

### Run

*IMPORTANT*: a Firefox window will be opened, do not close it!

The application supports three parameters, "a" to download an specific album, "v" to set the verification code in case that two factor authentication is enable
and "o" to set the download directory.

You can skip the "v" parameter if two factor authentication is not enable in your account.

You can skip the "a" parameter if you want to download all you albums.

You can skip the "o" parameter, the default directory is located in "albums" under the application directory.

#### In Ubuntu/Debian

    $ picasa-crawler -a "My dog" -v 123456 -o /tmp/picasa-albums

In the example above the album "My dog" is located under /tmp/picasa-albums

#### In other Systems
        
Go to the directory where picasa-crawler (jar and picasa-crawler script) was downloaded.
        
    $ ./picasa-crawler -a "My dog" -v 123456 -o /tmp/picasa-albums
    
In the example above the album "My dog" is located under /tmp/picasa-albums

## How to build it

    $ mvn clean install
