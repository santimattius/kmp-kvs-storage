// swift-tools-version:5.3
import PackageDescription

let package = Package(
    name: "KvsStorage",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(
            name: "KvsStorage",
            targets: ["KvsStorage"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "KvsStorage",
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/1.0.0-ALPHA09/KvsStorage-1.0.0-ALPHA09.xcframework.zip",
            checksum: "22152227e19cac554d8a3d864475fb1e50fbb757cfcd63d4064e5f7117e3a9f8"
        )
    ]
)
