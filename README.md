## Quoqo Health Insurance system (Java Message Service)

### Architecture:

A distributed health insurance quotation system with a client entry-point, 3 health insurance provider services, and a broker service to facilitate communication between the 4.
This version replaces SpringBoot REST with an Apache ActiveMQ service and uses Java Message Service to allow the use of the Apache messaging queues within a Java application.
An ActiveMQ image is pulled from the Docker public repo and used in this application. All 4 services connect to this server in order to create queues, topics, and consumers.

An 'Applications' Topic is created for insurance quote requests to be sent by the client. From this topic, requests are broadcast to all listening services (broker, and 
the 3 insurance proiders). Broker uses each broadcast application message to create an 'offers' queue. For each application made, a partial offer object is created and appended to an 'offersList'. 

The insurance services have a different response. When they receive applications, they calculate a quote which they then send to the broker via 
the 'Quotations' queue. For every quotation recieved, the broker appends the relevant data to the matching application in the offersList. Data is matched via a token-based
system. The broker uses a timeout system to ensure that enough time is allocated for the loan services to generate quotations. 
This is necessary due to the asynchronous nature of the messaging services. The timeout is triggered early if the broker realises all services have responded. 
On timeout, all listed offers are sent to the 'offers' queue and recieved by the client who presents the data to the user.

There are 3 loan provider services (auldfellas, dodgygeezers, girlsallowed) however this can be reduced or increased, and the system is tolerant of individual or multiple
failures.

Each service in the system has been dockerised and a compose file has been created to enable straightforward initiation of the overall system.

---


**To Run:**

Ensure docker is installed on your system and running.
Ensure that a docker network 'mynet' is created (run 'docker network create mynet')
Clone the project and open a terminal in the root directory.
Run the command 'docker compose up'.

---

#### User Interface:

This system is CLI based and does not take user input. Qoute requests are made automatically to showcase the system at work. Each request has the following format:

{
"name" : "John Doe",
"gender" : "M",
"age": 49,
"height": 1.549,
"weight": 80,
"smoker" : false,
"medicalIssues": false
}

A series of simimilar objects are sent by the client. And a text based reponse is presented to the user on the command-line.
In future a GUI-based solution could be introduced.




