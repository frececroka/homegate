package ch.homegate.initiator

import ch.homegate.QueryConstraintsRepository
import com.google.common.eventbus.EventBus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("local", "gcf")
@Suppress("UnstableApiUsage")
class CrawlInitiator(
    private val queryConstraintsRepository: QueryConstraintsRepository,
    @Qualifier("crawl-request-events") private val eventBus: EventBus
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun initiate() {
        log.info("Retreiving all query constraints")
        for (item in queryConstraintsRepository.getAll()) {
            log.info("Sending crawl request: $item")
            eventBus.post(item)
        }
    }

}
