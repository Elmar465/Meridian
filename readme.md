<div align="center">

# 🚀 Meridian

### Modern Project Management Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.x-blue?style=for-the-badge&logo=react)](https://reactjs.org/)
[![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

**Streamline your workflow. Empower your team. Deliver faster.**

[Live Demo](https://meridian-app.vercel.app) • [API Docs](https://api.meridian-app.com/swagger-ui.html) • [Report Bug](https://github.com/ELmar465/meridian/issues) • [Request Feature](https://github.com/ELmar465/meridian/issues)

---

<img src="docs/screenshots/dashboard.png" alt="Meridian Dashboard" width="800"/>

</div>

## ✨ Overview

**Meridian** is a full-stack project management platform designed for modern teams. Built with Spring Boot and React, it provides enterprise-grade features with an intuitive, beautiful interface.

Whether you're managing a small team or coordinating across departments, Meridian helps you track projects, manage issues, collaborate in real-time, and gain actionable insights through powerful analytics.

---

## 🎯 Key Features

<table>
<tr>
<td width="50%">

### 📊 **Smart Dashboard**
- Real-time project statistics
- Weekly activity tracking
- Quick action shortcuts
- Beautiful bento grid layout

### 📁 **Project Management**
- Create unlimited projects
- Assign team members
- Track progress with status updates
- Custom project keys (e.g., PROJ-123)

### 🎫 **Issue Tracking**
- Full CRUD operations
- Priority levels (Critical → Low)
- Status workflow (Todo → Done)
- Assignee management

</td>
<td width="50%">

### 👥 **Team Collaboration**
- Role-based access control
- Team invitations via email
- Real-time messaging
- Activity notifications

### 📈 **Analytics & Reports**
- Completion rate tracking
- Priority breakdown charts
- Status distribution
- Export to CSV

### 🔐 **Enterprise Security**
- JWT authentication
- Password encryption
- Role-based permissions
- Secure file uploads

</td>
</tr>
</table>

---

## 🖼️ Screenshots

<div align="center">
<table>
<tr>
<td><img src="docs/screenshots/dashboard.png" width="400" alt="Dashboard"/><br/><em>Dashboard</em></td>
<td><img src="docs/screenshots/projects.png" width="400" alt="Projects"/><br/><em>Projects</em></td>
</tr>
<tr>
<td><img src="docs/screenshots/issues.png" width="400" alt="Issues"/><br/><em>Issue Tracking</em></td>
<td><img src="docs/screenshots/analytics.png" width="400" alt="Analytics"/><br/><em>Analytics</em></td>
</tr>
<tr>
<td><img src="docs/screenshots/team.png" width="400" alt="Team"/><br/><em>Team Management</em></td>
<td><img src="docs/screenshots/messages.png" width="400" alt="Messages"/><br/><em>Messaging</em></td>
</tr>
</table>
</div>

---

## 🛠️ Tech Stack

### Backend
| Technology | Purpose |
|------------|---------|
| **Java 17** | Core language |
| **Spring Boot 3.5.7** | Application framework |
| **Spring Security** | Authentication & authorization |
| **Spring Data JPA** | Database operations |
| **MySQL 8** | Primary database |
| **JWT** | Token-based authentication |
| **Lombok** | Boilerplate reduction |
| **Springdoc OpenAPI** | API documentation |
| **Maven** | Dependency management |

### Frontend
| Technology | Purpose |
|------------|---------|
| **React 18** | UI library |
| **Vite** | Build tool |
| **Tailwind CSS** | Styling |
| **React Router** | Navigation |
| **Axios** | HTTP client |
| **Lucide React** | Icons |
| **Recharts** | Data visualization |

---

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.6+

### 1. Clone the Repository

```bash
git clone https://github.com/ELmar465/meridian.git
cd meridian
```

### 2. Backend Setup

```bash
# Navigate to backend
cd meridian-backend

# Create MySQL database
mysql -u root -p -e "CREATE DATABASE meridian;"

# Configure database (edit application.yml)
# Update username, password, and database URL

# Run the application
mvn spring-boot:run
```

The API will start on `http://localhost:8081`

### 3. Frontend Setup

```bash
# Navigate to frontend
cd meridian-frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The app will open on `http://localhost:5173`

---

## ⚙️ Configuration

### Backend (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/meridian
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: your-super-secret-key-min-256-bits
  expiration: 86400000  # 24 hours

server:
  port: 8081
```

### Frontend (.env)

```env
VITE_API_URL=http://localhost:8081/api
```

---

## 📚 API Documentation

### Interactive Docs

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8081/swagger-ui.html |
| OpenAPI Spec | http://localhost:8081/v3/api-docs |

### Authentication

```bash
# Register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "firstName": "John",
    "lastName": "Doe"
  }'

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123"
  }'
```

### Core Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| **Auth** |||
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | User login |
| **Projects** |||
| GET | `/api/projects` | List all projects |
| POST | `/api/projects` | Create project |
| GET | `/api/projects/{id}` | Get project details |
| PUT | `/api/projects/{id}` | Update project |
| DELETE | `/api/projects/{id}` | Delete project |
| POST | `/api/projects/{id}/members` | Add team member |
| DELETE | `/api/projects/{id}/members/{userId}` | Remove member |
| **Issues** |||
| GET | `/api/issues` | List all issues |
| POST | `/api/issues/project/{projectId}` | Create issue |
| GET | `/api/issues/{id}` | Get issue details |
| PUT | `/api/issues/{id}` | Update issue |
| PUT | `/api/issues/{id}/status` | Update status |
| PUT | `/api/issues/{id}/assign` | Assign issue |
| **Users** |||
| GET | `/api/users` | List all users |
| GET | `/api/users/me` | Current user |
| PUT | `/api/users/{id}` | Update profile |
| POST | `/api/users/avatar` | Upload avatar |
| **Messages** |||
| GET | `/api/messages/conversations` | Get conversations |
| POST | `/api/messages` | Send message |
| PUT | `/api/messages/{userId}/read` | Mark as read |

---

## 🗃️ Data Models

### User Roles
| Role | Permissions |
|------|-------------|
| `ADMIN` | Full system access |
| `MANAGER` | Manage projects and teams |
| `MEMBER` | Standard user access |

### Issue Status Flow
```
TODO → IN_PROGRESS → IN_REVIEW → DONE
```

### Issue Priority
```
LOW → MEDIUM → HIGH → CRITICAL
```

---

## 🌐 Deployment

### Deploy to Production

#### Backend (Railway)

```bash
# Push to GitHub, then:
1. Go to railway.app
2. New Project → Deploy from GitHub
3. Add MySQL database
4. Set environment variables:
   - DATABASE_URL
   - JWT_SECRET
   - SPRING_PROFILES_ACTIVE=prod
5. Deploy
```

#### Frontend (Vercel)

```bash
# Push to GitHub, then:
1. Go to vercel.com
2. Import project
3. Set environment variable:
   - VITE_API_URL=https://your-api.railway.app/api
4. Deploy
```

### Docker Deployment

```bash
# Build and run with Docker Compose
docker-compose up -d
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  backend:
    build: ./meridian-backend
    ports:
      - "8081:8081"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://db:3306/meridian
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      - db

  frontend:
    build: ./meridian-frontend
    ports:
      - "80:80"
    environment:
      - VITE_API_URL=http://backend:8081/api

  db:
    image: mysql:8
    environment:
      - MYSQL_DATABASE=meridian
      - MYSQL_ROOT_PASSWORD=${DB_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

---

## 🧪 Testing

```bash
# Backend tests
cd meridian-backend
mvn test

# Frontend tests
cd meridian-frontend
npm test
```

---

## 📁 Project Structure

```
meridian/
├── meridian-backend/
│   ├── src/main/java/com/projectnova/meridian/
│   │   ├── config/          # Security, JWT, CORS config
│   │   ├── controller/      # REST API endpoints
│   │   ├── dao/             # JPA repositories
│   │   ├── dto/             # Request/Response objects
│   │   ├── exception/       # Error handling
│   │   ├── model/           # JPA entities
│   │   ├── service/         # Business logic
│   │   └── util/            # Utilities
│   └── src/main/resources/
│       └── application.yml
│
├── meridian-frontend/
│   ├── src/
│   │   ├── components/      # Reusable UI components
│   │   ├── context/         # React context providers
│   │   ├── hooks/           # Custom hooks
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   └── utils/           # Helper functions
│   ├── public/
│   └── index.html
│
├── docs/
│   └── screenshots/
├── docker-compose.yml
└── README.md
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** your changes (`git commit -m 'Add AmazingFeature'`)
4. **Push** to the branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

### Code Standards

- Follow Java/React best practices
- Write meaningful commit messages
- Add tests for new features
- Update documentation as needed

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

<div align="center">

**Elmar Jafarli**

[![GitHub](https://img.shields.io/badge/GitHub-ELmar465-181717?style=for-the-badge&logo=github)](https://github.com/ELmar465)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0A66C2?style=for-the-badge&logo=linkedin)](https://linkedin.com/in/elmar-jafarli)
[![Email](https://img.shields.io/badge/Email-Contact-EA4335?style=for-the-badge&logo=gmail)](mailto:elmarjafarli@outlook.com)

</div>

---

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Backend framework
- [React](https://reactjs.org/) - Frontend library
- [Tailwind CSS](https://tailwindcss.com/) - Styling
- [Lucide](https://lucide.dev/) - Icons
- [Vercel](https://vercel.com/) - Frontend hosting
- [Railway](https://railway.app/) - Backend hosting

---

<div align="center">

**Built with ❤️ by Elmar Jafarli**

⭐ Star this repo if you find it useful!

</div>