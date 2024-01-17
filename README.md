## Quoqo Health Insurance system (Java Message Service)

### Architecture:

A distributed health insurance quotation system with a client entry-point, 3 health insurance provider services, and a broker service to facilitate communication between the 4.

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




