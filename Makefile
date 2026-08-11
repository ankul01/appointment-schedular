# Appointment scheduler — Maven / Spring Boot

.PHONY: build test clean tree

MVN ?= ./mvnw

build:
	$(MVN) -q -DskipTests package

test:
	$(MVN) test

clean:
	$(MVN) clean
	rm -rf out

tree:
	@find src -type f -name '*.java' | sort
