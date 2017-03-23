/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 */

using System.Collections.Generic;

namespace MicroServiceWorkshop.RapidsRivers
{
    public abstract class RapidsConnection
    {
        protected readonly List<IMessageListener> Listeners = new List<IMessageListener>();

        public virtual void Register(IMessageListener listener)
        {
            Listeners.Add(listener);
        }

        public abstract void Publish(string message);

        public interface IMessageListener
        {
            void HandleMessage(RapidsConnection sendPort, string message);
        }
    }
}