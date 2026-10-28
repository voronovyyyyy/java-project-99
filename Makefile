.PHONY: setup build build-frontend test lint lint-fix start dev

GRADLE := ./gradlew
ifeq ($(OS),Windows_NT)
GRADLE := gradlew.bat
endif

setup: build-frontend
	$(GRADLE) bootJar installDist

build-frontend:
	npm ci
	npm run build

build: setup

test:
	$(GRADLE) test jacocoTestReport

lint:
	$(GRADLE) spotlessCheck

lint-fix:
	$(GRADLE) spotlessApply

start:
	$(GRADLE) bootRun

dev:
	$(GRADLE) bootRun
