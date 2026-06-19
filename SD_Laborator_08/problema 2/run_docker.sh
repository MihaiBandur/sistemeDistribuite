#!/bin/bash

echo "=== Curatare containere vechi ==="
docker rm -f message-manager database teacher heartbeat student 2>/dev/null

echo "=== Configurare Retea si Volume ==="
# Creăm rețeaua internă (ignoram eroarea dacă există deja)
docker network create lab-network 2>/dev/null
# Creăm volumul pentru baza de date
docker volume create sqlite-data 2>/dev/null

echo "=== Construire Imagini Docker ==="
echo "Construiesc MessageManager..."
docker build -t message-manager-img ./MessageManagerMicroservice

echo "Construiesc Database..."
docker build -t database-img ./DatabaseMicroservice

echo "Construiesc Teacher..."
docker build -t teacher-img ./TeacherMicroservice

echo "Construiesc Heartbeat..."
docker build -t heartbeat-img ./HeartbeatMicroservice

echo "Construiesc Student..."
docker build -t student-img ./StudentMicroservice

echo "=== Pornire Containere ==="

# 1. Message Manager
echo "Pornesc Message Manager..."
docker run -d \
  --name message-manager \
  --network lab-network \
  -p 1500:1500 \
  message-manager-img

# 2. Database
echo "Pornesc Database..."
docker run -d \
  --name database \
  --network lab-network \
  -p 1700:1700 \
  -v sqlite-data:/db \
  database-img

# Așteptăm puțin ca primele două să se inițializeze
sleep 2

# 3. Teacher
echo "Pornesc Teacher..."
docker run -d \
  --name teacher \
  --network lab-network \
  -p 1600:1600 \
  -e MESSAGE_MANAGER_HOST=message-manager \
  -e DB_HOST=database \
  --restart on-failure \
  teacher-img

# 4. Heartbeat
echo "Pornesc Heartbeat..."
docker run -d \
  --name heartbeat \
  --network lab-network \
  -e MESSAGE_MANAGER_HOST=message-manager \
  -e HEARTBEAT_INTERVAL_MS=5000 \
  -e HEARTBEAT_MISSED_THRESHOLD=3 \
  --restart on-failure \
  heartbeat-img

# 5. Student
# Folosim -dit pentru a menține consola deschisă (echivalentul stdin_open: true și tty: true)
echo "Pornesc Student..."
docker run -dit \
  --name student \
  --network lab-network \
  -e MESSAGE_MANAGER_HOST=message-manager \
  --restart on-failure \
  student-img

echo "======================================================"
echo "Toate microserviciile au fost pornite folosind Docker!"
echo ""
echo "Pentru a pune intrebari ca student, ruleaza:"
echo "  docker attach student"
echo ""
echo "Pentru a iesi din consola studentului FARA a-l opri, apasa:"
echo "  Ctrl+P, urmat de Ctrl+Q"
echo ""
echo "Pentru a opri toate containerele, ruleaza:"
echo "  docker rm -f message-manager database teacher heartbeat student"
echo "======================================================"