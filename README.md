# Simple HTTP Web Server in Java

A simple HTTP Server implemented in Java based on W3C specifications (http://www.w3.org/Protocols/)

## Features
 * Implement with Java I/O streams
 * Support GET/HEAD requests
 * Support POST/PUT with query string and request payload
 * Respond to binary data request (ex. images)

It can also
 * Logging 
 * Multithreading

It is an part of [CS5011](https://info.cs.st-andrews.ac.uk/student-handbook/modules/CS5001.html) at University of St. Andrews.


## Running

To start the web server, clone and run the following command:

```sh
java WebServerMain <document_root> <port> <option>
```

`<document_root>` directory that contain files for your website

`<port>` port number

`<option>` there is only one option `-v` to enable logging by piping all System.in and System.out into a log file -- `access_log.txt` and `error_log.txt` respectively.

### For example

```sh
java WebServerMain ./www 8000 -v
```

To terminate the server, just press `Ctrl+C` on the command line.


### Todos
 * Do MORE Tests
 * Use `ThreadPoolExecutor` to handle queue and schedule requests
 * Implement request timeout handler
 * Handle malformed HTTP request
 
----

## Author
[Pakawat Nakwijit](http://curve.in.th); An ordinary programmer who would like to share and challange himself. It is a part of my 2018 tasks to open source every projects in my old treasure chest with some good documentation. 

## License
This project is licensed under the terms of the MIT license.




