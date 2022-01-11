-include .env
SHELL := /bin/bash
PROJECT_NAME?=sdcparser

USER_ID:=$(shell id -u $$USER)

# Git repos
# REPO=git@github.com:PuraJuniper/CareSoFA.git

EXPOSE_PORT?=8080

# Use the docker buildkit enhancements, see https://docs.docker.com/develop/develop-images/build_enhancements
export DOCKER_BUILDKIT=1

# artifacts-dir:
# 	mkdir -p artifacts

####################################################################################
# DOCKER
####################################################################################

TAG=latest
AWS_REPO=469914954971.dkr.ecr.us-east-2.amazonaws.com
LOCAL_REPO=junipercds
AWS_IMAGE=$(AWS_REPO)/$(PROJECT_NAME):$(TAG)
LOCAL_IMAGE=$(LOCAL_REPO)/$(PROJECT_NAME)

# Builds all Docker images

docker-aws-authenticate:
	aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin $(AWS_REPO)

docker-aws-image:
	docker buildx build --platform=linux/amd64 -t $(PROJECT_NAME) . 

docker-aws-tag:
	docker tag $(PROJECT_NAME) $(AWS_IMAGE)

docker-aws-push:
	docker push $(AWS_IMAGE)  

docker-image:
	docker build . -t $(LOCAL_IMAGE)

docker-containers:
	docker run -p $(EXPOSE_PORT):$(EXPOSE_PORT) -d --name $(PROJECT_NAME) $(LOCAL_IMAGE)

# docker-remove-image:
# 	docker rmi $(LOCAL_REPO)$(PROJECT_NAME)

# docker-stop:
# 	docker stop $(PROJECT_NAME)

# docker-remove-container: docker-stop
# 	docker rm $(PROJECT_NAME)

####################################################################################
# DEVELOPMENT
####################################################################################

aws-push: docker-aws-authenticate docker-aws-image docker-aws-tag docker-aws-push

# dev-build:
# 	docker-compose build

dev-up:
	docker-compose up -d

dev-down:
	docker-compose down

# dev-clean: docker-remove-container

# dev-super-clean: docker-remove-container docker-remove-image
