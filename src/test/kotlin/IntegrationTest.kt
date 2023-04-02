import com.fazecast.jSerialComm.SerialPort
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.RunningMongoProcess
import de.flapdoodle.reverse.transitions.Start
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

open class IntegrationTest {
    companion object {
        private lateinit var runningMongoProcess: RunningMongoProcess

        @Suppress("unused", "unused")
        @BeforeAll
        @JvmStatic
        fun setupTests() {
            mockClock()
            mockSystemPorts()
            runMongo()
        }

        private fun mockClock(){
            val now = 1680345742000
            val fixedClock = Clock.fixed(Instant.ofEpochMilli(now), ZoneId.systemDefault())

            mockkStatic(Clock::class)
            // Default system clock
            every { Clock.systemUTC() } returns fixedClock
        }

        private fun mockSystemPorts(){
            // we cannot create a SerialPort instance ourselves, so let's use some generic port
            val ttyPort = SerialPort.getCommPort("/tty")

            mockkStatic(SerialPort::class)
            // Default system clock
            every { SerialPort.getCommPorts() } returns arrayOf(ttyPort)
        }

        private fun runMongo(){
            val mongoPort = 33027
            val mongod = Mongod.builder().net(
                Start.to(Net::class.java).initializedWith(
                    Net.defaults().withPort(mongoPort)
                )
            ).build()
            runningMongoProcess = mongod.start(Version.Main.V5_0).current()
            System.setProperty("embeddedMongoPort", mongoPort.toString())
        }

        @Suppress("unused")
        @AfterAll
        @JvmStatic
        fun cleanupTests() {
            runningMongoProcess.stop()
        }
    }
}
