#!/bin/bash
gradle --version
gradle build --continuous --console=plain &
gradle bootRun --console=plain