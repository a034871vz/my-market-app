plugins {
    application
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0")
    implementation("org.springframework.boot:spring-boot-starter-validation")
}

application {
    mainClass = "ru.yandex.practicum.payment.PaymentServiceApplication"
}