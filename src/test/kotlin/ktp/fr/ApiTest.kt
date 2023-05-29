package ktp.fr;

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.assertEquals
import org.junit.Test

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(
            "## WELCOME on KTOR-SVELTY V.0.0.1\n" +
                    "To display the most useful CLI, please go to «/help»", response.bodyAsText()
        )
    }

}