using System;

using MicroServiceWorkshop.RapidsRivers;
using MicroServiceWorkshop.RapidsRivers.RabbitMQ;
using Newtonsoft.Json.Linq;

namespace RentalOffer.Monitor
{
    class Monitor : River.IPacketListener
    {
        static void Main(string[] args)
        {
            string host = args[0];
            string port = args[1];

            var rapidsConnection = new RabbitMqRapids("monitor_in_csharp", host, port);
            var river = new River(rapidsConnection);
            // See RiverTest for various functions River supports to aid in filtering, like:
            //river.Require("key1", "key2");       // Reject packet unless it has key1 and key2
            //river.Forbid("key1", "key2");        // Reject packet if it does have key1 or key2
            river.Register(new Monitor());         // Hook up to the river to start receiving traffic
        }

        public void ProcessPacket(RapidsConnection connection, JObject jsonPacket, PacketProblems warnings)
        {
            Console.WriteLine(" [*] {0}", warnings);
        }

        public void ProcessError(RapidsConnection connection, PacketProblems errors)
        {
            Console.WriteLine(" [x] {0}", errors);
        }
    }
}
