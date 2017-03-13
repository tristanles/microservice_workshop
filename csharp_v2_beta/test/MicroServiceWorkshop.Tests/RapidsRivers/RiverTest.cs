/* 
 * Copyright (c) 2016 by Fred George
 * May be used freely except for training; license required for training.
 */
 
using MicroServiceWorkshop.RapidsRivers;
using Newtonsoft.Json.Linq;
using NUnit.Framework;

namespace MicroServiceWorkshop.Tests.RapidsRivers
{
    // Ensures Packets are well-formed or catch construction errors
    [TestFixture]
    public class RiverTest
    {
        private const string SolutionString =
            "{\"need\":\"car_rental_offer\"," +
            "\"user_id\":456," +
            "\"solutions\":[" +
            "{\"offer\":\"15% discount\"}," +
            "{\"offer\":\"500 extra points\"}," +
            "{\"offer\":\"free upgrade\"}" +
            "]," +
            "\"frequent_renter\":\"\"," +
            "\"sample_float_key\":1.25," +
            "\"system_read_count\":2," +
            "\"contributing_services\":[]}";

        private const string SimplePacketString =
            "{\"frequent_renter\":\"silver\"}";

        private const string MissingComma =
            "{\"frequent_renter\":\"\" \"system_read_count\":2}";

        private const string InvalidReadCountValue =
            "{\"frequent_renter\":\"\",\"system_read_count\":\"erroneous_value\"}";

        private const string NeedKey = "need";
        private const string UserIdKey = "user_id";
        private const string SampleFloatKey = "sample_float_key";
        private const string KeyToBeAdded = "key_to_be_added";
        private const string EmptyArrayKey = "contributing_services";
        private const string InterestingKey = "frequent_renter";
        private const string SolutionsKey = "solutions";
        private const string ReadCountKey = "system_read_count";

        private TestRapidsConnection _rapidsConnection;
        private River _river;

        [SetUp]
        public void SetUp()
        {
            _rapidsConnection = new TestRapidsConnection();
            _river = new River(_rapidsConnection);
            _rapidsConnection.Register(_river);
        }

        [Test]
        public void ValidJson()
        {
            _river.Register(new TestRiver((connection, jsonPacket, warnings) =>
            {
                Assert.False(warnings.HasErrors());
            }));
            _rapidsConnection.Process(SolutionString);
        }

        [Test]
        public void InvalidJson()
        {
            _river.Register(new TestRiver((connection, problems) =>
            {
                Assert.True(problems.HasErrors());
                Assert.That(problems.ToString(), Does.Contain("Invalid JSON format"));
            }));
            _rapidsConnection.Process(MissingComma);
        }

        [Test]
        public void RequiredStringKeyExists()
        {
            _river.Require(NeedKey);
            _river.Register(new TestRiver((connection, jsonPacket, warnings) =>
            {
                Assert.False(warnings.HasErrors());
                Assert.AreEqual("car_rental_offer", (string)jsonPacket[NeedKey]);
            }));
            _rapidsConnection.Process(SolutionString);
        }

        [Test]
        public void RequiredIntegerKeyExists()
        {
            _river.Require(UserIdKey);
            _river.Register(new TestRiver((connection, jsonPacket, warnings) =>
            {
                Assert.False(warnings.HasErrors());
                Assert.AreEqual(456, (int)jsonPacket[UserIdKey]);
            }));
            _rapidsConnection.Process(SolutionString);
        }

        [Test]
        public void RequiredFloatKeyExists()
        {
            _river.Require(SampleFloatKey);
            _river.Register(new TestRiver((connection, jsonPacket, warnings) =>
            {
                Assert.False(warnings.HasErrors());
                Assert.AreEqual(1.25, (float)jsonPacket[SampleFloatKey]);
            }));
            _rapidsConnection.Process(SolutionString);
        }

        [Test]
        public void RequiredMultipleKeysExist()
        {
            _river.Require(NeedKey, UserIdKey, SampleFloatKey);
            _river.Register(new TestRiver((connection, jsonPacket, warnings) =>
            {
                Assert.False(warnings.HasErrors());
                Assert.AreEqual("car_rental_offer", (string)jsonPacket[NeedKey]);
                Assert.AreEqual(456, (int)jsonPacket[UserIdKey]);
                Assert.AreEqual(1.25, (float)jsonPacket[SampleFloatKey]);
            }));
            _rapidsConnection.Process(SolutionString);
        }

        [Test]
        public void MissingRequiredKeyDetected()
        {
            _river.Require("missing_key");
            _river.Register(new TestRiver((connection, problems) =>
            {
                Assert.True(problems.HasErrors());
                Assert.That(problems.ToString(), Does.Contain("missing_key"));
            }));
            _rapidsConnection.Process(SolutionString);
        }

