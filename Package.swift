// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "KvsStorage",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(name: "KvsStorage", targets: ["KvsStorage"])
    ],
    targets: [
        .binaryTarget(
            name: "KvsStorage",
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/1.0.0-ALPHA02/KvsStorage.xcframework.zip",
            checksum:"6f0b2832759d7d3d2c97a7efc879a894fbca533e4eb3053809b2f17130b242e6")
    ]
)

