package blazern.lexisoup.domain.config.api

import blazern.lexisoup.domain.config.api.model.Config
import kotlinx.coroutines.flow.Flow

interface ConfigProvider {
    val config: Flow<Config>
}
