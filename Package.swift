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
            url: "https://github.com/santimattius/kmp-kvs-storage/releases/download/1.2.0-ALPHA01/KvsStorage-1.2.0-ALPHA01.xcframework.zip",
            checksum: "07d1f6d07f7d7706a86ee4a40356d5b7fde443ee9e011c7ed523f02ad18b97b5"
        )
    ]
)
