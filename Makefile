# Appointment scheduler — compile skeleton (javac only; no Maven/Gradle required).

SRC_ROOT := .
PKG_ROOT := scheduling
OUT      := out/classes

SOURCES := $(shell find $(PKG_ROOT) -name '*.java' | sort)

.PHONY: build clean tree

build:
	@mkdir -p $(OUT)
	javac --release 21 -d $(OUT) $(SOURCES)
	@echo "OK: compiled $$(echo $(SOURCES) | wc -w | tr -d ' ') source(s) → $(OUT)"

clean:
	rm -rf out

tree:
	@find $(PKG_ROOT) -type f -name '*.java' | sort
