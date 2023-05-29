package ktp.fr;

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import org.junit.Test;

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("## WELCOME on KTOR-SVELTY V.0.0.1\n" +
                "To display the most useful CLI, please go to «/help»", response.bodyAsText())
    }

}