# Starts a Ruby session
docker run --name workshop_ruby -it -v c:/Users/dev/src/microservice_workshop/ruby_v2:/workshop -w /workshop fredgeorge/microservice_ruby:latest bash

# Starts monitor_all. Ensure IP and port correct!
docker exec -it workshop_ruby ruby ./lib/rental_offer/monitor_all.rb 192.168.254.120 5672

# Starts rental_need. Ensure IP and port correct!
docker exec -it workshop_ruby ruby ./lib/rental_offer/rental_need.rb 192.168.254.120 5672
