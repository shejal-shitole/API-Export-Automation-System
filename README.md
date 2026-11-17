# API Export Automation System

## Project Overview
The API Export Automation System is a web-based application developed to discover potential buyers, collect buyer information, manage buyer records, and export buyer data for business use.

The application uses a Java Spring Boot backend, React frontend, and MySQL database.

## Technologies Used

**Backend**
- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- Tavily API
- Gemini API (where configured)

**Frontend**
- React.js
- Vite
- JavaScript
- HTML
- CSS

**Tools**
- Eclipse IDE
- Visual Studio Code
- MySQL Workbench
- Postman
- Git and GitHub

## Key Features

- Buyer discovery using product-related search queries
- Buyer information extraction from search results
- Buyer record management
- Add, edit, delete, and view buyer records
- Buyer classification and product relevance tracking
- Email availability management
- Dashboard with buyer statistics
- CSV export
- PDF report generation

## Project Structure

```text
API-Export-Automation-System/
├── Backend/
│   ├── src/
│   ├── pom.xml
│   └── ...
├── Frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── ...
└── README.md
```

## Backend Setup

1. Open the Backend folder.
2. Configure your MySQL database.
3. Set the required environment variables:
   - DB_URL
   - DB_USERNAME
   - DB_PASSWORD
   - TAVILY_API_KEY
   - GEMINI_API_KEY (if using Gemini functionality)
4. Run the Spring Boot application.

Backend URL: `http://localhost:8080`

## Frontend Setup

Open a terminal inside the Frontend folder and run:

```bash
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/buyers` | Retrieve buyer records |
| POST | `/api/search/discover-buyers?query=...` | Discover potential buyers |

## Author

Shejal Shitole

## Project Purpose

This project was developed to automate buyer discovery and simplify buyer data management for export-related business activities.
