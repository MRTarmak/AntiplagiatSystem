rootProject.name = "antiplagiat-system"

include(
    ":api-gateway",
    ":file-storage-service",
    ":file-analysis-service"
)
include("eureka-server")
