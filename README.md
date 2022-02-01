# Install locally

To get a full development version of the app just execute the following commands:

### Prerequisites

  - **Java 17**

    *Check version*:

        $ java -version

     *Install*:

        $ sudo apt-get install -y default-jdk

  - **AngularCLI** ( which requires  Node.js > 4.x.x  and  npm > 3.x.x )

    *Check versions*:

        $ node -v
        $ npm -v
        $ ng -v

     *Install*:

        $ sudo apt-get install nodejs
        $ sudo apt-get install npm
        $ npm install -g @angular/cli

  - **Gradle**

    *Check version*:

        $ gradle -v

    *Install*:

        $ sudo apt-get install gradle

### Installation and execution

  First of all, it is necessary to create a schema for PostgreSQL. The name must be: **full-teaching**

  Next step is the setting of environment variables to interact with Telegram.

1. Log in to your Telegram core: [Telegram Application](https://my.telegram.org).
2. Go to *API development tools* and fill out the form.
3. You will get basic addresses as well as the *api_id* and *api_hash* parameters required for user authorization.
4. set environment variables using generated previously *api_id* and *api_hash* in the following format: **TG_API_ID=your_api_id;TG_API_HASH=your_api_hash*

In order to run Kurento MediaServer (needed for videoconferencing), the following command in the terminal must be executed:

*docker run -p 4443:4443 --rm -e OPENVIDU_SECRET=MY_SECRET openvidu/openvidu-server-kms:2.20.0*

After all this steps you can run the project and go to *http://localhost:4200/*

### **IMPORTANT**:

  - Before executing the script to build and run the app, it is necessary to change the following properties of *ClassGram/backend/src/main/resources/application-dev.properties* file to match your credentials for PostgreSQL:

        spring.datasource.username=YOUR_USERNAME
        spring.datasource.password=YOUR_PASS

- In the same file you have to also uncomment line number 4:
           
        spring.jpa.generate-ddl=true

- You may have to change permissions in order to execute the scripts.
