/* 
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 */

using System;
using MicroServiceWorkshop.RapidsRivers;
using MicroServiceWorkshop.RapidsRivers.RabbitMQ;
using Newtonsoft.Json.Linq;

namespace MicroServiceWorkshop.CarRentalOffer
{
    public class Monitor : RapidsRivers.River.IPacketListener
    {

        public static void Main(string[] args)  // Pass in <IP address> and <port> for RabbitMQ
        {
            var rapidsConnection = new RabbitMqRapids("monitor_all_csharp", args[0], args[1]);
            var river = new River(rapidsConnection);
            river.Register(new Monitor());
        }

        public void ProcessPacket(RapidsConnection connection, JObject jsonPacket, PacketProblems warnings)
        {
            Console.WriteLine(" [x] {0}", warnings);
        }

        public void ProcessError(RapidsConnection connection, PacketProblems errors)
        {
            Console.WriteLine(" [x] {0}", errors);
        }
    }
}