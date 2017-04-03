#!/usr/bin/env ruby
# encoding: utf-8

# Copyright (c) 2017 by Fred George.
# May be used freely except for training; license required for training.

# For debugging...
# require 'pry'
# require 'pry-nav'

require 'securerandom'

require '../rapids_rivers/rabbit_mq/rabbit_mq_rapids'

# Understands the complete stream of messages on an event bus
class RentalNeed
  attr_reader :service_name

  def initialize(host_ip, port)
    @rapids_connection = RabbitMqRapids.new(host_ip, port)
    @service_name = 'rental_need_ruby_' + SecureRandom.uuid
  end

  def start
    loop do
      @rapids_connection.publish need_packet
      puts " [<] Published a rental offer need on the bus:\n\t     #{need_packet.to_json}"
      sleep 5
    end
  end

  private

    def need_packet
      Packet.new(need: 'car_rental_offer')
    end

end

RentalNeed.new(ARGV.shift, ARGV.shift.to_i).start
