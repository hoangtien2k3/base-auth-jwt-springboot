#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print banner
echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║                                                           ║"
echo "║        Auth JWT Spring Boot with Redis                   ║"
echo "║        Startup Script                                    ║"
echo "║                                                           ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

if ! command_exists docker; then
    echo -e "${RED}❌ Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

if ! command_exists docker-compose; then
    echo -e "${RED}❌ Docker Compose is not installed. Please install Docker Compose first.${NC}"
    exit 1
fi

if ! command_exists java; then
    echo -e "${RED}❌ Java is not installed. Please install Java 17+ first.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ All prerequisites are installed${NC}\n"

# Start Docker services
echo -e "${YELLOW}Starting Docker services (PostgreSQL + Redis)...${NC}"
docker-compose up -d

# Wait for services to be ready
echo -e "${YELLOW}Waiting for services to be ready...${NC}"
sleep 5

# Check PostgreSQL
echo -e "${YELLOW}Checking PostgreSQL...${NC}"
until docker exec auth-jwt-postgres pg_isready -U admin -d auth_jwt_db > /dev/null 2>&1; do
    echo -e "${YELLOW}Waiting for PostgreSQL...${NC}"
    sleep 2
done
echo -e "${GREEN}✅ PostgreSQL is ready${NC}"

# Check Redis
echo -e "${YELLOW}Checking Redis...${NC}"
until docker exec auth-jwt-redis redis-cli ping > /dev/null 2>&1; do
    echo -e "${YELLOW}Waiting for Redis...${NC}"
    sleep 2
done
echo -e "${GREEN}✅ Redis is ready${NC}\n"

# Display service URLs
echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                   Services Status                         ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
echo -e "${GREEN}✅ PostgreSQL:      ${NC}localhost:5432"
echo -e "${GREEN}✅ Redis:           ${NC}localhost:6379"
echo -e "${GREEN}✅ Redis Commander: ${NC}http://localhost:8081"
echo -e "${GREEN}✅ pgAdmin:         ${NC}http://localhost:5050"
echo -e ""

# Ask if user wants to start Spring Boot application
echo -e "${YELLOW}Do you want to start the Spring Boot application? (y/n)${NC}"
read -r response

if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    echo -e "${YELLOW}Starting Spring Boot application...${NC}\n"
    
    # Check if gradlew exists
    if [ -f "./gradlew" ]; then
        chmod +x ./gradlew
        ./gradlew bootRun
    else
        echo -e "${RED}❌ gradlew not found. Please run manually: ./gradlew bootRun${NC}"
        exit 1
    fi
else
    echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║              Manual Start Instructions                    ║${NC}"
    echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
    echo -e "${YELLOW}To start the application manually, run:${NC}"
    echo -e "${GREEN}  ./gradlew bootRun${NC}"
    echo -e ""
    echo -e "${YELLOW}To stop Docker services, run:${NC}"
    echo -e "${GREEN}  docker-compose down${NC}"
    echo -e ""
    echo -e "${YELLOW}To view logs, run:${NC}"
    echo -e "${GREEN}  docker-compose logs -f${NC}"
    echo -e ""
fi

echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                  Useful Commands                          ║${NC}"
echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
echo -e "${YELLOW}Redis CLI:${NC}"
echo -e "  docker exec -it auth-jwt-redis redis-cli"
echo -e ""
echo -e "${YELLOW}PostgreSQL CLI:${NC}"
echo -e "  docker exec -it auth-jwt-postgres psql -U admin -d auth_jwt_db"
echo -e ""
echo -e "${YELLOW}View Docker logs:${NC}"
echo -e "  docker-compose logs -f redis"
echo -e "  docker-compose logs -f postgres"
echo -e ""
echo -e "${YELLOW}Stop all services:${NC}"
echo -e "  docker-compose down"
echo -e ""
echo -e "${GREEN}Happy coding! 🚀${NC}"
