package blazern.lexisoup

import kotlin.test.Test
import kotlin.test.assertEquals

class TemplateKmpCommonTest {
    @Test
    fun `failing purpose test`() {
        assertEquals(
            "this module is the default template for all new ones",
            TemplateKmpCommon.purpose,
        )
    }
}