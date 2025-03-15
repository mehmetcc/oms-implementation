APP_NAME=myapp
DOCKER_IMAGE=myapp:latest

build:
	go build -o $(APP_NAME) ./cmd

run:
	go run ./cmd

docker-build:
	docker build -t $(DOCKER_IMAGE) .

docker-run:
	docker run -p 668:668 $(DOCKER_IMAGE)
