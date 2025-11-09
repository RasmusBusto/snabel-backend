#!/bin/bash
# Database setup script for Snabel Accounting

echo "Setting up database for Snabel Accounting..."

# For development, we'll use docker to run PostgreSQL
if ! command -v docker &> /dev/null; then
    echo "Docker not found. Please install Docker or set up PostgreSQL manually."
    echo "Manual setup: Create database 'snabel_accounting' with user 'snabel' and password 'snabel'"
    exit 1
fi

# Check if container already exists
if docker ps -a --format '{{.Names}}' | grep -q "^snabel-postgres$"; then
    echo "Container 'snabel-postgres' already exists. Starting it..."
    docker start snabel-postgres
else
    echo "Creating new PostgreSQL container..."
    docker run --name snabel-postgres \
        -e POSTGRES_DB=snabel_accounting \
        -e POSTGRES_USER=snabel \
        -e POSTGRES_PASSWORD=snabel \
        -p 5432:5432 \
        -d postgres:16-alpine
fi

echo "Waiting for PostgreSQL to be ready..."
sleep 3

echo "Database setup complete!"
echo "Connection details:"
echo "  Host: localhost"
echo "  Port: 5432"
echo "  Database: snabel_accounting"
echo "  User: snabel"
echo "  Password: snabel"
