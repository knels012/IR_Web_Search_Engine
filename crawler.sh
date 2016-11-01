#!/bin/bash
# Web Crawler bash script

# DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# seed_file="$1"
# ls "$DIR"
# seed_file="seed.txt"
# java -jar CS172_Web_Crawler.jar "$seed_file" "$2" "$3" "$4"
java -jar CS172_Web_Crawler.jar "$@"
