using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using MicroServiceWorkshop.RapidsRivers;
using MicroServiceWorkshop.RapidsRivers.RabbitMQ;
using Newtonsoft.Json.Linq;

namespace RentalOffer.Need
{
    class Need
    {
        static void Main(string[] args)
        {
            string host = args[0];
            string port = args[1];

            var rapidsConnection = new RabbitMqRapids("monitor_in_csharp", host, port);
            Publish(rapidsConnection);
        }

        private static void Publish(RapidsConnection rapidsConnection)
        {
            while (true)
            {
                var jsonMessage = NeedPacket();
                Console.WriteLine(" [<] {0}", jsonMessage);
                rapidsConnection.Publish(jsonMessage);
                System.Threading.Thread.Sleep(5000);
            }
        }

        private static string NeedPacket()
        {
            return
                "{\"need\":\"car_rental_offer\"}";
        }
    }
}
