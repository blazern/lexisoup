package blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.di

import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsForExamplesProvider
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsAccentsEnhancerProvider
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsAccentsEnhancerProviderImpl
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsForExamplesProviderImpl
import org.koin.dsl.module

fun examplesToolsModule() = module {
    factory<FormsForExamplesProvider> {
        FormsForExamplesProviderImpl(
            kaikki = get(),
        )
    }
    factory<FormsAccentsEnhancerProvider> {
        FormsAccentsEnhancerProviderImpl(
            formsProvider = get(),
        )
    }
}
