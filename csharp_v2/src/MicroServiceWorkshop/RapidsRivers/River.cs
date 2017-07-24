/*
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 */

using System;
using System.Collections.Generic;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace MicroServiceWorkshop.RapidsRivers
{
    public class River : RapidsConnection.IMessageListener
    {
        private const string RepublishCountKey = "system_republish_count";

        private readonly List<IPacketListener> _listeners = new List<IPacketListener>();
        private readonly List<IValidation> _validations = new List<IValidation>();

        public River(RapidsConnection rapidsConnection)
        {
            rapidsConnection.Register(this);
        }

        public void Register(IPacketListener listener)
        {
            _listeners.Add(listener);
        }
        
        public void HandleMessage(RapidsConnection sendPort, string message)
        {
            PacketProblems problems = new PacketProblems(message);
            JObject jsonPacket = JsonPacket(message, problems);
            Validate(problems, jsonPacket);
            TriggerService(sendPort, problems, jsonPacket);
        }

        private void TriggerService(RapidsConnection sendPort, PacketProblems problems, JObject jsonPacket)
        {
            if (problems.HasErrors())
            {
                OnError(sendPort, problems);
                return;
            }
            IncrementRepublishCount(jsonPacket);
            OnPacket(sendPort, jsonPacket, problems);
        }

        private void Validate(PacketProblems problems, JObject jsonPacket)
        {
            foreach (IValidation v in _validations)
            {
                if (problems.AreSevere()) break;
                v.Validate(jsonPacket, problems);
            }
        }

        private JObject JsonPacket(string message, PacketProblems problems)
        {
            try
            {
                return JObject.Parse(message);
            }
            catch (JsonException)
            {
                problems.SevereError("Invalid JSON format per NewtonSoft JSON library");
            }
            catch (Exception e)
            {
                problems.SevereError("Unknown failure. Exception message is: " + e.Message);
            }
            return null;
        }

        private void IncrementRepublishCount(JObject packet)
        {
            if (packet[RepublishCountKey] == null || packet[RepublishCountKey].Type != JTokenType.Integer)
                packet[RepublishCountKey] = 0;
            packet[RepublishCountKey] = (int)packet[RepublishCountKey] + 1;
        }

        private void OnError(RapidsConnection sendPort, PacketProblems errors)
        {
            foreach (IPacketListener l in _listeners)
                l.ProcessError(sendPort, errors);
        }

        private void OnPacket(RapidsConnection sendPort, JObject jsonPacket, PacketProblems warnings)
        {
            foreach (IPacketListener l in _listeners)
                l.ProcessPacket(sendPort, jsonPacket, warnings);
        }

        public River RequireValue(string key, string value)
        {
            _validations.Add(new RequiredValue(key, value));
            return this;
        }

        public River Require(params string[] jsonKeyStrings)
        {
            _validations.Add(new RequiredKeys(jsonKeyStrings));
            return this;
        }

        public River Forbid(params string[] jsonKeyStrings)
        {
            _validations.Add(new ForbiddenKeys(jsonKeyStrings));
            return this;
        }

        private static bool IsMissingValue(JToken token)
        {
            // Tests as suggested by NewtonSoft recommendations
            return token == null ||
                   token.Type == JTokenType.Array && !token.HasValues ||
                   token.Type == JTokenType.Object && !token.HasValues ||
                   token.Type == JTokenType.String && token.ToString() == string.Empty ||
                   token.Type == JTokenType.Null;
        }

        public interface IPacketListener
        {
            void ProcessPacket(RapidsConnection connection, JObject jsonPacket, PacketProblems warnings);
            void ProcessError(RapidsConnection connection, PacketProblems errors);
        }

        private interface IValidation
        {
            void Validate(JObject jsonPacket, PacketProblems problems);
        }

        private class RequiredValue : IValidation
        {
            private readonly string _requiredKey;
            private readonly string _requiredValue;

            internal RequiredValue(string key, string value) // Possible use of KeyValuePair object
            {
                _requiredKey = key;
                _requiredValue = value;
            }

            public void Validate(JObject jsonPacket, PacketProblems problems)
            {
                if (jsonPacket[_requiredKey] == null)
                {
                    problems.Error($"Missing required key \'{_requiredKey}\'");
                    return;
                }
                if ((string) jsonPacket[_requiredKey] != _requiredValue)
                    problems.Error($"Required key \'{_requiredKey}\' should be \'{_requiredValue}\', but has unexpected value of \'{jsonPacket[_requiredKey]}\'");
            }
        }

        private class RequiredKeys : IValidation
        {
            private readonly string[] _requiredKeys;

            internal RequiredKeys(string[] requiredKeys)
            {
                _requiredKeys = requiredKeys;
            }

            public void Validate(JObject jsonPacket, PacketProblems problems)
            {
                foreach (string key in _requiredKeys)
                {
                    if (IsMissingValue(jsonPacket[key]))
                        problems.Error($"Missing required key \'{key}\'");
                    else
                        problems.Information($"Required key \'{key}\' actually exists");
                }
            }
        }

        private class ForbiddenKeys : IValidation
        {
            private readonly string[] _forbiddenKeys;

            internal ForbiddenKeys(string[] forbiddenKeys)
            {
                _forbiddenKeys = forbiddenKeys;
            }

            public void Validate(JObject jsonPacket, PacketProblems problems)
            {
                foreach (string key in _forbiddenKeys)
                {
                    if (IsMissingValue(jsonPacket[key]))
                        problems.Information($"Forbidden key \'{key}\' does not exist");
                    else
                        problems.Error($"Forbidden key \'{key}\' actually exists");
                }
            }
        }
    }
}