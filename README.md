## How to Run This Project

Hey! If you want to get this project up and running on your local machine, just follow these simple steps:

### 1. Get the Frontend Ready
First, we need to handle the Angular part:
- Go into the `src/main/frontend` folder.
- Run `npm install` (this might take a minute, it's just grabbing the necessary packages).
- Run `ng build`. This will create the "static" files that our Backend needs to show the website.

### 2. Fire Up the Backend
Now, let's start the Spring Boot engine:
- Go back to the main project folder.
- Run the application using your IDE (like IntelliJ) or use `mvn spring-boot:run`.
- Once it's started, open your browser and go to `http://localhost:8080`. That's it! 

###  Quick Tips
- **Working on the UI?** If you're just making changes to the design and want to see them instantly, run `ng serve` in the frontend folder.
- **Before you start**: Make sure you have **Node.js** and **Java (JDK 17 or newer)** installed on your computer.