        [Test]
        public void ForbiddenKeyMissing()
        {
            _river.Forbid("forbidden_key_1", "forbidden_key_2");
            _river.Register(new TestRiver((connection, jsonPacket, warnings) =>
            {
                Assert.False(warnings.HasErrors());
                Assert.That(warnings.ToString(), Does.Contain("forbidden_key_1"));
                Assert.That(warnings.ToString(), Does.Contain("forbidden_key_2"));
            }));
            _rapidsConnection.Process(SolutionString);
        }

        [Test]
        public void EmptyStringPassesForbiddenValidation()
        {
            _river.Forbid(InterestingKey);
            _river.Register(new TestRiver((connection, jsonPacket, warnings) =>
            {
                Assert.False(warnings.HasErrors());
                Assert.That(warnings.ToString(), Does.Contain(InterestingKey));
            }));
            _rapidsConnection.Process(SolutionString);
        }

        [Test]
        public void EmptyArrayPassesForbiddenValidation()
        {
            _river.Forbid(EmptyArrayKey);
            _river.Register(new TestRiver((connection, jsonPacket, warnings) =>
            {
                Assert.False(warnings.HasErrors());
                Assert.That(warnings.ToString(), Does.Contain(EmptyArrayKey));
            }));
            _rapidsConnection.Process(SolutionString);
        }

        [Test]
        public void ForbiddenFieldRejected()
        {
            _river.Forbid(NeedKey);
            _river.Register(new TestRiver((connection, problems) =>
            {
                Assert.True(problems.HasErrors());
                Assert.That(problems.ToString(), Does.Contain(NeedKey));
            }));
            _rapidsConnection.Process(SolutionString);
        }

        [Test]
        public void ReadCountIncremented()
        {
            _river.Register(new TestRiver((connection, jsonPacket, warnings) =>
            {
                Assert.False(warnings.HasErrors());
                Assert.AreEqual(3, (int)jsonPacket[ReadCountKey]);
            }));
            _rapidsConnection.Process(SolutionString);
        }

        [Test]
        public void ReadCountInjectedIfMissing()
        {
            _river.Register(new TestRiver((connection, jsonPacket, warnings) =>
            {
                Assert.False(warnings.HasErrors());
                Assert.AreEqual(1, (int)jsonPacket[ReadCountKey]);
            }));
            _rapidsConnection.Process(SimplePacketString);
        }

        [Test]
        public void ReadCountResetIfInvalid()
        {
            _river.Register(new TestRiver((connection, jsonPacket, warnings) =>
            {
                Assert.False(warnings.HasErrors());
                Assert.AreEqual(1, (int)jsonPacket[ReadCountKey]);
            }));
            _rapidsConnection.Process(InvalidReadCountValue);
        }

        // Understands a mock RapidsConnection to allow tests to send messages
        private class TestRapidsConnection : RapidsConnection
        {
            public override void Publish(string message) { }  // Ignore for this test
            internal void Process(string message)
            {
                foreach (IMessageListener l in Listeners) l.HandleMessage(this, message);
            }
        }

        // Understands a mock River that invokes different delegates on success or failure
        delegate void Packet(RapidsConnection connection, JObject jsonPacket, PacketProblems warnings);

        delegate void OnError(RapidsConnection connection, PacketProblems errors);

        private class TestRiver : River.IPacketListener
        {
            private readonly Packet _successDelegate;
            private readonly OnError _failureDelegate;

            public TestRiver(Packet successDelegate) : this(successDelegate, UnexpectedFailure) { }
            public TestRiver(OnError failureDelegate) : this(UnexpectedSuccess, failureDelegate) { }

            private TestRiver(Packet successDelegate, OnError failureDelegate)
            {
                _successDelegate = successDelegate;
                _failureDelegate = failureDelegate;
            }

            public void ProcessPacket(RapidsConnection connection, JObject jsonPacket, PacketProblems warnings)
            {
                _successDelegate(connection, jsonPacket, warnings);
            }

            public void ProcessError(RapidsConnection connection, PacketProblems errors)
            {
                _failureDelegate(connection, errors);
            }

            private static void UnexpectedSuccess(RapidsConnection connection, JObject jsonPacket,
                PacketProblems warnings)
            {
                Assert.Fail("Unexpected success parsing JSON packet. Packet is:\n"
                            + jsonPacket
                            + "\nConclusions from parsing/validation are:\n"
                            + warnings);
            }

            private static void UnexpectedFailure(RapidsConnection connection, PacketProblems errors)
            {
                Assert.Fail("Unexpected JSON packet problem(s):\n" + errors);
            }
        }
    }
}