/* 
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 */

using MicroServiceWorkshop.RapidsRivers;
using NUnit.Framework;

namespace MicroServiceWorkshop.Tests.RapidsRivers
{
    // Ensures that PacketProblems operates correctly
    [TestFixture]
    public class PacketProblemsTest
    {
        private static readonly string ValidJson = "{\"key1\":\"value1\"}";
        private PacketProblems _problems;

        [SetUp]
        public void SetUp()
        {
            _problems = new PacketProblems(ValidJson);
        }

        [Test]
        public void NoProblemsFoundDefault()
        {
            Assert.False(_problems.HasErrors());
        }

        [Test]
        public void ErrorsDetected()
        {
            _problems.Error("Simple error");
            Assert.True(_problems.HasErrors());
            Assert.That(_problems.ToString(), Does.Contain("Simple error"));
        }

        [Test]
        public void SevereErrorsDetected()
        {
            _problems.SevereError("Severe error");
            Assert.True(_problems.HasErrors());
            Assert.That(_problems.ToString(), Does.Contain("Severe error"));
        }

        [Test]
        public void WarningsDetected()
        {
            _problems.Warning("Warning message");
            Assert.False(_problems.HasErrors());
            Assert.That(_problems.ToString(), Does.Contain("Warning message"));
        }

        [Test]
        public void InformationalMessageDetected()
        {
            _problems.Information("Information only message");
            Assert.False(_problems.HasErrors());
            Assert.That(_problems.ToString(), Does.Contain("Information only message"));
        }
    }
}