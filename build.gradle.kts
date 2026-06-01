import org.springframework.boot.gradle.tasks.bundling.BootJar

// ─── 플러그인 선언 ─────────────────────────────────────────────────────────────
// apply false: 루트 프로젝트에는 적용하지 않고 서브 프로젝트에서 선택적으로 적용
plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

// ─── 전체 프로젝트 공통 설정 ───────────────────────────────────────────────────
allprojects {
    group = "io.github.tsseo"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

// ─── 서브 프로젝트 공통 설정 ───────────────────────────────────────────────────
subprojects {
    // Java + Spring 플러그인을 모든 서브 프로젝트에 적용
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    // Java 21 툴체인 설정
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    // 모든 서브 프로젝트 공통 테스트 의존성
    dependencies {
        "testImplementation"(rootProject.libs.junit.jupiter)
        "testRuntimeOnly"(rootProject.libs.junit.platform.launcher)
    }

    // JUnit5 플랫폼으로 테스트 실행
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // ─── BootJar / Jar 기본값 설정 ─────────────────────────────────────────────
    // 기본: 일반 Jar 활성화, BootJar 비활성화 (modules/supports 용)
    // apps 하위 프로젝트만 BootJar 활성화 (실행 가능한 Spring Boot 앱)
    tasks.withType<Jar> { enabled = true }
    tasks.withType<BootJar> { enabled = false }
}

// ─── apps 하위 프로젝트: BootJar 활성화 ────────────────────────────────────────
// apps 디렉토리의 자식 프로젝트만 실행 가능한 BootJar 를 생성
configure(allprojects.filter { it.parent?.name == "apps" }) {
    tasks.withType<Jar> { enabled = false }
    tasks.withType<BootJar> { enabled = true }
}

// ─── 컨테이너 프로젝트 태스크 비활성화 ────────────────────────────────────────
// apps, modules, supports 는 실제 코드가 없는 논리적 그룹 프로젝트이므로
// Gradle 태스크를 실행하지 않도록 비활성화
project(":apps") { tasks.configureEach { enabled = false } }
project(":modules") { tasks.configureEach { enabled = false } }
project(":supports") { tasks.configureEach { enabled = false } }
