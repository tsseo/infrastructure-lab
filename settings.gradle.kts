rootProject.name = "infrastructure-lab"

include(
    ":apps:lab-redis",
    ":modules:redis",
    ":supports:monitoring",
)
