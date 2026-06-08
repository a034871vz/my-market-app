plugins {
    id("org.openapi.generator")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.28")

    implementation("org.liquibase:liquibase-core")

    runtimeOnly("org.postgresql:r2dbc-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
}

openApiGenerate {
    generatorName.set("java")
    library.set("webclient")
    inputSpec.set("$rootDir/payment-api/payment-api.yaml")
    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)
    apiPackage.set("ru.yandex.practicum.payment.client")
    modelPackage.set("ru.yandex.practicum.payment.dto")
    invokerPackage.set("ru.yandex.practicum.payment.invoker")
    configOptions.set(mapOf(
        "reactive" to "true",
        "useSpringBoot3" to "true",
        "useTags" to "true",
        "dateLibrary" to "java8",
        "useJakartaEe" to "true"
    ))
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/src/main/java"))
        }
    }
}

tasks.withType<JavaCompile> {
    dependsOn(tasks.named("openApiGenerate"))
}