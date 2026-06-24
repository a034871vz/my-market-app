plugins {
    id("org.openapi.generator")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("io.swagger.core.v3:swagger-annotations:2.2.28")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$rootDir/payment-api/payment-api.yaml")
    outputDir.set(layout.buildDirectory.dir("generated").get().asFile.absolutePath)
    apiPackage.set("ru.yandex.practicum.payment.api")
    modelPackage.set("ru.yandex.practicum.payment.model")
    invokerPackage.set("ru.yandex.practicum.payment.invoker")
    configOptions.set(mapOf(
        "reactive" to "true",
        "interfaceOnly" to "true",
        "useSpringBoot3" to "true",
        "useTags" to "true"
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