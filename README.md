# money-transfer

Simple RESTful API for money transfers between accounts

Build&run is maven based:

clean compile exec:java (to run)

clean test (to execute unit/integration tests)

App has endpoints to open/get/close accounts, make/get deposits, make/get withdrawals and make/get transfers, as listed here:

http://localhost:1080/application.wadl

Framework/APIS used are:

1. Jersey + Jackson + Grizzly for exposing the RESTful API itself with JSON as common data exchange format.

2. jOOQ for simple implementation of the persistence layer and H2 as in memory database

3. JUnit for unit/integration testing together with jsonj for easy JSON manipulation

App is currently deployed on Heroku:

1. TODO
