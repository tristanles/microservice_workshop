/* 
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 */

using System.Collections.Generic;
using System.Text;
using System.Linq;

namespace MicroServiceWorkshop.RapidsRivers
{
    // Understands various information about a received JSON packet
    public class PacketProblems
    {
        private readonly string _originalJson;
        private readonly List<string> _informationalMessages = new List<string>();
        private readonly List<string> _warnings = new List<string>();
        private readonly List<string> _errors = new List<string>();
        private readonly List<string> _severeErrors = new List<string>();

        public PacketProblems(string originalJson)
        {
            _originalJson = originalJson;
        }

        public bool AreSevere()
        {
            return _severeErrors.Any();
        }

        public bool HasErrors()
        {
            return AreSevere() ||_errors.Any();
        }

        private bool HasMessages()
        {
            return HasErrors() || _informationalMessages.Any() || _warnings.Any();
        }

        public void Information(string explanation)
        {
            _informationalMessages.Add(explanation);
        }

        public void Warning(string explanation)
        {
            _warnings.Add(explanation);
        }

        public void Error(string explanation)
        {
            _errors.Add(explanation);
        }

        public void SevereError(string explanation)
        {
            _severeErrors.Add(explanation);
        }

        public override string ToString()
        {
            if (!HasMessages()) return "No errors detected in JSON:\n\t" + _originalJson;
            StringBuilder builder = new StringBuilder();
            builder.Append("Errors and/or messages exist. Original JSON string is:\n\t");
            builder.Append(_originalJson);
            Append("Severe errors", _severeErrors, builder);
            Append("Errors", _errors, builder);
            Append("Warnings", _warnings, builder);
            Append("Information", _informationalMessages, builder);
            builder.Append("\n");
            return builder.ToString();
        }

        private void Append(string label, List<string> explanations, StringBuilder builder)
        {
            if (explanations.Count == 0) return;
            builder.Append("\n");
            builder.Append(label);
            builder.Append(": ");
            builder.Append(explanations.Count);
            foreach (string explanation in explanations)
            {
                builder.Append("\n\t");
                builder.Append(explanation);
            }
        }
    }
}